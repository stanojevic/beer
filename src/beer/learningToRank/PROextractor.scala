package beer.learningToRank

object PROextractor {

  type Features = Map[Int, Double]

  def extractInstances(winners: List[Features], losers: List[Features]): List[(Features, Boolean)] = 
    winners zip losers flatMap { case (winner, loser) =>
      onePair(winner, loser)
    }

  def onePair(winner: Features, loser: Features): List[(Features, Boolean)] = {
    val featureNames = (winner.keys ++ loser.keys).toSet

    val positiveInstance = featureNames.map {
      case feature =>
        val value = winner.getOrElse(feature, 0.0) - loser.getOrElse(feature, 0.0)
        (feature, value)
    }.filter{_._2 != 0}.toMap

    val negativeInstance = positiveInstance mapValues { -_ }

    List(
      (positiveInstance, true),
      (negativeInstance, false))
  }

}