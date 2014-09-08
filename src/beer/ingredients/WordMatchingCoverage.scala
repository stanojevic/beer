package beer.ingredients

import scala.collection.immutable.Map
import beer.alignment.Aligner.{PhrasePair=>PP, SentencePair}
import beer.permutation.finders.HandleUnaligned
import beer.Configuration

class WordMatchingCoverage(configuration:Configuration, paramsArg:List[Map[String, Any]]) extends SparseIngredient {
  
  val featureName = "WordMatching"
    
  private val moduleNameToNum = Map(
      "exact"      -> 0,
      "stem"       -> 1,
      "synonym"    -> 2,
      "paraphrase" -> 3,
      "nonMatched" -> -1
  )
  
  private val configurations : List[(String, Set[Int], Set[String])] = paramsArg.toList map { desc:Map[String, Any] =>
    val moduleNames = desc("modules").asInstanceOf[List[String]]
    val configDesc = moduleNames.mkString(":")
    val moduleNums = moduleNames.map{moduleNameToNum(_)}.toSet
    val functions = desc("scores").asInstanceOf[List[String]].toSet
    (configDesc, moduleNums, functions)
  }
  
  def eval(sp:SentencePair) : Map[String, Double] = {
    val nullUpdatedSP = HandleUnaligned.updateSentencePairWithNulls(sp)
    var scores = List[(String, Double)]()
    for((configDesc:String, moduleNums:Set[Int], functions:Set[String]) <- configurations){
      for(function <- functions){
        function match {
          case "precision" =>
            val score = precision(moduleNums, nullUpdatedSP)
            val name  = s"$featureName:$configDesc:precision"
            scores ::= (name, score)
          case "recall" =>
            val score = recall(moduleNums, nullUpdatedSP)
            val name  = s"$featureName:$configDesc:recall"
            scores ::= (name, score)
          case "fScore" =>
            val score = fScore(moduleNums, nullUpdatedSP)
            val name  = s"$featureName:$configDesc:fScore"
            scores ::= (name, score)
        }
      }
    }
    scores.toMap
  }

  private def fScore(modules:Set[Int], sp:SentencePair) : Double = {
    val p = precision(modules, sp)
    val r = recall(modules, sp)
    
    val f1 = 2*p*r/(p+r)
    f1
  }
  
  private def recall(modules:Set[Int], sp:SentencePair) : Double = {
    var counter = 0
    sp.a.foreach{ pp:PP =>
      if(modules contains pp.module){
        counter += pp.refEnd - pp.refStart +1
      }
    }
    
    counter.toDouble / sp.ref.size
  }

  private def precision(modules:Set[Int], sp:SentencePair) : Double = {
    var counter = 0
    sp.a.foreach{ pp:PP =>
      if(modules contains pp.module){
        counter += pp.sysEnd - pp.sysStart +1
      }
    }
    
    counter.toDouble / sp.sys.size
  }

}
