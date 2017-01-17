package beer.learning

import math.{pow, sqrt}

/**
 * @author milos
 */
object Kernel {
  
  def kernelize(x:Array[Double], gamma:Double, d:Int) : Array[Double] =
    d match {
      case 1 => x // careful here; it's not a copy
      case 2 => kernel_d2(x, gamma)
      case 3 => kernel_d3(x, gamma)
    }

  private def kernel_d2(original:Array[Double], gamma:Double) : Array[Double] = {
    val n = original.size
    val n_expanded:Int = n*(n+3)/2+1
    var expansion = new Array[Double](n_expanded)
    
    var freePos = 0

    for(i <- 0 until n){
      val x1 = original(i)
      expansion(freePos)= gamma*x1*x1            // degree 2
      freePos+=1
      expansion(freePos)= sqrt(2.0*gamma)*x1     // degree 1
      freePos+=1
      for(j <- i+1 until n){
        val x2 = original(j)
        expansion(freePos)= sqrt(2.0)*gamma*x1*x2  // degree 2 mixed
        freePos+=1
      }
    }
    expansion(freePos)= 1                    // degree 0
    freePos+=1
    
    expansion
  }
  
  
  private def kernel_d3(original:Array[Double], gamma:Double) : Array[Double] = {
    val n = original.size
    val n_expanded = 1.0/6.0*pow(n, 3) + pow(n, 2) + 11.0/6.0*n + 1
    var expansion = new Array[Double](n_expanded.toInt)
    
    var freePos = 0

    for(i <- 0 until n){
      val x1 = original(i)
      expansion(freePos)= pow(gamma, 3.0/2.0) * pow(x1, 3) // degree 3
      freePos+=1
      expansion(freePos)= sqrt(3.0)*gamma*x1*x1            // degree 2
      freePos+=1
      expansion(freePos)= sqrt(3.0*gamma)*x1               // degree 1
      freePos+=1

      for(j <- i+1 until n){
        val x2 = original(j)
        
        expansion(freePos)= sqrt(3.0)*pow(gamma, 3.0/2.0)*x1*x1*x2    // degree 3 mixed
        freePos+=1
        expansion(freePos)= sqrt(3.0)*pow(gamma, 3.0/2.0)*x1*x2*x2    // degree 3 mixed
        freePos+=1
        expansion(freePos)= sqrt(6.0)*gamma*x1*x2                     // degree 2 mixed
        freePos+=1
        for(k <- j+1 until n){
          val x3 = original(k)
          expansion(freePos)= sqrt(6) * pow(gamma, 3.0/2.0) * x1*x2*x3 // degree 3
          freePos+=1
        }
      }
    }
    expansion(freePos)= 1                    // degree 0
    freePos+=1
    
    expansion
  }
  
}
