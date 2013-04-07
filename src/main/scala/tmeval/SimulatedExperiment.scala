package tmeval

object Perplexity {

  def main (args: Array[String]) {
    println("Creating model.")
    val tm = new SimulatedTopicModel(200,10000)

    //val doc = tm.generateDocument()
    //println("Kalman: " + KalmanEvaluator(tm,doc))
    //println("Left-to-right: " + new LeftToRightEvaluator(20)(tm,doc))

    println("Creating eval documents.")
    val numDocs = 100
    val docs = (1 to numDocs).map(i=>tm.generateDocument())

    println("Kalman eval." )
    val t1 = System.currentTimeMillis
    val kalmanEval = new KalmanEvaluator(tm)
    println(" LL: " + docs.map(kalmanEval).sum)
    val t2 = System.currentTimeMillis
    println(" Time: " + (t2-t1))

    println("L2R:" )
    val u1 = System.currentTimeMillis
    val l2rEval = new LeftToRightEvaluator(tm, 10)
    println(" LL: " + docs.map(l2rEval).sum)
    val u2 = System.currentTimeMillis
    println(" Time: " + (u2-u1))
    
  }

}

object SimulatedExperiment {

  def main (args: Array[String]) {
    val Array(numTopics, vocabSize, numDocs, docLength) = args.map(_.toInt)

    val numRepetitions = 10
    
    val results = 
      (0 until numRepetitions)
        .map(repetitionId => trainAndEval(numTopics,vocabSize,numDocs,docLength))
        .toArray
        .transpose

    val evalTypes = Array("Kalman", "Our-L2R(1)", "Our-L2R(50)", "Mallet-L2R(50)")
    println("Settings: [topics=" + numTopics + "] [vocabsize=" + vocabSize + "]" + "] [numdocs=" + numDocs + "]" + "] [doclength=" + docLength + "]")
    evalTypes.zip(results).foreach { case (etype, llResults) => {
      println(etype + "," + llResults.sum/numRepetitions + "," + llResults.mkString(","))
    }}

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

