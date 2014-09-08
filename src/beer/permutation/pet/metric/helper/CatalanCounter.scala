package beer.permutation.pet.metric.helper

import beer.permutation.pet.representation.ChartDefinition.{Chart, Term => ChartTerm, NonTerm => ChartNonTerm}
import beer.permutation.pet.representation.{TreeNode, Term => TreeTerm, NonTerm => TreeNonTerm}
import beer.permutation.pet.representation.FooBar.{isMonotone, isInverted}
import scala.annotation.tailrec

object CatalanCounter {
  
  /**
   * @param tree flattened tree
   * @return number of PETs to which tree could be expanded
   */
  def countLinear(tree:TreeNode) : BigInt = {
    tree.map{
      case TreeNonTerm(_, _, _, _, op, children) if isMonotone(op) || isInverted(op) => catalan(children.size-1)
      case _ => BigInt(1)
    } product
  }
  
  /**
   * Here definition of catalan is as on http://en.wikipedia.org/wiki/Catalan_number
   * meaning that if we want to get the number of binary trees for permutation of
   * length n we ask for catalan(n-1)
   */
  def catalan(n:BigInt) : BigInt = {
    if(n<2)
      1
    else
      fact(2*n)/(fact(n+1)*fact(n))
  }
  
  @tailrec
  private def fact(x:BigInt, acc:BigInt=1) : BigInt = {
    if(x==1)
      acc
    else
      fact(x-1, acc*x)
  }

  def emptyCountCache(n:Int) : Array[Array[BigInt]] = {
    val cache = Array.fill[BigInt](n + 1, n + 1)(-1)
    for(i <- 0 to n){
      cache(i)(0) = Integer.MIN_VALUE  // this is done in order to easily detect if these
      cache(0)(i) = Integer.MIN_VALUE  // fields influence the computation (they shouldn't)
    }
    cache
  }

  def countInside(
      cache:Array[Array[BigInt]],
      chart : Chart,
      i:Int,
      j:Int) : BigInt = {
    if(cache(i)(j) < 0){
      cache(i)(j) = chart(i)(j) match {
        case ChartTerm(_) => {
          1
        }
        case ChartNonTerm(operator, inferences) => {
          inferences map { splitPoints =>
            val starts = i::splitPoints
            val ends = splitPoints.map{_-1} :+ j
            val spans = starts zip ends
            
            if(i % 10 == 1)
              i + 1

            spans map { case (a,b) =>
              countInside(cache, chart, a, b)
            } product
          } sum
        }
      }
    }

    cache(i)(j)
  }

}