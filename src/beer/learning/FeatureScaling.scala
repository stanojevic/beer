package beer.learning

import scala.io.Source
import beer.data.judgments.FeaturesPair
import java.io.PrintWriter

/**
 * @author milos
 */
class FeatureScaling (mins:Array[Double], maxs:Array[Double]) {
  
  type FeatureMap = Array[Double]
  // if #features is large this should be replaced by sparse vector
  
  def scale(x:FeatureMap) : FeatureMap = {
    val y = new Array[Double](x.size)

    for(i <- 0 until x.size){
      val min = mins(i)
      val max = maxs(i)
      y(i) = (x(i) - min)/(max - min)
    }
    
    y
  }
  
  def scaleWMTpair(pair:FeaturesPair) : FeaturesPair = {
    val scaledWinner = scale(pair.winner)
    val scaledLoser = scale(pair.loser)
    new FeaturesPair(scaledWinner, scaledLoser)
  }
  
  def saveModel(filename:String) : Unit = {
    val pw = new PrintWriter(filename)
    
    for(i <- 0 until mins.size){
      pw.println(mins(i)+" "+maxs(i))
    }
    
    pw.close()
  }
  
}

object FeatureScaling{
  
  def loadModel(filename:String) : FeatureScaling = {
    var mins = List[Double]()
    var maxs = List[Double]()
    
    for(line <- Source.fromFile(filename).getLines){
      val fields = line.split(" ")
      assert(fields.size == 2)
      mins ::= fields(0).toDouble
      maxs ::= fields(1).toDouble
    }
    
    new FeatureScaling(mins.reverse.toArray, maxs.reverse.toArray)
  }

  def trainModelFromWMTpairs(pairs : List[FeaturesPair]) : FeatureScaling = {
    val featureCount = pairs.head.winner.size
    val mins = new Array[Double](featureCount)
    val maxs = new Array[Double](featureCount)
    
    for(pair <- pairs){
      for(i <- 0 until featureCount){
        val value = pair.winner(i)
        if(mins(i) > value){
          mins(i) = value
        }
        if(maxs(i) < value){
          maxs(i) = value
        }
      }
      for(i <- 0 until featureCount){
        val value = pair.loser(i)
        if(mins(i) > value){
          mins(i) = value
        }
        if(maxs(i) < value){
          maxs(i) = value
        }
      }
    }
    new FeatureScaling(mins, maxs)
  }
  
}
