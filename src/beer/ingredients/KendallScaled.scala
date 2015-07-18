package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.permutation.finders.MeteorAlignmentToPermutation
import beer.permutation.metric.scorers.{Kendall => KendallScorer}
import beer.Configuration
import beer.permutation.finders.HandleUnaligned

class KendallScaled (configuration:Configuration, params:Map[String, Object]) extends SparseIngredient {

  val featureName = "KendallScaled"

  val alphas : List[Double] = List(0.25, 0.5, 0.75)
  val unalignedStrategy = "ignore and normalize"

  def eval(sp:SentencePair):Map[String, Double] = {
    val perm = MeteorAlignmentToPermutation.convertMeteorToPermutation(unalignedStrategy, sp)
    val kendallScore = KendallScorer.evaluate(perm)
    val recall = this.recall(sp)

    var output = Map[String, Double]()

    for(alpha <- alphas){
      val score = Math.pow(kendallScore*recall, alpha)
      val name = s"$featureName:$alpha"
      if(score.isNaN || score.isInfinite){
        output += name -> 0.0
      }else{
        output += name -> score
      }
    }
    output
  }
  
  private def recall(pureSp:SentencePair) : Double = {
    val sp = HandleUnaligned.updateSentencePairWithNulls(pureSp)
    val matched = sp.a.map{ pp:PhrasePair => if(pp.sysStart < 0) 0 else pp.refEnd-pp.refStart+1}.sum
    val total = sp.ref.size // a.map{ pp:PhrasePair => pp.refEnd-pp.refStart+1}.sum
    matched.toDouble/total.toDouble
  }

}