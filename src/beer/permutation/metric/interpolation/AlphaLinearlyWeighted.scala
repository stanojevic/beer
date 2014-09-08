package beer.permutation.metric.interpolation

class AlphaLinearlyWeighted(
    val alpha : Double,
    val orderingMetric : (List[Int])      => Double,
    val lexicalMetric  : (List[String], List[String]) => Double,
    val brevityPenalty : (Int, Int)       => Double
    ) {

  def evaluate(ref:String, sys:String, refPermutation:List[Int]) : Double = {
    val refWords = ref split " +" toList

    val sysWords = sys split " +" toList

    evaluate(refWords, sysWords, refPermutation)
  }

  def evaluate(refWords:List[String], sysWords:List[String], refPermutation:List[Int]) : Double = {
    val refLen = refWords.size
    val sysLen = sysWords.size
    val BP = brevityPenalty(refLen, refPermutation.size)
    
    val lexicalScore  = lexicalMetric(refWords,sysWords)
    val orderingScore = orderingMetric(refPermutation)

    alpha*lexicalScore + (1-alpha)*BP*orderingScore
  }

}
