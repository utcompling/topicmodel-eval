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
