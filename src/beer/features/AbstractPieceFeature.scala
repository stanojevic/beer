package beer.features

trait AbstractPieceFeature extends AbstractFeature {
  
  type PiecesCollection = scala.collection.mutable.Map[String, Double]
  
  protected val forceSymmetry:Boolean
  
  protected val fScoresToCompute:List[Double]

  protected val gScoresToCompute:List[Double]

  protected def getPieces(x:Sentence) : PiecesCollection
  
  def expectedFeatureValues(nbest:NBest) : List[RawFeatureMap] = {

    val allNBestPieces:List[PiecesCollection] = nbest.par.map{case (sys, prob) => getPieces(sys)}.toList

    val expRefPieces = scala.collection.mutable.Map[String, Double]().withDefaultValue(0.0)
    for(((sys,prob), pieces) <- nbest zip allNBestPieces){
      for((piece, count) <- pieces){
        expRefPieces(piece) += count*prob
      }
    }
    
    val expRefSize = expRefPieces.values.sum
    
    val nbestFeatures:List[RawFeatureMap] = allNBestPieces.map{ sysPieces =>
      val sysSize = sysPieces.values.sum
      val expOverlap = getOverlap(sysPieces, expRefPieces)
      
      computeFeatures(expOverlap, sysSize, expRefSize)
    }
    
    nbestFeatures
  }
  
  def featureValues(sys_words:Sentence, ref_words:Sentence) : RawFeatureMap = {
    val sysPieces = getPieces(sys_words);
    val refPieces = getPieces(ref_words);
    
    val sysSize = sysPieces.values.sum
    val refSize = refPieces.values.sum
    
    val overlap = getOverlap(sysPieces, refPieces)
    
    computeFeatures(overlap, sysSize, refSize)
  }
  
  private def computeFeatures(overlap:Double, sysSize:Double, refSize:Double) : RawFeatureMap = {
    val p = if(sysSize == 0) 0.0 else overlap/sysSize
    val r = if(refSize == 0) 0.0 else overlap/refSize
    
    val a = if(forceSymmetry) math.min(p,r) else p
    val b = if(forceSymmetry) math.max(p,r) else r
    
    var features = List[(String, Double)]()
    
    features ::= (s"$name.a", a)
    features ::= (s"$name.b", b)
    
    for(beta <- fScoresToCompute){
      if(beta*beta*a+b == 0.0){
        features ::= (s"$name.f.$beta", 0.0)
      }else{
        val fScore = (1+beta*beta)*a*b/(beta*beta*a+b)
        features ::= (s"$name.f.$beta", fScore)
      }
    }

    for(beta <- gScoresToCompute){
      val gScore = math.pow(a*math.pow(b, beta), 1.0/(1+beta))
      features ::= (s"$name.g.$beta", gScore)
    }

    features
  }
  
  private def getOverlap(sysPieces:PiecesCollection, refPieces:PiecesCollection) : Double = {
    var overlap = 0.0
    for( (piece, sysCount) <- sysPieces ){
      if(refPieces.contains(piece)){
        overlap += math.min(sysCount, refPieces(piece))
      }
    }
    overlap
  }
  
}