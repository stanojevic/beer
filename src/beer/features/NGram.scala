package beer.features

class NGram (order:Int, symmetry:Boolean, fScores:List[Double], gScores:List[Double]) extends AbstractPieceFeature {
  
  val name : String = s"ngram$order"
  
  private val separator = "-------"
  
  protected val forceSymmetry:Boolean = symmetry
  
  protected val fScoresToCompute:List[Double] = fScores

  protected val gScoresToCompute:List[Double] = gScores

  protected def getPieces(x:Sentence) : PiecesCollection = {
    val pieces = scala.collection.mutable.Map[String, Double]().withDefaultValue(0.0)
    
    for(i <- 0 until x.size-order+1){
      val ngram = new StringBuilder
      for(j <- i until i+order){
        ngram.append(separator)
        ngram.append(x(j))
      }
      pieces(ngram.toString)+=1
    }
    
    val startNgram = new StringBuilder
    startNgram.append("<S>")
    for(j <- 0 until math.min(x.size, order)){
      startNgram.append(separator)
      startNgram.append(x(j))
    }
    pieces(startNgram.toString())+=1
    
    val endNgram = new StringBuilder
    for(j <- math.max(x.size-order, 0) until x.size){
      endNgram.append(x(j))
      endNgram.append(separator)
    }
    endNgram.append("</S>")
    pieces(endNgram.toString())+=1
    
    pieces
  }
}

object NGram {
  
  def constructFromConfObject(conf:Map[String, Object]) : NGram = {
    val order = conf("order").asInstanceOf[Int]
    val symmetry = conf("symmetry").asInstanceOf[Boolean]
    val fScores = conf("fScores").asInstanceOf[List[Double]]
    val gScores = conf("gScores").asInstanceOf[List[Double]]
    new NGram(order, symmetry, fScores, gScores)
  }
  
}
