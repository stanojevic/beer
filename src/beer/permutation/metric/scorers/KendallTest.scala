package beer.permutation.metric.scorers

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.permutation.metric.scorers.Kendall.evaluate

class KendallTest extends FlatSpec with ShouldMatchers {
  "for monotone score" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val score = evaluate(p)

    score should equal (1) 
  }

  "for inverted score" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val score = evaluate(p)

    score should equal (0)
  }

  "for sort of half score" should "be around 0.9" in {
    val p = List(0, 1, 2, 4, 3)
    val score = evaluate(p)

    score should be (0.9 plusOrMinus 0.00001)
  }

}
