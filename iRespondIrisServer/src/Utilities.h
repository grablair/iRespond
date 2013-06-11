#ifndef __IRESPOND_UTILITIES_
#define __IRESPOND_UTILITIES_

#include <ostream>

namespace irespond {
namespace utilities {

/**
 * Returns the log stream. If the service is not in verbose
 * mode, then the log stream does nothing.
 */
std::ostream &log();

/**
 * Initializes the log stream for verbose mode.
 */
void initLog();

/**
 * Returns the current memory mode.
 * 
 * @return true iff all data should be stored in memory.
 */
bool memoryMode();

/**
 * Sets the memory mode to the value of on.
 * 
 * @param on A boolean value. If true, memory mode if on,
 *           off otherwise.
 */
void setMemoryMode(bool on);

}
}

#endif // __IRESPOND_UTILITIES_
