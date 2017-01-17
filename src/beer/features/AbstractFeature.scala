package beer.features

import java.util.concurrent.ConcurrentHashMap

trait AbstractFeature {
  
  type RawFeatureMap = List[(String, Double)]
  type Sentence = Array[String]
  type Probability = Double
  type NBest = List[(Sentence, Probability)]

  val name : String
  
  def expectedFeatureValues(nbest:NBest) : List[RawFeatureMap]
  
  def featureValues(sys_words:Sentence, ref_words:Sentence) : RawFeatureMap
  
}