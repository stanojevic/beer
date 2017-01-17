package beer.features

import org.yaml.snakeyaml.Yaml
import java.io.{File, FileInputStream}
import java.util.{List => JavaList, Map => JavaMap }
import scala.collection.JavaConversions._
import beer.data.judgments.NonMappedFeaturesPair
import beer.data.judgments.PairwiseJudgment
import beer.data.judgments.NonMappedFeaturesPair

/**
 * @author milos
 */
class FeatureExtractor(featureFunctions:List[AbstractFeature]) {

  type RawFeatureMap = List[(String, Double)]
  type Sentence = Array[String]
  type Probability = Double
  type NBest = List[(Sentence, Probability)]
  
  def expectedFeatureValues(nbest:NBest): List[RawFeatureMap] = {
    featureFunctions.map{_.expectedFeatureValues(nbest)}.transpose.map{_.flatten}
  }

  def extractFeatures(sys:String, ref:String): RawFeatureMap = {
    extractFeatures(sys.split(" "), ref.split(" "))
  }
  
  def extractFeatures(sys_words:Sentence, ref_words:Sentence): RawFeatureMap = {
    featureFunctions.flatMap{_.featureValues(sys_words, ref_words)}
  }
  
  def extractFeatures_for_WMT_pair(j:PairwiseJudgment): NonMappedFeaturesPair = {
    val winnerFeatures = this.extractFeatures(j.winnerWords, j.refWords)
    val loserFeatures = this.extractFeatures(j.loserWords, j.refWords)
    new NonMappedFeaturesPair(winnerFeatures, loserFeatures)
  }

}
  
object FeatureExtractor {
  
  def constructFromFile(filename:String) : FeatureExtractor = {
    val config = parseConfFile(filename)

    var features = List[AbstractFeature]()

    val featureDescs = config("features").asInstanceOf[List[Map[String, Object]]]
    for(featureDesc <- featureDescs){
      val featureName = featureDesc("name").asInstanceOf[String]
      val feature:AbstractFeature = featureName match {
        case "ngram"             => NGram.constructFromConfObject(featureDesc)
        case "skip-bigram"       => SkipBigram.constructFromConfObject(featureDesc)
        case "char-ngram"        => CharNGram.constructFromConfObject(featureDesc)
        case "length-disbalance" => LengthDisbalance.constructFromConfObject(featureDesc)
      }
      features ::= feature
    }

    new FeatureExtractor(features)
  }
  

  private def parseConfFile(confFile:String) : Map[String, Object] = {
    val yaml = new Yaml()
    val stream = new FileInputStream(confFile)
    val configInJava = yaml.load(stream)
    deepScalatize(configInJava).asInstanceOf[Map[String, Object]]
  }
  
  private def deepScalatize(x:Any) : Any = {
    x match {
      case aList : JavaList[_] => aList.map{deepScalatize(_)}.toList
      case aMap  : JavaMap[_, _] => aMap.mapValues{deepScalatize(_)}.toMap
      case _ => x
    }
  }
}
