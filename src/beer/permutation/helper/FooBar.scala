package beer.permutation.helper

object FooBar {
  def monotonePermutation(n:Int) : List[Int] = (0 until n).toList

  def invertedPermutation(n:Int) : List[Int] = (0 until n).toList.reverse

  def randomPermutation(n:Int) : List[Int] = {
    val r = new scala.util.Random
    r.shuffle((0 until n).toList)
  }

}
