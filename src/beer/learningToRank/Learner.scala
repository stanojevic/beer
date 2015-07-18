package beer.learningToRank

import scala.io.Source
import java.io.PrintWriter

trait Learner {

  val mappingFileName = "featureMapping"
  type Features = Map[Int, Double]
  type UnFilteredFeatures = Map[String, Double]
  
  var featureMapping = Map[String, Int]()


  protected def load(modelDir:String):Unit
  
  protected def classify(features:Map[Int, Double]) : Double

  final def trainModel(data:List[(Map[String, Double], Map[String, Double], Double)]) : Unit = {
    featureMapping = data.flatMap{ case (win, los, _) => List(win, los)}.flatMap{_.keys}.toSet.toList.sorted.zipWithIndex.toMap

    val changedData = data.map{ case (win, los, weight) =>
      val winMapped = win.map{case (k,v) => (featureMapping(k), v)}
      val losMapped = los.map{case (k,v) => (featureMapping(k), v)}
      (winMapped, losMapped, weight)
    }
    train(changedData)
  }
  
  protected def train(data:List[(Map[Int, Double], Map[Int, Double], Double)]) : Unit
  
  final def saveModel(modelDir:String) : Unit = {
    saveMapping(s"$modelDir/$mappingFileName", featureMapping)
    save(modelDir)
  }
  
  protected def save(modelDir:String) : Unit
  
  final def loadModel(modelDir:String) : Unit = {
    featureMapping = loadMapping(s"$modelDir/$mappingFileName")
    load(modelDir)
  }
  
  final def scoreInstance(features:UnFilteredFeatures) : Double = {
    val filteredFeatures = filterNonExistingFeatures(featureMapping, features)
    val convertedFeatures = convertFeatures(featureMapping, filteredFeatures)

    val score = classify(convertedFeatures)
    if(score.isInfinite() || score.isNaN()){
      0.0
    }else{
      score
    }
  }
  
  private final def convertFeatures(featureMapping:Map[String,Int], features:UnFilteredFeatures) : Features = {
    features.map{case (k:String, v:Double) => (featureMapping(k), v)}
  }
  
  private def filterNonExistingFeatures(mapping:Map[String, Int], features:UnFilteredFeatures) : UnFilteredFeatures = {
    features.filter{mapping contains _._1}
  }
      
  private def loadMapping(fn:String) : Map[String, Int] = 
    Source.fromFile(fn, "UTF-8").getLines().toList.map{ line =>
      val fields = line split " "
      (fields(0), fields(1).toInt)
    }.toMap

  final private def saveMapping(destination:String, featureMapping:Map[String, Int]) : Unit = {
    val pw = new PrintWriter(destination)
    featureMapping.keys.toList.sorted.foreach{ featureName =>
      val mapping = featureMapping(featureName)
      pw.println(s"$featureName $mapping")
    }
    pw.close()
  }

}