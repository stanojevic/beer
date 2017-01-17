package beer.features

/**
 * @author milos
 */
class LengthDisbalance (exponential:Boolean, degrees:List[Double]) extends AbstractFeature {
  if(degrees.size == 0){
    throw new Exception("degrees should have at least one element")
  }

  // type RawFeatureMap = List[(String, Double)]
  // type Sentence = Array[String]
  // type Probability = Double
  // type NBest = List[(Sentence, Probability)]

  val name = "len_disbalance"+(if(exponential) "_exp" else "")
  
  def expectedFeatureValues(nbest:NBest) : List[RawFeatureMap] = {
    var expWordCountRef = 0.0
    var expCharCountRef = 0.0
    
    for((sent, prob) <- nbest){
      val wLen = sent.size
      val cLen = sent.mkString(" ").length()
      expWordCountRef += prob*wLen
      expCharCountRef += prob*cLen
    }
    
    nbest.map{ case (sent, prob) =>
      val wLen = sent.size
      val cLen = sent.mkString(" ").length()
      
      val wordDisbalance = computeDisbalance(wLen, expWordCountRef)
      val charDisbalance = computeDisbalance(cLen, expCharCountRef)
      
      computeDegrees(name+"_word", wordDisbalance) ++ computeDegrees(name+"_char", charDisbalance)
    }
  }
  
  private def computeDegrees(name:String, x:Double) : List[(String, Double)] = {
    degrees.map{degree => (s"$name.d.$degree",math.pow(x, degree))}
  }
  
  private def computeDisbalance(x:Double, y:Double) : Double = {
    val a = math.max(x, y)
    val b = math.min(x, y)
    
    if(b == 0.0){
      return 100000
    }else{
      if(exponential){
        val disbalance = math.exp(a/b) - math.E
        disbalance
      }else{
        val disbalance = a/b - 1.0
        disbalance
      }
    }
  }
  
  def featureValues(sys_words:Sentence, ref_words:Sentence) : RawFeatureMap = {
    val sysWlen = sys_words.size
    val refWlen = ref_words.size

    val sysClen = sys_words.mkString(" ").length()
    val refClen = ref_words.mkString(" ").length()

    val wordDisbalance = computeDisbalance(sysWlen, refWlen)
    val charDisbalance = computeDisbalance(sysClen, refClen)
    
    computeDegrees(name+"_word", wordDisbalance) ++ computeDegrees(name+"_char", charDisbalance)
  }
  
}

object LengthDisbalance {
  
  def constructFromConfObject(conf:Map[String, Object]) : LengthDisbalance = {
    val exponential = conf("exponential").asInstanceOf[Boolean]
    val degrees = conf("degrees").asInstanceOf[List[Double]]
    new LengthDisbalance(exponential, degrees)
  }
  
}
