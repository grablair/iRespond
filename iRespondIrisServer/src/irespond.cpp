#include <iostream>
#include <string>
#include <ostream>

#include "Utilities.h"
#include "iRespondServer.h"

#define OPTION_VERBOSE 'v'

using std::cerr;
using std::cout;
using std::endl;
using std::string;

bool verbose = false;

int main(int argc, char **argv) {
  if (argc < 2 || argc > 3) {
    cout << "Usage: irespond [-v] <port number>" << endl;
    exit(1);
  }
  
  cout << "iRespond Biometric Server" << endl;
  cout << "This product is Copyright iRespond 2013" << endl << endl;

  unsigned short port;
  // Parse arguments.
  if (argv[1][0] == '-') {
    for (uint8_t i = 1; i < strlen(argv[1]); i++) {
      switch (argv[1][i]) {
      case OPTION_VERBOSE:
        irespond::utilities::initLog();
      }
    }
    port = atoi(argv[2]);
  } else {
    port = atoi(argv[1]);
  }
  
  // Create the server.
  irespond::IrespondServer server(port);
  
  // Start the server.
  server.Run();
}
