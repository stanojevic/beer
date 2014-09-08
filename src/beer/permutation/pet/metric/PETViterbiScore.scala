package beer.permutation.pet.metric

import beer.permutation.pet.parser.ChartParser
import beer.permutation.pet.representation.ChartDefinition.{Chart,
                                                ChartEntry,
                                                Term,
                                                NonTerm,
                                                Inference}
import beer.permutation.pet.representation.FooBar.{invertedPermutation, isMonotone}

class PETViterbiScore  (beta:Double) {

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

    val viterbiScore =
      viterbi(scoreCache, chart, 1, n)

    viterbiScore
  }

  private def emptyScoreCache(n:Int) : Array[Array[Double]] = {
    val cache = Array.fill[Double](n + 1, n + 1)(-1)
    for(i <- 0 to n){
      cache(i)(0) = Double.NegativeInfinity  // this is done in order to easily detect if these
      cache(0)(i) = Double.NegativeInfinity  // fields influence the computation (they shouldn't)
    }
    cache
  }

  private def viterbi(cache: Array[Array[Double]], chart:Chart, i:Int, j:Int) : Double = {
    if(cache(i)(j)<0){
      val entry = chart(i)(j)
      cache(i)(j) = entry match {
        case Term(_) => 1
        case NonTerm(op, inferences) => {
          val opScore = operatorScore(op)
          val span:Double = j - i + 1

          val childrenScore:Double =
            inferences.toList map { splitPoints:Inference =>
              val starts = i::splitPoints
              val ends = splitPoints.map{_-1} :+ j
              val spans = starts zip ends
              val weightedChildrenScores =
                spans map { case (a,b) =>
                  viterbi(cache, chart, a, b)*(b - a + 1)/span
                }

              weightedChildrenScores.sum
            } max

          val entryScore = beta*opScore + (1-beta)*childrenScore
          entryScore
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

}
