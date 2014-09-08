package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.FooBar.isMonotone
import beer.permutation.pet.parser.HelperFunctions

class PETmonotoneRatio (useChartSpans:Boolean) {

  /**
   * expects permutation that starts from 1
   */
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val n = p.size
    if(n<3) return 0

    val canonicalPET = ShiftReduce.parse(p)
    val flatPET = HelperFunctions.collapseTree(canonicalPET)

    val count = getMonotoneCount(flatPET)
    val maxPossibleCount = useChartSpans match {
      case true  => n*(n-1.0)/2
      case false => n-1.0
    }

    count / maxPossibleCount
  }

  private def getMonotoneCount(node:TreeNode) : Int = {
    node match {
      case Term(_, _) => 0
      case NonTerm(_, _, _, _, op, children) => {
        val childrenMonotones = children map getMonotoneCount sum
        val n = children.size
        val parentsMonotones = (useChartSpans, isMonotone(op)) match {
          case (true , true) => n*(n-1)/2
          case (false, true) => n-1
          case _             => 0
        }

        childrenMonotones + parentsMonotones
      }
    }
  }

}
