#include <iostream>
#include <fstream>
#include <memory>
#include <vector>
#include <sstream>
#include <stdint.h>
#include <string>
#include <exception>

#include <boost/uuid/uuid.hpp>            // uuid class
#include <boost/uuid/uuid_generators.hpp> // generators
#include <boost/uuid/uuid_io.hpp>         // streaming operators etc.

extern "C" {
#include <bozorth.h>
#include <wsq.h>
#include <lfs.h>
#include <defs.h>
#include <nfiq.h>
}

#include "./iRespondServer.h"
#include "./PracticalSocket.h"

using std::cerr;
using std::cout;
using std::endl;
using std::string;
using boost::uuids::uuid;

namespace iris {

// This is the function that threads are dispatched into
// in order to process new client connections.
void IrisServer_ThrFn(ThreadPool::Task *t);

string receiveFullMessage(TCPSocket *sock);

void sendMessage(TCPSocket *socket, string message);

IrisServer::IrisServer(unsigned short port)
  : port_(port), kNumThreads(100) { 
  this->database = new FingerprintDatabase("database");
}

IrisServer::~IrisServer(void) {
  delete this->database;
}

bool IrisServer::Run(void) {
  // Create the server listening socket.
  cout << "Listening on port: " << port_ << endl << endl;
  cout << "=================================" << endl << endl;
  try {
    TCPServerSocket ss(port_);
    // Spin, accepting connections and dispatching them.  Use a
    // threadpool to dispatch connections into their own thread.
    ThreadPool tp(kNumThreads);
    while (1) {
      IrisServerTask *ist = new IrisServerTask(IrisServer_ThrFn);
      try {
        ist->client = ss.accept();
        ist->database = this->database;
      } catch (SocketException& e) {
        break;
      }
      // The accept succeeded; dispatch it.
      tp.Dispatch(ist);
    }
    return true;
  } catch (SocketException& e) {
    cout << "  error creating listening socket. Quitting." << endl;
    return false;
  }
}

#define HEADER_WSQ_IDENTIFY       0x01
#define HEADER_WSQ_VERIFY         0x02
#define HEADER_IDENTIFY_SUCCESS   0x03
#define HEADER_IDENTIFY_FAILURE   0x06
#define HEADER_VERIFY_SUCCESS     0x04
#define HEADER_VERIFY_FAILURE     0x05
#define HEADER_ERROR              0x00
#define HEADER_ENROLL             0x07

void IrisServer_ThrFn(ThreadPool::Task *t) {
  // Cast back our IrisServerTask structure with all of our new
  // client's information in it.
  IrisServerTask *ist = static_cast<IrisServerTask *>(t);
  
  cout << "Connection received from " << ist->client->getForeignAddress() << ":" << ist->client->getForeignPort() << "." << endl << endl;
  
  // First extract xytq, for quality testing.
  struct xytq_struct fingerTemplate;
  string message = receiveFullMessage(ist->client);
  uint8_t header = (uint8_t) message.at(0);
  try {
    if (header != (char) HEADER_WSQ_IDENTIFY && 
        header != (char) HEADER_WSQ_VERIFY) {
      throw "Invalid header.";
    }
    
    string wsqData = message.substr(1);
    if (header == HEADER_WSQ_VERIFY) {
      wsqData = wsqData.substr(16);
    }
    
    ProcessWSQTransfer(fingerTemplate, wsqData);
  } catch (char const *errorMessage) {
    cout << "Error: " << errorMessage << endl << endl;
    cout << "=================================" << endl << endl;
    
    string errorResponse;
    errorResponse += (char) HEADER_ERROR;
    errorResponse += errorMessage;
    
    sendMessage(ist->client, errorResponse);
    
    delete ist;
    return;
  }
  
  // Good quality, shrink it down to a template.
  template_t *probe = bz_prune(&fingerTemplate, 0);
  
  if (header == HEADER_WSQ_IDENTIFY) {
    cout << endl << "Performing identification." << endl << endl;
    // Perform match.
    boost::uuids::uuid uuid;
    if (ist->database->identify(probe, uuid)) {
      cout << "Match found: ";
    } else {
      cout << "No match found, new ID: ";
    }
    
    cout << uuid << endl << endl;
    
    string response;
    response += (char) HEADER_IDENTIFY_SUCCESS;
    for (auto itr = uuid.begin(); itr != uuid.end(); ++itr) {
      response += (char) *itr;
    }
    
    sendMessage(ist->client, response);
  } else if (header == HEADER_WSQ_VERIFY) {
    uuid verifyUuid;
    for (uint8_t i = 0; i < verifyUuid.size(); i++) {
      verifyUuid.data[i] = (uint8_t) message.at(i+1);
    }
    
    cout << endl << "Performing verification against: " << verifyUuid << endl << endl;
    
    string response;
    if (ist->database->verify(probe, verifyUuid)) {
      response += (char) HEADER_VERIFY_SUCCESS;
      cout << "Verification successful." << endl << endl;
    } else {
      response += (char) HEADER_VERIFY_FAILURE;
      cout << "Verification unsuccessful." << endl << endl;
    }
    
    sendMessage(ist->client, response);
  }
  
  cout << "=================================" << endl << endl;
  // Send ID;
  
  delete ist;
}

/**
 * Receives and processes a WSQ file over the network into a 
 * NIST xytq minutiae template.
 */
void ProcessWSQTransfer(struct xytq_struct &oxytq, string wsqDataStr) {
  //cout << "Parsing WSQ stream." << endl;
  
  int wsqLen = wsqDataStr.length();
  const char *wsqData = wsqDataStr.c_str();
  
  unsigned char *data;
  int w, h, d, ppi, lossyflag;
  
  // Decode the transferred WSQ data.
  if (wsq_decode_mem(&data, &w, &h, &d, &ppi, &lossyflag, (unsigned char *) wsqData, wsqLen)) {
    throw "Error decoding WSQ file.";
  }
  
  // Check image quality.
  int nfiq;
  float conf;
  int flag = 0;
  int ret = comp_nfiq(&nfiq, &conf, data, w, h, d, ppi, &flag);
  
  if (ret) {
    throw "Error generating image quality.";
  } else if (nfiq > 3) {
    throw "Image quality too low.";
  }
  
  cout << "Quality: " << nfiq << endl;
  
  MINUTIAE *minutiae;
  int *quality_map, *direction_map, *low_contrast_map, *low_flow_map,
      *high_curve_map, map_w, map_h, bw, bh, bd;
  unsigned char *bdata;
  
  double ppmm = ppi / (double)MM_PER_INCH;
  
  // Compute the minutiae from the raw data.
  if (get_minutiae(&minutiae, &quality_map, &direction_map, &low_contrast_map,
                 &low_flow_map, &high_curve_map, &map_w, &map_h,
                 &bdata, &bw, &bh, &bd, data, w, h,
                 d, ppmm, &lfsparms_V2)) {
    throw "Error extracting minutiae.";
  }
  
  // Parse the minutia into the xytq struct.
  ParseMinutiae(oxytq, minutiae, w, h);
  
  // Free resources.
  free_minutiae(minutiae);
  free(quality_map);
  free(direction_map);
  free(low_contrast_map);
  free(low_flow_map);
  free(high_curve_map);
  free(bdata);
  free(data);
}

void ParseMinutiae(struct xytq_struct &oxytq, MINUTIAE *minutiae, int w, int h) {
  oxytq.nrows = minutiae->num;
  
  MINUTIA *minutia;
  for (int i = 0; i < minutiae->num; i++) {
    minutia = minutiae->list[i];
    
    int ox, oy, ot;
    lfs2nist_minutia_XYT(&ox, &oy, &ot, minutia, w, h);
    
    int oq = sround(minutia->reliability * 100.0);
    
    oxytq.xcol[i] = ox;
    oxytq.ycol[i] = oy;
    oxytq.thetacol[i] = ot;
    oxytq.qualitycol[i] = oq;
  }
}

string receiveFullMessage(TCPSocket *sock) {
  string fullMsg = "";
  
  uint32_t length;
  sock->recv(&length, 4);
    
  char buf[1024];
  int recvMsgSize;
  while (fullMsg.length() < length && 
        (recvMsgSize = sock->recv(buf, 1024)) > 0) {  // Zero means
                                                      // end of transmission
    fullMsg += string(buf, recvMsgSize);
  }
    
  return fullMsg;
}

void sendMessage(TCPSocket *socket, string message) {
  uint32_t messageLen = message.length();
  message = string((char *) &messageLen, 4) + message;
  
  socket->send(message.c_str(), message.length());
}



}  // namespace iris
