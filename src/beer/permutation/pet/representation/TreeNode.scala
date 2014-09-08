package beer.permutation.pet.representation

import scala.collection.Traversable

abstract class TreeNode extends Traversable[TreeNode]{
  
  def foreach[U](f:TreeNode => U) : Unit = {
    def depthFirstPreOrder(node:TreeNode) : Unit = {
      f(node)
      node match {
        case NonTerm(_, _, _, _, _, children) => children map depthFirstPreOrder
        case Term(_, _) => 
      }
    }
    depthFirstPreOrder(this)
  }

  def span() : Int = this match {
    case NonTerm(start, end, _, _, _, _) => end - start + 1
    case Term(_, _) => 1
  }

  def left() : Int = this match {
    case NonTerm(start, _, _, _, _, _) => start
    case Term(pos, _) => pos
  }

  def right() : Int = this match {
    case NonTerm(_, end, _, _, _, _) => end
    case Term(pos, _) => pos
  }

  def prettyPrint(depth:Int = 0) : String = {
    val nodeDesc = this match {
      case NonTerm(start, end, _, _, _, _) => s"($start, $end)"
      case Term(pos, el) => s"$pos"
    }
    val childrenDesc = this match {
      case NonTerm(_, _, _, _, _, children) => children map {_.prettyPrint(depth + 1)} mkString ("")
      case Term(_, _) => ""
    }
    val opDesc = this match {
      case NonTerm(_, _, _, _, op, _) => op.toString
      case Term(_, _) => "TERM"
    }
    "  "*depth + s"$nodeDesc <$opDesc>\n$childrenDesc"
  }
}
case class NonTerm(val start:Int, val end:Int, val min:Int, val max:Int, val operator:List[Int], val children:List[TreeNode]) extends TreeNode
case class Term(val position:Int, val el:Int) extends TreeNode

