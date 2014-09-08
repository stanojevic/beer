package beer.permutation.pet.representation

object ChartDefinition{

  type Chart = Array[Array[ChartEntry]]

  type Inference = List[Int]

  abstract class ChartEntry
  case class NonTerm(val operator:List[Int], val inferences:List[Inference]) extends ChartEntry
  case class Term(val element:Int) extends ChartEntry

  /**
   * chart starts from 1 (the same way as permutation)
   */
  def createEmptyChart(permutationSize:Int) : Chart = {
    // scalastyle:off
    val chart:Chart = Array.fill(permutationSize + 1, permutationSize + 1)(null)
    // scalastyle:on
    chart
  }

}
