package beer.io

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.alignment.Aligner.{PhrasePair, SentencePair}
import FactoredScores.EvalRecord
import java.io.PrintWriter

class FactoredScoresTest extends FlatSpec with ShouldMatchers {
  
  "save and then load" should "keep things the same" in {
    val features1 = Map("precision"->1.0, "recall"->0.2)
    val features2 = Map("precision"->0.33, "recall"->0.6)
    val score1 = Some(0.3)
    val score2 = None
    val finalScore1 = score1
    val finalScore2 = score2

    val sys  = "This is system translation."
    val ref1 = "This is ref no 1"
    val ref2 = "This is ref no 2"

    val record1 = EvalRecord(sys, List(ref1, ref2), List(features1, features2), List(score1, score2))
    val record2 = EvalRecord(sys, List(ref2, ref1), List(features2, features1), List(score2, score1))
    
    val recordsToSave = List(record1, record2)
    
    val tmpFile = "just_testing_output_format.beer"
    var evaluationId = 1
    val pw = new PrintWriter(tmpFile)
    for(record <- recordsToSave){
      pw.println(s"evaluation $evaluationId")
      evaluationId+=1
      val recordString = FactoredScores.recordToString(record)
      pw.println(recordString)
      pw.println()
    }
    pw.close()
    val loadedRecords = FactoredScores.loadFactoredScores(tmpFile)
    recordsToSave should equal(loadedRecords)
  }

}
