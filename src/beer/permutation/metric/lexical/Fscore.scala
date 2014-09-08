package beer.permutation.metric.lexical

class Fscore (beta:Double = 1, clipping:Boolean = true){

  def score(ref:List[String], sys:List[String]) : Double = {
    val refUnigramsInSys:Double = unigramMatches(ref, sys)
    val refUnigrams = ref.size
    val r = refUnigramsInSys/refUnigrams

    val sysUnigramsInRef:Double = unigramMatches(sys, ref)
    val sysUnigrams = sys.size
    val p = sysUnigramsInRef/sysUnigrams

    (1 + beta*beta) * (p*r)/(beta*beta*p + r)
  }

  /**
   * @return number of words from x that appear in y
   */
  private def unigramMatches(
      x:List[String],
      y:List[String]) : Int = {
    if(clipping){
      val xCount = x.groupBy(identity).
        mapValues{_.size}.withDefaultValue(0)
      val yCount = y.groupBy(identity).
        mapValues{_.size}.withDefaultValue(0)
      x.toSet map { word:String => math.min(xCount(word), yCount(word))} sum
    }else{
      val ys = y.toSet
      x count (ys contains _)
    }
  }

}
