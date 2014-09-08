package beer.permutation.metric.scorers

import org.scalatest.{FlatSpec, ShouldMatchers}
import beer.permutation.metric.scorers.PETscoreFacade.{
                                               evaluatePEF,
                                               evaluatePEFold,
                                               evaluatePET,
                                               evaluatePETmaxArity,
                                               evaluatePETmonotoneRatio,
                                               evaluatePETcountRatio,
                                               evaluatePETnodeCount,
                                               evaluatePETviterbi}

class PETscoreFacadeTest extends FlatSpec with ShouldMatchers {

  /////////////////////////////////////////////////////////
  //           PEF
  /////////////////////////////////////////////////////////

  "for monotone score NEW PEF" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val beta = 0.5
    val score = evaluatePEF(beta, p)
    
    score should equal (1) 
  }

  "for inverted score NEW PEF" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val beta = 0.5
    val score = evaluatePEF(beta, p)
    
    score should equal (0)
  }
  
  "for sort of half score NEW PEF" should "be around 0.918" in {
    val p = List(0, 1, 2, 4, 3)
    val beta = 0.5
    val score = evaluatePEF(beta, p)
    
    score should be (0.918 plusOrMinus 0.001)
  }

  "for empty permutation NEW PEF" should "be 0" in {
    val p = List()
    val beta = 0.5
    val score = evaluatePEF(beta, p)
    
    score should be (0)
  }

  "for permutation of size 1 NEW PEF" should "be 1" in {
    val p = List(0)
    val beta = 0.5
    val score = evaluatePEF(beta, p)
    
    score should be (1)
  }

  /////////////////////////////////////////////////////////
  //           old PEF
  /////////////////////////////////////////////////////////

  "for monotone score OLD PEF" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val beta = 0.5
    val score = evaluatePEFold(beta, p)
    
    score should equal (1) 
  }

  "for inverted score OLD PEF" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val beta = 0.5
    val score = evaluatePEFold(beta, p)
    
    score should equal (0)
  }
  
  "for sort of half score OLD PEF" should "be around 0.921" in {
    val p = List(0, 1, 2, 4, 3)
    val beta = 0.5
    val score = evaluatePEFold(beta, p)
    
    score should be (0.921 plusOrMinus 0.001)
  }

  /////////////////////////////////////////////////////////
  //           canonical PET
  /////////////////////////////////////////////////////////

  "for monotone score CANONICAL PET" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val beta = 0.5
    val flatPET = false
    val score = evaluatePET(beta, flatPET, p)
    
    score should equal (1) 
  }

  "for inverted score CANONICAL PET" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val beta = 0.5
    val flatPET = false
    val score = evaluatePET(beta, flatPET, p)
    
    score should equal (0)
  }
  
  "for sort of half score CANONICAL PET" should "be around 0.874" in {
    val p = List(0, 1, 2, 4, 3)
    val beta = 0.5
    val flatPET = false
    val score = evaluatePET(beta, flatPET, p)
    
    score should be (0.874 plusOrMinus 0.001)
  }

  /////////////////////////////////////////////////////////
  //           flat PET
  /////////////////////////////////////////////////////////

  "for monotone score FLAT PET" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val beta = 0.5
    val flatPET = true
    val score = evaluatePET(beta, flatPET, p)
    
    score should equal (1) 
  }

  "for inverted score FLAT PET" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val beta = 0.5
    val flatPET = true
    val score = evaluatePET(beta, flatPET, p)
    
    score should equal (0)
  }
  
  "for sort of half score FLAT PET" should "be around 0.8" in {
    val p = List(0, 1, 2, 4, 3)
    val beta = 0.5
    val flatPET = true
    val score = evaluatePET(beta, flatPET, p)
    
    score should be (0.8 plusOrMinus 0.001)
  }
  
  
  /////////////////////////////////////////////////////////
  //           Viterbi PET
  /////////////////////////////////////////////////////////

  "for monotone score VITERBI PET" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val beta = 0.5
    val score = evaluatePETviterbi(beta, p)
    
    score should equal (1) 
  }

  "for inverted score VITERBI PET" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val beta = 0.5
    val score = evaluatePETviterbi(beta, p)
    
    score should equal (0)
  }
  
  "for sort of half score VITERBI PET" should "be around 0.968" in {
    val p = List(0, 1, 2, 4, 3)
    val beta = 0.5
    val score = evaluatePETviterbi(beta, p)
    
    score should be (0.968 plusOrMinus 0.001)
  }
  
  /////////////////////////////////////////////////////////
  //           PET Arity
  /////////////////////////////////////////////////////////

  "for monotone score PET ARITY" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val score = evaluatePETmaxArity(p)
    
    score should equal (1) 
  }

  "for inverted score PET ARITY" should "return 1" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val score = evaluatePETmaxArity(p)
    
    score should equal (1)
  }
  
  "for sort of half score PET ARITY" should "be 1" in {
    val p = List(0, 1, 2, 4, 3)
    val score = evaluatePETmaxArity(p)
    
    score should be (1)
  }

  /////////////////////////////////////////////////////////
  //           PET MONOTONE RATIO CHART
  /////////////////////////////////////////////////////////

  "for monotone score PET MONOTONE SPAN CHART" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val useChartSpan = true
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should equal (1) 
  }

  "for inverted score PET MONOTONE SPAN CHART" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val useChartSpan = true
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should equal (0)
  }
  
  "for sort of half score PET MONOTONE SPAN CHART" should "be 0.6" in {
    val p = List(0, 1, 2, 4, 3)
    val useChartSpan = true
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should be (0.6)
  }


  /////////////////////////////////////////////////////////
  //           PET MONOTONE RATIO PET
  /////////////////////////////////////////////////////////

  "for monotone score PET MONOTONE RATIO TREE" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val useChartSpan = false
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should equal (1) 
  }

  "for inverted score PET MONOTONE RATIO TREE" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val useChartSpan = false
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should equal (0)
  }
  
  "for sort of half score PET MONOTONE RATIO TREE" should "be 0.75" in {
    val p = List(0, 1, 2, 4, 3)
    val useChartSpan = false
    val score = evaluatePETmonotoneRatio(useChartSpan, p)
    
    score should be (0.75)
  }

  /////////////////////////////////////////////////////////
  //           PET COUNT RATIO PET
  /////////////////////////////////////////////////////////

  "for monotone score PET MONOTONE RATIO TREE" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val score = evaluatePETcountRatio(p)
    
    score should equal (1) 
  }

  "for inverted score PET MONOTONE RATIO TREE" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val score = evaluatePETcountRatio(p)
    
    score should equal (0)
  }
  
  "for sort of half score PET MONOTONE RATIO TREE" should "be 0.75" in {
    val p = List(0, 1, 2, 4, 3)
    val score = evaluatePETcountRatio(p)
    
    score should be (0.75)
  }

  /////////////////////////////////////////////////////////
  //           PET NODE COUNT RATIO PET
  /////////////////////////////////////////////////////////

  "for monotone score PET MONOTONE RATIO TREE" should "return 1" in {
    val p = List(0, 1, 2, 3, 4)
    val score = evaluatePETnodeCount(p)
    
    score should equal (1) 
  }

  "for inverted score PET MONOTONE RATIO TREE" should "return 0" in {
    val p = List(5, 4, 3, 2, 1, 0)
    val score = evaluatePETnodeCount(p)
    
    score should equal (0)
  }
  
  "for sort of half score PET MONOTONE RATIO TREE" should "be 0.75" in {
    val p = List(0, 1, 2, 4, 3)
    val score = evaluatePETnodeCount(p)
    
    score should be (0.75)
  }


}
