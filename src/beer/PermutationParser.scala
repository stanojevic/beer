package beer

import scala.io.Source
import beer.permutation.pet.parser.ShiftReduce
import beer.permutation.pet.parser.ChartParser
import beer.permutation.pet.representation.ChartDefinition.Chart
import beer.permutation.pet.representation.ChartDefinition.{NonTerm => ChartNonTerm, Term => ChartTerm}
import beer.permutation.pet.representation.TreeNode
import beer.permutation.pet.representation.{NonTerm, Term}
import beer.permutation.pet.representation.ChartDefinition.ChartEntry
import java.io.PrintStream
import java.io.File

object PermutationParser {
  
  case class Config(
      chart:Boolean = false,
      outputFormat:String = "visual"
      )
  
  def main(args:Array[String]) : Unit = {
    val parser = new scopt.OptionParser[Config]("BEER's permutation parser") {
      head("BEER's permutation parser", beer.Constants.VERSION.toString)

      help("help") text ("prints this usage text")

      version("version") text ("prints the current version")

      opt[String]("outputFormat") action { (x,c) =>
        c.copy(outputFormat = x)
      } text ("output format for the parse result : Penn(default), DerivationRules, dot(not implemented yet)")

    }
    
    parser.parse(args, this.Config()).map{ config =>
      if(config.chart && config.outputFormat != "DerivationRules"){
        System.err.println("you cannot output chart with any format other than DerivationRules")
        System.exit(-1)
      }

      for (ln <- Source.fromInputStream(System.in, "UTF-8").getLines){
        val permutation = ln.split(" +").toList.map{_.toInt}

        // println("permutation "+permutation.mkString(" "))
      
        if(permutation.min != 1){
          throw new Exception("permutations start from 1")
        }
        if(permutation.max != permutation.size){
          throw new Exception("not a valid permutation")
        }
        
        if(config.chart){
          val chart = ChartParser.shiftReduceChartParse(permutation)
          printChart(chart)
        }else{
          val tree = ShiftReduce.parse(permutation)
          config.outputFormat match{
            case "Penn" =>
              printPennTree(tree)
            case "qtree" =>
              printQTree(tree)
            case "dot"  =>
              printDotTree(System.out, tree, permutation)
            case "visual" =>
              val file = File.createTempFile("visual", "")
              file.deleteOnExit()
              val tmpFileName = file.getPath()
              val pw = new PrintStream(s"$tmpFileName.dot")
              printDotTree(pw, tree, permutation)
              pw.close()

              val dotCmd = s"dot -Tpng $tmpFileName.dot -O"
              val pDot = Runtime.getRuntime().exec(dotCmd);
              pDot.waitFor()
              new File(s"$tmpFileName.dot.png").deleteOnExit()

              val xdgCmd = System.getProperty("os.name") match {
                case "Linux" => s"xdg-open $tmpFileName.dot.png"
                case _       => s"open $tmpFileName.dot.png"
              }
              val pXdg = Runtime.getRuntime().exec(xdgCmd);
            case "DerivationRules" =>
              printDerivationRulesTree(tree)
            case _ =>
              System.err.println(config.outputFormat+" unsuported format")
              System.exit(-1)
          }
        }
        println()
      }
      
    }
    
  }

  private def printQTree(tree:TreeNode, depth:Int=0) : Unit = {
    if(depth == 0){
      print("\\Tree ")
    }
    for(i <- 0 until depth)
      print("  ")
    tree match {
      case NonTerm(start, end, min, max, operator, children) =>
        print("[.$"+opToStr(operator)+"$\n")
        for(child <- children)
          printQTree(child, depth+1)
        for(i <- 0 until depth)
          print("  ")
        print(" ]")
      case Term(position, el) =>
        print(el)
    }
    println()
  }

  private def printPennTree(tree:TreeNode, depth:Int=0) : Unit = {
    for(i <- 0 until depth)
      print("  ")
    tree match {
      case NonTerm(start, end, min, max, operator, children) =>
        print("("+opToStr(operator)+"\n")
        for(child <- children)
          printPennTree(child, depth+1)
        for(i <- 0 until depth)
          print("  ")
        print(")")
      case Term(position, el) =>
        print(s"[$el]")
    }
    println()
  }
  
  private def opToStr(op:List[Int]) : String = {
    "<"+op.mkString(",")+">"
  }
  
  private def printDotTree(pw:PrintStream, tree:TreeNode, permutation:List[Int], nodeId:String="node0") : Unit = {
    val colorMapping = Map[String, String](
        "<1,2>" -> "green3",
        "<2,1>" -> "firebrick1"
        ).withDefaultValue("blue")
    val terminalColor = "lightblue2"

    tree match {
      case NonTerm(start, end, min, max, operator, children) =>
        if(nodeId == "node0"){
          pw.println("graph { ")
        }
    
        val opString = opToStr(operator)
        pw.println(nodeId+"[label=\""+opString+"\"; fontcolor="+colorMapping(opString)+"; style=bold];")
        children.zipWithIndex.foreach{ case (child, index) =>
          child match {
            case NonTerm(start, end, min, max, operator, children) =>
              printDotTree(pw, child, permutation, nodeId+index)
              pw.println(nodeId+" -- "+(nodeId+index)+" ;")
            case Term(position, el) =>
              pw.println(nodeId+" -- term"+position+" ;")
          }
        }

        if(nodeId == "node0"){
          pw.println("subgraph {rank=same;")
          permutation.zipWithIndex.foreach{ case (el, pos) =>
            pw.println("  term"+(pos+1)+"[shape=box; label="+el+"; color="+terminalColor+"];")
          }
          pw.println("edge[style=\"invis\"];")
          pw.println("  "+(1 to permutation.size).map{"term"+_}.mkString(" -- ") + " ; ")
          pw.println("} ")
          pw.println("} ")
        }

      case Term(position, el) =>
    }
    
  }
    
  private def printDerivationRulesTree(tree:TreeNode) : Unit = {
    tree match {
      case NonTerm(start, end, min, max, operator, children) =>
        val lhs = opToStr(operator)
        val lhs_span = s"{$start-$end}"
        
        val childStrs = children.map {
          case NonTerm(childStart, childEnd, min, max, childOperator, children) =>
            s"{$childStart-$childEnd}"
          case Term(position, el) =>
            s"{$position-$position}"
        }
        
        println(s"$lhs $lhs_span -> "+childStrs.mkString(" "))
        
        for(child <- children)
          printDerivationRulesTree(child)
      case Term(position, el) =>
        println(s"[$el] {$position-$position}")
    }

  }
  
  private def printChart(chart:Chart) : Unit = {
    val n = chart.size
    for(i <- 0 until n){
      for(j <- i until n){
        if(chart(i)(j) != null){
          chart(i)(j) match {
            case ChartNonTerm(operator, inferences) =>
              val lhs = opToStr(operator)
              val lhs_span = s"{$i-$j}"
              for(inference <- inferences){
                val newInf = List(i) ++ inference ++ List(j+1)
                val spans = newInf.sliding(2)
                val childStr = spans.map{case List(x,y) => s"{$x-"+(y-1)+"}"}.mkString(" ")
                println(s"$lhs $lhs_span -> $childStr")
              }
            case ChartTerm(element) =>
              println(s"[$element] {$i-$j}")
          }
        }
      }
    }
  }

}