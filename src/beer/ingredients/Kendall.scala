package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.permutation.finders.MeteorAlignmentToPermutation
import beer.permutation.metric.scorers.{Kendall => KendallScorer}
import beer.Configuration

class Kendall (configuration:Configuration, params:Map[String, Object]) extends SparseIngredient {

  val featureName = "Kendall"

  val unalignedStrategy = params.
        getOrElse("unalignedStrategy","ignore and normalize").asInstanceOf[String]

  def eval(sp:SentencePair):Map[String, Double] = {
    val perm = MeteorAlignmentToPermutation.convertMeteorToPermutation(unalignedStrategy, sp)
    val score = KendallScorer.evaluate(perm)
    Map(featureName -> score)
  }

}