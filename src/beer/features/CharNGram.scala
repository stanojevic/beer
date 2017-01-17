package beer.features

/**
 * @author milos
 */
class CharNGram (order:Int, symmetry:Boolean, fScores:List[Double], gScores:List[Double]) extends AbstractPieceFeature {
  
  val name : String = s"char$order"
  
  protected val forceSymmetry:Boolean = symmetry
  
  protected val fScoresToCompute:List[Double] = fScores

  protected val gScoresToCompute:List[Double] = gScores

  protected def getPieces(x_words:Sentence) : PiecesCollection = {
    val pieces = scala.collection.mutable.Map[String, Double]().withDefaultValue(0.0)
    
    val x = x_words.mkString(" ")
    
    for(i <- 0 until x.size-order+1){
      val ngram = x.substring(i, i+order-1)
      pieces(ngram)+=1
    }
    
    pieces
  }
}

object CharNGram {
  
  def constructFromConfObject(conf:Map[String, Object]) : CharNGram = {
    val order = conf("order").asInstanceOf[Int]
    val symmetry = conf("symmetry").asInstanceOf[Boolean]
    val fScores = conf("fScores").asInstanceOf[List[Double]]
    val gScores = conf("gScores").asInstanceOf[List[Double]]
    new CharNGram(order, symmetry, fScores, gScores)
  }
  
}
