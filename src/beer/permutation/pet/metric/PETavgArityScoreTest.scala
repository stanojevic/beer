package beer.permutation.pet.metric

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class PETavgArityScoreTest extends FlatSpec with ShouldMatchers{

  "for monotone score PET avg arity score" should "return 1" in {
    val p = List(1, 2, 3, 4, 5)
    val score = PETavgArityScore.evaluate(p)
    
    score should equal (1) 
  }

  "for inverted score PET avg arity score" should "return 1" in {
    val p = List(6, 5, 4, 3, 2, 1)
    val score = PETavgArityScore.evaluate(p)
    
    score should equal (1)
  }
  
  "for sort of half monotone PET avg arity score" should "return 1" in {
    val p = List(1, 2, 3, 5, 4)
    val score = PETavgArityScore.evaluate(p)
    
    score should equal (1)
  }

  "for Wu permutation PET avg arity score" should "return 0" in {
    val p = List(2, 4, 1, 3)
    val score = PETavgArityScore.evaluate(p)
    
    score should equal (0)
  }

}
