package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.permutation.finders.HandleUnaligned
import beer.Configuration

class PhrasePairMatch(configuration:Configuration, paramsArg:Map[String, Object]) extends SparseIngredient{
  
  val featureName = "PP"
    
  def eval(sp:SentencePair):Map[String, Double] = {
    val refNulls = HandleUnaligned.findRefNulls(sp)
    val sysNulls = HandleUnaligned.findSysNulls(sp)
    
    var tuples = List[(String, Double)]()

    for(refNull <- refNulls)
      for(refI <- refNull.refStart to refNull.refEnd)
        tuples ::= (s"$featureName-NULL-TO-"+sp.ref(refI), 1.0)

    for(sysNull <- sysNulls)
      for(sysI <- sysNull.sysStart to sysNull.sysEnd)
        tuples ::= (s"$featureName-"+sp.sys(sysI)+"-TO-NULL", 1.0)

    for(phrase <- sp.a){
      val sysPhrase = extractPhrase(sp.sys, phrase.sysStart, phrase.sysEnd)
      val refPhrase = extractPhrase(sp.ref, phrase.refStart, phrase.refEnd)
      tuples ::= (s"$featureName-$sysPhrase-$refPhrase", 1.0)
    }

    tuples.toMap
  }
  
  private def extractPhrase(sent:List[String], start:Int, end:Int) : String = {
    sent.drop(start).take(end-start+1).mkString(" ")
  }

}