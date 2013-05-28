#include <iostream>
#include <fstream>
#include <sstream>

#include <sys/time.h>

#include "FingerprintDatabase.h"

#define MATCH_THRESHOLD 40

namespace fs = boost::filesystem;
using boost::uuids::uuid;

/* Default bozorth settings */
int m1_xyt                  = 0;
int max_minutiae            = DEFAULT_BOZORTH_MINUTIAE;
int min_computable_minutiae = MIN_COMPUTABLE_BOZORTH_MINUTIAE;
int verbose_main      = 0;
int verbose_load      = 0;
int verbose_bozorth   = 0;
int verbose_threshold = 0;
FILE * errorfp            = FPNULL;
/* End bozorth settings */

namespace iris {
    
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
  int probeLen = bozorth_probe_init(probe);
  
  struct timeval tv;
  struct timeval tv2;
  
  gettimeofday(&tv, NULL);
  
  int maxMatchScore = 0;
  uuid maxUuid;
  int count = 0;
  for (auto itr = templates.begin(); itr != templates.end(); ++itr) {
    int matchScore = bozorth_to_gallery(probeLen, probe, itr->second.get());
    //if (matchScore >= 0) {
    //  cout << "Potential match with: " << itr->first << endl;
    //  cout << "         Match Score: " << matchScore << endl << endl;
    //}
    if (matchScore > maxMatchScore) {
      maxMatchScore = matchScore;
      maxUuid = itr->first;
    }
    count++;
  }
  
  gettimeofday(&tv2, NULL);
  
  //cout << "Num Minutiae: " << probe->nrows << endl;
  //cout << "Probe Length: " << probeLen << endl;
  //cout << "Match Rate:   " << ((double) count / (tv2.tv_usec - tv.tv_usec) * 1000000) << " fingerprints / sec" << endl;
  
  if (maxMatchScore < MATCH_THRESHOLD) {
    return false;
  }
  
  //cout << "Match Score:  " << maxMatchScore << endl << endl;
  
  oUuid = maxUuid;
  return true;
}

bool FingerprintDatabase::verify(template_t *probe, std::set<uuid> &uuids) {
  for (auto itr = uuids.begin(); itr != uuids.end(); ++itr) {
    auto entry = templates.find(*itr);
    if (entry == templates.end())
      continue;
    
    boost::shared_ptr<template_t> gallery = entry->second;
    
    int matchScore = bozorth_main(probe, gallery.get());
    
    //cout << "Match score: " << matchScore << endl << endl;
    
    if (matchScore >= MATCH_THRESHOLD)
      return true;
  }
  
  return false;
}

uuid FingerprintDatabase::enroll(template_t *temp) {
  boost::uuids::random_generator gen;
  uuid uuid = gen();
    
  boost::shared_ptr<template_t> ptr(temp);
  templates[uuid] = ptr;
  
  std::stringstream ss;
  ss << uuid;

  const std::string uuidString = ss.str();
  
  std::ofstream outputFile;
  outputFile.open(this->databaseFolder + "/" + uuidString + TEMPLATE_EXTENSION);
  
  for (int i = 0; i < temp->nrows; i++) {
    outputFile << temp->xcol[i] << " " << temp->ycol[i] << " " << temp->thetacol[i] << endl;
  }
  
  outputFile.close();
  
  return uuid;
}


}  // namespace iris
