package beer.learningToRank

import beer.Configuration
import java.io.{ ObjectOutputStream, FileOutputStream }
import weka.classifiers.{ Classifier => WekaClassifier }
import weka.core.{Instance, DenseInstance, SparseInstance, Instances,
                  FastVector, Attribute }
import beer.io.Log
import weka.core.OptionHandler

class Weka(configuration:Configuration) extends Learner with PRO {
  
  private val wekaClassName:String = configuration.modelConfig("brewer").asInstanceOf[Map[String, Object]]("params").asInstanceOf[Map[String, String]]("classifier").trim()
  private val classifierOptions = configuration.modelConfig("brewer").asInstanceOf[Map[String, Object]]("params").asInstanceOf[Map[String, String]]("classifierParams")
  
  private var classifier : WekaClassifier = null // Class.forName(wekaClassName).getConstructors()(0).newInstance().asInstanceOf[WekaClassifier]
  
  private def standardDataSetFormat() : Instances = {
    val instanceSize = featureMapping.size+1

    val attributes = new java.util.ArrayList[Attribute](instanceSize)
    for(i <- 1 to instanceSize) attributes.add(null)

    featureMapping.foreach{ case (string:String, integer:Int) =>
      attributes.set(integer, new Attribute(string))
    }

    val classValues = new java.util.ArrayList[String](2)
    for(i <- 1 to 2) classValues.add(null)
    classValues.set(0, "negative")
    classValues.set(1, "positive")
    val classAttribute = new Attribute("classAttribute", classValues)

    attributes.set(instanceSize-1, classAttribute)

    val instances = new Instances("TrainingData", attributes, 0)
    instances.setClassIndex(instanceSize-1)
    
    instances
  }
  
  protected def train(data:List[(Map[Int, Double], Map[Int, Double], Double)]) : Unit = {
    val instanceSize = featureMapping.size+1

    val instances = standardDataSetFormat()

    val sparseData = data.flatMap{ case (winnerRawFeatures, loserRawFeatures, weight) =>
      val rawInstances = extractOneTrainingPair(winnerRawFeatures, loserRawFeatures)
      
      rawInstances.map{ rawInstance =>
        val instance = new SparseInstance(instanceSize)
        instance.setDataset(instances)
        rawInstance._1.foreach{case (k,v) => instance.setValue(k, v)}
        // instance.setValue(classAttribute, if(rawInstance._2) "positive" else "negative")
        instance.setClassValue(if(rawInstance._2) "positive" else "negative")
        instance.setWeight(weight)
        instance
      }
    }

    // val instances = new Instances("TrainingData", attributes, sparseData.size)
    sparseData.foreach{ instance => instances.add(instance) }

    classifier = Class.forName(wekaClassName).getConstructors()(0).newInstance().asInstanceOf[WekaClassifier]
    val options = weka.core.Utils.splitOptions(classifierOptions)
    classifier.asInstanceOf[OptionHandler].setOptions(options)
    classifier.buildClassifier(instances)
  }
  
  protected def save(modelDir:String) : Unit = {

    System.err.println(s"\nSAVING MODEL LOCATED IN $modelDir\n")

    val oos = new ObjectOutputStream(new FileOutputStream(s"$modelDir/model"));
    oos.writeObject(classifier);
    oos.flush();
    oos.close();
  }
  
  protected def load(modelDir:String) : Unit = {
    classifier = weka.core.SerializationHelper.read(s"$modelDir/model").asInstanceOf[WekaClassifier]
    // System.err.println(classifier.toString())
  }
  
  protected def classify(features:Map[Int, Double]) : Double = {
    val instances = standardDataSetFormat()
    val instance:Instance = convertToInstance(features)
    instance.setDataset(instances)
    
    val distribution = classifier.distributionForInstance(instance)
    distribution(1)
  }
  
  private def convertToInstance(features:Map[Int, Double]) : Instance = {
    val instanceSize = featureMapping.size+1
    //val instanceSize = features.size+1
    val instance = new DenseInstance(instanceSize)
    
    features.toList.sortBy(_._1).foreach{ case (fId:Int, value:Double) =>
      instance.setValueSparse(fId, value)
    }
    
    instance
  }
  

}