package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.HelperFunctions
import beer.permutation.pet.metric.helper.CatalanCounter.catalan
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.FooBar.{isMonotone, isInverted}

class PETcountMonoRestricted {

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
    val actualPETcount = flatPET map {
      case NonTerm(_, _, _, _, op, children) if isMonotone(op) => catalan(children.size-1)
      case _ => BigInt(1)
    } product
    val maxPETcount = catalan(p.size-1)
    val preciseCountRatio = BigDecimal(actualPETcount-1)/BigDecimal(maxPETcount-1)
    
    preciseCountRatio.toDouble
  }

}
