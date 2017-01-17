package beer.learning

import de.bwaldvogel.liblinear.Model
import java.io.File
import de.bwaldvogel.liblinear.Problem
import de.bwaldvogel.liblinear.Feature
import de.bwaldvogel.liblinear.FeatureNode
import de.bwaldvogel.liblinear.SolverType
import de.bwaldvogel.liblinear.Parameter
import de.bwaldvogel.liblinear.Linear
import scala.io.Source

/**
 * @author milos
 */
class LinearRegressionScaling(rawModel:Model) extends OutputScaler{

  private var weights:Array[Double] = null
  
  def scale(rawScore:Double) : Double = {
    val f = LinearRegressionScaling.transformation(rawScore)
    val scaled = if(weights == null){
      scaleWithLibLinear(f)
    }else{
      scaleWithWeights(f)
    }
    
    if(scaled < 0.0){
      0.0
    }else if(scaled/100.0 > 1.0){
      1.0
    }else{
      scaled/100.0
    }
  }

  private def scaleWithWeights(f:Array[Double]) : Double = {
    if(f.size != weights.size){
      System.err.println("#features "+f.size+" and #weights "+weights.size+" don't match")
      System.exit(-1)
    }
    var product = 0.0
    var i = 0
    while(i < f.size){
      product += f(i) * weights(i)
      i+=1
    }
    
    product
  }
  
  private def scaleWithLibLinear(f:Array[Double]) : Double = {
    val instance = LinearRegressionScaling.convertToInstance(f)
    val predictions = new Array[Double](2)
    Linear.predictValues(rawModel, instance, predictions)
    
    predictions(0)
  }
  
  def saveModel(modelDir:String) : Unit = {
    rawModel.save(new File(s"$modelDir/output_scaling"))
  }
  
}

object LinearRegressionScaling {

  private def transformation(rawScore:Double) : Array[Double] = {
    Array(
        1.0,
        rawScore// ,
        // math.signum(rawScore)*math.pow(rawScore, 2),
        // math.pow(rawScore, 3)
        )
  }

  private def convertToInstance(x:Array[Double]) : Array[Feature] = {
    val instance = new Array[Feature](x.size)
    for(i <- 0 until x.size){
      instance(i) = new FeatureNode(i+1, x(i))
    }
    instance
  }
  
  def trainFromRawScores(data:List[(Double, Double)]) : LinearRegressionScaling = {
    // first one is rawScore and second human score
    val problem = new Problem()
    val l = data.size
    problem.bias = 0
    problem.l = l
    problem.n = transformation(0.0).size
    problem.y = new Array[Double](l)
    problem.x = new Array[Array[Feature]](l)
    var instanceId = 0
    for(instance <- data){
      val (rawScore, humanScore) = instance
      problem.y(instanceId) = humanScore
      problem.x(instanceId) = convertToInstance(transformation(rawScore))
      
      instanceId += 1
    }
    
    val C = 1.0
    val epsilon = 0.01
    
    val parameter = new Parameter(SolverType.L2R_L2LOSS_SVR, C, epsilon)
    
    val rawModel = Linear.train(problem, parameter)

    new LinearRegressionScaling(rawModel)
  }

  def loadModel(modelDir:String) : LinearRegressionScaling = {
    val rawModel = Model.load(new File(s"$modelDir/output_scaling"))
    new LinearRegressionScaling(rawModel)
  }
  
  def loadModelOptimized(modelDir:String) : LinearRegressionScaling = {
    val lines = Source.fromFile(modelDir+"/output_scaling").getLines().toList
    val weights = lines.dropWhile {_ != "w"}.tail.map{_.toDouble}.toArray
    
    val scaler = new LinearRegressionScaling(null)
    scaler.weights = weights
    
    scaler
  }

}
