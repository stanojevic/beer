package beer

import java.io.{File, FileInputStream}
import java.util.{List => JavaList, Map => JavaMap }
import scala.collection.JavaConversions._
import scala.sys.process._
import org.yaml.snakeyaml.Yaml

class Configuration (args:Array[String], beerHome:String, configurationFile:String){

  val arguments:Config = parse(args)
  val globalConfig:Map[String, Object] = parseConfFile(configurationFile)
  val resources:Map[String, String] = resourcesConfiguration(globalConfig)
  val modelConfig:Map[String, Object] = parseConfFile( if(arguments.modelDescFile == Config().modelDescFile)
                                                          resources("exactModelDir")+"/description.yaml"
                                                       else
                                                          arguments.modelDescFile  )
  
  ////////////////////////////////////////////////////////////////////////////////////////////
  
  private def resourcesConfiguration(config:Map[String, Object]):Map[String, String] = {
    
    val meteorHome = arguments.meteorHome
    val lang = arguments.lang

    val resourcesConfig = config("resources").asInstanceOf[Map[String, Object]].get(lang).asInstanceOf[Option[Map[String, Object]]]
    
    var resources = Map[String, String]()
    
    resourcesConfig match {
      case Some(x) =>
        val exactModelDir:String = x("models").
                                     asInstanceOf[Map[String, String]](arguments.modelType).
                                     replaceAll("\\$METEOR", meteorHome).
                                     replaceAll("\\$BEER", beerHome)

        resources += "exactModelDir" -> exactModelDir

      case _ =>
        val modelType = arguments.modelType
        System.err.println(s"The language $lang or the model $modelType that you are requesting is not present in the configuration.yaml") //"other"
        System.exit(-1)
    }

    resourcesConfig match {
      case Some(params:Map[String, Object]) =>
        if(params.keys contains "paraphrase"){
          val paraFile = params("paraphrase").asInstanceOf[String].replaceAll("\\$METEOR", meteorHome).replaceAll("\\$BEER", beerHome)
          resources += "paraFile"->paraFile
        }
        if(params.keys contains "synonym"){
          val synDir = params("synonym").asInstanceOf[String].replaceAll("\\$METEOR", meteorHome).replaceAll("\\$BEER", beerHome)
          resources += "synDir"->synDir
        }
        if(params.keys contains "stem"){
          val stem = params("stem").asInstanceOf[Boolean]
          resources += "stemming"->stem.toString
        }
        if(params.keys contains "normalization"){
          val normalization = params("normalization").asInstanceOf[Boolean]
          resources += "normalization"->normalization.toString
        }

        val funcWordsFile = params("funcWords").asInstanceOf[String].
                              replaceAll("\\$METEOR", meteorHome).replaceAll("\\$BEER", beerHome)
        resources += "funcWordsFile" -> funcWordsFile
      case None =>
        // theoretically you should never enter this place
        resources += "funcWordsFile" -> s"$meteorHome/resources/function/other.words"
    }
    
    resources
  }
  
  private def parse(args:Array[String]) : this.Config = {

    val parser = createArgumentParser()
    
    var configHolder:Config = null
    parser.parse(args, this.Config()) map { config =>

      configHolder = config
      
      configHolder = configHolder.copy(beerHome = this.beerHome)

//      // find tmpDir
//      if(configHolder.tmpDir == Config().tmpDir){
//        configHolder = configHolder.copy(tmpDir = configHolder.beerHome+"/tmp")
//        val tmpDir = new File(configHolder.tmpDir)
//        if( ! tmpDir.exists()){
//          tmpDir.mkdir()
//          tmpDir.deleteOnExit()
//        }
//      }

      // find meteorHome
      if(configHolder.meteorHome == Config().meteorHome){
        val potentialFiles = new File(configHolder.beerHome+"/lib").list()
        if(potentialFiles == null){
          System.err.println("Can't access to the lib directory where METEOR is supposed to be located")
          System.err.println("I thought it was "+configHolder.beerHome+"/lib")
          System.err.println("or "+configHolder.beerHome+"/..")
          System.exit(-1)
        }else{
          var meteorDirs = potentialFiles.filter(_ startsWith "meteor").map{"./lib/"+_}
          if(meteorDirs.size == 0){
            // try again; maybe you are used trough multeval
            val potentialFiles = new File(configHolder.beerHome).getParentFile().list()
            meteorDirs = potentialFiles.filter(_ startsWith "meteor").map{"../"+_}
          }
            
          if(meteorDirs.size == 0){
            System.err.println("Something is wrong, I can't find METEOR in the lib directory which contains files:")
            potentialFiles.foreach{ file =>
              System.err.println(file)
            }
            System.exit(-1)
          }
          configHolder = configHolder.copy(meteorHome = configHolder.beerHome+"/"+meteorDirs.head)
        }
      }
      
    } getOrElse {
      System.exit(-1)
    }

    configHolder
  }

