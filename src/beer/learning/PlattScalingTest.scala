package beer.learning

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
 * @author milos
 */
class PlattScalingTest extends FlatSpec with ShouldMatchers {
  
  "scaling with raw numbers" should "work" in {
    val pairs:List[(Double, Int)] = List( (1.2321, 1), (3.112314, 1), (13.1231, -1), (-213.1, -1))

    val platt = PlattScaling.trainFromRawScores(pairs.map{_._1}, 2)
    
    println("1 "+platt.scale(-53.2131))
    println("2 "+platt.scale(-3.2131))
    println("3 "+platt.scale(3.2131))
    println("4 "+platt.scale(53.2131))
    
    platt.saveModel(".")
    
    val platt2 = PlattScaling.loadModel(".")
    println("1r "+platt2.scale(-53.2131))
    println("2r "+platt2.scale(-3.2131))
    println("3r "+platt2.scale(3.2131))
    println("4r "+platt2.scale(53.2131))

  }
  
}