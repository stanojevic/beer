package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm

object PETnodeCountScore {

  /**
   * expects permutation that starts from 1
   */
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val n = p.size
    if(n<3) return 1

    val tree = ShiftReduce.parse(p)
    val count = getNodeCount(tree)

    (count-1).toDouble/(n-2)
  }

  private def getNodeCount(node:TreeNode) : Int = {
    node match {
      case Term(_, _) => 0
      case NonTerm(_, _, _, _, _, children) => {
        1 :: (children map getNodeCount) sum
      }
    }
  }

}