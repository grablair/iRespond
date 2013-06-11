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

}
}

#endif // __IRESPOND_UTILITIES_
