#include "BeerScorer.h"

#include <algorithm>
#include <cmath>
#include <fstream>
#include <iterator>
#include <sstream>
#include <stdexcept>
#include <stdio.h>
#include <string>
#include <vector>

#include <boost/thread/mutex.hpp>

#if defined(__GLIBCXX__) || defined(__GLIBCPP__)
#include "Fdstream.h"
#endif

#include "ScoreStats.h"
#include "Util.h"

using namespace std;

namespace MosesTuning
{

// Beer supported
#if defined(__GLIBCXX__) || defined(__GLIBCPP__)

// for clarity
#define CHILD_STDIN_READ pipefds_input[0]
#define CHILD_STDIN_WRITE pipefds_input[1]
#define CHILD_STDOUT_READ pipefds_output[0]
#define CHILD_STDOUT_WRITE pipefds_output[1]

BeerScorer::BeerScorer(const string& config)
  : StatisticsBasedScorer("BEER",config) {
  beer_jar = getConfig("jar", "");
  beer_lang = getConfig("lang", "other");
  beer_decimals = atoi(getConfig("decimals", "5").c_str());
  beer_modelType = getConfig("modelType", "tuning");
  beer_otherArguments = getConfig("otherArguments", "");
  if (beer_jar == "") {
    throw runtime_error("Beer jar required, see BeerScorer.h for full list of options: --scconfig decimals:6 jar:/path/to/beer_1.0.jar lang:ru modelType:evaluation otherArguments:'    '");
  }
  int pipe_status;
  int pipefds_input[2];
  int pipefds_output[2];
  // Create pipes for process communication
  pipe_status = pipe(pipefds_input);
  if (pipe_status == -1) {
    throw runtime_error("Error creating pipe");
  }
  pipe_status = pipe(pipefds_output);
  if (pipe_status == -1) {
    throw runtime_error("Error creating pipe");
  }
  // Fork
  pid_t pid;
  pid = fork();
  if (pid == pid_t(0)) {
    // Child's IO
    dup2(CHILD_STDIN_READ, 0);
    dup2(CHILD_STDOUT_WRITE, 1);
    close(CHILD_STDIN_WRITE);
    close(CHILD_STDOUT_READ);
    // Call Beer
    stringstream beer_cmd;
    beer_cmd << "java -Xmx3G -jar " << beer_jar << " --workingMode interactive " << " -l " << beer_lang << " --modelType " << beer_modelType << " " << beer_otherArguments;
    TRACE_ERR("Executing: " + beer_cmd.str() + "\n");
    execl("/bin/bash", "bash", "-c", beer_cmd.str().c_str(), (char*)NULL);
    throw runtime_error("Continued after execl");
  }
  // Parent's IO
  close(CHILD_STDIN_READ);
  close(CHILD_STDOUT_WRITE);
  m_to_beer = new ofdstream(CHILD_STDIN_WRITE);
  m_from_beer = new ifdstream(CHILD_STDOUT_READ);
}

BeerScorer::~BeerScorer() {
  // Cleanup IO
  delete m_to_beer;
  delete m_from_beer;
}

void BeerScorer::setReferenceFiles(const vector<string>& referenceFiles)
{
  // Just store strings since we're sending lines to an external process
  for (int incRefs = 0; incRefs < (int)referenceFiles.size(); incRefs++) {
    m_references.clear();
    ifstream in(referenceFiles.at(incRefs).c_str());
    if (!in) {
      throw runtime_error("Unable to open " + referenceFiles.at(incRefs));
    }
    string line;
    while (getline(in, line)) {
      line = this->preprocessSentence(line);
      m_references.push_back(line);
    }
    m_multi_references.push_back(m_references);
  }
  m_references=m_multi_references.at(0);
}

void BeerScorer::prepareStats(size_t sid, const string& text, ScoreStats& entry)
{
  string sentence = this->preprocessSentence(text);
  string stats_str;
  stringstream input;
  // EVAL BEST ||| text ||| ref1 ||| ref2 ||| ...
  input << "EVAL BEST";
  input << " ||| " << text;
  for (int incRefs = 0; incRefs < (int)m_multi_references.size(); incRefs++) {
    if (sid >= m_multi_references.at(incRefs).size()) {
      stringstream msg;
      msg << "Sentence id (" << sid << ") not found in reference set";
      throw runtime_error(msg.str());
    }
    string ref = m_multi_references.at(incRefs).at(sid);
    input << " ||| " << ref;
  }
  input << "\n";
  // Threadsafe IO
#ifdef WITH_THREADS
  mtx.lock();
#endif
  //TRACE_ERR ( "in: " + input.str() );
  *m_to_beer << input.str();
  m_from_beer->getline(stats_str);
  float score = atof(stats_str.c_str());
  int denominator = pow(10, beer_decimals);
  int nominator = score*denominator;
  vector<ScoreStatsType> stats(2);
  stats.push_back(nominator);
  stats.push_back(denominator);

  //TRACE_ERR ( "out: " + stats_str + "\n" );
#ifdef WITH_THREADS
  mtx.unlock();
#endif
  entry.set(stats);
}

float BeerScorer::calculateScore(const vector<int>& comps) const
{
  return comps[0]*1.0/comps[1];
}

float beerScore(const std::vector<float>& stats){
  return stats[0]*1.0/stats[1];
}


#else

// Beer unsupported, throw error if used

BeerScorer::BeerScorer(const string& config)
  : StatisticsBasedScorer("BEER",config) {
  throw runtime_error("Beer unsupported, requires GLIBCXX");
}

BeerScorer::~BeerScorer() {}

void BeerScorer::setReferenceFiles(const vector<string>& referenceFiles) {}

void BeerScorer::prepareStats(size_t sid, const string& text, ScoreStats& entry) {}

float BeerScorer::calculateScore(const vector<int>& comps) const
{
  // Should never be reached
  return 0.0;
}

#endif

}
