package beer.permutation.pet.metric

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class PEFscoreTest extends FlatSpec with ShouldMatchers{

  "for monotone score PEF score" should "return 1" in {
    val p = List(1, 2, 3, 4, 5)
    val beta = 0.6
    val score = new PEFscore(beta).evaluate(p)
    
    score should equal (1) 
  }

  "for inverted score PEF score" should "return 0" in {
    val p = List(6, 5, 4, 3, 2, 1)
    val beta = 0.6
    val score = new PEFscore(beta).evaluate(p)
    
    score should equal (0)
  }
  
  "for sort of half score PEF score" should "be 0.93" in {
    val p = List(1, 2, 3, 5, 4)
    val beta = 0.6
    val score = new PEFscore(beta).evaluate(p)
    
    score should be (0.93 plusOrMinus 0.01)
  }

}