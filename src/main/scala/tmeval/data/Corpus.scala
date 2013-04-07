package tmeval.data

/**
 * Wrapper app that extracts the five datasets that come with the code.
 *
 * @author jasonbaldridge
 */ 
object PrepareCorpora extends App {
  
  println("Extracting four datasets: 20 news groups, Gutenberg, "
          + "SGU transcripts, PCL Travel, and Reuters. This will take some time "
          + "(perhaps 20-30 minutes).")

  TwentyNewsGroupsPreparer.extract()
  GutenbergPreparer.extract()
  SguTranscriptPreparer.extract()
  PclTravelPreparer.extractParagraphs()
  ReutersPreparer.extract()

}

/**
 * Collects the basic properties of the corpora, e.g. the size of the vocab,
 * the number of tokens, the average length of the documents. Note that these values
 * are obtained after filtering for stopwords.
 *
 * @author jasonbaldridge
 */ 
object CorpusStats {

  import java.io._
  import cc.mallet.topics._
  import cc.mallet.types._
  import cc.mallet.pipe.iterator._
  import scala.collection.JavaConversions._
  import tmeval.Constants.TMEVAL_DIR

  val tmevalDir = new File(TMEVAL_DIR)
  val extractDir = new File(tmevalDir, "data/extracted")

  private def getFile(dataset: String, subset: String) =
    new File(extractDir, dataset+"/"+subset)

  private def getFiles(dataset: String) = 
    (getFile(dataset, "train"), getFile(dataset, "eval"))

  def main(args: Array[String]) {
    if (extractDir.exists) {
      val datasets = extractDir.listFiles.map(_.getName)
      val stats = datasets.map(getStats)
      println
      stats.foreach(x => println(x.mkString(",")))
    } else {
      println("No corpora extracted. Run 'bin/tmeval prepare' and"
              + " and then run corpus-stats again.")
    }
  }

  def getStats (dataset: String) = {
    
    val (trainDir, evalDir) = getFiles(dataset)
    println("Getting info for " + dataset)

    // Get the training instances
    val pipeline = getPipeline
    val trainingInstances = new InstanceList(pipeline)
    val trainingFiles = new FileIterator(Array(trainDir), FileIterator.STARTING_DIRECTORIES, true)
    trainingInstances.addThruPipe(trainingFiles)

    // Get the evaluation instances
    val evalInstances = new InstanceList(pipeline)
    val evalFiles = new FileIterator(Array(evalDir), FileIterator.STARTING_DIRECTORIES, true)
    evalInstances.addThruPipe(evalFiles)

    val allInstances = trainingInstances ++ evalInstances

    val vocabSize = trainingInstances.getDataAlphabet.toArray.length
    println("Vocab size: " + vocabSize)

    //evalInstances.getDataAlphabet.toArray.foreach(println)
    val lengths = allInstances.map(_.getData.asInstanceOf[FeatureSequence].getFeatures.length).toList
    val numDocs = lengths.length
    val numTokens = lengths.sum
    println("Num tokens: " + numTokens)

    val avgLength = numTokens/numDocs
    println("Avg length: " + avgLength)

    val stdDeviation = math.sqrt(lengths.map(l => l-avgLength).map(x=>x*x).map(_/numDocs).sum)
    println("Std deviation: " + stdDeviation)

    println(List(dataset, vocabSize, numTokens, avgLength, stdDeviation).mkString(","))

    List(dataset, vocabSize, numTokens, avgLength, stdDeviation)
  }

  def getPipeline = {
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


object CorpusUtil {

  import java.io._

  /**
   * Create a non-validating XML parser.
   */
  def getNonValidatingXmlParser = {
    val parserFactory = javax.xml.parsers.SAXParserFactory.newInstance
    parserFactory.setValidating(false)
    parserFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    parserFactory.newSAXParser
  }

  /**
   * Write some string to a file in a given directory.
   */
  def writeString(outputDir: File, filename: String, contents: String) {
    val articleFile = new File(outputDir, filename+".txt") 
    val writer = new PrintWriter(new BufferedWriter(new FileWriter(articleFile)))
    writer.write(contents)
    writer.close
  }


}
