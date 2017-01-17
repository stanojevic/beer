package beer.data.nbest

import org.scalatest.{FlatSpec, ShouldMatchers}


/**
 * @author milos
 */
class WMT09syscombTest extends FlatSpec with ShouldMatchers {
  
  "loading" should "work" in {
    val dir = "data/wmt09-nbest/"
    
    val x = WMT09syscomb.load(dir, 3)
    
    println("hello")
  }
  
}