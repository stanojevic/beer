package beer.permutation.pet.metric

import beer.permutation.pet.parser.ChartParser
import beer.permutation.pet.representation.ChartDefinition.{Chart,
                                                ChartEntry,
                                                Term,
                                                NonTerm,
                                                Inference}
import beer.permutation.pet.representation.FooBar.{invertedPermutation, isMonotone}
import beer.permutation.pet.metric.helper.CatalanCounter

class PEFscore (beta:Double) {

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

    val countCache = CatalanCounter.emptyCountCache(n)
    val petsCount : BigInt  = CatalanCounter.countInside(countCache, chart, 1, n)

    val scoreCache = emptyScoreCache(n)
    val cummulativeScore : BigDecimal =
      insideScore(scoreCache, countCache, chart, 1, n)


    (cummulativeScore/BigDecimal(petsCount)).toDouble
  }

  private def emptyScoreCache(n:Int) : Array[Array[BigDecimal]] = {
    val cache = Array.fill[BigDecimal](n + 1, n + 1)(-1)
    for(i <- 0 to n){
      cache(i)(0) = -1.0  // this is done in order to easily detect if these
      cache(0)(i) = -1.0  // fields influence the computation (they shouldn't)
    }
    cache
  }

  private def insideScore(
      scoreCache:Array[Array[BigDecimal]],
      countCache:Array[Array[BigInt]],
      chart : Chart,
      i:Int,
      j:Int) : BigDecimal = {
    if(scoreCache(i)(j) < 0){
      val entry = chart(i)(j)

      entry match {
        case Term(_) => {
          scoreCache(i)(j) = 1
        }
        case NonTerm(operator, inferences) => {
          val span:Double = j - i + 1

          var childrenScore : BigDecimal = 0.0
          for(splitPoints <- inferences){
            val starts = i::splitPoints
            val ends = splitPoints.map{_-1} :+ j
            val childrenSpans = starts zip ends
            
            val inferencePETcount = childrenSpans map {	
              case (a,b) => countCache(a)(b)} product

            for(childSpan <- childrenSpans){
              val childScore : BigDecimal = insideScore(scoreCache, countCache, chart, childSpan._1, childSpan._2)
              val treePasses : BigInt = inferencePETcount / countCache(childSpan._1)(childSpan._2)
              val weight : Double = (childSpan._2 - childSpan._1 + 1)/span

              childrenScore += childScore * BigDecimal(treePasses) * weight
            }
          }

          val opScore = countCache(i)(j) * operatorScore(operator)

          scoreCache(i)(j) = beta*BigDecimal(opScore) + (1-beta)*childrenScore
        }
      }
    }

    scoreCache(i)(j)
  }

  private def operatorScore(op:List[Int]) =
    isMonotone(op) match {
      case true  => 1
      case false => 0
    }

}
