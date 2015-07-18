package beer.ingredients

import beer.Configuration
import beer.alignment.Aligner.SentencePair
import edu.stanford.nlp.tagger.maxent.MaxentTagger
import edu.stanford.nlp.parser.nndep.DependencyParser
import edu.stanford.nlp.process.DocumentPreprocessor
import edu.stanford.nlp.trees.GrammaticalStructure
import java.io.StringReader
import edu.stanford.nlp.ling.TaggedWord
import edu.stanford.nlp.ling.IndexedWord

class DependencyMatching (configuration:Configuration, paramsArg:Map[String, Object]) extends SparseIngredient {

  val featureName: String = "Dep"
  
  private val tagger = DependencyMatching.buildTagger()
  private val parser = DependencyMatching.buildParser()

  def eval(sp:SentencePair) : Map[String, Double] = {
    val sys = sp.sys.mkString(" ")
    val (sysTags, sysParse) = DependencyMatching.parse(sys, tagger, parser)

    val ref = sp.ref.mkString(" ")
    val (refTags, refParse) = DependencyMatching.parse(ref, tagger, parser)
    
    val arcFeatureValues:Map[String, Double] = computeArcFeatureValues(sysParse, sys.size, refParse, ref.size)
    val posFeatureValues:Map[String, Double] = computePOSfeatureValues(sysTags, refTags)
    val valencyFeatureValues:Map[String, Double] = computeValencyFeatureValues(sysParse, sys.size, refParse, ref.size)
    val depBigramFeatureValues:Map[String, Double] = computeDepBigramMatchFeatureValues(sysParse, sys.size, refParse, ref.size)

    arcFeatureValues ++ posFeatureValues ++ valencyFeatureValues ++ depBigramFeatureValues  
  }
 
 
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
  private def computeValencyFeatureValues(sysTree:GrammaticalStructure, sysSize:Int, refTree:GrammaticalStructure, refSize:Int) : Map[String, Double] = {
    val sysValencyTuples = findAllValencyTuples(sysTree)
    val refValencyTuples = findAllValencyTuples(refTree)
    overlapFeatures(featureName+":valency", sysValencyTuples, sysSize, refValencyTuples, refSize)
  }
  
