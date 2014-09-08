package beer.permutation.pet.parser

import scala.collection.immutable.Stack
import scala.annotation.tailrec
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.NonTerm

object ShiftReduce {

  /** Finds the canonical (left branching) permutation tree
   *
   *  @param p permutation that starts from 1 (not 0)
   *  @return left branching permutation tree
   */
  def parse(p: List[Int]) : TreeNode = {
    assert( ! p.contains(0), "permutation starts from 1, not 0")
    if(p.size == 0){
      throw new IllegalArgumentException("Can't parse an empty permutation")
    }else if(p.size == 1){
      Term(1, p.head)
    }else{
      val pTerms = p.zipWithIndex.map{case (el, pos) => Term(pos + 1 , el)}
      recParse(pTerms, Stack[TreeNode]())
    }
  }


  @tailrec
  private final
  def recParse(p:List[Term], stack:Stack[TreeNode]) : TreeNode = {
    if(p.isEmpty){
      assert(stack.size==1, "is something wrong with the permutation?")
      stack.head
    }else{
      val (newP, newStack) = shift(p, stack)
      val newStack2 = tryMultiReduce(newStack)
      recParse(newP, newStack2)
    }
  }

  private final
  def shift(p:List[Term], stack:Stack[TreeNode]) : (List[Term], Stack[TreeNode]) = {
    val newStack = stack.push(p.head)
    val newP = p.tail

    (newP, newStack)
  }

  @tailrec
  private final
  def tryMultiReduce(stack:Stack[TreeNode]) : Stack[TreeNode] = {
    tryReduce(stack) match {
      case (true , newStack) => tryMultiReduce(newStack)
      case (false, _)        => stack
    }
  }

  private final
  def tryReduce(stack:Stack[TreeNode]) : (Boolean, Stack[TreeNode]) = {
    val (top:TreeNode, rest:Stack[TreeNode]) = stack.pop2
    top match {
      case NonTerm(_, _, min, max, _, _) => recTryReduce(min, max, max - min + 1, List(top), rest)
      case Term(pos, el)                => recTryReduce(el , el , 1        , List(top), rest)
    }
  }

  @tailrec
  private final
  def recTryReduce(
      min:Int,
      max:Int,
      span:Int,
      before:List[TreeNode],
      currentStack:Stack[TreeNode] ) : (Boolean, Stack[TreeNode]) = {
    if(currentStack.isEmpty){
      (false, Stack()) //value of the second argument should be irrelevant
    }else{
      val (top:TreeNode, rest:Stack[TreeNode]) = currentStack.pop2
      val (topMin, topMax, topSpan) = top match {
        case NonTerm(_, _, min, max, _, _) => (min, max, max - min + 1)
        case Term(_, el)                => (el , el , 1)
      }
      val newMin = Math.min(min, topMin)
      val newMax = Math.max(max, topMax)
      val newSpan= span + topSpan

      val children = top::before

      if(newMax - newMin + 1 == newSpan){ // great! it is reducable
        val newNode = HelperFunctions.mergeChildren(children)
        (true, rest push newNode)
      }else{ // maybe later
        recTryReduce(newMin, newMax, newSpan, children, rest)
      }
    }
  }

}
