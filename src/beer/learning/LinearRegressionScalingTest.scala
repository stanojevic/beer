package beer.learning

import org.scalatest.{FlatSpec, ShouldMatchers}

/**
 * @author milos
 */
class LinearRegressionScalingTest extends FlatSpec with ShouldMatchers {
  
  "scaling with raw numbers" should "work" in {
    val data = List(
        (50.2, 51.2),
        (45.1, 46.15),
        (47.1, 48.15),
        (45.3, 46.55)
    )
    val model = LinearRegressionScaling.trainFromRawScores(data)
    
    println("0 " + model.scale(50.2))
    println("0 " + model.scale(45.1))
    println("0 " + model.scale(47.1))
    println("0 " + model.scale(45.3))
    
    
    model.saveModel(".")

    println("1 "+model.scale(-53.2131))
    println("2 "+model.scale(-3.2131))
    println("3 "+model.scale(3.2131))
    println("4 "+model.scale(53.2131))
    
    
    val model2 = LinearRegressionScaling.loadModel(".")
    
    println("1 "+model2.scale(-53.2131))
    println("2 "+model2.scale(-3.2131))
    println("3 "+model2.scale(3.2131))
    println("4 "+model2.scale(53.2131))
    
    val model3 = LinearRegressionScaling.loadModelOptimized(".")
    println("1r "+model3.scale(-53.2131))
    println("2r "+model3.scale(-3.2131))
    println("3r "+model3.scale(3.2131))
    println("4r "+model3.scale(53.2131))

  }
  
}
  