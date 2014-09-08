package beer.permutation.metric.scorers

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.FooBar.isMonotone

class PEToperatorLengthScore {
  
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val n = p.size
    if(n<3) return 0

    val tree = ShiftReduce.parse(p)
    val treeNodesScores = tree map {
      case Term(_, _) => 0
      case NonTerm(_, _, _, _, operator, children) => {
        if(isMonotone(operator))
          1.0
        else
          1.0/children.size
      }
    }

    treeNodesScores.sum/(n-1)
  }

}
