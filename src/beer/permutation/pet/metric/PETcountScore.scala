package beer.permutation.pet.metric

import beer.permutation.pet.metric.helper.CatalanCounter
import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.HelperFunctions
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.FooBar.isMonotone

object PETcountScore{
  
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )
    if(p.size == 1){
      return 1 //to avoid division with 0 later
    }else if(p == List(2,1)){
      return 0
    }else if(p == List(1,2)){
      return 1
    }
  

    val n = p.size
    val pet = ShiftReduce.parse(p)
    val flatPET = HelperFunctions.collapseTree(pet)
    val actualPETcount = CatalanCounter.countLinear(flatPET)
    val maxPETcount = CatalanCounter.catalan(p.size-1)
    
    val preciseCountRatio:Double = (BigDecimal(actualPETcount-1)/BigDecimal(maxPETcount-1)).toDouble
    
    preciseCountRatio
  }
  
}
