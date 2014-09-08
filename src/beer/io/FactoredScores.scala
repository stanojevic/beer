package beer.io

import java.io.PrintWriter
import scala.io.Source

object FactoredScores {
  
  private type Features = Map[String, Double]
  
  case class EvalRecord(
      sys:String,
      refs:List[String],
      featuresAllRefs:List[Features],
      scoresAllRefs:List[Option[Double]])
      
  def getBestFinalScore(scores:List[Option[Double]]) : Option[Double] = {
    val nonNones = scores.filter{
      case Some(_) => true
      case None    => false
    }.map{
      case Some(x) => x
      case None    => System.err.println("ERROR!!!");System.exit(-1); 0
    }
    if(nonNones.isEmpty){
      None
    }else{
      Some(nonNones.max)
    }
  }
  
  def recordToString(record:EvalRecord) : String = record match {
    case EvalRecord(sys, refs, featuresAllRefs, scoresAllRefs) =>

      val sysString = s"sys           : $sys"
      
      val allCombinations = List((1 to refs.size).toList, refs, featuresAllRefs, scoresAllRefs)
      
      var allReferencesLines = List[String]()
      
      for(List(index:Int, ref:String, features:Features, score:Any) <- allCombinations.transpose){
        val refString = s"ref         $index : $ref"
        val featuresString = s"ingredients $index : " + features.toList.sortBy(_._1).map{case (f, s) => s"$f=$s"}.mkString(" ")
        val scoreString = score match {
          case Some(existingScore:Double) => s"beer        $index : $existingScore"
          case None => s"beer        $index : $noScore"
        }
        allReferencesLines ::= List(refString,featuresString, scoreString).mkString("\n")
      }
      val refsString = allReferencesLines.reverse.mkString("\n")

      val finalScoreString = getBestFinalScore(scoresAllRefs) match {
        case Some(score) => s"best beer     : $score"
        case None        => s"best beer     : $noScore"
      }

      List(sysString, refsString, finalScoreString).mkString("\n")
  }
  
  /////////////////////////// reading scores ///////////////////////
  
  private val noScore = "NoBeer"
  
  private val evaluationRx   = """^evaluation (\d+)""".r
  private val sysRx          = """^sys           : (.+)$""".r
  private val sysRxEmpty     = """^sys           : $""".r
  private val refRx          = """^ref         (\d+) : (.+)$""".r
  private val refRxEmpty     = """^ref         (\d+) : $""".r
  private val featuresRx     = """^ingredients (\d+) : (.+)$""".r
  private val featuresRxEmpty= """^ingredients (\d+) : $""".r
  private val scoreRx        = """^beer        (\d+) : ([0-9.]+)""".r
  private val noScoreRx      = """^beer        (\d+) : NoBeer""".r
  private val finalScoreRx   = """^best beer     : ([0-9.]+)""".r
  private val noFinalScoreRx = """^best beer     : NoBeer""".r
  private val emptyRx        = """^\s*$""".r
  private val commentRx      = """^#""".r
  private val corpusBeerRx   = """^corpus beer   : ([0-9.]+)""".r
  
  def loadFactoredScores(fn:String) : List[EvalRecord] = {
    var records = List[EvalRecord]()

    var currEvalId = 0
    var currSys = ""
    var currRefs = List[String]()
    var currFeaturesAllRefs = List[Map[String, Double]]()
    var currScoresAllRefs = List[Option[Double]]()
    var currFinalScore:Option[Double] = None
    
    var recordsReadIn = 0
    
    for(line <- Source.fromFile(fn, "UTF-8").getLines()){
      line match {
        case evaluationRx(index) =>
          currEvalId = index.toInt
        case sysRx(sys) =>
          currSys = sys
        case sysRxEmpty() =>
          currSys = ""
        case refRx(index, ref) =>
          currRefs ::= ref
        case refRxEmpty(index) =>
          currRefs ::= ""
        case featuresRx(index, features) =>
          currFeaturesAllRefs ::= features.split(" ").toList.map{pair =>
            val List(f, s) = pair.split("=").toList
            (f,s.toDouble)
          }.toMap
        case featuresRxEmpty(index) =>
          currFeaturesAllRefs ::= Map()
        case noScoreRx(index) =>
          currScoresAllRefs ::= None
        case scoreRx(index, score) =>
          currScoresAllRefs ::= Some(score.toDouble)
        case noFinalScoreRx() =>
          currFinalScore = None
          val currRecord = EvalRecord(currSys, currRefs.reverse, currFeaturesAllRefs.reverse, currScoresAllRefs.reverse)
          currRefs = List()
          currFeaturesAllRefs = List()
          currScoresAllRefs = List()
          records ::= currRecord
          recordsReadIn += 1
          if(recordsReadIn % 100000 == 0)
            System.err.println(s"read $recordsReadIn from $fn")
        case finalScoreRx(finalScore) =>
          currFinalScore = Some(finalScore.toDouble)
          val currRecord = EvalRecord(currSys, currRefs.reverse, currFeaturesAllRefs.reverse, currScoresAllRefs.reverse)
          currRefs = List()
          currFeaturesAllRefs = List()
          currScoresAllRefs = List()
          records ::= currRecord
          recordsReadIn += 1
          if(recordsReadIn % 100000 == 0)
            System.err.println(s"read $recordsReadIn from $fn")
        case emptyRx() =>
        case commentRx() =>
        case corpusBeerRx(avgBeer) =>
      }
    }
    
    records.reverse
  }

}
