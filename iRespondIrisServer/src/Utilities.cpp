#include <iostream>

#include "Utilities.h"

namespace irespond {
namespace utilities {

bool _memoryMode = false;
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

/**
 * Returns whether or not we are in memory mode.
 */
bool memoryMode() {
  return _memoryMode;
}

/**
 * Sets the memory mode to on iff the param is true.
 */
void setMemoryMode(bool on) {
  _memoryMode = on;
}

}
}
