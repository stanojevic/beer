package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm

object PETmaxArityScore {

  /**
   * expects permutation that starts from 1
   */
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val n = p.size
    if(n<3) return 0

    val tree = ShiftReduce.parse(p)
    val arity = getArity(tree)

    (n-arity).toDouble/(n-2)
  }

  private def getArity(node:TreeNode) : Int = {
    node match {
      case Term(_, _) => 1
      case NonTerm(_, _, _, _, _, children) => {
        children.size :: (children map getArity) max
      }
    }
  }

}