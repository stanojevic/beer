package beer.permutation.metric.scorers

import beer.permutation.helper.FooBar.monotonePermutation

object Ulam {

  def evaluate(p:List[Int]):Double={
    if(p.size == 0) return 0
    val score = ulamScore(p, monotonePermutation(p.size))

    score
  }

  private def ulamScore(x:List[Int], y:List[Int]) = 1- ulamDistance(x,y)
  
  private def ulamDistance(x:List[Int], y:List[Int]) : Double = {
    val n = x.length
    1-(lcs(x,y).toFloat-1)/(n-1) // becuase minimal value of lcs is 1 not 0
  }
  
  private def lcs(x:List[Int], y:List[Int]):Int={
    val lengths = Array.ofDim[Int](x.length+1, y.length+1)
    lengths(0)(0)=0
    for(i <- 0 until x.length){
      for(j <- 0 until y.length){
        if(x(i)==y(j))
            lengths(i+1)(j+1) = lengths(i)(j)+1
        else
            lengths(i+1)(j+1) = Math.max(lengths(i+1)(j), lengths(i)(j+1))
      }
    }
    lengths(x.length)(y.length)
  }

}
