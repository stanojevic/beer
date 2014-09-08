package beer.permutation.pet.metric.helper

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.HelperFunctions
import beer.permutation.pet.parser.ChartParser
import beer.permutation.pet.representation.FooBar

class CatalanCounterTest extends FlatSpec with ShouldMatchers {

  "for monotone permutation both inside and linear" should "return the same score" in {
    val p = List(1, 2, 3, 4)

    val pet = ShiftReduce.parse(p)
    val flatPET = HelperFunctions.collapseTree(pet)
    val linearCount = CatalanCounter.countLinear(flatPET)

    val n = p.size
    val chart = ChartParser.shiftReduceChartParse(p)
    val cache = CatalanCounter.emptyCountCache(n)
    val insideCount = CatalanCounter.countInside(cache, chart, 1, n)

    insideCount should equal (linearCount) 
  }

  "catalan of 4" should "be 5" in {
    val n = 4

    val cat = CatalanCounter.catalan(n)

    cat should equal (14)
  }

  "for random permutation both inside and linear" should "return the same score" in {
    val n = 5
    val p = FooBar.randomPermutation(n)

    val pet = ShiftReduce.parse(p)
    val flatPET = HelperFunctions.collapseTree(pet)
    val linearCount = CatalanCounter.countLinear(flatPET)

    val chart = ChartParser.shiftReduceChartParse(p)
    val cache = CatalanCounter.emptyCountCache(n)
    val insideCount = CatalanCounter.countInside(cache, chart, 1, n)

    insideCount should equal (linearCount) 
  }

  "for hard permutation both inside and linear" should "return the same score" in {
    val p = List(1, 2, 3, 5, 4, 8, 7, 6)
    val n = p.size

    val pet = ShiftReduce.parse(p)
    val flatPET = HelperFunctions.collapseTree(pet)
    val linearCount = CatalanCounter.countLinear(flatPET)

    val chart = ChartParser.shiftReduceChartParse(p)
    val cache = CatalanCounter.emptyCountCache(n)
    val insideCount = CatalanCounter.countInside(cache, chart, 1, n)

    insideCount should equal (linearCount) 
  }
}