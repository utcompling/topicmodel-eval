package tmeval

/**
 * An object that sets up the configuration for command-line options using
 * Scallop and returns the options, ready for use.
 */
object CorpusExperimentOpts {

  import org.rogach.scallop._
  
  def apply(args: Array[String]) = new ScallopConf(args) {
    banner("""
For usage see below:
	     """)
    val help = opt[Boolean]("help", noshort = true, descr = "Show this message")
    val repetitions = opt[Int]("repetitions", default=Some(10), validate = (0<), descr="The number of samples from the posterior to obtain for each dataset.")
    val numTopics = opt[Int]("num-topics", default=Some(100), validate = (0<), descr="The number of topics to use in the model.")
    val datasets = Set("20news","gutenberg","nyt","pcl-travel","reuters","sgu","all")
    val dataset = opt[String](
      "dataset", default=Some("all"), validate = datasets,
      descr="The dataset to use. Possible values: " + datasets.toSeq.sorted.mkString(",") 
        + ". The 'all' dataset means to run on all datasets found in data/extracted.")
    val output = opt[String]("output", default=Some("stdout"), descr="The file to save the model as. If left unspecified, it will write to standard output.")

  }
}


/**
 * Train and evaluate topic models on text corpora in the data/extracted
 * directory. (This is generated from files in data/orig by running the
 * "bin/tmeval prepare" command.)
 *
 * @author jasonbaldridge
 */
object CorpusExperiment {

  import java.io._
  import cc.mallet.topics._
  import cc.mallet.types._
  import cc.mallet.pipe.iterator._

  import scala.collection.JavaConversions._

  val extractDir = new File(Constants.TMEVAL_DIR, "data/extracted")

  def main (args: Array[String]) {
    
    // Parse and get the command-line options
    val opts = CorpusExperimentOpts(args)

    // Get the datasets: one can either specify a single dataset, or grab all
    // the datasets in the data/extracted directory.
    val datasets = opts.dataset() match {
      case "all" => extractDir.listFiles.map(_.getName)
      case singleDataset => Array(singleDataset)
    }

    // Set up the output writer for producing the final CSV formatted results
    val outputWriter = opts.output() match {
      case "stdout" => new PrintWriter(System.out)
      case file => new PrintWriter(new BufferedWriter(new FileWriter(new File(file))))
    }

    // Run on all datasets and write the results to the desired output stream.
    println("Running experiments on: " + datasets.mkString(" "))
    datasets
      .toIterator
      .flatMap(runOneDataset(_, opts.numTopics(), opts.repetitions()))
      .foreach {
        results => 
          outputWriter.write(results.mkString(",") + "\n")
          outputWriter.flush
      }

    outputWriter.close

  }

  /**
   * Run a repeated experiment for one dataset and return the results.
   */
  def runOneDataset(
    dataset: String, 
    numTopics: Int = 100, 
    numRepetitions: Int = 10): Array[Array[String]] = {

    val (trainDir, evalDir) = getFiles(dataset)

    val pipeline = MalletUtil.createPipeline

    // Get the training instances
    val trainingInstances = new InstanceList(pipeline)
    val trainingFiles = new FileIterator(Array(trainDir), FileIterator.STARTING_DIRECTORIES, true)
    trainingInstances.addThruPipe(trainingFiles)

    // Get the evaluation instances
    val evalInstances = new InstanceList(pipeline)
    val evalFiles = new FileIterator(Array(evalDir), FileIterator.STARTING_DIRECTORIES, true)
    evalInstances.addThruPipe(evalFiles)

    val results = 
      (0 until numRepetitions)
        .map(repetitionId => trainAndEval(numTopics, trainingInstances, evalInstances))
        .toArray
        .transpose

    val evalTypes = Array("Kalman", "L2R(1)", "L2R(50)", "Mallet-L2R(50)")
    //println("Dataset: " + dataset)
    for ((etype, llResults) <- evalTypes.zip(results)) yield {
      val average = llResults.sum/numRepetitions
      Array(dataset,etype,average.toString) ++ llResults.map(_.toString)
    }
  }

  /**
   * Train a model using Mallet, and then evaluate it using different methods.
   */ 
  def trainAndEval(
    numTopics: Int, 
    trainingInstances: InstanceList, 
    evalInstances: InstanceList
  ) = {

    val lda = new ParallelTopicModel(numTopics, numTopics/10, 0.01)

    lda.printLogLikelihood = false
    lda.setTopicDisplay(500, 10)
    lda.addInstances(trainingInstances)
    lda.setNumThreads(4)
    lda.numIterations = 1000
    lda.estimate

    println(lda.displayTopWords(10, false))

    val mlda = new MalletLdaModel(
        lda.numTopics, lda.alpha, lda.alphaSum, lda.beta, 
        lda.typeTopicCounts, lda.tokensPerTopic)

    println("Kalman eval.")
    val kalmanEval = new KalmanEvaluator(mlda)
    val u1 = System.currentTimeMillis
    val llKalman = evalInstances
      .map(instance => instance.getData.asInstanceOf[FeatureSequence].getFeatures)
      .map(kalmanEval)
      .sum
    val u2 = System.currentTimeMillis
    println("  LL: " + llKalman)
    println("  Time: " + (u2-u1))
    println

    println("Our L2R(1) eval.")
    val v1 = System.currentTimeMillis
    val ourL2REvaluator1 = new LeftToRightEvaluator(mlda, 1)
    val llOurL2R1 = evalInstances
      .map(instance => instance.getData.asInstanceOf[FeatureSequence].getFeatures)
      .map(ourL2REvaluator1)
      .sum
    val v2 = System.currentTimeMillis
    println("  Log-likelihood: " + llOurL2R1)
    println("  Time: " + (v2-v1))
    println

    println("Our L2R(50) eval.")
    val w1 = System.currentTimeMillis
    val ourL2REvaluator50 = new LeftToRightEvaluator(mlda, 50)
    val llOurL2R50 = evalInstances
      .map(instance => instance.getData.asInstanceOf[FeatureSequence].getFeatures)
      .map(ourL2REvaluator50)
      .sum
    val w2 = System.currentTimeMillis
    println("  Log-likelihood: " + llOurL2R50)
    println("  Time: " + (w2-w1))
    println

    // Do left-to-right evaluation
    val l2rEval = 
      new MarginalProbEstimator(
        lda.numTopics, lda.alpha, lda.alphaSum, lda.beta, 
        lda.typeTopicCounts, lda.tokensPerTopic)

    println("L2R eval.")
    val t1 = System.currentTimeMillis
    val llL2RMallet = l2rEval.evaluateLeftToRight(evalInstances, 50, false, null)
    //val llL2RMallet = l2rEval.evaluateLeftToRight(evalInstances, 50, true, null)
    val t2 = System.currentTimeMillis
    println("  LL: " + llL2RMallet)
    println("  Time: " + (t2-t1))
    println

    Array(llKalman, llOurL2R1, llOurL2R50, llL2RMallet)
  }

  private def getFile(dataset: String, subset: String) =
    new File(extractDir, dataset+"/"+subset)

  private def getFiles(dataset: String) = 
    (getFile(dataset, "train"), getFile(dataset, "eval"))


}
