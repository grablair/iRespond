#include <iostream>
#include <fstream>
#include <sstream>

#include <sys/time.h>

#include "Utilities.h"
#include "FingerprintDatabase.h"

#define MATCH_THRESHOLD 40

namespace fs = boost::filesystem;
using boost::uuids::uuid;

/* Default bozorth settings
 * These are required to run the matching algorithm
 */
int m1_xyt                  = 0;
int max_minutiae            = DEFAULT_BOZORTH_MINUTIAE;
int min_computable_minutiae = MIN_COMPUTABLE_BOZORTH_MINUTIAE;
int verbose_main      = 0;
int verbose_load      = 0;
int verbose_bozorth   = 0;
int verbose_threshold = 0;
FILE * errorfp            = FPNULL;
/* End bozorth settings */

namespace irespond {
    
// NOTE:
// !! Requires modification !! Currently, all templates are
// loaded into memory at the time of creation. This will
// not work for a database with a large amount of fingerprints.
FingerprintDatabase::FingerprintDatabase(fs::path folder) {
  if (!fs::exists(folder)) {
    // First time database ran in this folder, no templates.
    fs::create_directory(folder);
    return;
  }
  
  fs::directory_iterator end_itr;
  boost::uuids::string_generator gen;
  
  int count = 0;
  
  // Read in all .xyt (template) files.
  for (fs::directory_iterator itr(folder); itr != end_itr; ++itr) {
    std::string filename = itr->path().leaf().string();
    if (filename.length() > 4 && !filename.compare(filename.length() - 
            TEMPLATE_EXTENSION.length(), TEMPLATE_EXTENSION.length(), TEMPLATE_EXTENSION)) {
      // Load the template from file and add to the database.
      boost::shared_ptr<template_t> temp(bz_load(itr->path().string().c_str()));
      boost::uuids::uuid uuid = gen(filename.substr(0, filename.length() - 
            TEMPLATE_EXTENSION.length()));

      templates[uuid] = temp;
      count++;
    }
  }
  
  cout << "Loaded " << count << " fingerprint templates." << endl;
  
  this->databaseFolder = folder.string();
}

/**
 * Iterate through all the templates in the database,
 */
bool FingerprintDatabase::identify(template_t *probe, uuid &oUuid) {
  utilities::log() << "Performing identification:" << endl;
  
  // Parse the probe template. This is the template
  // sent by the client.
  int probeLen = bozorth_probe_init(probe);
  
  struct timeval tv;
  struct timeval tv2;
  
  gettimeofday(&tv, NULL);
  
  int maxMatchScore = 0;
  uuid maxUuid;
  int count = 0;
  // Iterate over all the fingerprints, looking for a
  // match with a score above MATCH_THRESHOLD. We need
  // to iterate over the entire database because even
  // though there may be a match early on, we do not
  // know whether or not it is the best match.
  for (auto itr = templates.begin(); itr != templates.end(); ++itr) {
    // Parse the gallery template. This is the current template
    // we are comparing the probe against.
    int matchScore = bozorth_to_gallery(probeLen, probe, itr->second.get());
    if (matchScore >= MATCH_THRESHOLD) {
      utilities::log() << "Potential match with: " << itr->first << endl;
      utilities::log() << "         Match Score: " << matchScore << endl << endl;
    }
    if (matchScore > maxMatchScore) {
      // We found the best match so far, save it.
      maxMatchScore = matchScore;
      maxUuid = itr->first;
    }
    count++;
  }
  
  gettimeofday(&tv2, NULL);
  
  utilities::log() << "Num Minutiae: " << probe->nrows << endl;
  utilities::log() << "Probe Length: " << probeLen << endl;
  utilities::log() << "Match Rate:   " << ((double) count /
      (tv2.tv_usec - tv.tv_usec) * 1000000) << " fingerprints / sec" << endl;
  
  if (maxMatchScore < MATCH_THRESHOLD) {
    // No match of high enough match score
    // found. Return false.
    return false;
  }
  
  utilities::log() << "Match Score:  " << maxMatchScore << endl;
  utilities::log() << "Match UUID:   " << maxUuid << endl << endl;  

  // We found a match. Set the output parameter
  // and return true.
  oUuid = maxUuid;
  return true;
}

/**
 * Only iterate over the selected fingerprints.
 */
bool FingerprintDatabase::verify(template_t *probe, std::set<uuid> &uuids) {
  for (auto itr = uuids.begin(); itr != uuids.end(); ++itr) {
    auto entry = templates.find(*itr);
    if (entry == templates.end())
      continue;
    
    boost::shared_ptr<template_t> gallery = entry->second;
    
    // Run the match algorithm on both templates.
    int matchScore = bozorth_main(probe, gallery.get());
    
    utilities::log() << "Match score: " << matchScore << endl << endl;
    
    if (matchScore >= MATCH_THRESHOLD) {
      // We return true if any of the chosen templates
      // matches over the MATCH_THRESHOLD. In this case,
      // we do not need to match against the entire lot.
      return true;
    }
  }
  
  return false;
}

/**
 * Enroll the given fingerprint.
 */
uuid FingerprintDatabase::enroll(template_t *temp) {
  boost::uuids::random_generator gen;
  uuid uuid = gen();
  
  // Add the new template.
  boost::shared_ptr<template_t> ptr(temp);
  templates[uuid] = ptr;
  
  // Generate the string UUID for file creation.
  std::stringstream ss;
  ss << uuid;

  const std::string uuidString = ss.str();
  
  // Open the new template file.
  std::ofstream outputFile;
  outputFile.open(this->databaseFolder + "/" + uuidString + TEMPLATE_EXTENSION);
  
  // Write the template file.
  for (int i = 0; i < temp->nrows; i++) {
    outputFile << temp->xcol[i] << " " << temp->ycol[i] << " " << temp->thetacol[i] << endl;
  }
  
  // Close the template file.
  outputFile.close();
  
  return uuid;
}


}  // namespace irespond
