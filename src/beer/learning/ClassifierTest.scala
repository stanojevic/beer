package beer.learning

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.features.FeatureExtractor

/**
 * @author milos
 */
class ClassifierTest extends FlatSpec with ShouldMatchers {
  
  "optimized model" should "be the same as non optimized" in {
    val nonOptimizedClassifier = Classifier.loadModel("models/default")
    val optimizedClassifier = Classifier.loadOptimizedModel("models/default")
    
    val featureExtractor = FeatureExtractor.constructFromFile("models/default/feature_template.yaml")
    val nameMapper = FeatureNameMapping.loadModel("models/default/feature_names_mapping")
    val inputScaling = FeatureScaling.loadModel("models/default/feature_values_scaling")
    
    val sentA = "Hello my name is Milos"
    val ref = "I am Milos and I'm greeting you"
    
    def pipeline(e:FeatureExtractor, m:FeatureNameMapping, s:FeatureScaling, c:Classifier)(sys:String, ref:String) : Double = {
      val feat1 = featureExtractor.extractFeatures(sentA, ref)
    
      val feat2 = nameMapper.mapNames(feat1)
    
      val feat3 = inputScaling.scale(feat2)
      
      val score = c.rawScore(feat3)
      
      score
    }
    
    val optimizedPipeline = pipeline(featureExtractor, nameMapper, inputScaling, optimizedClassifier)(_, _)
    val nonOptimizedPipeline = pipeline(featureExtractor, nameMapper, inputScaling, nonOptimizedClassifier)(_, _)
    
    val scoreNonOptimized = nonOptimizedPipeline(sentA, ref)
    val scoreOptimized = optimizedPipeline(sentA, ref)
    
    System.err.println(s"optimized\t$scoreOptimized")
    System.err.println(s"non optimized\t$scoreNonOptimized")
  }
  
}