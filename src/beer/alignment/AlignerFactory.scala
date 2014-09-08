package beer.alignment

import beer.Configuration
import beer.io.Log

object AlignerFactory {
  
  def createAligner(alignerClassName:String, configuration:Configuration) : Aligner = {
    Log.println(configuration, s"Loading $alignerClassName . . . ")
    val aligner = 
        Class.forName(alignerClassName).getConstructors()(0).newInstance(configuration).asInstanceOf[Aligner]
    Log.println(configuration, "DONE")
    aligner
  }

}