package beer.permutation.pet.metric

import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.Term

object PETavgArityScore {
  
  def evaluate(p:List[Int]) : Double = {
    assert( ! p.contains(0) )

    val n = p.size
    if(n<3) return 0 // worst and best is the same so we just give it 0 
                     // arbitrary decision. It can also be 1

    val tree = ShiftReduce.parse(p)
    val avgArity = getAvgArity(tree)

    (n-avgArity).toDouble/(n-2)
  }

  private def getAvgArity(tree:TreeNode) : Double = {
    val totalArity = tree map {
      case Term(_, _) => 0
      case NonTerm(_, _, _, _, _, children) => children.size
    } sum

    val treeSize = tree map {
      case Term(_, _) => 0
      case _ => 1
    } sum

    totalArity/treeSize
  }

}