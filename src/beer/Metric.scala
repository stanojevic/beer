package beer

import beer.learning.FeatureScaling
import beer.learning.Classifier
import beer.learning.FeatureNameMapping
import beer.features.FeatureExtractor
import beer.learning.PlattScaling
import beer.learning.OutputScaler
import java.io.File
import beer.learning.LinearRegressionScaling

class Metric(
    val featureExtractor:FeatureExtractor,
    val nameMapper:FeatureNameMapping,
    val inputScaling:FeatureScaling,
    val classifier:Classifier,
    val outputScaling:OutputScaler){
  
  def score(sent:String, refs:List[String]) : Double = {
    refs.map{score(sent, _)}.max
  }
  
  def score(sent:String, ref:String) : Double = {
    score(sent.split(" +"), ref.split(" +"))
  }
  
  def rawScore(sent:String, ref:String) : Double = {
    rawScore(sent.split(" +"), ref.split(" +"))
  }
  
  def rawScore(sent:Array[String], ref:Array[String]) : Double = {
      val feat1 = featureExtractor.extractFeatures(sent, ref)
    
      val feat2 = nameMapper.mapNames(feat1)
    
      val feat3 = inputScaling.scale(feat2)
      
      val rawScore = classifier.rawScore(feat3)
      
      rawScore
  }

  
  def score(sent:Array[String], ref:Array[String]) : Double = {
      val feat1 = featureExtractor.extractFeatures(sent, ref)
    
      val feat2 = nameMapper.mapNames(feat1)
    
      val feat3 = inputScaling.scale(feat2)
      
      val rawScore = classifier.rawScore(feat3)
      
      val score = outputScaling.scale(rawScore)
      
      score
  }
  
}

object Metric {
  
  def loadModel(modelDir:String) : Metric = {
    val classifier = Classifier.loadOptimizedModel(modelDir)
    val featureExtractor = FeatureExtractor.constructFromFile(modelDir+"/feature_template.yaml")
    val nameMapper = FeatureNameMapping.loadModel(modelDir+"/feature_names_mapping")
    val inputScaling = FeatureScaling.loadModel(modelDir+"/feature_values_scaling")
    
    val outputScaling = if(new File(s"$modelDir/platt_scaling.properties").exists){
      PlattScaling.loadModel(modelDir)
    }else{
      LinearRegressionScaling.loadModelOptimized(modelDir)
    }
    
    new Metric(featureExtractor, nameMapper, inputScaling, classifier, outputScaling)
  }

}
