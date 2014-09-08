package beer.permutation.pet.parser

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.ChartDefinition

class HelperFunctionsTest extends FlatSpec with ShouldMatchers {

  "for paper example" should "return correct collapsed tree" in {
    // scalastyle:off
    val p = List(5, 7, 4, 6, 3, 1, 2)
    val originalPet = NonTerm(1, 7, 1, 7, List(2,1), List(
                        NonTerm(1, 5, 3, 7, List(2,1), List(
                          NonTerm(1, 4, 4, 7, List(2,4,1,3), List(
                            Term(1, 5), 
                            Term(2, 7),
                            Term(3, 4),
                            Term(4, 6))),
                          Term(5, 3))),
                        NonTerm(6, 7, 1,2, List(1,2), List(
                          Term(6, 1),
                          Term(7, 2)))))
    val correctCollapsedPet = 
                      NonTerm(1, 7, 1, 7, List(2,1), List(
                        NonTerm(1, 4, 4, 7, List(2,4,1,3), List(
                          Term(1, 5), 
                          Term(2, 7),
                          Term(3, 4),
                          Term(4, 6))),
                        Term(5, 3),
                        NonTerm(6, 7, 1,2, List(1,2), List(
                          Term(6, 1),
                          Term(7, 2)))))
    // scalastyle:on

    val collapsedPet = HelperFunctions.collapseTree(originalPet)

    collapsedPet should equal (correctCollapsedPet)
  }

}
