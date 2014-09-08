package beer.permutation.pet.parser

import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.ChartDefinition.Chart
import beer.permutation.pet.representation.Term
import beer.permutation.pet.representation.NonTerm
import beer.permutation.pet.representation.ChartDefinition
import beer.permutation.pet.representation.ChartDefinition.{Term      => ChartTerm,
                                                NonTerm   => ChartNonTerm,
                                                Inference => ChartInference}

object ChartParser {

  def shiftReduceChartParse(permutation:List[Int]) : Chart = {
    createChart(ShiftReduce.parse(_), permutation)
  }

  private def createChart(canonicalParser:List[Int]=>TreeNode, permutation:List[Int]) : Chart = {
    val canonicalTree = canonicalParser(permutation)
    val collapsedTree = HelperFunctions.collapseTree(canonicalTree)
    val chart = createChart(collapsedTree)
    chart
  }

  /**
   * @param collapsedTree the tree MUST be collapsed
   */
  private def createChart(collapsedTree:TreeNode) : Chart = {
    val chart = ChartDefinition.createEmptyChart(collapsedTree.right)

    fillNodesRec(chart, collapsedTree)

    chart
  }

  private final
  def fillNodesRec(chart:Chart, collapsedTree:TreeNode) : Unit = {
    collapsedTree match {
      case Term(pos, el) => chart(pos)(pos) = ChartTerm(el)
      case NonTerm(start, end, min, max, op, children) => {
        children.foreach(fillNodesRec(chart, _))
        if(op.size==2){
          for(span <- 2 to children.size){
            for(spannedChildren <- children.sliding(span)){
              val spanStart = spannedChildren.head.left
              val spanEnd   = spannedChildren.last.right
              var inferences = List[ChartInference]()
              for(splitPoint <- spannedChildren.tail.map{_.left}){
                inferences ::= List(splitPoint)
              }
              chart(spanStart)(spanEnd) = ChartNonTerm(op, inferences.reverse)
            }
          }
        }else{
          val splitPoints = children.tail.map(_.left)
          val entry = ChartNonTerm(op, List(splitPoints))
          chart(start)(end) = entry
        }
      }
    }
  }

}
