#ifndef _IRESPOND_SERVER_H_
#define _IRESPOND_SERVER_H_

#include <stdint.h>
#include <string>
#include <netinet/in.h>

extern "C" {
#include <bozorth.h>
#include <lfs.h>
#undef max
#undef min
}

#include "ThreadPool.h"
#include "PracticalSocket.h"
#include "FingerprintDatabase.h"

#define ntohll(x) \
  ( ((uint64_t) (ntohl((uint32_t)((x << 32) >> 32) )) << 32) |   \
    ntohl(((uint32_t)(x >> 32))) )
#define htonll(x) (ntohll(x))

namespace irespond {

/**
 * The IrespondServer handles all the network communication
 * for the iRespond fingerprint recognition system.
 */
class IrespondServer {
  public:
    /**
     * Creates a new IrespondServer object for port "port".
     */
    explicit IrespondServer(unsigned short port);

    /**
     * Destructor that shuts down the thread pool and database.
     */
    virtual ~IrespondServer(void);

    /**
     * Creates the listening socket and loops, waiting for
     * connections, then assigns the connection to a
     * Thread Pool for execution.
     */
    bool Run(void);

  private:
    unsigned short port_;
    const int kNumThreads;
    FingerprintDatabase *database;
};

/**
 * The IrespondServerTask simply holds the connection
 * socket, and a copy of the database.
 */
class IrespondServerTask : public ThreadPool::Task {
  public:
    explicit IrespondServerTask(ThreadPool::thread_task_fn f)
      : ThreadPool::Task(f) { }
    ~IrespondServerTask() { delete client; }

    TCPSocket *client;
    FingerprintDatabase *database;
};

/**
 * Parses the minutiae in the given minutiae, and stores them
 * in the oxytq output parameter.
 * 
 * @param oxytq The output XYTQ struct, for matching.
 * @param minutiae The minutiae to parse into the XYTQ struct.
 * @param w The width of the image the minutiae is extracted from.
 * @param h The height of the image the minutiae is extracted from.
 */
void ParseMinutiae(struct xytq_struct &oxytq, MINUTIAE *minutiae, int w, int h);

/**
 * Processes the WSQ file sent over the network, and extracts the
 * template, placing it in the oxytq output parameter.
 * 
 * @param oxytq The output XYTQ struct, for matching.
 * @param wsqLen The length of the passed wsqData in bytes.
 * @param wsqData the data of the WSQ file.
 */
void ProcessWSQTransfer(struct xytq_struct &oxytq, int32_t wsqLen, const char *wsqData);

/**
 * The function the iRespond server is dispatched
 * out to via the thread pool.
 * 
 * @param t The task containing all the information.
 */
void IrespondServer_ThrFn(ThreadPool::Task *t);

/**
 * A wrapper for reading the TCP socket.
 * 
 * @param socket The socket to read from.
 * @param vbuf The buffer to write to.
 * @param len The length of the buffer. This is also the
 *            amount required to read before returning.
 */
void receive(TCPSocket *socket, void *vbuf, int32_t len);

}  // namespace irespond

#endif  // _IRESPOND_SERVER_H_
