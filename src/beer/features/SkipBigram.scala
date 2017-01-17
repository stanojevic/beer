package beer.features

/**
 * @author milos
 */
class SkipBigram (skipSize:Int, symmetry:Boolean, fScores:List[Double], gScores:List[Double]) extends AbstractPieceFeature {
  
  val name : String = s"skip$skipSize"

  private val separator = "-------"
  
  protected val forceSymmetry:Boolean = symmetry
  
  protected val fScoresToCompute:List[Double] = fScores

  protected val gScoresToCompute:List[Double] = gScores

  protected def getPieces(x:Sentence) : PiecesCollection = {
    val pieces = scala.collection.mutable.Map[String, Double]().withDefaultValue(0.0)
    
    for(i <- 0 until x.size-1){
      val word1 = x(i)
      for(j <- i+1 until math.min(i+skipSize+2, x.size)){
        val word2 = x(j)
        val skipGram = word1+separator+word2
        pieces(skipGram)+=1
      }
    }
    
    pieces
  }
}

object SkipBigram {
  
  def constructFromConfObject(conf:Map[String, Object]) : SkipBigram = {
    val order = conf("maxSkipSize").asInstanceOf[Int]
    val symmetry = conf("symmetry").asInstanceOf[Boolean]
    val fScores = conf("fScores").asInstanceOf[List[Double]]
    val gScores = conf("gScores").asInstanceOf[List[Double]]
    new SkipBigram(order, symmetry, fScores, gScores)
  }
  
}
