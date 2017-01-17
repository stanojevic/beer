package beer

import java.io.File
import scala.io.Source

/**
 * @author milos
 */
case class EvalConfig(
    printSentScores : Boolean = false,
    model : String = "default",
    threads : Int = 1,
    sysFile : String = "",
    refFiles : List[String] = List()
    )

object EvaluationApp {
  
  val beerHome = EvaluationApp.getBeerHome()
  
  def main(args:Array[String]) : Unit = {
    val parser = new scopt.OptionParser[EvalConfig]("BEER") {

      head("BEER", "2.0")

      opt[String]('s',"sysFile") action { (x, c) =>
        c.copy(sysFile = x)
      } valueName ("file") required()

      opt[String]('r',"refFiles") action { (x, c) =>
        c.copy(refFiles = x.split(":").toList)
      } valueName ("file1:file2:file3...") required()

      opt[Unit]("printSentScores") action { (_, c) =>
        c.copy(printSentScores = true)
      }

      opt[Int]('t',"threads") action { (x, c) =>
        c.copy(threads = x)
      } valueName ("Int")

      opt[String]("model") action { (x, c) =>
        c.copy(model = x)
      } valueName ("default|linear_self_train")

      help("help") text ("prints this usage text")

    }

    parser.parse(args, EvalConfig()) match {
      case Some(config) =>
        setParallelismGlobally(config.threads)
        val metric = Metric.loadModel(EvaluationApp.beerHome+"/models/"+config.model)
        doTheEvaluationWork(config, metric)
        
      case None         =>
        System.exit(-1)
    }
    
  }
  
  private def doTheEvaluationWork(config:EvalConfig, metric:Metric) : Unit = {
    val sysSource = toSource(config.sysFile).getLines()
    val refSources = config.refFiles.map{toSource(_).getLines()}
    
    var totalScore = 0.0
    var sentCount = 1
    
    while( ! sysSource.isEmpty){
      val sys = sysSource.next()
      val refs = refSources.map{_.next()}
      val sentScore = metric.score(sys, refs)
      if(config.printSentScores){
        println(s"sent $sentCount score is $sentScore")
      }
      totalScore += sentScore
      sentCount += 1
    }
    
    val finalScore = totalScore/sentCount
    
    println(s"total BEER $finalScore")
  }
  
  private def getBeerHome() : String = {
    var applicationDir = getClass().getProtectionDomain().getCodeSource().getLocation().getPath()
    applicationDir = new File(applicationDir).getParent()
    if( ! new File(s"$applicationDir/lib").exists()){
      applicationDir = new File(applicationDir).getParent()
    }
    applicationDir
  }
  
  def setParallelismGlobally(numThreads: Int): Unit = {
    val parPkgObj = scala.collection.parallel.`package`
    val defaultTaskSupportField = parPkgObj.getClass.getDeclaredFields.find {
      _.getName == "defaultTaskSupport"
    }.get

    defaultTaskSupportField.setAccessible(true)
    defaultTaskSupportField.set(
      parPkgObj,
      new scala.collection.parallel.ForkJoinTaskSupport(
        new scala.concurrent.forkjoin.ForkJoinPool(numThreads)))
  }

  private def toSource(file: String): scala.io.BufferedSource = {
    import java.nio.charset.Charset
    import java.nio.charset.CodingErrorAction
    val decoder = Charset.forName("UTF-8").newDecoder()
    decoder.onMalformedInput(CodingErrorAction.IGNORE)
    scala.io.Source.fromFile(file)(decoder)
  }

}
