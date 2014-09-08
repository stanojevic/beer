package beer.ingredients

import beer.Configuration
import beer.io.Log

object IngredientsFactory {
  
  def createIngredient(configuration:Configuration, ingredient:String, params:Object) : SparseIngredient = {

    Log.println(configuration, s"Loading $ingredient . . . ")

    val sparseIngredient = 
        Class.forName(ingredient).getConstructors()(0).newInstance(configuration, params).asInstanceOf[SparseIngredient]

    Log.println(configuration, "DONE")

    sparseIngredient
  }

}
