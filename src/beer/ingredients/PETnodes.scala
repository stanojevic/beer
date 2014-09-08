package beer.ingredients

import beer.permutation.pet.representation.TreeNode
import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.permutation.finders.MeteorAlignmentToPermutation
import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.HelperFunctions
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.FooBar.{isMonotone, isInverted}
import beer.Configuration

class PETnodes (configuration:Configuration, params:Map[String, Object]) extends SparseIngredient {
  val functions:List[String] = params("functions").asInstanceOf[List[String]]
  val unalignedStrategy = params.
        getOrElse("unalignedStrategy","ignore and normalize").asInstanceOf[String]

  val featureName = "PETnodes"

  private val monoRegex = """mono""".r
  private val invRegex = """inv""".r
  private val biggerThanRegex = """branching>(.+)""".r
  private val lesserThanRegex = """branching<(.+)""".r
  private val equalRegex = """branching=(.+)""".r
  
  private val nodePredicates:List[(String, TreeNode=>Boolean)] = functions map parseFunctionLine

  private def parseFunctionLine(line: String): (String, TreeNode => Boolean) =
    line match {
      case monoRegex() =>
        ("MonoNode", dummyMonoPredicate(_))
      case invRegex() =>
        ("InvNode", dummyInvertedPredicate(_))
      case biggerThanRegex(size) =>
        val arity = size.toInt
        val predicate: TreeNode => Boolean = dummySizeBiggerThanPredicate(_, arity)
        (s".BT.$arity", predicate)
      case lesserThanRegex(size) =>
        val arity = size.toInt
        val predicate: TreeNode => Boolean = dummySizeSmallerThanPredicate(_, arity)
        (s".LT.$arity", predicate)
      case equalRegex(size) =>
        val arity = size.toInt
        val predicate: TreeNode => Boolean = dummyExactSizePredicate(_, arity)
        (s".EQ.$arity", predicate)
    }
       

  def eval(sp:SentencePair):Map[String, Double] = {
    val perm = MeteorAlignmentToPermutation.convertMeteorToPermutation(unalignedStrategy, sp)
    if(perm.size == 0)
      return Map()
    val incrementedPerm = perm map {_+1}

    val canonicalPET = ShiftReduce.parse(incrementedPerm)
    //val flatPET = HelperFunctions.collapseTree(canonicalPET)
    
    nodePredicates.map{ case (name, predicate) =>
      val positiveCounts = canonicalPET.count(predicate)
      val allCounts      = canonicalPET.size - perm.size
      val score = positiveCounts.toDouble/allCounts
      (name, score)
    }.toMap
  }

  private def dummyExactSizePredicate(node : TreeNode, size : Int) : Boolean =
    node match {
      case NonTerm(_, _, _, _, _, children) if children.size == size => true
      //case Term(_, _)                       if             0 == size => true
      case _                                                         => false
    }
  
  private def dummySizeBiggerThanPredicate(node : TreeNode, size : Int) : Boolean =
    node match {
      case NonTerm(_, _, _, _, _, children) if children.size >  size => true
      //case Term(_, _)                       if             0 > size => true
      case _                                                         => false
    }
  
  private def dummySizeSmallerThanPredicate(node : TreeNode, size : Int) : Boolean =
    node match {
      case NonTerm(_, _, _, _, _, children) if children.size <  size => true
      //case Term(_, _)                       if             0 == size => true
      case _                                                         => false
    }
  
  private def dummyMonoPredicate(node : TreeNode) : Boolean =
    node match {
      case NonTerm(_, _, _, _, operator, _) if isMonotone(operator) => true
      //case Term(_, _)                              if             0 == size => true
      case _                                                        => false
    }
  
  private def dummyInvertedPredicate(node : TreeNode) : Boolean =
    node match {
      case NonTerm(_, _, _, _, operator, _) if isInverted(operator) => true
      //case Term(_, _)                              if             0 == size => true
      case _                                                        => false
    }
  
  
}
