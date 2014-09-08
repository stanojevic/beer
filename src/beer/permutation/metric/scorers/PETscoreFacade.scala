package beer.permutation.metric.scorers

import beer.permutation.pet.metric.PEFscore
import beer.permutation.pet.metric.PETscore
import beer.permutation.pet.metric.PETViterbiScore
import beer.permutation.pet.metric.PEFoldScore
import beer.permutation.pet.metric.PETmaxArityScore
import beer.permutation.pet.metric.PETmonotoneRatio
import beer.permutation.pet.metric.PETcountScore
import beer.permutation.pet.metric.PETcountMonoRestricted
import beer.permutation.pet.metric.PETnodeCountScore
import beer.permutation.pet.metric.PETavgArityScore
import beer.permutation.pet.metric.PETcountMonoRestricted

object PETscoreFacade {

  def evaluatePEF(beta:Double, perm: List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PEFscore(beta)
      evaluator.evaluate(p)
    }
  }

  def evaluatePET(
      beta:Double,
      flatPET:Boolean,
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PETscore(beta, flatPET)
      evaluator.evaluate(p)
    }
  }

  def evaluatePETviterbi(
      beta:Double,
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PETViterbiScore(beta)
      evaluator.evaluate(p)
    }
  }

  def evaluatePEFold(
      beta:Double,
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PEFoldScore(beta)
      evaluator.evaluate(p)
    }
  }

  def evaluatePETmaxArity(
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = PETmaxArityScore
      evaluator.evaluate(p)
    }
  }

  def evaluatePETavgArity(
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = PETavgArityScore
      evaluator.evaluate(p)
    }
  }

  def evaluatePETmonotoneRatio(
      useChartSpans:Boolean,
      perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PETmonotoneRatio(useChartSpans)
      evaluator.evaluate(p)
    }
  }

  def evaluatePETcountRatio(perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      PETcountScore.evaluate(p)
    }
  }

  def evaluatePETcountMonoRestrictedRatio(perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PETcountMonoRestricted
      evaluator.evaluate(p)
    }
  }
  
  def evaluatePEToperatorLengthScore(perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      val evaluator = new PEToperatorLengthScore
      evaluator.evaluate(p)
    }
  }

  def evaluatePETnodeCount(perm:List[Int]) : Double = {
    if(perm.size == 0){
      0
    }else if(perm.size == 1){
      1
    }else{
      val p = perm map {_ + 1}
      PETnodeCountScore.evaluate(p)
    }
  }

}
