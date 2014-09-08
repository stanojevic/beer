package beer.alignment

trait Aligner {

  def align(sys:String, ref:String) : Aligner.SentencePair

}

object Aligner {
  
  case class PhrasePair(sysStart:Int, sysEnd:Int, refStart:Int, refEnd:Int, module:Int, score:Double)
  case class SentencePair(sys:List[String], ref:List[String], a:List[PhrasePair])
  
}
