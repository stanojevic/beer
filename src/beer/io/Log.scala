package beer.io

object Log {
  
  def print(conf: beer.Configuration, msg:String) : Unit = {
    if(conf.arguments.verbose)
      System.err.print(msg)
  }

  def println(conf: beer.Configuration, msg:String) : Unit = {
    if(conf.arguments.verbose)
      System.err.println(msg)
  }

}