/*
 * BeerScorer.h
 *
 *  Created on: Mar 23, 2015
 *      Author: milos
 */

#ifndef BEERSCORER_H_
#define BEERSCORER_H_

#include <set>
#include <string>
#include <vector>
#include <time.h>

#ifdef WITH_THREADS
#include <boost/thread/mutex.hpp>
#endif

#include "Types.h"
#include "StatisticsBasedScorer.h"

namespace MosesTuning {

class ofdstream;
class ifdstream;
class ScoreStats;

/**
 * BEER scoring
 *
 * https://github.com/stanojevic/beer
 *
 * Config:
 *
 * beerDir - directory containing beer
 *
 * lang - language (default: other)
 *
 * modelType - (default: evaluation)
 *
 */

class BeerScorer : public StatisticsBasedScorer
{
public:
  explicit BeerScorer(const std::string& config = "");
  ~BeerScorer();

  virtual void setReferenceFiles(const std::vector<std::string>& referenceFiles);
  virtual void prepareStats(std::size_t sid, const std::string& text, ScoreStats& entry);
  virtual void prepareStats(
		  std::vector<std::size_t>& sindexs,
		  std::vector<std::string>& texts,
          //ScoreDataHandle& score_data
		  boost::shared_ptr<ScoreData> score_data
  );

  virtual std::size_t NumberOfScores() const {
    // number of different numbers per evaluated sentence
    return 2;
  }

  virtual float calculateSentenceLevelBackgroundScore(const std::vector<ScoreStatsType>& totals, const std::vector<ScoreStatsType>& bg) {
	calculateScore(totals);
  }

  virtual float calculateScore(const std::vector<ScoreStatsType>& comps) const;

private:
  // BEER and process IO
  int m_processed_sentences_statistics;
  int m_processed_sentences_scored;
  int batch_size;
  time_t startTime;

  std::string beer_dir;
  std::string beer_lang;
  std::string beer_modelType;
  int beer_threads;
  ofdstream* m_to_beer;
  ifdstream* m_from_beer;
#ifdef WITH_THREADS
  mutable boost::mutex mtx;
#endif // WITH_THREADS

  // data extracted from reference files
  std::vector<std::string> m_references;
  std::vector<std::vector<std::string> > m_multi_references;

  virtual float getReferenceLength(const std::vector<ScoreStatsType>& totals) const {
    return totals[1];
  }

  // no copying allowed
  BeerScorer(const BeerScorer&);
  BeerScorer& operator=(const BeerScorer&);

};

} /* namespace MosesTuning */

#endif /* BEERSCORER_H_ */
