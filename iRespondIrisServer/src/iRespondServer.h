#ifndef _IRIS_SERVER_H_
#define _IRIS_SERVER_H_

#include <stdint.h>
#include <string>

extern "C" {
#include <bozorth.h>
#include <lfs.h>
#undef max
#undef min
}

#include "./ThreadPool.h"
#include "./PracticalSocket.h"
#include "FingerprintDatabase.h"

namespace iris {

// The HttpServer class contains the main logic for the web server.
class IrisServer {
 public:
  // Creates a new IrisServer object for port "port" and serving
  // files out of path "staticfile_dirpath".  The indices for
  // query processing are located in the "indices" list. The constructor
  // does not do anything except memorize these variables.
  explicit IrisServer(unsigned short port);

  // The destructor closes the listening socket if it is open and
  // also kills off any threads in the threadpool.
  virtual ~IrisServer(void);

  // Creates a listening socket for the server and launches it, accepting
  // connections and dispatching them to worker threads.  Returns
  // "true" if the server was able to start and run, "false" otherwise.
  bool Run(void);

 private:
  unsigned short port_;
  const int kNumThreads;
  FingerprintDatabase *database;
};

class IrisServerTask : public ThreadPool::Task {
 public:
  explicit IrisServerTask(ThreadPool::thread_task_fn f)
    : ThreadPool::Task(f) { }
  ~IrisServerTask() { delete client; }

  TCPSocket *client;
  FingerprintDatabase *database;
};

void ParseMinutiae(struct xytq_struct &oxytq, MINUTIAE *minutiae, int w, int h);

void ProcessWSQTransfer(struct xytq_struct &oxytq, int32_t wsqLen, const char *wsqData);

}  // namespace hw4

#endif  // _IRIS_SERVER_H_
