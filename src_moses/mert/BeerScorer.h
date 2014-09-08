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
 * Beer scoring 
 *
 * https://github.com/stanojevic/beer
 * http://www.statmt.org/wmt14/pdf/W14-3354.pdf
 *
 * Config:
 * jar - location of beer_*.jar (beer_1.0.jar at time of writing)
 * lang - optional language code (default: other)
 * decimals - optional precision of BEER score (default: 5)
 * modelType - optional model type (default: tuning)
 * otherArguments - optional other arguments
 *
 * Usage with mert-moses.pl:
 * --mertargs="--sctype BEER --scconfig decimals:6 jar:/path/to/beer_1.0.jar lang:ru modelType:evaluation otherArguments:'    '"
 */
class BeerScorer: public StatisticsBasedScorer
{
public:
  explicit BeerScorer(const std::string& config = "");
  ~BeerScorer();

  virtual void setReferenceFiles(const std::vector<std::string>& referenceFiles);
  virtual void prepareStats(std::size_t sid, const std::string& text, ScoreStats& entry);

  virtual std::size_t NumberOfScores() const {
    return 2;
  }

  virtual float calculateScore(const std::vector<int>& comps) const;

private:
  // Beer and process IO
  std::string beer_jar;
  std::string beer_lang;
  std::string beer_modelType;
  std::string beer_otherArguments;
  int beer_decimals;
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

float beerScore(const std::vector<float>& stats);

}

#endif // MERT_BEER_SCORER_H_
