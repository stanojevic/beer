package beer
import scala.io.Source
import java.io.File
import beer.io.FactoredScores.EvalRecord
import beer.io.FactoredScores
import java.nio.charset.StandardCharsets
import java.nio.charset.CodingErrorAction
import java.io.InputStreamReader
import beer.io.Log

object Evaluation {
  

  def main(args:Array[String]) : Unit = {
    
    val beerHome = getBeerHome()
    val configurationFile = s"$beerHome/configuration.yaml"
    
    val beer = new BEER(args.mkString(" "), beerHome, configurationFile)
    beer.conf.arguments.workingMode match {
      case "interactive" => interactive(beer)
      case "factors" => evaluate(beer, false)
      case "evaluate" | "evaluation" => evaluate(beer, true)
      case "evaluateReordering" | "evaluationReordering" => evaluateReordering(beer)
      case "train" => train(beer)
      case x =>
        System.err.println(s"Value $x is not supported as workingMode parameter")
        System.exit(-1)
    }

  }
  
  private def evaluateReordering(beer:BEER) : Unit = {
    val refs = beer.conf.arguments.referenceFiles
    val sys  = beer.conf.arguments.systemFile
    
    var allSentScores = List[Double]()
    var allAvgRefLens = List[Double]()
    
    val metricName = beer.conf.arguments.reorderingMetric 
    Log.println(beer.conf, s"evaluating with $metricName")
    
    var numOfSents = -1
    (sys::refs).map{ case file =>
      val listOfSents = Source.fromFile(file, "UTF-8").getLines().toList
      val n = listOfSents.size
      if(numOfSents < 0){
        numOfSents = n
      }else if(numOfSents != n){
        System.err.println("ERROR: Number of system and reference sentences is not the same")
        System.exit(-1)
      }
      listOfSents
    }.transpose.foreach{ case lines =>
      val sys = lines.head
      val refs = lines.tail
      var max = Double.MinValue
      var scoresAllRefs = List[Double]()

      allAvgRefLens ::= refs.map{_.split(" +").size}.sum/refs.size // with words
      // allAvgRefLens ::= refs.map{_.length}.sum/refs.size // with characters

      var sysToken = ""
      var refsToken = List[String]()
      for(ref <- refs){
        val score = beer.reorderingScore(sys, ref, metricName)
        scoresAllRefs ::= score
      }
      val maxScore = scoresAllRefs.max
      allSentScores ::= maxScore

      if(beer.conf.arguments.printSentScores){
        println(s"best $metricName     : "+maxScore)
      }
    }
    val n = allSentScores.size
    val corpusScore = (allAvgRefLens zip allSentScores).map{case (len, score) => len*score}.sum/allAvgRefLens.sum
    println(s"corpus $metricName        : $corpusScore")
  }
  
  private def readParallelSentencesFromManyFiles(files:List[String]) : List[List[String]] = {
    files.map{ file =>
      Source.fromFile(file, "UTF-8").getLines().toList
    }.transpose
  }
  
  private def evaluate(beer:BEER, withScoring:Boolean) : Unit = {
    val refs = beer.conf.arguments.referenceFiles
    val sys  = beer.conf.arguments.systemFile
    
    var allSentScores = List[Double]()
    var allSentFeatures = List[Map[String, Double]]()
    var allAvgRefLens = List[Double]()
    
    var numOfSents = -1
    (sys::refs).map{ case file =>
      val listOfSents = Source.fromFile(file, "UTF-8").getLines().toList
      val n = listOfSents.size
      if(numOfSents < 0){
        numOfSents = n
      }else if(numOfSents != n){
        System.err.println("ERROR: Number of system and reference sentences is not the same")
        System.exit(-1)
      }
      listOfSents
    }.transpose.foreach{ case lines =>
      val sys = lines.head
      val refs = lines.tail
      var max = Double.MinValue
      var featuresAllRefs = List[Map[String, Double]]()
      var scoresAllRefs = List[Option[Double]]()

      allAvgRefLens ::= refs.map{_.split(" +").size}.sum/refs.size // with words
      // allAvgRefLens ::= refs.map{_.length}.sum/refs.size // with characters

      var sysToken = ""
      var refsToken = List[String]()
      for(ref <- refs){
        val (factors, sysTokenized, refTokenized) = beer.factorsAndTokens(sys, ref)
        sysToken = sysTokenized.mkString(" ")
        refsToken ::= refTokenized.mkString(" ")
        featuresAllRefs ::= factors
        if(withScoring){
          val score = beer.evaluate(factors)
          scoresAllRefs ::= Some(score)
        }else{
          scoresAllRefs ::= None
        }
      }
      val record = EvalRecord(sysToken, refsToken.reverse, featuresAllRefs.reverse, scoresAllRefs.reverse)
      if(withScoring){
        val (maxFeatures:Map[String, Double], maxScore:Double) = (featuresAllRefs zip scoresAllRefs.map{_.get}).maxBy(_._2)
        //val score = FactoredScores.getBestFinalScore(record.scoresAllRefs).getOrElse(0.0)
        allSentScores ::= maxScore
        allSentFeatures ::= maxFeatures
      }

      if(beer.conf.arguments.printFeatureValues || !withScoring){ // ether workingMode is factors OR user requests features
        println(FactoredScores.recordToString(record))
        println()
      }else if(beer.conf.arguments.printSentScores){
        println(s"best beer     : "+FactoredScores.getBestFinalScore(record.scoresAllRefs).get)
      }
    }
    if(withScoring){
      val n = allSentScores.size
      
      val corpusScore = beer.evaluateCorpus(allAvgRefLens, allSentFeatures)

      println(s"corpus beer        : $corpusScore")
    }

  }
  
