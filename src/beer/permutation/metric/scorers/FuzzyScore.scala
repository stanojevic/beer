package beer.permutation.metric.scorers

object FuzzyScore {

  def evaluate(permutation: List[Int]) : Double = {
    if(permutation.size == 0) return 0
    val score = fuzzyReorderingScore(permutation)
    score
  }

  private def fuzzyPenalty(x:List[Int]) : Double ={
    var C = 1

    for(i <- 1 until x.length)
      if(x(i-1)+1!=x(i))
        C+=1

    var M = x.length 

    (C-1).toFloat/(M-1)
  }

  private def fuzzyReorderingScore(x:List[Int]) = 1 - fuzzyPenalty(x)

}
