set.seed(19871121)
source("holdoututils.R")

nsims = 25
numtopics = 20
numwords = 1000
docsize = 250
corpuslength = 500

# Sample a bag of words model from the prior
alphadocs = rnorm(numtopics,0,1); alphadocs = exp(alphadocs)
alphawords = rep(1/numtopics,numwords)

B = matrix(0, nrow=numwords, ncol=numtopics)
for(k in 1:numtopics)
{
	B[,k] = rdirichlet(1,alphawords)
}


# Make a test document from the simulated model
y = rep(0, docsize)
f = rdirichlet(1,alphadocs)
for(i in 1:docsize)
{
	k = sample(1:numtopics,1,prob=f)
	y[i] = sample(1:numwords, 1, prob=B[,k])
}


# Small documents with likelihood evaluated at true B
LLsave1 = matrix(0, nrow=nsims, ncol=5)
for(m in 1:nsims)
{
	cat("Sim", m, "\n")
	LLsave1[m,1] = kalmanll(y, B, alphadocs)$loglike
	ll1 = PLloglike(y, B, alphadocs, 100)
	LLsave1[m,2] = sum(ll1)
	ll2 = lefttoright(y, B, alphadocs, 1)
	LLsave1[m,3] = sum(ll2)
	ll3 = lefttoright(y, B, alphadocs, 100)
	LLsave1[m,4] = sum(ll3)
	LLsave1[m,5] = montecarloML(y, B, alphadocs, 1000)
}


# Same B, different orders for words
LLsave2 = matrix(0, nrow=nsims, ncol=5)
y0 = y
for(m in 1:nsims)
{
	y = permute(y0)
	cat("Sim", m, "\n")
	LLsave2[m,1] = kalmanll(y, B, alphadocs)$loglike
	ll1 = PLloglike(y, B, alphadocs, 100)
	LLsave2[m,2] = sum(ll1)
	ll2 = lefttoright(y, B, alphadocs, 1)
	LLsave2[m,3] = sum(ll2)
	ll3 = lefttoright(y, B, alphadocs, 100)
	LLsave2[m,4] = sum(ll3)
	LLsave2[m,5] = montecarloML(y, B, alphadocs, 1000)
}


# Different draws from the same posterior

B = matrix(0, nrow=numwords, ncol=numtopics)
b0 = 0.1
SmoothCounts = B
for(k in 1:numtopics)
{
	B[,k] = rdirichlet(1,alphawords)
	SmoothCounts[,k] = rmultinom(1, size=corpuslength*docsize/numtopics, prob=B[,k]) + b0
}


# Draw a single B from the posterior and make a test document
for(k in 1:numtopics)
{
	B[,k] = rdirichlet(1,SmoothCounts[,k])
}
y = rep(0, docsize)
f = rdirichlet(1,alphadocs)
for(i in 1:docsize)
{
	k = sample(1:numtopics,1,prob=f)
	y[i] = sample(1:numwords, 1, prob=B[,k])
}


# Now every time we draw a new B from the posterior
LLsave3 = matrix(0, nrow=nsims, ncol=5)
for(m in 1:nsims)
{
	cat("Sim", m, "\n")
	# Draw a new B from the posterior
	for(k in 1:numtopics)
	{
		B[,k] = rdirichlet(1,SmoothCounts[,k])
	}
	
	LLsave3[m,1] = kalmanll(y, B, alphadocs)$loglike
	ll1 = PLloglike(y, B, alphadocs, 100)
	LLsave3[m,2] = sum(ll1)
	ll2 = lefttoright(y, B, alphadocs, 1)
	LLsave3[m,3] = sum(ll2)
	ll3 = lefttoright(y, B, alphadocs, 100)
	LLsave3[m,4] = sum(ll3)
	LLsave3[m,5] = montecarloML(y, B, alphadocs, 1000)
}


# Now new draw and random order
y0 = y
LLsave4 = matrix(0, nrow=nsims, ncol=5)
for(m in 1:nsims)
{
	cat("Sim", m, "\n")
	y = permute(y0)
	
	# Draw a new B from the posterior
	for(k in 1:numtopics)
	{
		B[,k] = rdirichlet(1,SmoothCounts[,k])
	}
	
	LLsave4[m,1] = kalmanll(y, B, alphadocs)$loglike
	ll1 = PLloglike(y, B, alphadocs, 100)
	LLsave4[m,2] = sum(ll1)
	ll2 = lefttoright(y, B, alphadocs, 1)
	LLsave4[m,3] = sum(ll2)
	ll3 = lefttoright(y, B, alphadocs, 100)
	LLsave4[m,4] = sum(ll3)
	LLsave4[m,5] = montecarloML(y, B, alphadocs, 1000)
}


colnames(LLsave1) = c("Filtering", "PL (100)", "L2R (1)", "L2R (100)", "MC (1000)")
colnames(LLsave2) = c("Filtering", "PL (100)", "L2R (1)", "L2R (100)", "MC (1000)")
colnames(LLsave3) = c("Filtering", "PL (100)", "L2R (1)", "L2R (100)", "MC (1000)")
colnames(LLsave4) = c("Filtering", "PL (100)", "L2R (1)", "L2R (100)", "MC (1000)")



mylims = range(rbind(LLsave1, LLsave2, LLsave3, LLsave4))
mylims = mylims + c(-2,2)

pdf("smallsims.pdf", width=6.5, height=3)
par(mar=c(4,6,1,1), mfrow=c(2,2))
boxplot(LLsave1, las=2, range=Inf,
	ylim=mylims,
	cex.axis=0.8, cex.main=0.9,
	main="Same posterior draw, same word order", horizontal=TRUE)

boxplot(LLsave2, las=2, range=Inf,
	ylim=mylims,
	cex.axis=0.8, cex.main=0.9,
	main="Same posterior draw, different word order", horizontal=TRUE)

boxplot(LLsave3, las=2, range=Inf,
	ylim=mylims,
	cex.axis=0.8, cex.main=0.9,
	main="Different posterior draws, same word order", horizontal=TRUE)

boxplot(LLsave4, las=2, range=Inf,
	ylim=mylims,
	cex.axis=0.8, cex.main=0.9, cex.lab=0.8,
	xlab="Hold-out log likelihood",
	main="Different posterior draw and word order", horizontal=TRUE)

dev.off()