  private def train(beer:BEER) : Unit = {
    val winnerFeaturesFile = beer.conf.arguments.winnerFeaturesFile
    val winnerFeatures = FactoredScores.loadFactoredScores(winnerFeaturesFile).map{_.featuresAllRefs(0)}
    val loserFeaturesFile = beer.conf.arguments.loserFeaturesFile 
    val loserFeatures  = FactoredScores.loadFactoredScores(loserFeaturesFile).map{_.featuresAllRefs(0)}
    val labeledData = (winnerFeatures zip loserFeatures)

    val unlabeledFeaturesFile1 = beer.conf.arguments.unlabeledFeatureFile1
    val unlabeledFeatures1 = if(unlabeledFeaturesFile1 != "") FactoredScores.loadFactoredScores(unlabeledFeaturesFile1).map{_.featuresAllRefs(0)} else List()
    val unlabeledFeaturesFile2 = beer.conf.arguments.unlabeledFeatureFile2
    val unlabeledFeatures2 = if(unlabeledFeaturesFile2 != "") FactoredScores.loadFactoredScores(unlabeledFeaturesFile2).map{_.featuresAllRefs(0)} else List()
    val unlabeledData = (unlabeledFeatures1 zip unlabeledFeatures2)

    val unsupervisedIterations = 0

    beer.train(labeledData, unlabeledData, unsupervisedIterations)
  }
  
  private def interactive(beer:BEER) : Unit = {
    System.err.println("Ready for interacitve mode")
    System.err.println("Shoot!")
    for (ln <- Source.fromInputStream(System.in, "UTF-8").getLines){
      val fields = ln split """ \|\|\| """
      fields(0) match{
        case "EVAL BEST" =>
          val sys = fields(1)
          val refs = (2 until fields.size) map (fields(_))
          val scores = refs map { ref => beer.evaluate(sys, ref)} toList

          println(scores.max)
          
        case "EVAL" =>
          val sys = fields(1)
          val refs = (2 until fields.size) map (fields(_))
          val scores = refs map { ref => beer.evaluate(sys, ref)} toList

          println(scores mkString " ||| ")

        case "FACTORS" =>
          val sys = fields(1)
          val refs:List[String] = (2 until fields.size).toList map (fields(_))
          val outputString = refs.map{ ref:String =>
            val factors = beer.factors(sys, ref)
            factors.map{ case (k:String, v:Double) => s"$k=$v" } mkString " "
          }.mkString(" ||| ")
          println(outputString)
          
        case "SCORE FACTORS" =>
          val factorStrings = (1 until fields.size).toList map (fields(_))
          val outputString = factorStrings.map{ factorsString:String =>
            val factors:Map[String, Double] = factorsString.split(" ").toList.map{
              x => val kvArray = x.split("=")
              (kvArray(0), kvArray(1).toDouble)
              }.toMap
            val score = beer.evaluate(factors)
            score
          }.mkString(" ||| ")
          println(outputString)
          
        case "EXIT" =>
          System.err.println("Good bye")
          return
        
        case _ =>
          System.err.println("Non existing command")
      }
    }
  }
  
  private def getBeerHome() : String = {
    var applicationDir = getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
    applicationDir = new File(applicationDir).getParent()
    if( ! new File(s"$applicationDir/lib").exists()){
      applicationDir = new File(applicationDir).getParent()
    }
    applicationDir
  }
  
}
