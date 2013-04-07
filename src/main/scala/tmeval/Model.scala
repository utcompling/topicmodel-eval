package tmeval

trait TopicModel {
  def topicweights: Array[Double]
  def topics: Array[Map[Int,Double]]

  lazy val cachedTypeProfiles = collection.mutable.HashMap[Int,Array[Double]]()

  // Get the probability of a word in each topic. Cache the values we've looked up
  // already to minimize hash lookups. Could be done much better.
  def typeProfile(index: Int) = cachedTypeProfiles.get(index) match {
    case Some(profile) => profile
    case None =>
      val profile = topics.map(_(index))
      cachedTypeProfiles(index) = profile
      profile
  }
}

class SimulatedTopicModel(val numtopics: Int = 50, val numwords: Int = 1000) 
extends TopicModel {

  import breeze.stats.distributions.{Gaussian, Dirichlet, Multinomial}

  val topicweights = new Gaussian(0, 1).sample(numtopics).map(math.exp).toArray
  val topicDistributions = 
    (1 to numtopics).map(k => Multinomial(Dirichlet.sym(0.1,numwords).draw)).toArray

  val topics = topicDistributions.map(d=>d.params.data.zipWithIndex.map(_.swap).toMap)

  def generateDocument(docsize: Int = 500) = { 
    val topicMultinomial = Multinomial(Dirichlet(topicweights).draw)
    (1 to docsize).map(i => topicDistributions(topicMultinomial.draw).draw).toArray
  }
}


class MalletLdaModel (
  numtopics: Int, 
  alpha: Array[Double], 
  alphaSum: Double,
  beta: Double,
  typeTopicCounts: Array[Array[Int]],
  tokensPerTopic: Array[Int]
) extends TopicModel {
  
  val topicMask = 
    if (Integer.bitCount(numtopics) == 1) 
      numtopics - 1
    else // otherwise add an extra bit
      Integer.highestOneBit(numtopics) * 2 - 1

  val topicBits = Integer.bitCount(topicMask)
  val topicweights = alpha

  val topics = {
    val topicTypeCounts = 
      (0 until numtopics).map(i => collection.mutable.HashMap[Int,Double]().withDefault(x=>0.0))
    
    typeTopicCounts.zipWithIndex.map { case(currentTypeTopicCounts,typeIndex) => {
      var index = 0
      while (index < currentTypeTopicCounts.length && currentTypeTopicCounts(index) > 0) {
        val currentTopic = currentTypeTopicCounts(index) & topicMask
        val currentValue = currentTypeTopicCounts(index) >> topicBits
        topicTypeCounts(currentTopic)(typeIndex) += currentValue
        index += 1
      }
    }}

    val numTypes = typeTopicCounts.length 
    val pseudoCounts = numTypes*beta
    
    // Compute the topic distributions, with appropriate default for words 
    // not seen with the topic.
    topicTypeCounts.toArray.map { currentCounts => {
      // Note: currentCounts.values.sum is also available via (and is the 
      // same as) tokensPerTopic.
      val denominator = currentCounts.values.sum + pseudoCounts
      currentCounts
        .mapValues(v => (v+beta)/denominator)
        .toMap
        .withDefault(x=>beta/denominator)
    }}
  }

}


object MalletUtil {

  def createPipeline() = {
    import cc.mallet.pipe._
    import cc.mallet.util.CharSequenceLexer

    val pipeList = new java.util.ArrayList[Pipe]()
    pipeList.add(new Target2Label)
    pipeList.add(new SaveDataInSource)
    pipeList.add(new Input2CharSequence(java.nio.charset.Charset.defaultCharset.displayName))
    pipeList.add(new CharSequence2TokenSequence(CharSequenceLexer.LEX_ALPHA))
    pipeList.add(new TokenSequenceLowercase)
    pipeList.add(new TokenSequenceRemoveStopwords(false, false))
    pipeList.add(new TokenSequence2FeatureSequence)
    new SerialPipes(pipeList)
  }

}

object OutputTopics {
  import java.io._
  import cc.mallet.topics._
  import cc.mallet.types._
  import cc.mallet.pipe.iterator._
  import scala.collection.JavaConversions._


  def main (args: Array[String]) {
    val extractDir = new File(Constants.TMEVAL_DIR, "data/extracted")

    // Parse and get the command-line options
    val opts = CorpusExperimentOpts(args)

    val numTopics = opts.numTopics()

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


    for (dataset <- datasets) {
      val datasetDir = new File(extractDir, dataset)

      // Get the instances
      val allInstances = new InstanceList(MalletUtil.createPipeline)
      val allFiles = new FileIterator(Array(datasetDir), FileIterator.STARTING_DIRECTORIES, true)
      allInstances.addThruPipe(allFiles)

      val lda = new ParallelTopicModel(numTopics, numTopics/10, 0.01)

      lda.printLogLikelihood = false
      lda.setTopicDisplay(500, 10)
      lda.addInstances(allInstances)
      lda.setNumThreads(4)
      lda.numIterations = 1000
      lda.estimate

      outputWriter.write("\n# Topics for " + dataset + "\n")
      outputWriter.write("```\n")
      outputWriter.write(lda.displayTopWords(20, false))
      outputWriter.write("```\n\n")
    }
    outputWriter.close
  }
  
}
