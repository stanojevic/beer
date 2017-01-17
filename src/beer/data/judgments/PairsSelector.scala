package beer.data.judgments

import scala.collection.mutable.{Map => MutableMap}

class PairsSelector (min_agreement:Double, min_judgments:Int) {
  
  def selectPairs(judgments:List[Judgment]) : List[PairwiseJudgment] = {
    
    val pairs = MutableMap[String, MutableMap[String, MutableMap[String, MutableMap[String, MutableMap[String, Int]]]]]()
    
    for(judgment <- judgments){
      for(i <- 0 until judgment.sents.size-1){
        for(j <- i+1 until judgment.sents.size){
          val (a, b) = if(judgment.sents(i).compareTo(judgment.sents(j))>0)
              (i, j)
            else
              (j, i)
          val sentA = judgment.sents(a)
          val sentB = judgment.sents(b)
          
          if(! pairs.contains(judgment.lp))
            pairs(judgment.lp) = MutableMap[String, MutableMap[String, MutableMap[String, MutableMap[String, Int]]]]()
          if(! pairs(judgment.lp).contains(judgment.ref))
            pairs(judgment.lp)(judgment.ref) = MutableMap[String, MutableMap[String, MutableMap[String, Int]]]()
          if(! pairs(judgment.lp)(judgment.ref).contains(sentA))
            pairs(judgment.lp)(judgment.ref)(sentA) = MutableMap[String, MutableMap[String, Int]]()
          if(! pairs(judgment.lp)(judgment.ref)(sentA).contains(sentB))
            pairs(judgment.lp)(judgment.ref)(sentA)(sentB) = MutableMap[String, Int]().withDefaultValue(0)
          
          if(judgment.rankings(a) > 0 && judgment.rankings(b)>0){
            if(judgment.rankings(a) < judgment.rankings(b)){
              pairs(judgment.lp)(judgment.ref)(sentA)(sentB)("win") += 1
            }else if(judgment.rankings(a) > judgment.rankings(b)){
              pairs(judgment.lp)(judgment.ref)(sentA)(sentB)("los") += 1
            }else{
              pairs(judgment.lp)(judgment.ref)(sentA)(sentB)("tie") += 1
            }
          }
        }
      }
    }
    
    var pairsForTraining = List[PairwiseJudgment]()
    for((lp, rest) <- pairs){
      for((ref, rest) <- rest){
        for((sentA, rest) <- rest){
          for((sentB, rest) <- rest){
            val wins = pairs(lp)(ref)(sentA)(sentB)("win")
            val loss = pairs(lp)(ref)(sentA)(sentB)("los")
            val ties = pairs(lp)(ref)(sentA)(sentB)("tie")
            val totalCount:Double = wins+loss+ties
            val agreement:Double  = math.max(wins, loss)*1.0/totalCount
            if(totalCount > min_judgments && agreement > min_agreement){
              if(wins > loss){
                pairsForTraining ::= new PairwiseJudgment(ref, sentA, sentB)
              }else{
                pairsForTraining ::= new PairwiseJudgment(ref, sentB, sentA)
              }
            }
          }
        }
      }
    }
    
    pairsForTraining
  }
  
}
