package beer.alignment

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import edu.cmu.meteor.scorer.MeteorConfiguration
import edu.cmu.meteor.scorer.MeteorScorer
import edu.cmu.meteor.aligner.Alignment
import edu.cmu.meteor.util.Constants
import java.io.File
import scala.collection.JavaConversions._
import beer.Configuration

class MeteorAligner (configuration:Configuration) extends Aligner {
  
  private val meteorConfig:MeteorConfiguration = createMeteorConfig()
  private val scorer = new MeteorScorer(meteorConfig)

  private def createMeteorConfig():MeteorConfiguration = {
    val prop = new java.util.Properties()
    
    val modulesConf = configuration.modelConfig("aligner").asInstanceOf[Map[String, Map[String, Object]]]("params")
    
    val shortLangName = configuration.arguments.lang
    val meteorSupportedLanguages = Set("en", "cz", "cs", "de", "es", "fr", "ar", "da", "fi", "hu", "it", "nl", "no", "pt", "ro", "ru", "se", "tr")
    val meteorLangName = if(meteorSupportedLanguages contains shortLangName)
        shortLangName
      else
        "other"
    prop.setProperty("language", meteorLangName)

  
    val useNorm = configuration.arguments.useNorm
    if(useNorm){
      if(configuration.resources.getOrElse("normalization", "false") != "true"){
        System.err.println("Normalization is not supported for "+configuration.arguments.lang)
        System.exit(-1)
      }
    }
    prop.setProperty("norm"  , useNorm.toString)

    val useLower = if(configuration.arguments.useLower) "true" else "false"
    prop.setProperty("lower" , useLower)

    val noPunct  = if(configuration.arguments.usePunct) "false" else "true"
    prop.setProperty("noPunct", noPunct)
      
    
    var modules = List[String]()

    val useExact = modulesConf.getOrElse("exact", false).asInstanceOf[Boolean]
    if(useExact){
      modules ::= "exact"
    }

    val useStem = modulesConf.getOrElse("stem", false).asInstanceOf[Boolean]
    if(useStem){
      if(configuration.resources.getOrElse("stemming", "false") != "true"){
        System.err.println("Stemming is not supported for "+configuration.arguments.lang)
        System.exit(-1)
      }

      modules ::= "stem"
    }

    val usePara = modulesConf.getOrElse("paraphrase", false).asInstanceOf[Boolean]
    if(usePara){
      val paraFile = configuration.resources("paraFile")
      prop.setProperty("paraFile", paraFile)
      modules ::= "paraphrase"
    }
    
    val useSyn = modulesConf.getOrElse("synonym", false).asInstanceOf[Boolean]
    if(useSyn){
      val synDir = configuration.resources("synDir")
      prop.setProperty("synDir", synDir)
      modules ::= "synonym"
    }

    val funcWordsFile = configuration.resources("funcWordsFile")
    if(funcWordsFile != ""){
      prop.setProperty("wordFile", funcWordsFile)
    }
    prop.setProperty("modules", modules.reverse.mkString(" "))
    
    val config = new MeteorConfiguration(prop)
    config
  }
  
  def align(sys:String, ref:String) : SentencePair = {
    val meteorAlignment = scorer.getMeteorStats(sys, ref).alignment
    val beerAlignment = convertToBeerAlignment(meteorAlignment)
    beerAlignment
  }
  
  private def convertToBeerAlignment(meteorAlignment: Alignment) : SentencePair = {
    val sysWords:List[String] = meteorAlignment.words1.toList
    val refWords:List[String] = meteorAlignment.words2.toList
    
    var a = List[PhrasePair]()
    
    val phrasePairs = meteorAlignment.matches.toList.flatMap{ case a_match =>
      if(a_match == null){
        List()
      }else{
        val wrongModelNumber = a_match.module
        val rightModuleNumber = meteorConfig.getModules().get(wrongModelNumber)
        List(
          PhrasePair(
              a_match.matchStart,
              a_match.matchStart + a_match.matchLength - 1,
              a_match.start,
              a_match.start + a_match.length - 1,
              rightModuleNumber,
              a_match.prob
          )
        )
      }
    }

    SentencePair(sysWords, refWords, phrasePairs)
  }

}
