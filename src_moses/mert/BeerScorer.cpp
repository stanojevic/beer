/*
 * BeerScorer.cpp
 *
 *  Created on: Mar 23, 2015
 *      Author: milos
 */

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

namespace MosesTuning {

// BEER supported
#if defined(__GLIBCXX__) || defined(__GLIBCPP__)

// for clarity
#define CHILD_STDIN_READ pipefds_input[0]
#define CHILD_STDIN_WRITE pipefds_input[1]
#define CHILD_STDOUT_READ pipefds_output[0]
#define CHILD_STDOUT_WRITE pipefds_output[1]

BeerScorer::BeerScorer(const string& config)
  : StatisticsBasedScorer("BEER",config)
{
  m_processed_sentences_statistics = 0;
  m_processed_sentences_scored = 0;
  batch_size = 10000;
  startTime = time(0);

  beer_dir = getConfig("beerDir", "");
  beer_threads = atoi(getConfig("beerThreads", "8").c_str());
  beer_lang = getConfig("beerLang", "en");
  beer_modelType = getConfig("beerModelType", "evaluation");
  if(beer_dir == ""){
	  throw runtime_error("BEER installation directory required, see BeerScorer.h for full list of options: --scconfig beerDir:/path/to/beer/dir");
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
    beer_cmd << beer_dir << "/beer --workingMode interactive " << "--lang " << beer_lang << " --modelType " << beer_modelType ;
    // << " --caching "; // to add in the future version of BEER
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

void BeerScorer::prepareStats(
	  std::vector<std::size_t>& sindexs,
	  std::vector<std::string>& texts,
      // ScoreDataHandle& score_data
	  boost::shared_ptr<ScoreData> score_data
){
  TRACE_ERR("Batch prepareStats");
  int size = sindexs.size();

  std::vector<int> refLens;

  for(int i=0; i<size; i++){
	  // TODO printaj u BEER pipe push
	std::size_t sentence_index = sindexs[i];
	std::string text = texts[i];
    stringstream input;
    input << "EVAL BATCH PUSH";
    input << " ||| " << text;
    int maxLen = 0;
    for (int incRefs = 0; incRefs < (int)m_multi_references.size(); incRefs++) {
      if (sentence_index >= m_multi_references.at(incRefs).size()) {
        stringstream msg;
        msg << "Sentence id (" << sentence_index << ") not found in reference set";
        throw runtime_error(msg.str());
      }

      string ref = m_multi_references.at(incRefs).at(sentence_index);
      input << " ||| " << ref;

      vector<int> encoded_tokens;
      TokenizeAndEncode(ref, encoded_tokens);
      int len = encoded_tokens.size();
      if(len>maxLen){
    	  maxLen = len;
      }
    }
    refLens.push_back(maxLen);
    input << "\n";
    // Threadsafe IO
#ifdef WITH_THREADS
    mtx.lock();
#endif
    *m_to_beer << input.str();
#ifdef WITH_THREADS
    mtx.unlock();
#endif
  }
  TRACE_ERR("moses says DATA SENT")
  stringstream input;
  input << "RUN EVAL BEST BATCH ||| "<< beer_threads <<"\n";
#ifdef WITH_THREADS
  mtx.lock();
#endif
  *m_to_beer << input.str();
  std::vector<std::string> beerOutput;
  for(int i=0; i<size; i++){
    string stats_str;
    m_from_beer->getline(stats_str);
    beerOutput.push_back(stats_str);
  }
  TRACE_ERR("Got everything from BEER!");

  stringstream shutdownString;
  shutdownString << "EXIT\n";
  *m_to_beer << shutdownString.str();

#ifdef WITH_THREADS
  mtx.unlock();
#endif

  ScoreStats scoreentry;
  string stats_str;

  for(int i=0; i<size; i++){
	string stats_str = beerOutput[i];
	TRACE_ERR(stats_str.c_str());
    m_processed_sentences_statistics ++;
    if(m_processed_sentences_statistics % batch_size == 0){
      double seconds_since_start = difftime( time(0), startTime);
      std::ostringstream strs;
      strs << "Statistics " << m_processed_sentences_statistics
           << " in " << seconds_since_start << "s\n";
      TRACE_ERR(strs.str());
      startTime = time(0);
    }

	std::size_t sentence_index = sindexs[i];

    float score = atof(stats_str.c_str());
    scoreentry.reset();
    scoreentry.add(score);
    scoreentry.add(refLens[i]);

    score_data->add(scoreentry, sentence_index);
  }
}

void BeerScorer::prepareStats(size_t sid, const string& text, ScoreStats& entry)
{
  string sentence = this->preprocessSentence(text);
  string stats_str;
  stringstream input;
  // EVAL BEST ||| text ||| ref1 ||| ref2 ||| ... ||| refn
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

  m_processed_sentences_statistics ++;
  if(m_processed_sentences_statistics % batch_size == 0){
    double seconds_since_start = difftime( time(0), startTime);
    std::ostringstream strs;
    strs << "Statistics " << m_processed_sentences_statistics
         << " in " << seconds_since_start << "s\n";
    TRACE_ERR(strs.str());
    startTime = time(0);
  }

  int score = atof(stats_str.c_str());

  std::ostringstream strs2;
  strs2 << m_processed_sentences_statistics << " "
		<< score << " " << text << "s\n";
  TRACE_ERR(strs2.str());

  entry.reset();
  entry.add(score);
}

float BeerScorer::calculateScore(const vector<ScoreStatsType>& comps) const
{
  // m_processed_sentences_scored ++;
  // if(m_processed_sentences_scored % batch_size == 0){
  //   double seconds_since_start = difftime( time(0), startTime);
  //   std::ostringstream strs;
  //   strs << "Scoring " << m_processed_sentences_scored << " in " << seconds_since_start << "s\n";
  //   TRACE_ERR(strs.str());
  //   startTime = time(0);
  // }
  // int accScore = comps[0];
  // int denominator = comps[1];
  // float finalScore = (accScore + 0.0)/(denominator + 0.0);
  return comps[0];
}

#else

// BEER unsupported, throw error if used

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

} /* namespace MosesTuning */



