package beer.permutation.pet.parser

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.permutation.pet.representation.ChartDefinition.{createEmptyChart, 
                                                Term,
                                                NonTerm,
                                                Inference}

class ChartParserTest extends FlatSpec with ShouldMatchers {

  "for collapsed version of paper example" should "return correct chart" in {
    // scalastyle:off
    val p = List(5, 7, 4, 6, 3, 1, 2)
    val correctChart = createEmptyChart(p.size)
    p.zipWithIndex.foreach{
      case (el, pos) => correctChart(pos+1)(pos+1)=Term(el)
    }
    correctChart(1)(4) = NonTerm(List(2,4,1,3), List(List(2,3,4)))
    correctChart(6)(7) = NonTerm(List(1,2), List(List(7)))
    correctChart(5)(7) = NonTerm(List(2,1), List(List(6)))
    correctChart(1)(5) = NonTerm(List(2,1), List(List(5)))
    correctChart(1)(7) = NonTerm(List(2,1), List(List(5), List(6)))
    // scalastyle:on

    val chart = ChartParser.shiftReduceChartParse(p)

    chart should equal (correctChart)
  }

  "for inverted permutation" should "return fully covered chart" in {
    // scalastyle:off
    val p = List(5, 4, 3, 2, 1)
    val n = p.size
    val correctChart = createEmptyChart(n)
    p.zipWithIndex.foreach{
      case (el, pos) => correctChart(pos+1)(pos+1)=Term(el)
    }
    //correctChart(1)(4) = Set(Inference(List(2,4,1,3), List(2,3,4)))
    for(i <- 1 to n-1){
      for(j <- i+1 to n){
        correctChart(i)(j) = NonTerm(List(2,1),
            (i+1 to j).toList.map{List(_)})
      }
    }
    // scalastyle:on

    val chart = ChartParser.shiftReduceChartParse(p)

    chart should equal (correctChart)
  }

}