#include <iostream>
#include <string>
#include <fstream>
#include <cerrno>

#include <boost/uuid/uuid.hpp>            // uuid class
#include <boost/uuid/uuid_generators.hpp> // generators
#include <boost/uuid/uuid_io.hpp>         // streaming operators etc.

#include "./PracticalSocket.h"

using std::cerr;
using std::cout;
using std::endl;
using std::string;
using boost::uuids::uuid;

std::string get_file_contents(const char *filename);

string receiveFullMessage(TCPSocket &sock);

#define HEADER_WSQ_IDENTIFY       0x01
#define HEADER_WSQ_VERIFY         0x02
#define HEADER_IDENTIFY_SUCCESS   0x03
#define HEADER_VERIFY_SUCCESS     0x04
#define HEADER_VERIFY_FAILURE     0x05
#define HEADER_ERROR              0x00

int main(int argc, char **argv) {
  char header = (char) (argv[1][0] == 'i' ? HEADER_WSQ_IDENTIFY : HEADER_WSQ_VERIFY);
  
  char pref[2];
  pref[0] = header;
  pref[1] = '\0';
  
  string prefix = pref;
  if (header == (char) HEADER_WSQ_VERIFY) {
    boost::uuids::string_generator gen;
    uuid verifyUuid = gen(std::string(argv[3]));
    for (auto itr = verifyUuid.begin(); itr != verifyUuid.end(); ++itr) {
      prefix += (char) *itr;
    }
  }
  string message = prefix + get_file_contents(argv[2]);
  
  uint32_t length = message.length();
  char *lenBytes = (char *) &length;
  message = string(lenBytes, 4) + message;
        
  try {
    TCPSocket socket("localhost", 8080);
    socket.send(message.c_str(), message.length());
    
    string response = receiveFullMessage(socket);
    
    switch ((uint8_t) response.at(0)) {
    case HEADER_IDENTIFY_SUCCESS:
      uuid responseUuid;
      for (uint8_t i = 0; i < responseUuid.size(); i++) {
        responseUuid.data[i] = (uint8_t) response.at(i + 1);
      }
      cout << responseUuid << endl;
      break;
    case HEADER_VERIFY_SUCCESS:
      cout << "Verification successful." << endl;
      break;
    case HEADER_VERIFY_FAILURE:
      cout << "Verification failed." << endl;
      break;
    case HEADER_ERROR:
      cout << "Error: " << response.substr(1) << endl;
      break;
    }
  } catch (SocketException& e) {
    cout << e.what() << endl;
  }
}

std::string get_file_contents(const char *filename)
{
  std::ifstream in(filename, std::ios::in | std::ios::binary);
  if (in)
  {
    std::string contents;
    in.seekg(0, std::ios::end);
    contents.resize(in.tellg());
    in.seekg(0, std::ios::beg);
    in.read(&contents[0], contents.size());
    in.close();
    return(contents);
  }
  throw(errno);
}

string receiveFullMessage(TCPSocket &sock) {
  string fullMsg = "";
  
  uint32_t length;
  sock.recv(&length, 4);
    
  char buf[1024];
  int recvMsgSize;
  while (fullMsg.length() < length && 
        (recvMsgSize = sock.recv(buf, 1024)) > 0) {  // Zero means
                                                      // end of transmission
    fullMsg += string(buf, recvMsgSize);
  }
    
  return fullMsg;
}
