package beer.learning

import math.exp
import sys.process._
import java.util.Properties
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.PrintWriter
import java.io.File
// import de.bwaldvogel.liblinear.Problem
// import de.bwaldvogel.liblinear.Feature
// import de.bwaldvogel.liblinear.FeatureNode
// import de.bwaldvogel.liblinear.Parameter
// import de.bwaldvogel.liblinear.SolverType
// import de.bwaldvogel.liblinear.Linear

/**
 * @author milos
 */
class PlattScaling (val A:Double, val B:Double) extends OutputScaler {
  
  def scale(rawScore:Double) : Double = {
    val x = rawScore*A+B
    if(x >= 0.0){
      exp(-x)/(1.0+exp(-x))
    }else{
      1.0/(1.0+exp(x))
    }
  }
  
  def saveModel(modelDir:String) : Unit = {
    val props = new Properties()
    props.setProperty("A", A.toString())
    props.setProperty("B", B.toString())
    props.store(new FileOutputStream(s"$modelDir/platt_scaling.properties"), "Platt scaling 1.0/(1.0+exp(score*A+B))")
  }
  
}

object PlattScaling{

//  def trainFromValuePairsLiblinear(pairs : List[(Double, Int)]) : PlattScaling = {
//    val problem = new Problem()
//    problem.l = pairs.size
//    problem.n = 1
//    problem.bias = 1
//    problem.y = new Array[Double](problem.l)
//    problem.x = new Array[Array[Feature]](problem.l)
//    var instanceId = 0
//    for((score, label) <- pairs){
//      problem.y(instanceId) = label
//      problem.x(instanceId) = Array[Feature](new FeatureNode(1, score))
//      
//      instanceId += 1
//    }
//    
//    val C = 0.01
//    val parameter = new Parameter(SolverType.L2R_LR, C, 0.01)
//    val model = Linear.train(problem, parameter)
//    
//    model.save(new File("platt_liblinear"))
//    
//    null
//  }
  
  def trainFromRawScores(rawScores : List[Double], topAndBottomSamples:Int) : PlattScaling = {
    
    val sortedRawScores = rawScores.sorted
    val negativeScores = sortedRawScores.take(topAndBottomSamples)
    val positiveScores = sortedRawScores.reverse.take(topAndBottomSamples)
    
    val file = File.createTempFile("platt_scaling_", ".tmp")
    //file.deleteOnExit()
    System.err.println(file.toString())
    val pw = new PrintWriter(file)
    for(rawScore <- positiveScores){
      pw.println(s"$rawScore +1")
    }
    for(rawScore <- negativeScores){
      pw.println(s"$rawScore -1")
    }
    pw.close()
    
    val script = "lib/platt.py"
    val cmd = script+" "+file.toString()
    val res = cmd !!
    
    val fields = res.substring(1, res.size-2).split(", ")
    val A = fields(0).toDouble
    val B = fields(1).toDouble
    
    new PlattScaling(A, B)
  }
  
  def loadModel(modelDir:String) : PlattScaling = {
    val props = new Properties()
    props.load(new FileInputStream(s"$modelDir/platt_scaling.properties"))
    val A = props.getProperty("A").toDouble
    val B = props.getProperty("B").toDouble
    
    new PlattScaling(A,B)
  }
  
}
