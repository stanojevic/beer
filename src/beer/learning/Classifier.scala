package beer.learning

import beer.data.judgments.FeaturesPair
import de.bwaldvogel.liblinear.Model
import de.bwaldvogel.liblinear.Linear
import de.bwaldvogel.liblinear.Feature
import de.bwaldvogel.liblinear.FeatureNode
import java.io.File
import de.bwaldvogel.liblinear.Parameter
import de.bwaldvogel.liblinear.SolverType
import de.bwaldvogel.liblinear.Problem
import java.util.Properties
import java.io.FileOutputStream
import java.io.FileInputStream
import scala.io.Source



/**
 * @author milos
 */
class Classifier (rawModel:Model, val gamma:Double, val C:Double, val d:Int) {
  
  type Features = Array[Double]
  
  private var weights:Array[Double] = null
  
  def featureCount() : Int = {
    if(weights == null){
      rawModel.getNrFeature()
    }else{
      weights.size
    }
  }
  
  def rawScore(featuresRaw:Features) : Double = {
    val features = Kernel.kernelize(featuresRaw, gamma, d)
    
    if(weights == null){
      rawScoreWithLibLinear(features)
    }else{
      rawScoreWithWeights(features)
    }
  }
  
  private def rawScoreWithWeights(features:Features) : Double = {
    val n = featureCount()
    if(features.size != n){
      System.err.println("#features "+n+" and #weights "+features.size+" don't match")
      System.exit(-1)
    }

    var product = 0.0
    var i = 0
    while(i < n){
      product += features(i) * weights(i)
      i+=1
    }
    
    product
  }
  
  private def rawScoreWithLibLinear(features:Features) : Double = {
    val instance = Classifier.convertToInstance(features)    
    val predictions = new Array[Double](2)
    Linear.predictValues(rawModel, instance, predictions)
    
    predictions(0)
  }
  
  def saveModel(modelDir:String) : Unit = {
    val file = new File(modelDir)
    if(!file.exists()){
      file.mkdirs()
    }
    rawModel.save(new File(modelDir+"/core_model"))
    val props = new Properties()
    props.setProperty("polynomialDegree", d.toString())
    props.setProperty("gamma", gamma.toString())
    props.setProperty("C", C.toString())
    props.store(new FileOutputStream(s"$modelDir/kernel.properties"), "kernel properties")
  }
  
}

object Classifier {

  type Features = Array[Double]
  
  private def convertToInstance(x:Array[Double]) : Array[Feature] = {
    val instance = new Array[Feature](x.size)
    for(i <- 0 until x.size){
      instance(i) = new FeatureNode(i+1, x(i))
    }
    instance
  }
  
  def train(gamma:Double, C:Double, d:Int, wmtData:List[FeaturesPair]) : Classifier = {
    
    System.err.println("kernelization started")
    val trainData = wmtData.map{ pair =>
      val winK = Kernel.kernelize(pair.winner, gamma, d)
      val losK = Kernel.kernelize(pair.loser , gamma, d)
      val (posExample, negExample) = PRO(winK, losK)
      (convertToInstance(posExample), convertToInstance(negExample))
    }
    
    System.err.println("kernelization done")

    val problem = new Problem()
    val l = trainData.size*2
    problem.bias = 0
    problem.l = l
    problem.n = trainData.head._1.size
    problem.y = new Array[Double](l)
    problem.x = new Array[Array[Feature]](l)
    var instanceId = 0
    for((posInstance, negInstance) <- trainData){
      problem.y(instanceId) = 1
      problem.x(instanceId) = posInstance
      instanceId += 1
      problem.y(instanceId) = -1
      problem.x(instanceId) = negInstance
      instanceId += 1
    }

    val parameter = new Parameter(SolverType.L2R_L2LOSS_SVC, C, 0.01) // check if epsilon is relevant
    System.err.println("actual training started")
    val rawModel = Linear.train(problem, parameter)
    System.err.println("actual training done")
    new Classifier(rawModel, gamma, C, d)
  }
  
  private def PRO(winFeatures:Features, losFeatures:Features) : (Features, Features) = {
    val posExample = new Array[Double](winFeatures.size)
    val negExample = new Array[Double](winFeatures.size)
    
    for(i <- 0 until winFeatures.size){
      posExample(i) = winFeatures(i) - losFeatures(i)
      negExample(i) = -posExample(i)
    }
    
    (posExample, negExample)
  }
  
  def loadOptimizedModel(modelDir:String) : Classifier = {
    val props = new Properties()
    props.load(new FileInputStream(s"$modelDir/kernel.properties"))
    val d = props.getProperty("polynomialDegree").toInt
    val gamma = props.getProperty("gamma").toDouble
    val C = props.getProperty("C").toDouble
    
    val classifier = new Classifier(null, gamma, C, d)
    
    val lines = Source.fromFile(modelDir+"/core_model").getLines().toList
    val weights = lines.dropWhile {_ != "w"}.tail.map{_.toDouble}.toArray

    classifier.weights = weights
    
    classifier
  }

  def loadModel(modelDir:String) : Classifier = {
    val props = new Properties()
    props.load(new FileInputStream(s"$modelDir/kernel.properties"))
    val d = props.getProperty("polynomialDegree").toInt
    val gamma = props.getProperty("gamma").toDouble
    val C = props.getProperty("C").toDouble

    val rawModel = Model.load(new File(modelDir+"/core_model"))
    new Classifier(rawModel, gamma, C, d)
  }
  
}
