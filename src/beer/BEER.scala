package beer

import beer.alignment.Aligner.SentencePair
import beer.alignment.Aligner
import beer.learningToRank.{Learner, LearnerFactory}
import beer.ingredients.SparseIngredient
import beer.ingredients.IngredientsFactory
import beer.alignment.AlignerFactory
import java.io.File
import beer.io.Log
import beer.permutation.finders.MeteorAlignmentToPermutation
import beer.permutation.metric.scorers.PETscoreFacade
import beer.permutation.metric.scorers.Kendall
import beer.permutation.metric.scorers.Hamming
import beer.permutation.metric.scorers.Spearman
import beer.permutation.metric.scorers.Ulam
import beer.permutation.metric.scorers.FuzzyScore
import beer.permutation.metric.lexical.Fscore
import beer.permutation.metric.lexical.BLEU

class BEER (val arguments:String, val beerHome:String, val configurationFile:String){
  
  private val args:Array[String] = if(arguments == "") new Array[String](0) else arguments split " "

  val conf = new Configuration(args, beerHome, configurationFile)
  
  private val aligner : Aligner = if(conf.arguments.workingMode == "train") null else loadAligner(conf)
  
  private val model : Learner = loadLearner (conf)
  
  private val factoredMetric : SentencePair=>Map[String, Double] = composeMetric(conf)
  
  /////////////////////////// initialization ///////////////////////////
  
  
  private def loadAligner(conf:Configuration) : Aligner = {
    val alignerClassName = conf.modelConfig("aligner").asInstanceOf[Map[String, Object]]("class").toString
    AlignerFactory.createAligner(alignerClassName, conf)
  }
  
  private def loadLearner (conf:Configuration) : Learner  = {
    val exactModelDir = conf.resources("exactModelDir")
    val classifier = LearnerFactory.createLearner(conf)
    Log.println(conf, s"\nLOAD LEARNER FROM $exactModelDir MADE OF $classifier\n")
    if( ! Set("factors", "train").contains(conf.arguments.workingMode)){
      classifier.loadModel(exactModelDir)
    }
    classifier
  }
  
  private def composeMetric(conf:Configuration) : SentencePair=>Map[String, Double] = {
    var subMetrics = List[SparseIngredient]()

    val ingredients = conf.modelConfig("ingredients").asInstanceOf[List[Map[String, Object]]]
    for(ingradient <- ingredients){
      val className = ingradient("class").asInstanceOf[String]
      val params = ingradient.getOrElse("params", Map[String, Object]())
      subMetrics ::= IngredientsFactory.createIngredient(conf, className, params)
    }

    val composedMetric: SentencePair=>Map[String, Double] = (sp:SentencePair) => {
      val partialScores:List[Map[String, Double]] = subMetrics map {_.eval(sp)}
      val mergedScores = partialScores reduce (_ ++ _) mapValues {
        case score if score.isNaN      => 0
        case score if score.isInfinite => 0
        case score                     => score
      } filter (_._2 != 0)
      mergedScores
    }
    
    composedMetric
  }
  
  /////////////////////////// real code ///////////////////////////
  
  def evaluate(sys:String, refs:List[String]) : Double = {
    refs map {evaluate(sys, _)} max
  }
  
  private def evaluateCorpusAvgSent(weights:List[Double], allFactors:List[Map[String, Double]]) : Double = {
    val total = weights.sum
    val scaledWeights = weights map {_/total}
    (scaledWeights zip allFactors).map{case (weight, factors) => weight * evaluate(factors)}.sum
  }

  def evaluateCorpus(weights:List[Double], allFactors:List[Map[String, Double]]) : Double = evaluateCorpusAggregate(weights, allFactors)
  
  private def evaluateCorpusAggregate(weights:List[Double], allFactors:List[Map[String, Double]]) : Double = {
    val total = weights.sum
    val scaledWeights = weights map {_/total}
    
    val totalFactors = (scaledWeights zip allFactors).map{ case (weight, factors) =>
      factors.mapValues{_*weight}
    }.foldLeft(Map[String, Double]().withDefaultValue(0.0)){ (acc, f) => 
      acc ++ f.map{ case (k,v) => k -> (v + acc(k)) }
    }

    evaluate(totalFactors)
  }

  def evaluate(sys:String, ref:String) : Double = {
    val the_factors = factors(sys, ref)
    val score = model.scoreInstance(the_factors)
    score
  }
  
  def evaluate(factors:Map[String, Double]) : Double = {
    val score = model.scoreInstance(factors)
    score
  }
  
