package beer.permutation.finders

import beer.alignment.Aligner.{PhrasePair, SentencePair}

object HandleUnaligned {

  /**
   * @return permutation over reference that starts from 0
   */

  val monotonePermutation = List(1,2)
  val invertedPermutation = List(2,1)

  def insertUnaligned(nullStrategy:String, sorted:List[Int], unaligned:List[Int]) : List[Int] = {
    nullStrategy match {
      case "attach left"          =>
        unaligned.foldLeft(sorted)(insertUnalignedNumberLeft (_,_))
      case "attach right"         =>
        unaligned.foldLeft(sorted)(insertUnalignedNumberRight(_,_))
      case "ignore and normalize" => {
        unaligned.foldLeft(sorted.map{(_,0)})(normalize(_,_)).map{case(x,y)=>x-y}
      }
    }
  }

  private def normalize(p:List[(Int,Int)], u:Int) : List[(Int,Int)] = {
    p.map{case (element,substract) => if(element>u) (element, substract + 1) else (element,substract)}
  }

  private def insertUnalignedNumberLeft(p:List[Int], i:Int) : List[Int] = {
    val t = p.filter(_<i).sorted.reverse

    if(t.size == 0){
      i::p
    }else{
      val attachTo = t.head
      p.map(x => if(x==attachTo) List(x, i) else List(x)).flatten
    }
  }

  private def insertUnalignedNumberRight(p:List[Int], i:Int) : List[Int] = {
    val t = p.filter(_>i).sorted

    if(t.size == 0){
      p :+ i
    }else{
      val attachTo = t.head
      p.map(x => if(x==attachTo) List(i, x) else List(x)).flatten
    }
  }
  
  def updateSentencePairWithNulls(sp:SentencePair) : SentencePair = {
    val refNulls = findRefNulls(sp)
    val sysNulls = findSysNulls(sp)
    sp.copy(a = sp.a++refNulls++sysNulls)
  }

  def findRefNulls(sp:SentencePair) : List[PhrasePair] = {

    /// add ref nulls
    var refNulls = List[PhrasePair]()

    val sortedByRef = sp.a.sortBy(_.refStart)
    var expectedRefPos = 0
    for(pp <- sortedByRef){
      if(pp.refStart != expectedRefPos){
        val missingPP = PhrasePair(-1, -1, expectedRefPos, pp.refStart-1, -1, 0)
        refNulls ::= missingPP
      }
      expectedRefPos = pp.refEnd + 1
    }
    val refN = sp.ref.size
    if(expectedRefPos != refN){
      val missingPP = PhrasePair(-1, -1, expectedRefPos, refN-1, -1, 0)
      refNulls ::= missingPP
    }

    refNulls
  }

  def findSysNulls(sp:SentencePair) : List[PhrasePair] = {
    /// add sys nulls
    var sysNulls = List[PhrasePair]()

    val sortedBySys = sp.a.sortBy(_.sysStart)
    var expectedSysPos = 0
    for(pp <- sortedBySys){
      if(pp.sysStart != expectedSysPos){
        val missingPP = PhrasePair(expectedSysPos, pp.sysStart-1, -1, -1, -1, 0)
        sysNulls ::= missingPP
      }
      expectedSysPos = pp.sysEnd + 1
    }
    val sysN = sp.sys.size
    if(expectedSysPos != sysN){
      val missingPP = PhrasePair(expectedSysPos, sysN-1, -1, -1, -1, 0)
      sysNulls ::= missingPP
    }


    sysNulls
  }

}
