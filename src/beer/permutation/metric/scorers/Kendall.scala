package beer.permutation.metric.scorers

import beer.permutation.helper.FooBar.monotonePermutation

object Kendall {
  def evaluate(p:List[Int]):Double={
    if(p.size == 0) return 0
    val score = kendallScore(p, monotonePermutation(p.size))

    score
  }
  
  private def kendallScore(x:List[Int], y:List[Int]) = 1 - kendallDistance(x,y)
  
  private def kendallDistance(x:List[Int], y:List[Int]) : Double = {
    var concordant = 0
    var discordant = 0
    var x_set = Set[(Int, Int)]()
    
    for(i <- 0 until x.size-1){
      for(j <- i+1 until x.size){
        x_set += ((x(i),x(j)))
      }
    }
    
    for(i <- 0 until y.size-1){
      for(j <- i+1 until y.size){
        if(x_set contains (y(i),y(j)) ){
          concordant += 1
        }else{
          discordant += 1
        }
      }
    }
    
    val distance = discordant.toDouble / (discordant.toDouble+concordant.toDouble)
    
    distance
  }

}
