package beer.features

import org.scalatest.{FlatSpec, ShouldMatchers}


/**
 * @author milos
 */
class NGramTest extends FlatSpec with ShouldMatchers {
  
  "feature" should "work" in {
    val order = 3
    val symmetry = true
    val fScores = List()
    val gScores = List()
    val f = new NGram(order, symmetry, fScores, gScores)
    val sys = "Hello I am".split(" ")
    val ref = "Hello I am Milos".split(" ")
    val res1 = f.featureValues(sys, ref)
    println(res1)
    val res2 = f.featureValues(ref, sys)
    println(res2)
  }
  
  "nbest" should "not crash" in {
    val order = 3
    val symmetry = true
    val fScores = List()
    val gScores = List()
    val f = new NGram(order, symmetry, fScores, gScores)
    val nbest = List(
        ("Hello I am ba bam bab am Hello".split(" "), 0.5 ),
        //("Hello I am Milos".split(" "), 0.5 ),
        ("Hello am tra lal al alla".split(" "), 0.5 ))
    
    val res1 = f.expectedFeatureValues(nbest)
    println(res1)
  }
  
  "function" should "symmetric" in {
    val order = 3
    val symmetry = true
    val fScores = List()
    val gScores = List()
    val f = new NGram(order, symmetry, fScores, gScores)
    val sys = "Hello I am ba bam bab am Hello".split(" ")
    val ref = "Hello am tra lal al alla".split(" ")
    val res1 = f.featureValues(sys, ref)
    println(s"symmetry=$symmetry $res1")
    val res2 = f.featureValues(ref, sys)
    println(s"symmetry=$symmetry $res2")
  }
  
  "function" should "anti-symmetric" in {
    val order = 2
    val symmetry = false
    val fScores = List()
    val gScores = List()
    val f = new NGram(order, symmetry, fScores, gScores)
    val sys = "Hello am ba bam bab am Hello".split(" ")
    val ref = "Hello am tra lal al alla".split(" ")
    val res1 = f.featureValues(sys, ref)
    println(s"symmetry=$symmetry $res1")
    val res2 = f.featureValues(ref, sys)
    println(s"symmetry=$symmetry $res2")
  }
  
}