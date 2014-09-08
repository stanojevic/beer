package beer.permutation.metric.scorers

import beer.permutation.helper.FooBar.monotonePermutation

object Hamming {

  def evaluate(p:List[Int]):Double={
    if(p.size == 0) return 0
    val score = hammingScore(p, monotonePermutation(p.size))

    score
  }

  private def hammingScore(x:List[Int], y:List[Int]) = 1 - hammingDistance(x,y)

  private def hammingDistance(x:List[Int], y:List[Int]) : Double = {
    val n = x.size
    var d = 0
    for (i <- 0 until n) {
      if (x(i) != y(i))
        d += 1
    }
    d.toFloat / n
  }


}
