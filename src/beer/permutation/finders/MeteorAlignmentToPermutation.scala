package beer.permutation.finders

import beer.alignment.Aligner.{PhrasePair, SentencePair}

object MeteorAlignmentToPermutation {

  def convertMeteorToPermutation(nullStrategy:String, sp:SentencePair) : List[Int] = {

    val sorted:List[Int] = sp.a.sortBy(_.sysStart).map{x =>
      Stream.
        from(x.refStart).
        takeWhile(_ <= x.refEnd).
        toList
    }.flatten

    val refNulls = HandleUnaligned.findRefNulls(sp)
    val refNullNumbers = refNulls.map{x =>
      Stream.
        from(x.refStart).
        takeWhile(_ <= x.refEnd).
        toList
    }.flatten

    HandleUnaligned.insertUnaligned(nullStrategy, sorted, refNullNumbers)
  }

}
