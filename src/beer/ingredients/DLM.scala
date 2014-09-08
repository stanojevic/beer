package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.Configuration

class DLM (configuration:Configuration, paramsArg:Map[String, Object]) extends SparseIngredient {
  
  val featureName = "DLM"
    
  def eval(sp:SentencePair) : Map[String, Double] = {
    val features = sp.sys.sliding(2).
      map{_ mkString "-"}.
      toList.
      groupBy(identity).
      map{ case (ngramType, ngramTokens) => 
        (s"$featureName-$ngramType", ngramTokens.size.toDouble)
      }
    features
  }

}
