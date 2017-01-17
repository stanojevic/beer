#ifndef MERT_BEER_SCORER_H_
#define MERT_BEER_SCORER_H_

#include <set>
#include <string>
#include <vector>

#ifdef WITH_THREADS
#include <boost/thread/mutex.hpp>
#endif

#include "Types.h"
#include "StatisticsBasedScorer.h"

namespace MosesTuning
{

class ofdstream;
class ifdstream;
class ScoreStats;

/**
 * BEER scoring
 *
 * https://github.com/stanojevic/beer
 * 
 *
 * Config:
 * beerDir - location of beer installation
 *
 * Usage with mert-moses.pl:
 * --mertargs="--sctype BEER --scconfig beerDir:/path/to/beerDir"
 *
 * Usage with mert-moses.pl when using --batch-mira:
 * --batch-mira-args="--sctype BEER --scconfig beerDir:/path/to/beerDir,beerThreads:30"
 */
class BeerScorer: public StatisticsBasedScorer
{
public:
  explicit BeerScorer(const std::string& config = "");
  ~BeerScorer();

  virtual void setReferenceFiles(const std::vector<std::string>& referenceFiles);
  virtual void prepareStats(std::size_t sid, const std::string& text, ScoreStats& entry);

  virtual std::size_t NumberOfScores() const {
    // sentence score and constant 1 used for counting number of sentences
    return 2;
  }

  virtual float getReferenceLength(const std::vector<ScoreStatsType>& totals) const {
    // refLen is index 1 (see above stats comment)
    return totals[1];
  }

  virtual float calculateScore(const std::vector<ScoreStatsType>& comps) const;

private:
  // Beer and process IO
  std::string beer_dir;
  std::string beer_threads;
  std::string beer_model;
  ofdstream* m_to_beer;
  ifdstream* m_from_beer;
#ifdef WITH_THREADS
  mutable boost::mutex mtx;
#endif // WITH_THREADS

  // data extracted from reference files
  std::vector<std::string> m_references;
  std::vector<std::vector<std::string> > m_multi_references;

  // no copying allowed
  BeerScorer(const BeerScorer&);
  BeerScorer& operator=(const BeerScorer&);

};

}

#endif // MERT_BEER_SCORER_H_
