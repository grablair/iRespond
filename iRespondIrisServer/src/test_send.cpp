#include <iostream>
#include <string>
#include <fstream>
#include <cerrno>
#include <iomanip>

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

static uint8_t HEADER_WSQ_IDENTIFY      = 0x01;
static uint8_t HEADER_WSQ_VERIFY        = 0x02;
static uint8_t HEADER_WSQ_ENROLL        = 0x07;
static uint8_t HEADER_IDENTIFY_SUCCESS  = 0x03;
static uint8_t HEADER_IDENTIFY_FAILURE  = 0x06;
static uint8_t HEADER_VERIFY_SUCCESS    = 0x04;
static uint8_t HEADER_VERIFY_FAILURE    = 0x05;
static uint8_t HEADER_ENROLL_SUCCESS    = 0x08;
static uint8_t HEADER_ERROR             = 0x00;

int main(int argc, char **argv) {
  try {
    TCPSocket socket("localhost", (unsigned short) atoi(argv[1]));
    
    string wsqData;
    int32_t wsqLen, numUuids, numImages;
    switch (argv[2][0]) {
    case 'i':
    case 'I':
      socket.send(&HEADER_WSQ_IDENTIFY, 1);
      wsqData = get_file_contents(argv[3]);
      wsqLen = wsqData.length();
      socket.send(&wsqLen, 4);
      socket.send(wsqData.c_str(), wsqLen);
      break;
    case 'v':
    case 'V':
      socket.send(&HEADER_WSQ_VERIFY, 1);
      wsqData = get_file_contents(argv[3]);
      wsqLen = wsqData.length();
      
      numUuids = argc - 4;
      socket.send(&numUuids, 4);
      
      boost::uuids::string_generator gen;
      for (int i = 4; i < argc; i++) {
        uuid verifyUuid = gen(std::string(argv[i]));
        socket.send(verifyUuid.data, 16);
      }
      
      socket.send(&wsqLen, 4);
      socket.send(wsqData.c_str(), wsqLen);
      break;
    case 'e':
    case 'E':
      socket.send(&HEADER_WSQ_ENROLL, 1);
      
      numImages = argc - 3;
      socket.send(&numImages, 4);
      
      for (int i = 3; i < argc; i++) {
        wsqData = get_file_contents(argv[i]);
        wsqLen = wsqData.length();
        
        socket.send(&wsqLen, 4);
        socket.send(wsqData.c_str(), wsqLen);
      }
      
      break;
    default:
      cout << "Invalid function. [i]dentify, [v]erify, or [e]nroll." << endl;
      exit(EXIT_FAILURE);
    }
    
    uint8_t header;
    socket.recv(&header, 1);
    
    if (header == HEADER_IDENTIFY_SUCCESS || header == HEADER_ENROLL_SUCCESS) {
      uuid responseUuid;
      socket.recv(responseUuid.data, 16);
      cout << responseUuid << endl;
    } else if (header == HEADER_IDENTIFY_FAILURE) {
      cout << "No match found." << endl;
    } else if (header == HEADER_VERIFY_SUCCESS) {
      cout << "Verification successful." << endl;
    } else if (header == HEADER_VERIFY_FAILURE) {
      cout << "Verification unsuccessful." << endl;
    } else if (header == HEADER_ERROR) {
      int32_t errLen;
      socket.recv(&errLen, 4);
      char error[errLen + 1];
      error[errLen] = '\0';
      socket.recv(error, errLen);
      cout << "Error: " << error << endl;
    } else {
      cout << "Invalid response header: " << hex << (int) header << endl;
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
