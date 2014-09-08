package beer.permutation.pet.metric

import beer.permutation.pet.parser.ChartParser
import beer.permutation.pet.representation.ChartDefinition.{Chart,
                                                ChartEntry,
                                                Term,
                                                NonTerm,
                                                Inference}
import beer.permutation.pet.representation.FooBar.{invertedPermutation, isMonotone}

class PEFoldScore (beta:Double) {

  /**
   * expects permutation that starts from 1
   */
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val unScaledScore = rawScore(p)
    val invertedScore = rawScore(invertedPermutation(p.size))

    val scaledScore = (unScaledScore-invertedScore)/(1-invertedScore)

    scaledScore
  }

  private def rawScore(p:List[Int]) : Double = {
    val n = p.size
    val chart = ChartParser.shiftReduceChartParse(p)

    val scoreCache = emptyScoreCache(n)
    insideScore(scoreCache, chart, 1, n)
  }

  private def insideScore(cache:Array[Array[Double]],
                          chart:Chart,
                          i:Int, j:Int) : Double = {
    if(cache(i)(j)<0){
      cache(i)(j) = chart(i)(j) match {
        case Term(_) => 1
        case NonTerm(op, inferences) => {
          val opScore = operatorScore(op)
          val spanSize:Double = j - i + 1

          val childrenScore = inferences.map{ splitPoints =>
            val starts = i::splitPoints
            val ends = splitPoints.map{_-1} :+ j
            val spans = starts zip ends

            spans.map { case (a,b) =>
              insideScore(cache, chart, a, b) * (b - a + 1)
            }.sum / spanSize
          }.sum / inferences.size

          beta*opScore + (1-beta)*childrenScore
        }
      }
    }

    cache(i)(j)
  }

  private def operatorScore(op:List[Int]) =
    isMonotone(op) match {
      case true  => 1
      case false => 0
    }

  private def emptyScoreCache(n:Int) : Array[Array[Double]] = {
    val cache = Array.fill[Double](n + 1, n + 1)(-1)
    for(i <- 0 to n){
      cache(i)(0) = Double.NegativeInfinity  // this is done in order to easily detect if these
      cache(0)(i) = Double.NegativeInfinity  // fields influence the computation (they shouldn't)
    }
    cache
  }
}
