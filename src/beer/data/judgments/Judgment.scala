package beer.data.judgments

class Judgment (
  val dataset:String,
  val src_lang_short:String,
  val tgt_lang_short:String,
  val sentId:Int,
  val sys_names:List[String],
  val rankings:List[Int],
  val sents:List[String],
  val ref:String
){
  def lp = src_lang_short+"-"+tgt_lang_short
}