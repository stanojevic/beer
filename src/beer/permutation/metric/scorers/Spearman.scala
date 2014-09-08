package beer.permutation.metric.scorers

import beer.permutation.helper.FooBar.monotonePermutation
import Math.{pow, sqrt}

object Spearman {

  def evaluate(p:List[Int]) : Double = {
    if(p.size == 0) return 0
    val score = spearmanScore(p, monotonePermutation(p.size))

    score
  }

  private def spearmanScore(x:List[Int], y:List[Int]) = (spearmanCoeficient(x, y)+1)/2

  private def noTiesSpearmanCoeficient(a:List[Int], b:List[Int]) : Double ={
    val ds = (a zip b) map { case x => x._1 - x._2 }
    val n = ds.size
    val rho = 1-6*ds.map(pow(_,2)).sum/(n*(pow(n,2)-1))
    rho
  }

  private def normalizedSpearmanScore(x:List[Int], y:List[Int]) = (1+spearmanCoeficient(x,y))/2

  private def spearmanCoeficient(x:List[Int], y:List[Int]) : Double = {
    val x_ranks = normalizeRanks(x)
    val y_ranks = normalizeRanks(y)
    normalizedRanksSpearmanCoeficient(x_ranks, y_ranks)
  }

  private def normalizedRanksSpearmanCoeficient(x_ranks:List[Double], y_ranks:List[Double]) : Double = {
    val x_mean = x_ranks.sum/x_ranks.size
    val y_mean = y_ranks.sum/y_ranks.size

    val nominator = (x_ranks zip y_ranks).map { case (x_i, y_i) => (x_i-x_mean)*(y_i-y_mean)}.sum
    val denominator = sqrt(
        x_ranks.map{ case x_i => pow(x_i-x_mean,2)}.sum *
        y_ranks.map{ case y_i => pow(y_i-y_mean,2)}.sum )

    if(denominator == 0)
      0
    else
      nominator/denominator
  }

  private def normalizeRanks(a: List[Int]): List[Double] = {
    val ordered_a = a.sorted
    val rankMap = scala.collection.mutable.Map[Int, Double]()
    val n = a.size

    var current_count = 1
    var last_val = ordered_a(0)

    for (i <- 2 to n) {
      if (last_val == ordered_a(i - 1)) {
        current_count += 1
      } else {
        rankMap(last_val) = (i - 1 + i - 1 - current_count + 1).toFloat / 2
        last_val = ordered_a(i - 1)
        current_count = 1
      }
    }
    rankMap(last_val) = (n + n - current_count + 1).toFloat / 2

    var new_ordering = List[Double]()

    for (i <- a.size - 1 to 0 by -1) {
      new_ordering ::= rankMap(a(i))
    }

    new_ordering
  }

}
