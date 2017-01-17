package beer

import beer.data.judgments.WMT14
import beer.data.judgments.WMT13
import beer.data.judgments.WMT11
import beer.data.judgments.Judgment
import beer.data.judgments.PairsSelector
import beer.features.FeatureExtractor
import beer.learning.FeatureNameMapping
import beer.learning.FeatureScaling
import beer.learning.GridSearch
import beer.data.nbest.WMT09syscomb
import beer.learning.Classifier
import beer.data.judgments.FeaturesPair
import beer.learning.PlattScaling
import java.io.File
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.nio.file.Files.copy
import java.nio.file.Paths.get
import beer.data.absolute.WMT13absolute
import beer.learning.LinearRegressionScaling
import java.io.PrintWriter

case class TrainConfig(
    train_data_type: String = "",
    train_data_loc : File = null,
    valid_data_type: String = "",
    valid_data_loc : File = null,
    
    nbest_type : String = "wmt09",
    nbest_loc  : File   = null,
    nbest_topAndBottomK : Int = -1,
    
    self_train_training_size    : Int = 60000,
    self_train_iters            : Int = 0,
    
    absolute_judgments_loc : String = "",
    absolute_data_loc : String = "",
    absolute_judgments_type: String = "wmt13",
    
    features_desc         : File = null,
    kernel_degree         : Int = 1,
    out_dir               : File = null,
    threads               : Int = 1
    )

object TrainApp {
  
  
  def main(args:Array[String]) : Unit = {

    val parser = new scopt.OptionParser[TrainConfig]("BEER") {

      head("BEER", "2.0")

      help("help") text ("prints this usage text")

      opt[String]("train_data_type") action { (x, c) =>
        c.copy(train_data_type = x)
      } required() valueName ("<wmt11|wmt13|wmt14>")

      opt[File]("train_data_loc") action { (x, c) =>
        if(!x.exists()){
          System.err.println("error: directory "+x.toString+" doesn't exist")
          System.exit(-1)
        }
        c.copy(train_data_loc = x)
      } required() valueName ("<dir>")

      opt[String]("valid_data_type") action { (x, c) =>
        c.copy(valid_data_type = x)
      } required() valueName ("<wmt11|wmt13|wmt14>")

      opt[File]("valid_data_loc") action { (x, c) =>
        if(!x.exists()){
          System.err.println("error: directory "+x.toString+" doesn't exist")
          System.exit(-1)
        }
        c.copy(valid_data_loc = x)
      } required() valueName ("<dir>")
      
      opt[String]("nbest_type") action { (x,c) =>
        c.copy(nbest_type = x)
      } valueName ("<wmt09>")
      
      opt[File]("nbest_loc") action { (x,c) =>
        if(!x.exists()){
          System.err.println("error: directory "+x.toString+" doesn't exist")
          System.exit(-1)
        }
        c.copy(nbest_loc = x)
      } valueName ("<dir>")

      opt[Int]("nbest_topAndBottomK") action { (x,c) =>
        c.copy(nbest_topAndBottomK = x)
      } valueName ("-1")
      
      opt[Int]("self_train_training_size") action{ (x,c) =>
        c.copy(self_train_training_size = x)
      } valueName ("<Int>")

      opt[Int]("self_train_iters") action{ (x,c) =>
        c.copy(self_train_iters = x)
      } valueName ("<Int>")

      opt[String]("absolute_judgments_loc") action{ (x,c) =>
        c.copy(absolute_judgments_loc = x)
      } valueName ("<Dir>")

      opt[String]("absolute_data_loc") action{ (x,c) =>
        c.copy(absolute_data_loc = x)
      } valueName ("<Dir>")

      opt[String]("absolute_judgments_type") action{ (x,c) =>
        c.copy(absolute_judgments_type = x)
      } valueName ("wmt13")
      
      opt[File]("features_desc") action{ (x,c) =>
        if(!x.exists()){
          System.err.println("error: file "+x.toString+" doesn't exist")
          System.exit(-1)
        }
        c.copy(features_desc = x)
      } valueName ("<features_conf.yaml>") required()
      
      opt[Int]("kernel_degree") action{ (x,c) =>
        c.copy(kernel_degree = x)
      } valueName ("<Int>") required()
      
      opt[File]("out_dir") action{ (x,c) =>
        if(x.exists()){
          System.err.println("error: directory "+x.toString+" exist")
          System.exit(-1)
        }
        c.copy(out_dir = x)
      } valueName ("<dir>") required()
      
      opt[Int]("threads") action{ (x,c) =>
        c.copy(threads = x)
      } valueName ("<Int>")
      
    }

    parser.parse(args, TrainConfig()) match {
      case Some(config) =>
        
        doTheWork(config)
        
      case None         =>
        System.exit(-1)
    }
  }
  
