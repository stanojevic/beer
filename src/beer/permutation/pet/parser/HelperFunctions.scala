package beer.permutation.pet.parser

import beer.permutation.pet.representation.{TreeNode, Term, NonTerm}

object HelperFunctions {

  /**
   * merges nodes that have the same operator
   *   - only monotones and inverteds are considered
   *   - only parent left child nodes are considered (at arbitrary depth)
   * when children of two nodes are merged the operator doesn't change
   * for example if the number of children is >2 operator
   *   still has two elements: List(1,2) or List(2,1)
   * @param tree non collapsed left branching permutation tree
   * @return collapsed tree
   */
  def collapseTree(tree:TreeNode) : TreeNode = {
    tree match {
      case Term(_, _) => tree
      case NonTerm(start, end, min, max, op, children) => {
        val newChildren = children map collapseTree
        newChildren.head match{
          case NonTerm(_, _, _, _, cOp, cChildren) if(cOp == op && (op == List(1,2) || op == List(2,1)))
                 => NonTerm(start, end, min, max, op, cChildren++newChildren.tail)
          case _ => NonTerm(start, end, min, max, op, newChildren)
        }
      }
    }
  }

  def mergeChildren(children:List[TreeNode]) : TreeNode = {
    val operator = computeOperator(children)
    val (start, end) = findSpan(children)
    val newMin = children.map{
      case Term(pos, el) => el
      case NonTerm(_, _, min, _, _, _) => min
    }.min
    val newMax = children.map{
      case Term(pos, el) => el
      case NonTerm(_, _, _, max, _, _) => max
    }.max
    val newNode = new NonTerm(start, end, newMin, newMax, operator, children)
    newNode
  }

  private final
  def findSpan(children:List[TreeNode]) : (Int, Int) = {
    val lefts = children.map{ _.left}
    val rights = children.map{ _.right}
    val left = lefts.min
    val right = rights.max
    (left,right)
  }

  private final
  def computeOperator(children:List[TreeNode]) : List[Int] = {
    val childrenWithInd = children.zipWithIndex
    val sortedChildren = childrenWithInd.sortBy{ _._1 match {
      case NonTerm(_, _, min, _, _, _) => min
      case Term(_, el)              => el
    }}
    val newChildrenWithInd = sortedChildren.map{_._2}.zipWithIndex
    val newSortedChildren = newChildrenWithInd.sortBy{_._1}
    newSortedChildren.map{_._2 + 1}
  }
}
