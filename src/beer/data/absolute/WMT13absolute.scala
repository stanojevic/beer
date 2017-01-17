package beer.data.absolute

import java.io.File
import scala.io.Source
import scala.collection.mutable.{Map => MutableMap}

/**
 * @author milos
 */
class WMT13absolute (absoluteJudgmentsDir:String, dataDir:String) {

  val references_dir = dataDir+"/wmt13-baselines/wmt13-data/plain/references" // newstest2013-ref.$lang
  val system_dir = dataDir+"/wmt13-baselines/wmt13-data/plain/system-outputs/newstest2013" //  lang pairs dirs
  val langs = List("cs", "de", "es", "fr", "ru", "en")
  val lang_pairs = List("cs-en", "de-en", "es-en", "fr-en", "ru-en", "en-cs", "en-de", "en-es", "en-fr", "en-ru")
  
  type LangPair = String
  type Lang = String
  type System = String
  
  val system_sents = MutableMap[LangPair, MutableMap[System, Array[String]]]()
  val ref_sents = MutableMap[LangPair, Array[String]]()
  
  
  
  private def load_system_translations() : Unit = {
    for(lp <- lang_pairs){
      val system_sents = scala.collection.mutable.Map[String, Array[String]]()
      for(file <- WMT13absolute.getListOfFiles(system_dir+"/"+lp)){
        val system = file.getName
        system_sents(system) = WMT13absolute.loadContent(file)
      }
      this.system_sents(lp) = system_sents
    }
  }
  
  private def load_references():Unit={
    for(lang <- langs){
      val fn = references_dir+"/newstest2013-ref."+lang
      val content = WMT13absolute.loadContent(new File(fn))
      for(lp <- lang_pairs){
        if(lp matches s".*-$lang"){
          ref_sents(lp) = content
        }
      }
    }
  }
  
  private def getCSVs() : List[String] = {
    WMT13absolute.getListOfFiles(absoluteJudgmentsDir+"/raw-seg-scrs/").
          map{_.toString}.
          filter { _.matches(".*/seg.ad...-...csv$") }
  }
  
  private def load_csvs() : List[(String, String, Double)] = {
    var judgments = List[(String, String, Double)]()

    val csvs = getCSVs()
    
    for(csv <- csvs){
      Source.fromFile(csv).getLines.toList.tail.foreach { line => 
        val fields = line.split("\\s+")
        val lp = fields(0)
        val sysName = fields(1)
        val sentId = fields(2).toInt
        val score = fields(3).toDouble
        val fields2 = lp.split("-")
        val srcLang = fields2(0)
        val tgtLang = fields2(1)
        
        val refSent:String = ref_sents(lp)(sentId)
        val sysSent:String = system_sents(lp)(s"newstest2013.$lp.$sysName")(sentId)
        
        judgments ::= (sysSent, refSent, score)
      }
    }
    
    judgments
  }
  
  def load() : List[(String, String, Double)] = {
    load_system_translations();
    load_references()

    load_csvs()
  }
  
}

object WMT13absolute{

  def loadJudgments(absoluteJudgmentsDir:String, dataDir:String) : List[(String, String, Double)] = {
    new WMT13absolute(absoluteJudgmentsDir, dataDir).load()
  }
 
  private def loadContent(file:File) : Array[String] = {
    Source.fromFile(file, "UTF-8").getLines().toArray
  }
  
  private def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
  
}
