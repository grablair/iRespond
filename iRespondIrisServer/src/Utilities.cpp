#include <iostream>

#include "Utilities.h"

namespace irespond {
namespace utilities {

bool _verbose = false;
std::ostream _log(0);

/**
 * Returns the log.
 */
std::ostream &log() {
  return _verbose ? std::cout : _log;
}

/**
 * Sets the log to be std::cout.
 */
void initLog() {
  _verbose = true;
}

}
}
