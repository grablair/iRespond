#include <iostream>
#include <string>
#include <fstream>
#include <cerrno>

#include "./PracticalSocket.h"

using std::cerr;
using std::cout;
using std::endl;
using std::string;

std::string get_file_contents(const char *filename);

int main(int argc, char **argv) {
  char pref[2];
  pref[0] = (char) 0x01;
  pref[1] = '\0';
  
  string prefix = pref;
  string message = pref + get_file_contents(argv[1]);
  
  try {
    TCPSocket socket("localhost", 8080);
    socket.send(message.c_str(), message.length());
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
