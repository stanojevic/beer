package beer.learning

import beer.data.judgments.FeaturesPair
import java.io.File
import java.io.PrintWriter

/**
 * @author milos
 */
class GridSearch (polyDegree:Int, gammaParams:(Int,Int,Int), Cparams:(Int,Int,Int), out_dir:String) {
  
  def search(trainData:List[FeaturesPair], validData:List[FeaturesPair]) : Classifier = {
    
    new File(out_dir).mkdirs()
    
    if(polyDegree == 1 && gammaParams._1 != gammaParams._3){
      System.err.println("if polynomial is 1 you need only one (any) fake gamma")
      System.exit(-1)
    }
    
    System.err.println("starting grid search with "+trainData.size+" training pairs")
    System.err.println("starting grid search with "+validData.size+" validation pairs")
    
    val classifiers = (Cparams._1 to Cparams._3 by Cparams._2).par.flatMap{ Cdegree =>
      (gammaParams._1 to gammaParams._3 by gammaParams._2).par.map{ gammaDegree =>
        val C = math.pow(2, Cdegree)
        val gamma = math.pow(2, gammaDegree)

        System.err.println(s"started training Cdeg=$Cdegree gDeg=$gammaDegree")
        
        val classifier = Classifier.train(gamma, C, polyDegree, trainData)
        
        val goodness = validScore(classifier, validData)

        System.err.println(s"pre results $goodness C=$Cdegree g=$gammaDegree")

        (goodness, Cdegree, gammaDegree, classifier)
      }
    }
    
    val pw = new PrintWriter(out_dir+"/validation_scores.csv")
    pw.println("correlation C_degree gamma_degree")
    classifiers.foreach{ case (goodness, cDeg, gDeg, classifier) =>
        val desc = out_dir+"/model_"+cDeg+"_"+gDeg
        classifier.saveModel(desc)
        pw.println(s"$goodness $cDeg $gDeg")
    }
    pw.close()
    
    val bestClassifier = classifiers.maxBy(_._1)._4
    
    bestClassifier
  }
  
  private def validScore(classifier:Classifier, validData:List[FeaturesPair]) : Double = {
    validData.count { pair => 
      val winScore = classifier.rawScore(pair.winner)
      val losScore = classifier.rawScore(pair.loser)
      winScore>losScore
    }.toDouble/validData.size
  }
  
}
