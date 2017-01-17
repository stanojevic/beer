#include "BeerScorer.h"

#include <algorithm>
#include <cmath>
#include <fstream>
#include <iterator>
#include <sstream>
#include <stdexcept>
#include <cstdio>
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
#if (defined(__GLIBCXX__) || defined(__GLIBCPP__)) && !defined(_WIN32)

// for clarity
#define CHILD_STDIN_READ pipefds_input[0]
#define CHILD_STDIN_WRITE pipefds_input[1]
#define CHILD_STDOUT_READ pipefds_output[0]
#define CHILD_STDOUT_WRITE pipefds_output[1]

BeerScorer::BeerScorer(const string& config)
  : StatisticsBasedScorer("BEER",config)
{
  beer_dir = getConfig("beerDir", "");
  beer_threads = getConfig("beerThreads", "1");
  beer_model = getConfig("beerModel", "");
  if (beer_dir == "") {
    throw runtime_error("BEER dir is required, see BeerScorer.h for full list of options: --scconfig beerDir:/path/to/beerDir");
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
    // Call BEER
    stringstream beer_cmd;
    beer_cmd << beer_dir << "/scripts/interactive --threads " << beer_threads;
    if(beer_model != ""){
	    beer_cmd << " --model " << beer_model;
    }
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

BeerScorer::~BeerScorer()
{
  // Cleanup IO
  // delete m_to_beer;
  // delete m_from_beer;
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
  // SCORE ||| ref1 ||| ref2 ||| ... ||| text
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
  //TRACE_ERR ( "out: " + stats_str + "\n" );
#ifdef WITH_THREADS
  mtx.unlock();
#endif
  float score = atof(stats_str.c_str());
  entry.reset();
  entry.add(score);
  entry.add(1.0);
  //entry.set(stats_str);
}

float BeerScorer::calculateScore(const vector<ScoreStatsType>& comps) const
{
  double result = comps[0]/comps[1];
  return result;
}

#else

// Beer unsupported, throw error if used

BeerScorer::BeerScorer(const string& config)
  : StatisticsBasedScorer("BEER",config)
{
  throw runtime_error("BEER unsupported, requires GLIBCXX");
}

BeerScorer::~BeerScorer() {}

void BeerScorer::setReferenceFiles(const vector<string>& referenceFiles) {}

void BeerScorer::prepareStats(size_t sid, const string& text, ScoreStats& entry) {}

float BeerScorer::calculateScore(const vector<ScoreStatsType>& comps) const
{
  // Should never be reached
  return 0.0;
}

#endif

}
