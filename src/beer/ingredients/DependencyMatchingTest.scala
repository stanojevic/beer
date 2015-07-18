package beer.ingredients

import org.scalatest.{FlatSpec, ShouldMatchers}
import java.io.PrintStream
import java.io.FileOutputStream
import edu.stanford.nlp.trees.GrammaticalRelation
import edu.stanford.nlp.trees.TypedDependency

class DependencyMatchingTest extends FlatSpec with ShouldMatchers {
  
  "parser" should "parse" in {
    val tagger = DependencyMatching.buildTagger()
    val parser = DependencyMatching.buildParser()
    val sent = "I can almost always tell when movies use fake dinosaurs."
    val (tags, parse) = DependencyMatching.parse(sent, tagger, parser)
    
    val deps = parse.allTypedDependencies()
    val it = deps.iterator()
    while(it.hasNext()){
      val dep:TypedDependency = it.next()
      val rel:GrammaticalRelation = dep.reln()
      val t1 = dep.dep().tag()
      val w1 = dep.dep().word()
      val p1 = dep.dep().lemma()
      val e1 = dep.dep().index()
      println(s"t1=$t1 w1=$w1 p1=$p1 e1=$e1")
      val t2 = dep.gov().tag()
      val w2 = dep.gov().word()
      val p2 = dep.gov().lemma()
      val e2 = dep.gov().index()
      println(s"t2=$t2 w2=$w2 p2=$p2 e2=$e2")
      val ln = rel.getLongName()
      val sn = rel.getShortName()
      println(s"ln=$ln sn=$sn")
      println(dep)
      println(rel)
      println()
    }
    println(sent)

    System.out.println(parse)

  }

}