  private def parseConfFile(confFile:String) : Map[String, Object] = {
    val yaml = new Yaml()
    val stream = new FileInputStream(confFile)
    val configInJava = yaml.load(stream)
    deepScalatize(configInJava).asInstanceOf[Map[String, Object]]
  }
  
  private def deepScalatize(x:Any) : Any = {
    x match {
      case aList:JavaList[Any] => aList.map{deepScalatize(_)}.toList
      case aMap:JavaMap[String, Any] => aMap.mapValues{deepScalatize(_)}.toMap
      case _ => x
    }
  }
  
  ////////////////////////////////////////////////////////////////////////////////////////////

  case class Config(
      workingMode   : String = "evaluation",
      systemFile : String = "",
      referenceFiles : List[String] = List(),
      debugging : Boolean = false,
      winnerFeaturesFile : String = "",
      modelDescFile : String = "",
      loserFeaturesFile : String = "",
      unlabeledFeatureFile1 : String = "",
      unlabeledFeatureFile2 : String = "",
      modelType : String = "evaluation",
      lang: String = "other",
      meteorHome: String = "",
      beerHome: String = "",
      //tmpDir: String = "",
      useLower: Boolean = true,
      useNorm: Boolean = false,
      usePunct: Boolean = true,
      verbose: Boolean = false,
      printSentScores : Boolean = false,
      printFeatureValues : Boolean = false
  )

  ////////////////////////////////////////////////////////////////////////////////////////////

  private def createArgumentParser() = new scopt.OptionParser[Config]("BEER") {

    head("BEER", beer.Constants.VERSION.toString)
    
    help("help") text ("prints this usage text")

    version("version") text ("prints the current version")

    opt[Unit]("verbose") action { (_,c) =>
      c.copy(verbose = true)
    } text ("prints to standard error messages potentially useful for debugging beer")
    
    opt[Unit]("printSentScores") action { (_,c) =>
      c.copy(printSentScores = true)
    } text ("stops printing of sentence scores (together with feature values)")
    
    opt[Unit]("printFeatureValues") action { (_,c) =>
      c.copy(printFeatureValues = true)
    } text ("stops printing of feature values")
    
    opt[String]("workingMode") valueName ("mode") action { (x,c) =>
      c.copy(workingMode = x)
    } text ("what to do : evaluation (default), factors, interactive, train, permutationScoring")
    
    opt[String]("meteorHome") valueName ("/meteor/home") action { (x,c) =>
      c.copy(meteorHome = x)
    } text ("sets the location  of meteor")
    
    opt[Unit]("norm") action { (_,c) =>
      c.copy(useNorm = true)
    } text ("don't use normalization of input")
    
    opt[Unit]("noLower") action { (_, c) =>
      c.copy(useLower = false, useNorm = false)
    } text ("don't lowercase the input")
    
    opt[Unit]("noPunct") action { (_,c) =>
      c.copy(usePunct = false)
    } text ("don't use punctuation in evaluation")
    
    opt[String]("trainingUnlabeledFeaturesFile1") valueName ("file") action { (x,c) =>
      c.copy(unlabeledFeatureFile1 = x)
    } text ("first file with unlabeled data used for training the metric")
    
    opt[String]("trainingUnlabeledFeaturesFile2") valueName ("file") action { (x,c) =>
      c.copy(unlabeledFeatureFile2 = x)
    } text ("second file with unlabeled data used for training the metric")
    
    opt[String]("trainingWinnerFeaturesFile") valueName ("file") action { (x,c) =>
      c.copy(winnerFeaturesFile = x)
    } text ("file containing winner features")
    
    opt[String]("trainingLoserFeaturesFile") valueName ("file") action { (x,c) =>
      c.copy(loserFeaturesFile = x)
    } text ("file containing loser sentences")
    
    opt[String]("modelType") valueName ("modelType") action { (x,c) =>
      c.copy(modelType = x)
    } text ("Type of the model to use: evaluation (default) | tuning")
    
    opt[String]("modelDescFile") valueName ("/model/desc.yaml") action { (x,c) =>
      c.copy(modelDescFile = x)
    } text ("description file if not using pretrained model (use this option only for tuning the metric)")
    
    opt[String]('s', "system") valueName ("systemTranslation") action { (x,c) =>
      c.copy(systemFile = x)
    } text("system translation file")
    
    opt[String]('r', "references") valueName ("reference0:reference1:...") action { (x,c) =>
      c.copy(referenceFiles = x.split(":").toList)
    } text("reference translation files separated by :")
    
    opt[String]('l', "lang") valueName ("lang") action { (x,c) =>
      c.copy(lang = x)
    } text ("target language (en, fr, cs, de, es, ru, da, fi, hu, it, nl, no, pt, ro, se, tr, other)") required
    
  }
  
}
