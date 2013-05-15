#include <iostream>
#include <fstream>
#include <memory>
#include <vector>
#include <sstream>
#include <stdint.h>
#include <string>

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

namespace iris {

// This is the function that threads are dispatched into
// in order to process new client connections.
void IrisServer_ThrFn(ThreadPool::Task *t);

std::string receiveFullMessage(TCPSocket *sock);

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

void IrisServer_ThrFn(ThreadPool::Task *t) {
  // Cast back our IrisServerTask structure with all of our new
  // client's information in it.
  IrisServerTask *ist = static_cast<IrisServerTask *>(t);
  
  cout << "Connection received from " << ist->client->getForeignAddress() << "." << endl << endl;
  
  // First extract xytq, for quality testing.
  struct xytq_struct fingerTemplate;
  if (!ProcessWSQTransfer(fingerTemplate, receiveFullMessage(ist->client))) {
    // TODO: Handle error.
    cout << "Error." << endl;
    return;
  }
  
  // Good quality, shrink it down to a template.
  template_t *probe = bz_prune(&fingerTemplate, 0);
  
  cout << endl << "Performing match." << endl << endl;
  // Perform match.
  boost::uuids::uuid uuid;
  if (ist->database->identify(probe, uuid)) {
    cout << "Match found: ";
  } else {
    cout << "No match found, new ID: ";
  }
  
  cout << uuid << endl << endl;
  cout << "=================================" << endl << endl;

  // Send ID;
    
  delete ist;
}

#define HEADER_WSQ    0x01


/**
 * Receives and processes a WSQ file over the network into a 
 * NIST xytq minutiae template.
 */
bool ProcessWSQTransfer(struct xytq_struct &oxytq, string message) {
  if (message.at(0) != (char) HEADER_WSQ) {
    return false;
  }
  
  //cout << "Parsing WSQ stream." << endl;
  
  int wsqLen = message.length() - 1;
  const char *wsqData = message.c_str() + 1;
  
  unsigned char *data;
  int w, h, d, ppi, lossyflag;
  
  // Decode the transferred WSQ data.
  if (wsq_decode_mem(&data, &w, &h, &d, &ppi, &lossyflag, (unsigned char *) wsqData, wsqLen)) {
    return false;
  }
  
  // Check image quality.
  int nfiq;
  float conf;
  int flag = 0;
  int ret = comp_nfiq(&nfiq, &conf, data, w, h, d, ppi, &flag);
  
  if (ret) {
    return false;
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
    return false;
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
  
  return true;
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

std::string receiveFullMessage(TCPSocket *sock) {
  std::string fullMsg = "";
  
  char buf[1024];
  int recvMsgSize;
  while ((recvMsgSize = sock->recv(buf, 1024)) > 0) { // Zero means
                                                      // end of transmission
    fullMsg += std::string(buf, recvMsgSize);
  }
  
  return fullMsg;
}



}  // namespace iris
