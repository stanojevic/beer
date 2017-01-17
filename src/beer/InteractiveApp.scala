package beer

import scala.io.Source

/**
 * @author milos
 */
case class InteractiveConfig(
    model : String = "default",
    threads : Int = 1
    )

object InteractiveApp {
  
  def main(args:Array[String]) : Unit = {
    val parser = new scopt.OptionParser[InteractiveConfig]("BEER") {

      head("BEER", "2.0")

      opt[Int]('t',"threads") action { (x, c) =>
        c.copy(threads = x)
      } valueName ("Int")

      opt[String]("model") action { (x, c) =>
        c.copy(model = x)
      } valueName ("default|linear_self_train")

      help("help") text ("prints this usage text")

    }

    parser.parse(args, InteractiveConfig()) match {
      case Some(config) =>
        EvaluationApp.setParallelismGlobally(config.threads)
        val metric = Metric.loadModel(EvaluationApp.beerHome+"/models/"+config.model)
        doTheInteractiveWork(config, metric)
        
      case None         =>
        System.exit(-1)
    }
    
  }
  

  private def doTheInteractiveWork(config:InteractiveConfig, metric:Metric) : Unit = {
    
    for (ln <- Source.fromInputStream(System.in, "UTF-8").getLines){
      val fields = ln split """ \|\|\| """
      fields(0) match{

        case "EVAL BEST" =>
          val sys = fields(1)
          val refs = (2 until fields.size) map (fields(_))
          val scores = refs map { ref => metric.score(sys, ref)} toList

          println(scores.max)
          
        case "EVAL" =>
          val sys = fields(1)
          val refs = (2 until fields.size) map (fields(_))
          val scores = refs map { ref => metric.score(sys, ref)} toList

          println(scores mkString " ||| ")
          
        case "MARGIN" =>
          val sys = fields(1)
          val refs = (2 until fields.size) map (fields(_))
          val scores = refs map { ref => metric.rawScore(sys, ref)} toList

          println(scores mkString " ||| ")
          
        case "EXIT" =>
          System.err.println("Good bye")
          return
        
        case _ =>
          System.err.println("Non existing command")
      }
    }
  }
  
}
  