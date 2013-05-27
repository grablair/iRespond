#include <iostream>
#include <fstream>
#include <memory>
#include <vector>
#include <set>
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

void receive(TCPSocket *socket, void *vbuf, int32_t len);

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

static uint8_t HEADER_WSQ_IDENTIFY      = 0x01;
static uint8_t HEADER_WSQ_VERIFY        = 0x02;
static uint8_t HEADER_WSQ_ENROLL        = 0x07;
static uint8_t HEADER_IDENTIFY_SUCCESS  = 0x03;
static uint8_t HEADER_IDENTIFY_FAILURE  = 0x06;
static uint8_t HEADER_VERIFY_SUCCESS    = 0x04;
static uint8_t HEADER_VERIFY_FAILURE    = 0x05;
static uint8_t HEADER_ENROLL_SUCCESS    = 0x08;
static uint8_t HEADER_ERROR             = 0x00;

void IrisServer_ThrFn(ThreadPool::Task *t) {
  // Cast back our IrisServerTask structure with all of our new
  // client's information in it.
  IrisServerTask *ist = static_cast<IrisServerTask *>(t);
  
  cout << "Connection received from " << ist->client->getForeignAddress() << ":" << ist->client->getForeignPort() << "." << endl;
  
  // Get packet header.
  uint8_t header;
  ist->client->recv(&header, 1);
  
  try {
    if (header == HEADER_WSQ_IDENTIFY) {
      cout << "IDENTIFY" << endl;
      
      // Get WSQ file size.
      int32_t wsqSize;
      receive(ist->client, &wsqSize, 4);
      
      wsqSize = ntohl(wsqSize);
      
      cout << wsqSize << endl;
      
      // Get WSQ file bytes.
      char wsqData[wsqSize];
      receive(ist->client, wsqData, wsqSize);
      
      // Get XYTQ coordinates of minutiae
      struct xytq_struct fingerTemplate;
      ProcessWSQTransfer(fingerTemplate, wsqSize, wsqData);
      
      cout << "Processed." << endl;
      
      // Quality is good, shrink down to a template.
      template_t *probe = bz_prune(&fingerTemplate, 0);
      
      uuid matchUuid;
      if (ist->database->identify(probe, matchUuid)) {
        // Send identify success packet.
        ist->client->send(&HEADER_IDENTIFY_SUCCESS, 1);
        
        uint64_t *uuidLong = (uint64_t *) matchUuid.data;
        uint64_t mostSig = uuidLong[0];
        uint64_t leastSig = uuidLong[1];
        ist->client->send(&mostSig, 8);
        ist->client->send(&leastSig, 8);
      } else {
        // Send identify failure packet.
        ist->client->send(&HEADER_IDENTIFY_FAILURE, 1);
      }
    } else if (header == HEADER_WSQ_VERIFY) {
      // Perform Verification
      int32_t numUuids;
      receive(ist->client, &numUuids, 4);
      
      numUuids = ntohl(numUuids);
      
      std::set<uuid> verifyUuids;
      for (int32_t i = 0; i < numUuids; i++) {
        uuid verifyUuid;
        uint64_t mostSig, leastSig;
        uint64_t *uuidLong = (uint64_t *) verifyUuid.data;
        receive(ist->client, &mostSig, 8);
        uuidLong[0] = mostSig;
        receive(ist->client, &leastSig, 8);
        uuidLong[1] = leastSig;

        verifyUuids.insert(verifyUuid);
      }
      
      // Get WSQ file size.
      int32_t wsqSize;
      receive(ist->client, &wsqSize, 4);
      
      wsqSize = ntohl(wsqSize);
      
      // Get WSQ file bytes.
      char wsqData[wsqSize];
      receive(ist->client, wsqData, wsqSize);
      
      // Get XYTQ coordinates of minutiae
      struct xytq_struct fingerTemplate;
      ProcessWSQTransfer(fingerTemplate, wsqSize, wsqData);
      
      // Quality is good, shrink down to a template.
      template_t *probe = bz_prune(&fingerTemplate, 0);
      
      // Send result header.
      uint8_t retHeader = ist->database->verify(probe, verifyUuids) ? 
              HEADER_VERIFY_SUCCESS : HEADER_VERIFY_FAILURE;
      ist->client->send(&retHeader, 1);
    } else if (header == HEADER_WSQ_ENROLL) {
      // Perform Enrollment
      int32_t numImages;
      receive(ist->client, &numImages, 4);
      
      numImages = ntohl(numImages);
      
      // Get best template out of images.
      struct xytq_struct bestTemplate;
      bestTemplate.nrows = 0;
      for (int32_t i = 0; i < numImages; i++) {
        // Get WSQ file size.
        int32_t wsqSize;
        receive(ist->client, &wsqSize, 4);
        
        wsqSize = ntohl(wsqSize);
        
        // Get WSQ file bytes.
        char wsqData[wsqSize];
        receive(ist->client, wsqData, wsqSize);
        
        // Parse image.
        struct xytq_struct fingerTemplate;
        fingerTemplate.nrows = 0;
        try {
          ProcessWSQTransfer(fingerTemplate, wsqSize, wsqData);
        } catch (char const *errorMessage) {
          if (strcmp(errorMessage, "Image quality too low."))
            throw errorMessage;
        }
        
        // Set best template if better.
        if (fingerTemplate.nrows > bestTemplate.nrows)
          bestTemplate = fingerTemplate;
      }
      
      // If all images were too low quality, throw exception.
      if (!bestTemplate.nrows) {
        throw "Image quality too low.";
      }
      
      template_t *temp = bz_prune(&bestTemplate, 0);
      
      
      uuid uuid = ist->database->enroll(temp);
      
      ist->client->send(&HEADER_ENROLL_SUCCESS, 1);
      uint64_t *uuidLong = (uint64_t *) uuid.data;
      uint64_t mostSig = uuidLong[0];
      uint64_t leastSig = uuidLong[1];
      ist->client->send(&mostSig, 8);
      ist->client->send(&leastSig, 8);
    } else {
      throw "Invalid header.";
    }
  } catch (char const *errorMessage) {
    cout << "Error: " << errorMessage << endl << endl;
    cout << "=================================" << endl << endl;
    
    int32_t errLen = htonl(strlen(errorMessage));
    
    try {
      ist->client->send(&HEADER_ERROR, 1);
      ist->client->send(&errLen, 4);
      ist->client->send(errorMessage, errLen);
    } catch (SocketException &e) {
      cout << "Network error: " << e.what() << endl;
    }
  } catch (SocketException &e) {
    cout << "Network error: " << e.what() << endl;
  }
  
  delete ist;
}

/**
 * Receives and processes a WSQ file over the network into a 
 * NIST xytq minutiae template.
 */
void ProcessWSQTransfer(struct xytq_struct &oxytq, int32_t wsqLen, const char *wsqData) {
  //cout << "Parsing WSQ stream." << endl;
  
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
  
  //cout << "Quality: " << nfiq << endl;
  
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

void receive(TCPSocket *socket, void *vbuf, int32_t len) {
  char *buf = (char *) vbuf;
  int32_t totalRead = 0;
  while (totalRead < len) {
    totalRead += socket->recv(buf + totalRead, len - totalRead);
  }
}


}  // namespace iris
