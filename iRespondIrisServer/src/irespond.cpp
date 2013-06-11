#include <iostream>
#include <string>
#include <ostream>

#include "Utilities.h"
#include "iRespondServer.h"

#define OPTION_VERBOSE 'v'
#define OPTION_MEMORY_MODE 'm'

using std::cerr;
using std::cout;
using std::endl;
using std::string;

bool verbose = false;

int main(int argc, char **argv) {
  if (argc < 2 || argc > 3) {
    cout << "Usage: irespond [-vm] <port number>" << endl;
    cout << "Loads a fingerprint database and server from file, creating" << endl;
    cout << "a new database if needed." << endl << endl;
    cout << "    -v - Verbose. Shows more information on connections." << endl;
    cout << "    -m - Memory mode. All templates are loaded into memory" << endl;
    cout << "         at load-time. Not recommended for machines with" << endl;
    cout << "         low memory or databases with a large amount of" << endl;
    cout << "         fingerprint templates." << endl;
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
        break;
      case OPTION_MEMORY_MODE:
        irespond::utilities::setMemoryMode(true);
        break;
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
