package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.Configuration

class CharNGrams (configuration:Configuration, params:Map[String, Any]) extends SparseIngredient {
  val precision = params("precision").asInstanceOf[Boolean]
  val recall = params("recall").asInstanceOf[Boolean]
  val fScore = params("fScore").asInstanceOf[Boolean]
  val orders = params("orders").asInstanceOf[List[Integer]]
  
  val featureName = "CharNGrams"
  
  def eval(sp:SentencePair):Map[String, Double]={
    val precisionFeatures:Map[String, Double] = precision match{
      case true => 
        orders map { order => 
          (s"precision$featureName$order", precision(order, sp)) 
        } toMap
      case false=> Map[String, Double]()
    }
    
    val recallFeatures:Map[String, Double] = recall match{
      case true => 
        orders map { order =>
          (s"recall$featureName$order", recall(order, sp))
        } toMap
      case false=> Map[String, Double]()
    }
    
    val fScoreFeatures:Map[String, Double] = fScore match{
      case true => 
        orders map { order =>
          (s"fScore$featureName$order", fScore(order, sp))
        } toMap
      case false=> Map()
    }
    
    precisionFeatures++recallFeatures++fScoreFeatures
  }

  private def fScore(order:Int, sp: SentencePair): Double = {
    val p = precision(order, sp)
    val r = recall(order, sp)
    val f1 = 2 * p * r / (p + r)
    f1
  }
  
  private def precision(order:Int, sp : SentencePair): Double = {
    val sys = sp.sys.mkString(" ")
    val ref = sp.ref.mkString(" ")

    val sysSubStrs = (0 to sys.length-order) map {pos => sys.substring(pos, pos+order)}
    val refSubStrs = (0 to ref.length-order) map {pos => ref.substring(pos, pos+order)}
    
    val sysMultiSet = sysSubStrs.groupBy(identity).mapValues(_.size).withDefaultValue(0)
    val refMultiSet = refSubStrs.groupBy(identity).mapValues(_.size).withDefaultValue(0)
    
    var count = 0.0
    for(nGram <- sysMultiSet.keys){
      count += Math.min(sysMultiSet(nGram), refMultiSet(nGram))
    }
    val n = sys.size-order+1
    if(n==0 || n<count)
      return 0
    else
      count/n
  }

  private def recall(order:Int, sp : SentencePair): Double = {
    val sys = sp.sys.mkString(" ")
    val ref = sp.ref.mkString(" ")

    val sysSubStrs = (0 to sys.length-order) map {pos => sys.substring(pos, pos+order)}
    val refSubStrs = (0 to ref.length-order) map {pos => ref.substring(pos, pos+order)}
    
    val sysMultiSet = sysSubStrs.groupBy(identity).mapValues(_.size).withDefaultValue(0)
    val refMultiSet = refSubStrs.groupBy(identity).mapValues(_.size).withDefaultValue(0)
    
    var count = 0.0
    for(nGram <- refMultiSet.keys){
      count += Math.min(sysMultiSet(nGram), refMultiSet(nGram))
    }
    val n = ref.size-order+1
    if(n==0 || n<count)
      return 0
    else
      count/n
  }
  
}
