package beer.ingredients

import scala.io.Source
import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.Configuration

class FunctionAndContentWordsCoverage(config:Configuration, params:Map[String, Object]) extends SparseIngredient {

  val featureName = "FunctionAndContentWordsCoverage"
    
  val funcWordsFile = config.resources("funcWordsFile")

  var funcWords:Set[String] = loadFromFile(funcWordsFile)
  
  private def loadFromFile(fn:String) : Set[String] = {
    Source.fromFile(fn)(scala.io.Codec("UTF-8")).getLines().toSet.map { word: String => word.toLowerCase }
  }

  val scorersOfFuncWords :List[String] = params.get("functionWords").asInstanceOf[Option[List[String]]] match {
    case Some(functions:List[String]) => functions
    case None => List()
  }
  val scorersOfContWords :List[String] = params.get("contentWords").asInstanceOf[Option[List[String]]] match {
    case Some(functions:List[String]) => functions
    case None => List()
  }
  
  def eval(sp:SentencePair):Map[String, Double] = {
    val functionWordsScores = scorersOfFuncWords map {
      case "precision" => 
        val score = precision(functionWords = true, sp)
        val name  = s"$featureName:precision:funcWords"
        (name, score)
      case "recall" =>
        val score = recall(functionWords = true, sp)
        val name  = s"$featureName:recall:funcWords"
        (name, score)
      case "fScore" =>
        val score = fScore(functionWords = true, sp)
        val name  = s"$featureName:fScore:funcWords"
        (name, score)
    }

    val contentWordsScores = scorersOfFuncWords map {
      case "precision" => 
        val score = precision(functionWords = false, sp)
        val name  = s"$featureName:precision:contWords"
        (name, score)
      case "recall" =>
        val score = recall(functionWords = false, sp)
        val name  = s"$featureName:recall:contWords"
        (name, score)
      case "fScore" =>
        val score = fScore(functionWords = false, sp)
        val name  = s"$featureName:fScore:contWords"
        (name, score)
    }
    
    (functionWordsScores ++ contentWordsScores).toMap
  }
  
  private def getAlignedIndices(sp:SentencePair, sysSide:Boolean) : List[Int] =
    sp.a.flatMap{ phrasePair =>
      if(sysSide){
        Stream.from(phrasePair.sysStart).takeWhile(_<=phrasePair.sysEnd)
      }else{
        Stream.from(phrasePair.refStart).takeWhile(_<=phrasePair.refEnd)
      }
    }
  
  private def fScore(functionWords:Boolean, sp:SentencePair) : Double = {
    val p = precision(functionWords, sp)
    val r = recall(functionWords, sp)
    
    val f1 = 2*p*r/(p+r)
    
    f1
  }
  
  private def recall(functionWords:Boolean, sp:SentencePair) : Double = {
    val alignedIndices = getAlignedIndices(sp, false).sorted
    val desiredIndices = if(functionWords){
      alignedIndices.filter(i => funcWords.contains(sp.ref(i)))
    }else{
      alignedIndices.filter(i => ! funcWords.contains(sp.ref(i)))
    }
    var allIndices = Stream.from(0).take(sp.ref.size).toList
    
    desiredIndices.size.toDouble / allIndices.size
  }

  private def precision(functionWords:Boolean, sp:SentencePair) : Double = {
    val alignedIndices = getAlignedIndices(sp, true).sorted
    val desiredIndices = if(functionWords){
      alignedIndices.filter(i => funcWords.contains(sp.sys(i)))
    }else{
      alignedIndices.filter(i => ! funcWords.contains(sp.sys(i)))
    }
    var allIndices = Stream.from(0).take(sp.sys.size).toList
    
    desiredIndices.size.toDouble / allIndices.size
  }

}