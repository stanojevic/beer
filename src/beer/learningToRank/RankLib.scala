package beer.learningToRank

import ciir.umass.edu.learning.{RankerFactory,
                                Ranker,
                                DataPoint,
                                RankList,
                                DenseDataPoint,
                                SparseDataPoint,
                                RankerTrainer,
                                RANKER_TYPE}
import ciir.umass.edu.metric.MetricScorerFactory
import ciir.umass.edu.learning.neuralnet.RankNet

import scala.collection.JavaConverters._
import beer.Configuration

class RankLib(configuration:Configuration) extends Learner {
  
  private val useSparseFeatures = false

  private val rankLibConfig:Map[String, Object] =
    configuration.modelConfig("brewer").
      asInstanceOf[Map[String, Object]]("params").
      asInstanceOf[Map[String, Object]]

  private val rankerTypesMapping = Map(
     "MART"               -> RANKER_TYPE.MART,
     "RankNet"            -> RANKER_TYPE.RANKNET,
     "RankBoost"          -> RANKER_TYPE.RANKBOOST,
     "AdaRank"            -> RANKER_TYPE.ADARANK,
     "Coordinate Ascent"  -> RANKER_TYPE.COOR_ASCENT,
     "LambdaRank"         -> RANKER_TYPE.LAMBDARANK,
     "LambdaMART"         -> RANKER_TYPE.LAMBDAMART,
     "ListNet"            -> RANKER_TYPE.LISTNET,
     "Random Forests"     -> RANKER_TYPE.RANDOM_FOREST,
     "Linear Regression"  -> RANKER_TYPE.LINEAR_REGRESSION )

  private val rankerType  = rankerTypesMapping(rankLibConfig("ranker").asInstanceOf[String])
  private val hiddenNodes = rankLibConfig.getOrElse("hiddenNodes", 15).asInstanceOf[Int]
  RankNet.nHiddenNodePerLayer = hiddenNodes
  private val iterations  = rankLibConfig.getOrElse("epochs", 15).asInstanceOf[Int]
  RankNet.nIteration = iterations
  
  private var ranker:Ranker = null

  protected def train(data: List[(Map[Int, Double], Map[Int, Double], Double)]): Unit = {
    val trainer = new RankerTrainer()
    val trainingData = data.zipWithIndex.map{
      case ( (winner:Map[Int, Double], loser:Map[Int, Double], instanceWeight), qid:Int ) =>
        convertToRankList(qid+1, winner, loser, instanceWeight)
    }
    val mFact = new MetricScorerFactory()
    val trainScorer = mFact.createScorer("ERR@10")

    val numOfFeatures = featureMapping.size
    val features:Array[Int] = new Array(numOfFeatures)
    (1 to numOfFeatures) foreach ( featureId => features(featureId-1) = featureId )

    ranker = trainer.train(rankerType, trainingData.asJava, features, trainScorer);
  }

  protected def save(modelDir: String): Unit = {
    ranker.save(s"$modelDir/model")
  }

  protected def load(modelDir: String): Unit = {
    val rFact = new RankerFactory()
    ranker = rFact.loadRanker(s"$modelDir/model")
  }
  
  private def convertToRankList(   qid        : Int,
                                   winner     : Map[Int, Double],
                                   loser      : Map[Int, Double],
                                   instanceweight :  Double  ) : RankList = {
    val javaList = new java.util.ArrayList[DataPoint]()
    val winnerRelevance = 1
    val loserRelevance  = 0
    javaList.add(convertToDataPoint(winnerRelevance, qid, winner))
    javaList.add(convertToDataPoint(loserRelevance , qid, loser ))
    new RankList(javaList)
  }
  
  private def convertToDataPoint(relevance:Double, qid:Int, features:Map[Int, Double]) : DataPoint = {
    if(useSparseFeatures){
      val stringRep = features.toList.sortBy(_._1).map{case (k, v) => s"$k:$v"}.mkString(" ")
      new SparseDataPoint(s"$relevance qid:$qid $stringRep")
    }else{
      val numOfFeatures = featureMapping.size
      val stringRep = (1 to numOfFeatures).toList.map{ featureId:Int =>
        val value = features.getOrElse( featureId - 1 , 0.0)
        s"$featureId:$value"
      }.mkString(" ")
      new DenseDataPoint(s"$relevance qid:$qid $stringRep")
    }
  }

  protected def classify(features: Map[Int, Double]): Double = {
    val relevance = 0
    val qid = 1
    val dataPoint = convertToDataPoint(relevance, qid, features)
    ranker.eval(dataPoint)
  }

}
