package beer.learning

import java.util.concurrent.ConcurrentHashMap
import scala.io.Source
import beer.data.judgments.NonMappedFeaturesPair
import java.io.PrintWriter
import java.util.TreeSet
import scala.collection.JavaConversions._
import beer.data.judgments.FeaturesPair

/**
 * @author milos
 */
class FeatureNameMapping (mapping : ConcurrentHashMap[String, Int]) {
  
  type RawFeatureMap = List[(String, Double)]
  type FeatureMap = Array[Double]
  // if #features is large this should be replaced by sparse vector
  
  def mapNames(x:RawFeatureMap) : FeatureMap = {
    val y = new Array[Double](mapping.size)
    
    for((name, value) <- x){
      y(mapping.get(name)) = value
    }
    
    y
  }
  
  def saveModel(filename:String) : Unit = {
    val featureNames = new Array[String](mapping.size)
    for((name, index) <- mapping){
      featureNames(index)=name
    }
    val pw = new PrintWriter(filename)
    for(index <- 0 until featureNames.size){
      pw.println((index)+" "+featureNames(index))
    }
    pw.close()
  }
  
  def mapWMTpair(pair : NonMappedFeaturesPair) : FeaturesPair = {
    val mWinner = this.mapNames(pair.winner)
    val mLoser  = this.mapNames(pair.loser)
    new FeaturesPair(mWinner, mLoser)
  }
  
}

object FeatureNameMapping{
  
  def loadModel(filename:String) : FeatureNameMapping = {
    var params = new ConcurrentHashMap[String, Int]()
    
    for(line <- Source.fromFile(filename).getLines){
      val fields = line.split(" ")
      assert(fields.size == 2)
      params.put(fields(1), fields(0).toInt)
    }
    
    new FeatureNameMapping(params)
  }
  
  def trainModelFromWMTpairs(pairs : List[NonMappedFeaturesPair]) : FeatureNameMapping = {
    val allFeatureNames = scala.collection.mutable.Set[String]()
    for(pair:NonMappedFeaturesPair <- pairs){
      pair.winner.map{_._1}.foreach{allFeatureNames.add}
      pair.loser.map{_._1}.foreach{allFeatureNames.add}
    }

    val mapping = new ConcurrentHashMap[String, Int]()
    for((name, index) <- allFeatureNames.toList.sorted.zipWithIndex){
      mapping.put(name, index)
    }
    new FeatureNameMapping(mapping)
  }
  
}
