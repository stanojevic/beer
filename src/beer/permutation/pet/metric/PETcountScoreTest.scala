package beer.permutation.pet.metric
import org.scalatest.{FlatSpec, ShouldMatchers}

class PETcountScoreTest extends FlatSpec with ShouldMatchers{

  "for monotone score PET count score" should "return 1" in {
    val p = List(1, 2, 3, 4, 5)
    val score = PETcountScore.evaluate(p)
    
    score should equal (1) 
  }

  "for inverted score PET count score" should "return 1" in {
    val p = List(6, 5, 4, 3, 2, 1)
    val score = PETcountScore.evaluate(p)
    
    score should equal (1)
  }
  
  "for sort of half score PET count score" should "be 0.30" in {
    val p = List(1, 2, 3, 5, 4)
    val evaluator = PETcountScore
    val score = evaluator.evaluate(p)
    
    score should be (0.30 plusOrMinus 0.01)
  }


}