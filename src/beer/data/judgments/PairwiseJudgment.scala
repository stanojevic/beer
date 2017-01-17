package beer.data.judgments

/**
 * @author milos
 */
class PairwiseJudgment (
  ref: String,
  winner: String,
  loser: String
){
  lazy val winnerWords : Array[String] = winner.split(" +")
  lazy val loserWords : Array[String] = loser.split(" +")
  lazy val refWords : Array[String] = ref.split(" +")
}
