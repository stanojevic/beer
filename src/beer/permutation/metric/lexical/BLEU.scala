package beer.permutation.metric.lexical

/**
 * OOOOOOLD CODE
 * NEEEEEDS TO BE CHECKED
 *
 *
 */
class BLEU (val order:Int, val smoothed: Boolean) {

  def evaluate(ref:List[String], sys:List[String]) : Double = {
    BLEU.bleu(ref, sys, order, smoothed)
  }

}

object BLEU{

  private def precision(ref: List[String], sys: List[String], order: Int, smoothed: Boolean): Double = {
    var sysGrams = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)
    var refGrams = scala.collection.mutable.Map[String, Int]().withDefaultValue(0)

    var changingSys = sys
    while (changingSys.size >= order) {
      val nGram = changingSys.take(order).mkString(" ")
      sysGrams(nGram) += 1
      changingSys = changingSys.tail
    }

    var changingRef = ref
    while (changingRef.size >= order) {
      val nGram = changingRef.take(order).mkString(" ")
      refGrams(nGram) += 1
      changingRef = changingRef.tail
    }

    var matches = 0
    for (nGram <- refGrams.keys)
      matches += Math.min(sysGrams(nGram), refGrams(nGram))
    if (smoothed && matches == 0)
      matches = 1
    matches.toFloat / (sys.length - order + 1)
  }

  def brevityPenalty(refLenght: Int, sysLength: Int) =
    if (sysLength == 0)
      0
    else if (sysLength > refLenght) 
      1 
    else 
      Math.exp(1 - refLenght.toDouble / sysLength)

  private def bleu(ref: List[String], sys: List[String], maxBleu: Int, smoothed: Boolean): Double = {
    val bp = brevityPenalty(ref.length, sys.length)
    var precisions = List[Double]()

    for (order <- maxBleu to 1 by -1)
      precisions ::= precision(ref, sys, order, smoothed)

    bp * Math.sqrt(Math.sqrt(precisions.foldLeft(1.0)(_ * _)))
  }

}
