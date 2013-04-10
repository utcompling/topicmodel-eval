library(gtools)

# Now try to evaluate the hold-old likelihood
PLloglike = function(y, B, topicweights, numparticles = 100)
{
	K = ncol(B)
	N = length(y)
	loglike = rep(0,N)
	Z = matrix(0, nrow=numparticles, ncol=K)
	particleweights=rep(0,numparticles)
	propweights = Z
	# First observation
	priorweights = topicweights
	priorweights = priorweights/sum(priorweights)
	ll = priorweights*B[y[1],]
	loglike[1] = log(sum(ll))
	for(t in 1:numparticles)
	{
		k = sample(1:K, 1, prob=ll)
		Z[t,k] = Z[t,k] + 1
		#Z[t,] = Z[t,] + rmultinom(1,1,ll)
	}
	for(i in 2:N)
	{
		# Resample
		llt = 0
		for(t in 1:numparticles)
		{
			# compute one-step look-ahead likelihood
			priorweights = topicweights + Z[t,]
			priorweights = priorweights/sum(priorweights)
			ll = priorweights*B[y[i],]
			propweights[t,] = ll/sum(ll)
			particleweights[t] = sum(ll)
			llt = llt + log(sum(ll))
		}
		llt = llt/numparticles
		loglike[i] = llt
		myindices = sample(1:numparticles, size=numparticles, replace=TRUE, prob=particleweights)
		Z = Z[myindices,]
		propweights = propweights[myindices,]
		# Propagate
		for(t in 1:numparticles)
		{
			Z[t,] = Z[t,] + rmultinom(1,1,propweights[t,])
		}
	}
	return(loglike)
}


# # # Now try to evaluate the hold-old likelihood
# plwithchib = function(y, B, topicweights, numparticles = 100)
# {
	# K = ncol(B)
	# N = length(y)
	# loglike = rep(0,N)
	# Z = matrix(0, nrow=numparticles, ncol=K)
	# particleweights=rep(0,numparticles)
	# propweights = Z
	# # First observation
	# priorweights = topicweights
	# priorweights = priorweights/sum(priorweights)
	# ll = priorweights*B[y[1],]
	# loglike[1] = log(sum(ll))
	# for(t in 1:numparticles)
	# {
		# k = sample(1:K, 1, prob=ll)
		# Z[t,k] = Z[t,k] + 1
		# #Z[t,] = Z[t,] + rmultinom(1,1,ll)
	# }
	# for(i in 2:N)
	# {
		# # Resample
		# llt = 0
		# for(t in 1:numparticles)
		# {
			# # compute one-step look-ahead likelihood
			# priorweights = topicweights + Z[t,]
			# priorweights = priorweights/sum(priorweights)
			# ll = priorweights*B[y[i],]
			# propweights[t,] = ll/sum(ll)
			# particleweights[t] = sum(ll)
			# llt = llt + log(sum(ll))
		# }
		# llt = llt/numparticles
		# loglike[i] = llt
		# myindices = sample(1:numparticles, size=numparticles, replace=TRUE, prob=particleweights)
		# Z = Z[myindices,]
		# propweights = propweights[myindices,]
		# # Propagate
		# for(t in 1:numparticles)
		# {
			# Z[t,] = Z[t,] + rmultinom(1,1,propweights[t,])
		# }
	# }
	
	# zmeans = colMeans(Z)
	# zhat = zmeans + topicweights
	# fhat = zhat/sum(zhat)
	# loglikehat = 0
	# for(i in 1:N)
	# {
		# ll = fhat*B[y[i],]
		# loglikehat = loglikehat + log(sum(ll))
	# }
	
	# Gamma = matrix(0, nrow=numparticles, ncol=N)
	# phatf = rep(0, numparticles)
	# for(t in 1:numparticles)
	# {
		# z = Z[t,]
		# alphahat = z + topicweights
		# f = rdirichlet(1,alphahat)
		# for(i in 1:N)
		# {
			# ll = f*B[y[i],]
			# Gamma[t,i] = sample(1:K,1,prob=ll)
		# }
		# thisa = summary(factor(Gamma[t,], levels=1:K)) + topicweights
		# phatf[t] = ddirichlet(fhat, thisa)
	# }
	# chibloglike = loglikehat + log(ddirichlet(fhat, topicweights)) - log(mean(phatf))
	# list(plloglike = sum(loglike), chibloglike = chibloglike)
