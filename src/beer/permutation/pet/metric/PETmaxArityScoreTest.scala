package beer.permutation.pet.metric

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec

class PETmaxArityScoreTest extends FlatSpec with ShouldMatchers{

  "for monotone score PET max arity score" should "return 1" in {
    val p = List(1, 2, 3, 4, 5)
    val score = PETmaxArityScore.evaluate(p)
    
    score should equal (1) 
  }

  "for inverted score PET max arity score" should "return 1" in {
    val p = List(6, 5, 4, 3, 2, 1)
    val score = PETmaxArityScore.evaluate(p)
    
    score should equal (1)
  }
  
  "for sort of half monotone PET max arity score" should "return 1" in {
    val p = List(1, 2, 3, 5, 4)
    val score = PETmaxArityScore.evaluate(p)
    
    score should equal (1)
  }

  "for Wu permutation PET max arity score" should "return 0" in {
    val p = List(2, 4, 1, 3)
    val score = PETmaxArityScore.evaluate(p)
    
    score should equal (0)
  }

}

