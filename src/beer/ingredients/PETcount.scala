package beer.ingredients

import beer.alignment.Aligner.{PhrasePair, SentencePair}
import beer.permutation.pet.representation.TreeNode
import beer.permutation.finders.MeteorAlignmentToPermutation
import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.HelperFunctions
import beer.permutation.pet.metric.helper.CatalanCounter
import beer.permutation.pet.metric.PETcountScore
import beer.Configuration

class PETcount (configuration:Configuration, params:Map[String, Object]) extends SparseIngredient {

  val featureName = "PETcount"

  val unalignedStrategy = params.
        getOrElse("unalignedStrategy","ignore and normalize").asInstanceOf[String]

  def eval(sp:SentencePair):Map[String, Double] = {
    val perm = MeteorAlignmentToPermutation.convertMeteorToPermutation(unalignedStrategy, sp)
    val incrementedPerm = perm map {_+1}
    if(perm.size == 0)
      return Map(featureName -> 0)

    val score = PETcountScore.evaluate(incrementedPerm)
    Map(featureName -> score)
  }
}