  private def combinations(n:Int, k:Int) : Int = {
    val nominator = (k+1 to n).map{BigInt(_)}.product
    val denominator = (1 to k).map{BigInt(_)}.product
    val combination = nominator/denominator
    combination.toInt
  }
  
  def factors(sys:String, ref:String):Map[String, Double] = {
    val alignment = aligner.align(sys, ref)
    val factors = factoredMetric(alignment)
    factors
  }

  /**
   * @param metricName reordering metric that will be used
   *        one of: PEFrecursive, PETrecursiveViterbi, PEFsize, PETarity, PETsize, Kendall, Hamming, Spearman, Ulam, Fuzzy
   * @param alpha default=0.5 the weight of the lexical part
   * @param beta default=0.6 used only for recursive PET and PEF metrics
   * 
   */
  def reorderingScore(sys:String, ref:String, metricName:String, alpha:Double=0.5, beta:Double = 0.6) : Double = {
    val alignment   = aligner.align(sys, ref)
    val unalignedStrategy = "ignore and normalize"
    val permutation = MeteorAlignmentToPermutation.convertMeteorToPermutation(unalignedStrategy, alignment)
    
    val uncheckedLexical = new Fscore().score(alignment.ref, alignment.sys)
    val lexical = if(uncheckedLexical.isNaN) 0 else uncheckedLexical

    val uncheckedOrdering = metricName match {
      case "PEFrecursive" =>
        PETscoreFacade.evaluatePEF(beta, permutation)
      case "PETrecursiveViterbi" =>
        PETscoreFacade.evaluatePETviterbi(beta, permutation)
      case "PEFsize" =>
        PETscoreFacade.evaluatePETcountRatio(permutation)
      case "PETarity" =>
        PETscoreFacade.evaluatePETavgArity(permutation)
      case "PETsize" =>
        PETscoreFacade.evaluatePETnodeCount(permutation)
      case "Kendall" =>
        Kendall.evaluate(permutation)
      case "Hamming" =>
        Hamming.evaluate(permutation)
      case "Spearman" =>
        Spearman.evaluate(permutation)
      case "Ulam" =>
        Ulam.evaluate(permutation)
      case "Fuzzy" =>
        FuzzyScore.evaluate(permutation)
      case _ =>
        System.err.println(s"reordering metric $metricName is not supported")
        System.exit(-1)
        0.0
    }
    
    val ordering = if(uncheckedOrdering.isNaN) 0 else uncheckedOrdering
    
    val bp = BLEU.brevityPenalty(alignment.ref.size, permutation.size)
    
    val score = alpha*lexical + (1-alpha)*bp*ordering
    
    score
  }
  
  def factorsAndTokens(sys:String, ref:String):(Map[String, Double], List[String], List[String]) = {
    val alignment = aligner.align(sys, ref)
    val factors = factoredMetric(alignment)
    (factors, alignment.sys, alignment.ref)
  }
  
  def train(
      labeledData   : List[(Map[String, Double], Map[String, Double])],
      unlabeledData : List[(Map[String, Double], Map[String, Double])],
      unsupervisedIterations    : Int
      ) : Unit = {
    val modelDestination = conf.resources("exactModelDir")
    new File(modelDestination).mkdirs()
    
    val weightedLabeledData = labeledData.map{ case (win, los) => (win, los, 1.0)}
    
    System.gc()
    Log.println(conf, "STARTED TRAINING "+conf.arguments.lang)
    model.trainModel(weightedLabeledData) //initial model
    Log.println(conf, "FINISHED TRAINING "+conf.arguments.lang)
    
    for(iteration <- 1 to unsupervisedIterations) {

      val poorlyWeightedUnlabeledData = unlabeledData.map{ case (first, second) =>
        val firstScore = model.scoreInstance(first)
        val secondScore = model.scoreInstance(second)
        if(firstScore > secondScore){
          (first, second, firstScore-secondScore)
        }else{
          (second, first, secondScore-firstScore)
        }
      }
      
      val avgWeight = poorlyWeightedUnlabeledData.map{_._3}.sum/poorlyWeightedUnlabeledData.size
      
      val weightedUnlabeledData = poorlyWeightedUnlabeledData.map{ case (win, los, weight) =>
        (win, los, weight/avgWeight)
      }
      
      val trainingData = weightedLabeledData++weightedUnlabeledData
      model.trainModel(trainingData)
    }
    
    model.saveModel(modelDestination)
    
    // copying description file
    val src = new File(conf.arguments.modelDescFile)
    val tgt = new File(s"$modelDestination/description.yaml")
    new java.io.FileOutputStream(tgt) getChannel() transferFrom( new java.io.FileInputStream(src) getChannel, 0, Long.MaxValue )
  }

}
