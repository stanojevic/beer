package beer.features

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
 * @author milos
 */
class LengthDisbalanceTest extends FlatSpec with ShouldMatchers {
  
  "disbalance" should "work" in {
    val exponential = false
    val f = new LengthDisbalance(exponential, List(1.0))
    val sys = "Hello I am".split(" ")
    val ref = "Hello I am Milos".split(" ")
    val res1 = f.featureValues(sys, ref)
    println(res1)
    val res2 = f.featureValues(ref, sys)
    println(res2)
  }
  
  "nbest" should "not crash" in {
    val exponential = false
    val f = new LengthDisbalance(exponential, List(1.0))
    val nbest = List(
        ("Hello I am".split(" "), 0.2 ),
        ("Hello I am Milos".split(" "), 0.2 ),
        ("Hello am".split(" "), 0.3 ))
    
    val res1 = f.expectedFeatureValues(nbest)
    println(res1)
  }
  
}