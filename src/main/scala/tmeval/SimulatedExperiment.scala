package tmeval

/**
 * Run a simulated experiment on larger documents than were used with the R code.
 *
 * @author jasonbaldridge
 */
object LargeSimulatedExperiment {

  import java.io._

  def main (args: Array[String]) {
    val Array(numTopics, vocabSize, numDocs, docLength) = args.take(4).map(_.toInt)

    val outputFile = args(4)

    val outputWriter = 
      new PrintWriter(new BufferedWriter(new FileWriter(new File(outputFile))))

    val numRepetitions = 2
    
    val results = 
      (0 until numRepetitions)
        .map(repetitionId => trainAndEval(numTopics,vocabSize,numDocs,docLength))
        .toArray
        .transpose

    val evalTypes = Array("Kalman", "L2R(1)", "L2R(50)")
    println("Settings: [topics=" + numTopics + "] [vocabsize=" + vocabSize + "]" + "] [numdocs=" + numDocs + "]" + "] [doclength=" + docLength + "]")
    var id = 0
    for ((etype, llResults) <- evalTypes.zip(results)) {
      outputWriter.write("Run"+id + "," + etype + "," + llResults.sum/numRepetitions + "," + llResults.mkString(",")+"\n")
      id += 1
    }
    outputWriter.close
  }

  def trainAndEval(numTopics: Int, vocabSize: Int, numDocs: Int, docLength: Int) = {

    val tm = new SimulatedTopicModel(numTopics, vocabSize)

    println("Creating eval documents.")
    val docs = (1 to numDocs).map(i=>tm.generateDocument(docLength))

    println("Kalman eval.")
    val kalmanEval = new KalmanEvaluator(tm)
    val u1 = System.currentTimeMillis
    val llKalman = docs.map(kalmanEval).sum
    val u2 = System.currentTimeMillis
    println("  LL: " + llKalman)
    println("  Time: " + (u2-u1))
    println

    println("Our L2R(1) eval.")
    val v1 = System.currentTimeMillis
    val ourL2REvaluator1 = new LeftToRightEvaluator(tm, 1)
    val llOurL2R1 = docs.map(ourL2REvaluator1).sum
    val v2 = System.currentTimeMillis
    println("  Log-likelihood: " + llOurL2R1)
    println("  Time: " + (v2-v1))
    println

    println("Our L2R(50) eval.")
    val w1 = System.currentTimeMillis
    val ourL2REvaluator50 = new LeftToRightEvaluator(tm, 50)
    val llOurL2R50 = docs.map(ourL2REvaluator50).sum
    val w2 = System.currentTimeMillis
    println("  Log-likelihood: " + llOurL2R50)
    println("  Time: " + (w2-w1))
    println

    Array(llKalman, llOurL2R1, llOurL2R50)
  }
}

