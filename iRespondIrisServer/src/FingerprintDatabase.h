#ifndef _FINGERPRINT_DATABASE_H_
#define _FINGERPRINT_DATABASE_H_

#include <stdint.h>
#include <string>
#include <map>
#include <set>
#include <memory>

#include <boost/uuid/uuid.hpp>            // uuid class
#include <boost/uuid/uuid_generators.hpp> // generators
#include <boost/uuid/uuid_io.hpp>         // streaming operators etc.
#include <boost/filesystem.hpp>           // filesystem functions

extern "C" {
#include <bozorth.h>
#include <lfs.h>
#undef max
#undef min
}

#include "./ThreadPool.h"
#include "./PracticalSocket.h"

const std::string TEMPLATE_EXTENSION = ".xyt";

typedef struct xyt_struct template_t;

namespace irespond {
  
/**
 * The FingerprintDatabase class keeps track of all the fingerprints.
 * It performs identification, verification and enrollment.
 */
class FingerprintDatabase {
 public:
  /**
   * Creates a new database with the given base directory folder.
   * 
   * @param folder Folder to read the database from or start a new one in.
   */
  FingerprintDatabase(boost::filesystem::path folder);
  
  /**
   * Destructor.
   */
  ~FingerprintDatabase(void) { };

  /**
   * Identifies a fingerprint by its template. If the given template
   * matches a template in the database, the method returns true
   * and oUuid is set to the matching UUID. If no match is found,
   * false is returned and oUuid remains invalid.
   * 
   * @param probe The template to identify.
   * @param uuid The UUID reference to update with the matching or
   *             newly generated UUID.
   * @return true if a match was found or false otherwise.
   */
  bool identify(template_t *probe, boost::uuids::uuid &oUuid);
  
  /**
   * Verifies whether or not the given fingerprint matches the database's
   * stored template of the given UUID. One-to-one match.
   * 
   * @param probe The template to verify.
   * @param uuids The UUIDs to match against.
   * @return true if there is a match, false otherwise.
   */
  bool verify(template_t *probe, std::set<boost::uuids::uuid> &uuids);
  
  /**
   * Enrolls the given template into the database.
   * 
   * @param template The template to add to the database.
   * @return the UUID of the newly added template.
   */
  boost::uuids::uuid enroll(template_t *temp);

 private:
  /* The template map. */
  std::map<boost::uuids::uuid, boost::shared_ptr<template_t> > templates;
  std::string databaseFolder;
};

}  // namespace irespond

#endif  // _FINGERPRINT_DATABASE_H_
