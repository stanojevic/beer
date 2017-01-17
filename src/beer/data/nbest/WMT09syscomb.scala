package beer.data.nbest

import java.io.File
import scala.io.Source

/**
 * @author milos
 */
object WMT09syscomb {

  val langs = List("en", "hu", "de", "es", "cz", "it", "fr")
  
  // I am not interested in language pairs
  
  type NBestHyps = List[String] // List(translations)
  type NBest = (String, NBestHyps) // (ref, List(translations))
  type System = List[NBest] // List(nbests for each sentence)
  type Language = Map[String, List[System]] // Map[langauge -> List(systems)]
  type LanguageMerged = Map[String, List[NBest]] // Map[langauge -> nbest..huuuge]
  
  def load(dir:String, topAndBottomK:Int) : LanguageMerged = {
    var everything:LanguageMerged = Map[String, List[(String, List[String])]]()
    
    for(lang <- langs){
      val nbestFns = getNbestFileNames(dir, lang)
      val refFn = getRefFileName(dir, lang)

      val refs:List[String] = loadRef(refFn)
      var nbests = Map[String, List[List[String]]]()

      for(nbestFn <- nbestFns){
        nbests += nbestFn -> loadNbest(nbestFn, topAndBottomK)
      }
      
      var languageNBestsMerged = List[(String, List[String])]()
      for(i <- 0 until refs.size){
        val ref = refs(i)
        val allTranslations:List[String] = nbests.values.map{_(i)}.flatten.toList.distinct
        languageNBestsMerged ::= (ref, allTranslations)
      }
      everything += lang -> languageNBestsMerged
    }
    everything
  }

  private def loadNbest(nbestSgmFn:String, topAndBottomK:Int) : List[NBestHyps] = {
    var nbests = List[List[String]]()
    var currNbest = List[String]()
    
    val segStartR = """^<seg id=.+?>$""".r
    val segEndR   = """^</seg>$""".r
    val wordsR    = """^<words> (.+) </words>$""".r
    Source.fromFile(nbestSgmFn).getLines().foreach{ line =>
      line match {
        case wordsR(sent) =>
          currNbest ::= sent
        case segEndR() =>
          if(topAndBottomK > 0){
            val bottomK = currNbest.take(topAndBottomK)
            val topK = currNbest.reverse.take(topAndBottomK)
            nbests ::= (topK ++ bottomK)
          }else{
            nbests ::= currNbest
          }
          currNbest = List[String]()
        case _ =>
      }
    }
    
    nbests.reverse
  }
  
  private def loadRef(refSgmFn:String) : List[String] = {
    var sents = List[String]()
    val r = """^<seg id=.+?> (.*) </seg>$""".r
    Source.fromFile(refSgmFn).getLines().foreach{ line =>
      line match {
        case r(sent) => sents ::= sent
        case _ => 1+1
      }
    }
    sents.reverse
  }
  
  private def getNbestFileNames(dir:String, lang:String) : List[String] = {
    // submissions-nbest/newssyscomb2009/*-de*nbest*sgm
    val allFiles = new File(s"$dir/submissions-nbest/newssyscomb2009/").listFiles().map{_.toString}
    allFiles.filter{_.matches(s".*-$lang.*nbest.*sgm")}.toList
  }

  private def getRefFileName(dir:String, lang:String) : String = {
    // /test-split/newssyscomb2009/newssyscomb2009-ref.de.sgm
    val allFiles = new File(s"$dir/test-split/newssyscomb2009/").listFiles().map{_.toString}
    allFiles.filter{_.matches(s".*newssyscomb2009-ref.$lang.sgm")}.head
  }
  
}
