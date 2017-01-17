package beer.data.absolute

import org.scalatest.{FlatSpec, ShouldMatchers}


/**
 * @author milos
 */
class WMT13absoluteTest extends FlatSpec with ShouldMatchers {
  
  "loading" should "work" in {
    val absDir = "/home/milos/Dropbox/AMSTERDAM_ILLC_WORK/WORKSPACE_MAIN/Ninkasi/data/wmt13-absolute-judgments"
    val dataDir = "/home/milos/Dropbox/AMSTERDAM_ILLC_WORK/WORKSPACE_MAIN/Ninkasi/data/wmt13-metrics-task"
    
    val judgments = WMT13absolute.loadJudgments(absDir, dataDir)
    
    println("hello")
  }
  
}