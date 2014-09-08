package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.{TreeNode, Term, NonTerm}
import beer.permutation.pet.representation.FooBar.{isMonotone, invertedPermutation}
import beer.permutation.pet.parser.HelperFunctions

class PETscore (beta:Double, flatPet:Boolean) {

  /**
   * expects permutation that starts from 1
   */
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val unScaledScore = rawScore(p)
    val invertedScore = rawScore(invertedPermutation(p.size))

    val scaledScore = (unScaledScore-invertedScore)/(1-invertedScore)

    scaledScore
  }

  private def rawScore(p:List[Int]) : Double = {
    val rawTree = ShiftReduce.parse(p)
    val tree = flatPet match {
      case true  => HelperFunctions.collapseTree(rawTree)
      case false => rawTree
    }

    nodeScore(tree)
  }

  private def nodeScore(node: TreeNode) : Double =
    node match {

      case Term(_, _) => 1

      case NonTerm(_, _, _, _, operator, children) => {
        val opScore = operatorScore(operator)
        val parentSpan:Double = node.span
        val weightedChildrenScore = children.map{ child =>
          nodeScore(child) * child.span/parentSpan
        }
        val totalChildrenScore = weightedChildrenScore.sum

        beta*opScore + (1-beta)*totalChildrenScore
      }

    }

  private def operatorScore(op:List[Int]) =
    isMonotone(op) match {
      case true  => 1
      case false => 0
    }

}
