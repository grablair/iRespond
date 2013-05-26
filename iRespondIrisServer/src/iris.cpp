#include <iostream>
#include <string>

#include "./iRespondServer.h"

using std::cerr;
using std::cout;
using std::endl;
using std::string;

int main(int argc, char **argv) {
  if (argc == 1) {
    cout << "Usage: iris <port number>" << endl;
    exit(1);
  }
  
  cout << "iRespond IRIS Server" << endl;
  cout << "This product is Copyright iRespond 2013" << endl << endl;

  unsigned short port = atoi(argv[1]);
  iris::IrisServer server(port);
  
  server.Run();
}