  private def doTheWork(config:TrainConfig) : Unit = {


    /////////////////////////////////////////////////////////////////////////////

    System.err.println("setting number of threads to "+config.threads)
    EvaluationApp.setParallelismGlobally(config.threads)

    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("creating model directory "+config.out_dir.toString())
    config.out_dir.mkdirs()
    
    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("loading training and validations human judgments")
    val train_judgments = loadData(config.train_data_type, config.train_data_loc)
    val valid_judgments = loadData(config.valid_data_type, config.valid_data_loc)

    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("selecting the training and validation pairwise comparisons")
    val selector = new PairsSelector(min_agreement = 0.6, min_judgments = 1)
    var train_selected = selector.selectPairs(train_judgments)
    var valid_selected = selector.selectPairs(valid_judgments)
    
    /////////////////////////////////////////////////////////////////////////////
    
    // System.err.println("making it smaller for testing purposes")
    // train_selected = train_selected.take(1000)
    // valid_selected = valid_selected.take(30)

    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("extracting features from training data")
    val featureTemplateLoc = config.out_dir.toString()+"/feature_template.yaml"
    copy(get(config.features_desc.toString), get(featureTemplateLoc), REPLACE_EXISTING)
    val featureExtractor = FeatureExtractor.constructFromFile(featureTemplateLoc)
    val startTime = System.currentTimeMillis();
    val train_selected_featurized = train_selected.map{featureExtractor.extractFeatures_for_WMT_pair}
    val valid_selected_featurized = valid_selected.map{featureExtractor.extractFeatures_for_WMT_pair}
    val extractionCount = (train_selected_featurized.size + valid_selected_featurized.size)*2
    val endTime   = System.currentTimeMillis();
    val extractionTime = (endTime-startTime)/1000;
    System.err.println(s"extracted $extractionCount in $extractionTime s")

    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("creating feature name mapping")
    val nameMapper = FeatureNameMapping.trainModelFromWMTpairs(train_selected_featurized)
    nameMapper.saveModel(config.out_dir.toString()+"/feature_names_mapping")
    val train_name_mapped = train_selected_featurized.map{nameMapper.mapWMTpair}
    val valid_name_mapped = valid_selected_featurized.map{nameMapper.mapWMTpair}
    
    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("creating feature name mapping")
    val scaler = FeatureScaling.trainModelFromWMTpairs(train_name_mapped)
    scaler.saveModel(config.out_dir.toString()+"/feature_values_scaling")
    val train_pairs_scaled = train_name_mapped.map{scaler.scaleWMTpair}
    val valid_pairs_scaled = valid_name_mapped.map{scaler.scaleWMTpair}

    /////////////////////////////////////////////////////////////////////////////

    System.err.println("grid search")
    // in liblinear for linear model c_begin, c_end, c_step = -5,5,1
    // in liblinear for explicit expansion of degree 2 kernel parameters are
    //      c_begin, c_end, c_step = -3,10,2
    //      g_begin, g_end, g_step = -5,7,2
    val Cparams = if(config.kernel_degree == 1) (-15, 1, 5)  else (-10,2,10)
    val gammaParams = if(config.kernel_degree == 1) (1, 1, 1) else (-7, 1, 15)
    val grid = new GridSearch(
        polyDegree=config.kernel_degree,
        gammaParams=gammaParams,
        Cparams=Cparams,
        out_dir=config.out_dir+"/grid_search")
    val bestClassifier = grid.search(train_pairs_scaled, valid_pairs_scaled)
    bestClassifier.saveModel(config.out_dir.toString+"/self_train_models/model_0")
    
    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println("loading nbest")
    var nbestRaw : Map[String, List[(String, List[String])]] = null
    if(config.nbest_loc != null){
      nbestRaw = WMT09syscomb.load(config.nbest_loc.toString, config.nbest_topAndBottomK)
      for((lang, langData) <- nbestRaw){
        val sentCount = langData.size
        val hypCount = langData.map{_._2.size}.sum
        val avgHyps = if(sentCount == 0) 0.0 else hypCount.toDouble/sentCount
        System.err.println(s"nbest $lang has $sentCount sents and $hypCount hyps ($avgHyps hyps per sent)")
      }
    }

    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println(s"self training")
    
    var currClassifier = bestClassifier
    
    for(selfIter <- 1 to config.self_train_iters){
      System.err.println(s"self training iter $selfIter")
    
      val selfTrainNBests:List[(String, List[(String, Double)])] = nbestRaw("en").map{ case (ref, syss) =>
        val scoredSyss = syss.map{ sys =>
          val feat1 = featureExtractor.extractFeatures(sys, ref)
          val feat2 = nameMapper.mapNames(feat1)
          val feat3 = scaler.scale(feat2)
          val score = currClassifier.rawScore(feat3)
          (sys, score)
        }.sortBy(_._2)
        (ref, scoredSyss)
      }
      val topAndBottomToTake = math.sqrt(config.self_train_training_size/selfTrainNBests.size).toInt+1

      var wmtData = List[FeaturesPair]()
      for((ref, nbest) <- selfTrainNBests){
        // val scoredNbest = nbest.map{ entry => (currClassifier.rawScore(entry), entry) }.sortBy(_._1)
        val bottomEntries = nbest.take(topAndBottomToTake)
        val topEntries = nbest.reverse.take(topAndBottomToTake)
        for(win <- topEntries){
          for(los <- bottomEntries){
            val los_feat1 = featureExtractor.extractFeatures(los._1, ref)
            val los_feat2 = nameMapper.mapNames(los_feat1)
            val los_feat3 = scaler.scale(los_feat2)
            val win_feat1 = featureExtractor.extractFeatures(win._1, ref)
            val win_feat2 = nameMapper.mapNames(win_feat1)
            val win_feat3 = scaler.scale(win_feat2)
            wmtData ::= new FeaturesPair(win_feat3, los_feat3)
          }
        }
      }
      currClassifier = Classifier.train(currClassifier.gamma, currClassifier.C, currClassifier.d, wmtData)
      currClassifier.saveModel(config.out_dir.toString+"/self_train_models/model_"+selfIter)
    }
    currClassifier.saveModel(config.out_dir.toString)
    
    /////////////////////////////////////////////////////////////////////////////
    
    System.err.println(s"Scaling")

    if(config.absolute_data_loc != ""){
      System.err.println("Training SVR scaling")
      
      val judgments = WMT13absolute.loadJudgments(config.absolute_judgments_loc, config.absolute_data_loc)
      val data:List[(Double, Double)] = judgments.map{case (sys, ref, human) => 
        val feat1 = featureExtractor.extractFeatures(sys, ref)
        val feat2 = nameMapper.mapNames(feat1)
        val feat3 = scaler.scale(feat2)
        val rawScore = currClassifier.rawScore(feat3)
        (rawScore, human)
      }
      System.err.println("there are "+judgments.size+" absolute judgments")
      System.err.println("minimal rawScore\t="+data.map(_._1).min)
      System.err.println("maximal rawScore\t="+data.map(_._1).max)
      System.err.println("minimal humanScore\t="+data.map(_._2).min)
      System.err.println("maximal humanScore\t="+data.map(_._2).max)
      saveTheLinearScalingCSV(data, config.out_dir.toString+"/correlation.csv")
      val outputScaler = LinearRegressionScaling.trainFromRawScores(data)
      outputScaler.saveModel(config.out_dir.toString)
    }else{
      System.err.println("Training Platt scaling")
      
      var sentProcessed = 0
      
      val trainingValues:List[Double] = List("de", "fr", "cz", "es", "en").flatMap { lang =>
        nbestRaw(lang).par.flatMap{ case (ref, syss) =>
          syss.map{ sys =>
            val feat1 = featureExtractor.extractFeatures(sys, ref)
            val feat2 = nameMapper.mapNames(feat1)
            val feat3 = scaler.scale(feat2)
            val rawScore = currClassifier.rawScore(feat3)
            rawScore
          }
        }
      }

      //var referencesCountForPlatt = 0
      //var trainingValues = List[Double]()
      //for(lang <- List("de", "fr", "cz", "es", "en")){
      //  
      //  nbestRaw(lang).foreach{ case (ref, syss) =>
      //    sentProcessed += 1
      //    if(sentProcessed % 10 == 0){
      //      System.err.println(s"processed $sentProcessed")
      //    }
      //    referencesCountForPlatt += 1
      //  
      //    syss.foreach{ sys =>
      //      val feat1 = featureExtractor.extractFeatures(sys, ref)
      //      val feat2 = nameMapper.mapNames(feat1)
      //      val feat3 = scaler.scale(feat2)
      //      val rawScore = currClassifier.rawScore(feat3)
      //      trainingValues ::= rawScore
      //    }
      //  }
      //}
      
      val plattScalingInstances = 100 // referencesCountForPlatt
      val plattScaler = PlattScaling.trainFromRawScores(trainingValues, plattScalingInstances)
      plattScaler.saveModel(config.out_dir.toString)
    }
    
    /////////////////////////////////////////////////////////////////////////////

    System.err.println("done")
  }
  
//#DONE load training data
//#DONE load validation data
//#DONE select pairs for training
//#DONE select pairs for validation
//#DONE extract features for training
//#DONE extract features for testing
//#DONE mapping of features
//#DONE train scaling of features on training datapackage Metric::Amstel::App::TrainApp;
//
//
//#DONE scale features of training and validation data
//#DONE memory cleaning
//#DONE grid search:
//#DONE 	for each gamma
//#DONE 		for each C
//#DONE 			kernelize training instances
//#DONE 			make PRO training instances
//#DONE 			train classifier
//#DONE 			check on validation set (kernelize before of course)
//#DONE	  until here
//#DONE	load new n-best lists
//#DONE self-training:
//#	find all their features, scale and kernelize
//#	for iteration in 0..10 do
//#		score n-best lists
//#		for best and worst 5 create training pairs
//#		train
//#DONE find the best 2 and worst 2 for each sentence in each language pair
//#DONE train Platt scaling (with the best C&gamma) on them
//#DONE save it in some config file
 	


  private def loadData(dataType:String, dataLoc:File) : List[Judgment] = {
    dataType match {
      case "wmt11" => WMT11.loadJudgments(dataLoc.toString())
      case "wmt13" => WMT13.loadJudgments(dataLoc.toString())
      case "wmt14" => WMT14.loadJudgments(dataLoc.toString())
    }
  }

  private def saveTheLinearScalingCSV(f:List[(Double, Double)], fn:String) : Unit = {
    val pw = new PrintWriter(fn)
    
    pw.println("rawScore,humanScore")
    for((rawScore, humanScore) <- f){
      pw.println(s"$rawScore,$humanScore")
    }
    
    pw.close()
  }
}
