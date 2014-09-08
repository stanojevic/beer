package beer.permutation.metric.lexical

import org.scalatest.{FlatSpec, ShouldMatchers}

class FscoreTest extends FlatSpec with ShouldMatchers {

  "for equal sys and ref F score" should "return 1" in {
    val sys = List("how", "are", "you")
    val ref = List("how", "are", "you")
    val scorer = new Fscore
    val score = scorer.score(ref, sys)

    score should equal (1) 
  }

}