# }



# A simple Kalman-filter-like approximation
kalmanll = function(y, B, topicweights)
# topicweights is the parameter of a Dirichlet prior over topics within a document
# B is a (dictionarysize x numtopics) matrix of topic parameter; each column is a multinomial
{
	N = length(y)
	K = length(topicweights)
	# First observation
	priorweights = topicweights
	priorweights = priorweights/sum(priorweights)
	ll = priorweights*B[y[1],]
	loglike = log(sum(ll))
	z = ll
	for(i in 2:N)
	{
		priorweights = topicweights + z
		priorweights = priorweights/sum(priorweights)
		ll = priorweights*B[y[i],]
		sll = sum(ll)
		z = z + ll/sll
		loglike = loglike + log(sll)
	}
	list(loglike=loglike, allocations = z)
}


# The left-to-right algorithm of Wallach, 2009
lefttoright = function(y, B, topicweights, numparticles = 20, resample=FALSE)
# topicweights is the parameter of a Dirichlet prior over topics within a document
# B is a (dictionarysize x numtopics) matrix of topic parameter; each column is a multinomial
{
	N = length(y)
	K = length(topicweights)
	gamma = matrix(1,nrow=numparticles, ncol=N)
	
	# First observation: we only have the prior
	priorweights = topicweights
	priorweights = priorweights/sum(priorweights)
	postweights = priorweights*B[y[1],]
	gamma[,1] = sample(1:K, numparticles, replace=TRUE, prob=postweights)
	llt = sum(postweights)
	
	loglike = rep(0,N)
	loglike[1] = log(llt)
	
	# Sweep through the document
	for(i in 2:N)
	{
		# loop over particles
		#cat(i, "\t")	# if you want a print out to watch the progress
		llt = 0
		for(r in 1:numparticles)
		{
			# count up the topic allocations
			littlegamma = gamma[r,1:(i-1)]	# this is just for easy bookkeeping
			z = as.numeric(summary(factor(littlegamma, levels=1:K)))
			if(resample) {
			# Now do a Gibbs pass over this particle's previous allocations, 1 ... i-1
			for(j in 1:{i-1})
			{
				# Take word j away from the vector of counts
				k = littlegamma[j]
				z[k] = z[k] - 1
				# Calculate the weights over topic allocations, given y[j] and gamma[-j]
				priorweights = topicweights + z
				priorweights = priorweights/sum(priorweights)
				postweights = priorweights*B[y[j],]
				littlegamma[j] = sample(1:K, 1, prob=postweights)
				# Now add the count back in
				k = littlegamma[j]
				z[k] = z[k] + 1
			}
			gamma[r,1:(i-1)] = littlegamma
			}
			
			# Now sample the current topic allocation
			priorweights = topicweights + z
			priorweights = priorweights/sum(priorweights)
			postweights = priorweights*B[y[i],]
			gamma[r,i] = sample(1:K, 1, prob=postweights)
			
			# Add this particle's contribution to the step-i likelihood estimate
			llt = llt + sum(postweights)
		}
		llt = llt/numparticles
		loglike[i] = log(llt)
	}
	return(loglike)
}

montecarloML = function(y, B, topicweights, nsamples = 1000)
# just evaluates the marginal likelihood using draws from the prior
{
	N = length(y)
	K = length(topicweights)
	f = matrix(0,nrow=nsamples, ncol=K)
	ll = rep(0, nsamples)
	for(t in 1:nsamples)
	{
		f[t,] = rdirichlet(1, topicweights)
		for(i in 1:N)
		{
			ll[t] = ll[t] + log(sum(f[t,]*B[y[i],]))
		}
	}
	c = max(ll)
	return(c+log(mean(exp(ll-c))))
}

