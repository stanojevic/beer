package beer.permutation.pet.representation

object FooBar {

  /**
   * ! all permutations start with 1 !
   */

  val monotoneOperator = List(1,2)
  val invertedOperator = List(2,1)

  def monotonePermutation(n:Int) : List[Int] = (1 to n).toList

  def invertedPermutation(n:Int) : List[Int] = (1 to n).toList.reverse

  def randomPermutation(n:Int) : List[Int] = {
    val r = new scala.util.Random
    r.shuffle(1 to n).toList
  }

  /**
   * this is just to make testing for monotone operator more robust
   * number of elements in operator might not be the same as
   * the number of children (for example in flat PET), but
   * that might change in the future. That's the reason for this
   * weird test for monotone
   */
  def isMonotone(op:List[Int]) : Boolean = {
    val monotoneOp1 = monotoneOperator
    val monotoneOp2 = monotonePermutation(op.size)
    op == monotoneOp1 || op == monotoneOp2
  }

  def isInverted(op:List[Int]) : Boolean = {
    val invertedOp1 = invertedOperator
    val invertedOp2 = invertedPermutation(op.size)
    op == invertedOp1 || op == invertedOp2
  }
}
