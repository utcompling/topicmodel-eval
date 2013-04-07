package tmeval

trait TopicModelEvaluator extends (Array[Int] => Double)

class KalmanEvaluator(model: TopicModel) extends TopicModelEvaluator {

  val numtopics = model.topicweights.length

  // Normalize topic weights
  val topicWeightsSum = model.topicweights.sum
  val normalizedTopicWeights = model.topicweights.map(_/topicWeightsSum)

  /**
   * Using imperative style because while loops are faster. :(
   */
  def apply(document: Array[Int]) = {

    var priorweights = normalizedTopicWeights.clone

    // First observation
    val firstProfile = model.typeProfile(document(0))

    val ll = Array.fill(numtopics)(0.0)

    var topicIndex = 0
    while(topicIndex < numtopics) {
      ll(topicIndex) = priorweights(topicIndex) * firstProfile(topicIndex)
      topicIndex += 1
    }
    var firstSll = ll.sum
    var loglike = math.log(firstSll)

    val z = Array.fill(numtopics)(0.0)
    topicIndex = 0
    while(topicIndex < numtopics) {
      z(topicIndex) = ll(topicIndex)/firstSll
      topicIndex += 1
    }

    var tokenId = 1
    while (tokenId < document.length) {

      topicIndex = 0
      while(topicIndex < numtopics) {
        priorweights(topicIndex) = model.topicweights(topicIndex) + z(topicIndex)
        topicIndex += 1
      }
      
      val priorWeightSum = priorweights.sum
      topicIndex = 0
      while(topicIndex < numtopics) {
        priorweights(topicIndex) /= priorWeightSum
        topicIndex += 1
      }

      val profile = model.typeProfile(document(tokenId))
      topicIndex = 0
      while(topicIndex < numtopics) {
        ll(topicIndex) = priorweights(topicIndex) * profile(topicIndex)
        topicIndex += 1
      }

      val sll = ll.sum
      val normalizedLL = ll.map(_/sll)
      topicIndex = 0
      while(topicIndex < numtopics) {
        z(topicIndex) += normalizedLL(topicIndex)
        topicIndex += 1
      }

      loglike += math.log(sll)
      tokenId += 1
    }
    loglike
  }

}

class LeftToRightEvaluator(model: TopicModel, numparticles: Int = 10) 
extends TopicModelEvaluator {

  import breeze.stats.distributions.Multinomial
  import breeze.linalg.DenseVector
  
  val numtopics = model.topicweights.length

  // Normalize topic weights
  val topicWeightsSum = model.topicweights.sum
  val normalizedTopicWeights = model.topicweights.map(_/topicWeightsSum)

  def apply(document: Array[Int]) = {

    val gamma = Array.fill(numparticles, document.length)(1)

    // First observation: we only have the prior
    val priorweights = normalizedTopicWeights.clone
    val firstProfile = model.typeProfile(document(0))

    val postweights = Array.fill(numtopics)(0.0)
    var topicIndex = 0
    while (topicIndex < numtopics) {
      postweights(topicIndex) = priorweights(topicIndex) * firstProfile(topicIndex)
      topicIndex += 1
    }

    val loglike = Array.fill(document.length)(0.0)
    loglike(0) = math.log(postweights.sum)
    
    // Sweep through the document
    var tokenId = 1
    while (tokenId < document.length) {

     // loop over particles
      var llt = 0.0
      var particleId = 0
      while (particleId < numparticles) {
	// count up the topic allocations
	val littlegamma = gamma(particleId).slice(0,tokenId) // for easy bookkeeping
        val z = Array.fill(numtopics)(0.0)
        
        var prefixTokenId = 0
        while (prefixTokenId < tokenId) {
          z(littlegamma(prefixTokenId)) += 1
          prefixTokenId += 1
        }

	// Now sample the current topic allocation
        topicIndex = 0
        while (topicIndex < numtopics) {
          priorweights(topicIndex) = model.topicweights(topicIndex) + z(topicIndex)
          topicIndex += 1
        }

        val pwSum = priorweights.sum
        topicIndex = 0
        while (topicIndex < numtopics) {
          priorweights(topicIndex) /= pwSum
          topicIndex += 1
        }

        val profile = model.typeProfile(document(tokenId))
        topicIndex = 0
        while (topicIndex < numtopics) {
          postweights(topicIndex) = priorweights(topicIndex) * profile(topicIndex)
          topicIndex += 1
        }

        gamma(particleId)(tokenId) = Multinomial(DenseVector(postweights)).draw

	// Add this particle's contribution to the step-i likelihood estimate
	llt += postweights.sum
        particleId += 1
      }
      llt = llt/numparticles
      loglike(tokenId) = math.log(llt)
      tokenId += 1
    }
    loglike.sum
  }

}

