package beer.ingredients

import beer.alignment.Aligner.SentencePair


trait SparseIngredient {
  
  val featureName:String
  
  def eval(sp:SentencePair) : Map[String, Double]

}