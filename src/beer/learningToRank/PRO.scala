package beer.learningToRank

trait PRO {

  private def extractInstances(winners: List[Map[Int, Double]], losers: List[Map[Int, Double]]): List[(Map[Int, Double], Boolean)] = 
    winners zip losers flatMap { case (winner, loser) =>
      extractOneTrainingPair(winner, loser)
    }

  def extractOneTrainingPair(winner: Map[Int, Double], loser: Map[Int, Double]): List[(Map[Int, Double], Boolean)] = {
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