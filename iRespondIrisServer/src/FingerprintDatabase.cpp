#include <iostream>
#include <fstream>
#include <sstream>

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
  
  // Read in all .xyt (template) files.
  for (fs::directory_iterator itr(folder); itr != end_itr; ++itr) {
    std::string filename = itr->path().leaf().string();
    if (filename.length() > 4 && !filename.compare(filename.length() - 
            TEMPLATE_EXTENSION.length(), TEMPLATE_EXTENSION.length(), TEMPLATE_EXTENSION)) {
      boost::shared_ptr<template_t> temp(bz_load(itr->path().string().c_str()));
      boost::uuids::uuid uuid = gen(filename.substr(0, filename.length() - 
            TEMPLATE_EXTENSION.length()));

      templates[uuid] = temp;
    }
  }
  
  this->databaseFolder = folder.string();
}

/**
 * Iterate through all the templates in the database,
 */
bool FingerprintDatabase::identify(template_t *probe, uuid &oUuid) {
  int probeLen = bozorth_probe_init(probe);
  
  int maxMatchScore = 0;
  uuid maxUuid;
  for (auto itr = templates.begin(); itr != templates.end(); ++itr) {
    int matchScore = bozorth_to_gallery(probeLen, probe, itr->second.get());
    if (matchScore >= 20) {
      cout << "Potential match with: " << itr->first << endl;
      cout << "         Match Score: " << matchScore << endl << endl;
    }
    if (matchScore > maxMatchScore) {
      maxMatchScore = matchScore;
      maxUuid = itr->first;
    }
  }
  
  if (maxMatchScore < MATCH_THRESHOLD) {
    boost::uuids::random_generator gen;
    uuid newUuid = gen();
    add(newUuid, probe);
    oUuid = newUuid;
    return false;
  }
  
  oUuid = maxUuid;
  return true;
}

bool FingerprintDatabase::verify(template_t *probe, boost::uuids::uuid uuid) {
  auto entry = templates.find(uuid);
  if (entry == templates.end()) return false;
  
  boost::shared_ptr<template_t> gallery = entry->second;
  
  int matchScore = bozorth_main(probe, gallery.get());
  
  return matchScore >= MATCH_THRESHOLD;
}

void FingerprintDatabase::add(uuid uuid, template_t *temp) {
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
}

}  // namespace iris