  private def findAllValencyTuples(tree:GrammaticalStructure) : collection.mutable.Map[(String, Int), Int] = {
    val deps = tree.allTypedDependencies()
    val it = deps.iterator()
    val valencyTuples = collection.mutable.Map[(String, Int), Int]().withDefaultValue(0)
    
    val valencyCounts = collection.mutable.Map[IndexedWord, Int]().withDefaultValue(0)
    
    while(it.hasNext()){
      val dep = it.next()
      val headWord = dep.gov().word()
      valencyCounts(dep.gov()) += 1
    }
    
    for((k, v) <- valencyCounts) {
      valencyTuples((k.word(), v)) += 1
    }
    
    valencyTuples
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private def computeDepBigramMatchFeatureValues(sysTree:GrammaticalStructure, sysSize:Int, refTree:GrammaticalStructure, refSize:Int) : Map[String, Double] = {
    val sysDepBigrams = findAllDepBigrams(sysTree)
    val refDepBigrams = findAllDepBigrams(refTree)
    overlapFeatures(featureName+":depBigram", sysDepBigrams, sysSize, refDepBigrams, refSize)
  }
  
  private def findAllDepBigrams(tree:GrammaticalStructure) : collection.mutable.Map[(String, String), Int] = {
    val deps = tree.allTypedDependencies()
    val it = deps.iterator()
    val lexicalizedDepenenciesCounts = collection.mutable.Map[(String, String), Int]().withDefaultValue(0)

    while(it.hasNext()){
      val dep = it.next()
      // val headPOS  = dep.gov().tag()
      val headWord = dep.gov().word()
      // val dependentPOS  = dep.dep().tag()
      val dependentWord = dep.dep().word()
      // val arcType = dep.reln().getShortName()
      
      val bigram = (headWord, dependentWord)
      lexicalizedDepenenciesCounts(bigram) += 1
    }
    
    lexicalizedDepenenciesCounts
    
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def computePOSfeatureValues(sysTags:java.util.List[TaggedWord], refTags:java.util.List[TaggedWord]) : Map[String, Double] = {
    val sysTagBigrams = findAllPOSbigrams(sysTags)
    val refTagBigrams = findAllPOSbigrams(refTags)
    overlapFeatures(featureName + ":posBigram", sysTagBigrams, sysTags.size, refTagBigrams, refTags.size)
  }
  
  private def overlapFeatures[A](
      featPrefix:String,
      sysMap:collection.mutable.Map[A, Int], sysNormalizer:Int,
      refMap:collection.mutable.Map[A, Int], refNormalizer:Int) : Map[String, Double] = {
    val sysMatch:Int = sysMap.keys.map{refMap.getOrElse(_, 0)}.sum
    val refMatch:Int = refMap.keys.map{sysMap.getOrElse(_, 0)}.sum
    
    val p = sysMatch.toDouble/sysNormalizer
    val r = refMatch.toDouble/refNormalizer
    val f = 2*p*r/(p+r)

    Map(
        featPrefix+":precision" -> p,
        featPrefix+":recall" -> r,
        featPrefix+":fScore" -> f
    )
  }

  
  private def findAllPOSbigrams(tags:java.util.List[TaggedWord]) : collection.mutable.Map[(String, String), Int] = {
    val it = tags.iterator()
    val tagBigrams = collection.mutable.Map[(String, String), Int]().withDefaultValue(0)
    var prevTag = "START"
    while(it.hasNext()){
      val currentTag = it.next().tag()
      val bigram = (prevTag, currentTag)
      tagBigrams(bigram) += 1
      prevTag = currentTag
    }
    tagBigrams
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private def computeArcFeatureValues(sysTree:GrammaticalStructure, sysSize:Int, refTree:GrammaticalStructure, refSize:Int) : Map[String, Double] = {
    val features = collection.mutable.Map[String, Double]().withDefaultValue(0.0)
    val sysLexMap = getLexicalizedMap(sysTree)
    val refLexMap = getLexicalizedMap(refTree)
    
    val sysArcCount = sysLexMap.values.sum
    val refArcCount = refLexMap.values.sum
    
    for(arc <- sysLexMap.keys){
      if(refLexMap(arc) > 0){
        val shortDesc = featureName+":ArcMatched:"+arc.substring(0, arc.indexOf(":-:-:"))
        features(shortDesc) += 2.0/(sysArcCount + refArcCount)
      }else{
        val shortDesc = featureName+":ArcSysWrong:"+arc.substring(0, arc.indexOf(":-:-:"))
        features(shortDesc) += 1.0/sysArcCount
      }
    }

    for(arc <- refLexMap.keys){
      if(sysLexMap(arc) == 0){
        val shortDesc = featureName+":ArcRefWrong:"+arc.substring(0, arc.indexOf(":-:-:"))
        features(shortDesc) += 1.0/refArcCount
      }
    }
    
    features.toMap
  }
  
  
  private def getLexicalizedMap(tree:GrammaticalStructure) : collection.mutable.Map[String, Int] = {
    val deps = tree.allTypedDependencies()
    val it = deps.iterator()
    val lexicalizedDepenenciesCounts = collection.mutable.Map[String, Int]().withDefaultValue(0)

    while(it.hasNext()){
      val dep = it.next()
      // val headPOS  = dep.gov().tag()
      val headWord = dep.gov().word()
      // val dependentPOS  = dep.dep().tag()
      val dependentWord = dep.dep().word()
      val arcType = dep.reln().getShortName()
      
      val desc = s"$arcType:-:-:$headWord:$dependentWord"
      lexicalizedDepenenciesCounts(desc) += 1
    }
    
    lexicalizedDepenenciesCounts
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

object DependencyMatching {
  
  def buildTagger(location:String = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger") : MaxentTagger = {
    new MaxentTagger(location)
  }
  
  def buildParser(location:String = DependencyParser.DEFAULT_MODEL) : DependencyParser = {
    DependencyParser.loadFromModelFile(location)
  }
  
  def parse(sent:String, tagger:MaxentTagger, parser:DependencyParser) : (java.util.List[TaggedWord], GrammaticalStructure) = {
    val prepSent = new DocumentPreprocessor(new StringReader(sent)).iterator().next();
    val tagged = tagger.tagSentence(prepSent);
    val gs = parser.predict(tagged);

    (tagged, gs)
  }
  
}
