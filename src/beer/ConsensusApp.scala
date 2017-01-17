package beer

/**
 * @author milos
 */
case class ConsensusConfig(
    modelDir : String = "$BEERHOME/models/default".replace("$BEERHOME", EvaluationApp.beerHome),
    threads : Int = 1,
    alpha : Double = 1.0,
    nbestFile : String = "",
    nbestFormat : String = "moses"
    )

object ConsensusApp {
  
  def main(args:Array[String]) : Unit = {
    val parser = new scopt.OptionParser[ConsensusConfig]("BEER") {

      head("BEER", "2.0")

      opt[String]("nbestFile") action { (x, c) =>
        c.copy(nbestFile = x)
      } valueName ("file") required()

      opt[String]("nbestFormat") action { (x, c) =>
        c.copy(nbestFormat = x)
      } valueName ("moses")

      opt[Double]("alpha") action { (x, c) =>
        c.copy(alpha = x)
      } valueName ("Double") required()

      opt[Int]('t',"threads") action { (x, c) =>
        c.copy(threads = x)
      } valueName ("Int")

      opt[String]("modelDir") action { (x, c) =>
        c.copy(modelDir = x.replace("$BEERHOME", EvaluationApp.beerHome))
      } valueName ("$BEERHOME/models/default")

      help("help") text ("prints this usage text")

    }

    parser.parse(args, ConsensusConfig()) match {
      case Some(config) =>
        EvaluationApp.setParallelismGlobally(config.threads)
        val metric = Metric.loadModel(config.modelDir)
        doTheConsensusWork(config, metric)
        
      case None         =>
        System.exit(-1)
    }
    
  }
  
    
  private def doTheConsensusWork(config:ConsensusConfig, metric:Metric) : Unit = {
    //TODO
  }
  
}