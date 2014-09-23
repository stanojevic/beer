package beer

import org.scalatest.{FlatSpec, ShouldMatchers}
import java.io.PrintStream
import java.io.FileOutputStream

class EvaluationTest extends FlatSpec with ShouldMatchers {
  
  "testing reording metric PEF" should "not crash" in {

    val modelType = "evaluation"
    val workingMode = "evaluateReordering"
    val system = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified1"
    val reorderingMetric = "PEFrecursive"
    val reference = "/home/milos/multeval/newstest2014-ref.en-ru"
    val lang = "ur"
    val arg = s"-l $lang --modelType $modelType -s $system -r $reference --workingMode $workingMode --reorderingMetric $reorderingMetric --printSentScores"
    // Evaluation.main(arg split " ")
  }
  
  "testing metric on sent level" should "not crash" in {

    val modelType = "evaluation"
    val workingMode = "evaluation"
    val system = "data/en-ru/newstest2014.onlineA.0.en-ru"
    val reference = "/home/milos/multeval/newstest2014-ref.en-ru"
    val lang = "ur"
    val arg = s"-l $lang --modelType $modelType -s $system -r $reference --workingMode $workingMode --printSentScores"
    Evaluation.main(arg split " ")
  }
  
  "testing metric on corp level" should "not crash" in {

    val modelType = "evaluation"
    val workingMode = "evaluation"
    val system = "data/en-ru/newstest2014.rbmt1.0.en-ru"
    val reference = "/home/milos/multeval/newstest2014-ref.en-ru"
    val lang = "ur"
    val arg = s"-l $lang --modelType $modelType -s $system -r $reference --workingMode $workingMode"
    Evaluation.main(arg split " ")
  }
  
  "testing sign testing" should "not crash" in {
    val modelType = "evaluation"
    val workingMode = "signTest"

    val system1 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified1"
    val system2 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified6"
    val system3 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified4"

    val system4 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified2"
    val system5 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified3"
    val system6 = "/home/milos/multeval/newstest2014.PROMT-Hybrid.3080.en-ru_modified5"
    
    val system7 = "/home/milos/multeval/newstest2014.onlineB.0.en-ru"

    val reference = "/home/milos/multeval/newstest2014-ref.en-ru"
    val lang = "ur"
      
    //val arg = s"-l $lang --modelType $modelType --system1Files $system1:$system2:$system3 --system2Files $system4:$system5:$system6 -r $reference --workingMode $workingMode"
    val arg = s"-l $lang --modelType $modelType --system1Files $system1 --system2Files $system7 -r $reference --workingMode $workingMode"
    //Evaluation.main(arg split " ")
  }

  "testing evaluation" should "not crash" in {
    val modelType = "evaluationANN"
    // val modelType = "evaluation"
    val workingMode = "evaluate"
    val lang = "ru"

    println("Evaluating winner")
    val referenceFile = "data/game/SENTENCE_LEVEL_REFERENCE"
    val winnerFile  = "data/game/SENTENCE_LEVEL_WINNER"
    val argWinner = s"-l $lang --modelType $modelType -s $winnerFile -r $referenceFile --workingMode $workingMode"
    // Evaluation.main(argWinner split " ")

    println("Evaluating loser")
    val loserFile   = "data/game/SENTENCE_LEVEL_LOSER"
    val argLoser = s"-l $lang --modelType $modelType -s $loserFile -r $referenceFile --workingMode $workingMode"
    // Evaluation.main(argLoser split " ")

    val system1 = "data/en-ru/newstest2014.PROMT-Rule-based.3081.en-ru"
    val system2 = "data/en-ru/newstest2014.rbmt1.0.en-ru"
    val system3 = "data/en-ru/newstest2014.uedin-unconstrained.3445.en-ru"
    val system4 = "data/en-ru/newstest2014.onlineB.0.en-ru"
    val reference = "data/en-ru/newstest2014-ref.en-ru"
    // val system = "data/game/SENTENCE_LEVEL_WINNER"
    // val reference1 = "data/game/SENTENCE_LEVEL_REFERENCE"
    // val reference2 = "data/game/SENTENCE_LEVEL_LOSER"
      
    println(s"Evaluating $system1")
    val arg = s"-l $lang --modelType $modelType -s $system1 -r $reference --workingMode $workingMode --verbose"
    // Evaluation.main(arg split " ")
    println(s"Evaluating $system2")
    val arg2 = s"-l $lang --modelType $modelType -s $system2 -r $reference --workingMode $workingMode --verbose"
    //Evaluation.main(arg2 split " ")
    println(s"Evaluating $system3")
    val arg3 = s"-l $lang --modelType $modelType -s $system3 -r $reference --workingMode $workingMode --verbose"
    //Evaluation.main(arg3 split " ")
    println(s"Evaluating $system4")
    val arg4 = s"-l $lang --modelType $modelType -s $system4 -r $reference --workingMode $workingMode --verbose"
    // Evaluation.main(arg4 split " ")
    System.out.println(modelType)
  }
  
  "testing without arguments" should "not crash" in{
    val args = s"--help"

    // Evaluation.main(args split " ")
  }
  
  "testing training" should "not crash" in {
    //val modelType = "evaluation"
    val modelType = "ru_func_exact_para_stem_RankNet"
    val workingMode = "train" 
    val modelDescFile = "templates/func_exact_para_stem_RankNet.yaml"

    val winnerFeaturesFile  = "data/game/SENTENCE_LEVEL_WINNER.features"
    val loserFeaturesFile   = "data/game/SENTENCE_LEVEL_LOSER.features"
    val lang = "ru"
      
    val arg = s"-l $lang --modelType $modelType --trainingLoserFeaturesFile $loserFeaturesFile --trainingWinnerFeaturesFile $winnerFeaturesFile --workingMode $workingMode --modelDescFile $modelDescFile"
    // Evaluation.main(arg split " ")
  }

  "testing feature extraction" should "not crash" in {

    val winnerTextFile = "data/game/SENTENCE_LEVEL_WINNER"
    val loserTextFile = "data/game/SENTENCE_LEVEL_LOSER"
    val referenceTextFile = "data/game/SENTENCE_LEVEL_REFERENCE"
    val lang = "ru"
      
    val modelType = "evaluation"

    val workingMode = "factors"

    val originalSTDOUT = System.out
    
    val winArg = s"-l $lang --modelType $modelType -s $winnerTextFile -r $referenceTextFile --workingMode $workingMode"
    // Evaluation.main(winArg split " ")
    
    val losArg = s"-l $lang --modelType $modelType -s $loserTextFile -r $referenceTextFile --workingMode $workingMode"
    // Evaluation.main(losArg split " ")
  }

}