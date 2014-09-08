package beer.permutation.finders

import org.scalatest.{FlatSpec, ShouldMatchers}

class HandleUnalignedTest extends FlatSpec with ShouldMatchers {

  // scalastyle:off
  "for partial permutation left attach strategy" should "return correct full permutation" in {
    val partialSortedRefPerm = List(2,1,5,6)
    val unaligned = List(0,3,4)
    val unalignedStrategy = "attach left"
    val correctPermutation = List(0,2,3,4,1,5,6)

    val p = HandleUnaligned.insertUnaligned(unalignedStrategy, partialSortedRefPerm, unaligned)
    p should equal (correctPermutation)
  }
  // scalastyle:on

  "for partial permutation rigth attach strategy" should "return correct full permutation" in {
    // scalastyle:off
    val partialSortedRefPerm = List(2,1,5,6)
    val unaligned = List(0,3,4)
    val unalignedStrategy = "attach right"
    val correctPermutation = List(2,0,1,3,4,5,6)
    // scalastyle:on

    val p = HandleUnaligned.insertUnaligned(unalignedStrategy, partialSortedRefPerm, unaligned)
    p should equal (correctPermutation)
  }

  "for partial permutation ignore strategy" should "return correct full permutation" in {
    // scalastyle:off
    val partialSortedRefPerm = List(2,1,5,6)
    val unaligned = List(0,3,4)
    val unalignedStrategy = "ignore and normalize"
    val correctPermutation = List(1,0,2,3)
    // scalastyle:on

    val p = HandleUnaligned.insertUnaligned(unalignedStrategy, partialSortedRefPerm, unaligned)
    p should equal (correctPermutation)
  }

}