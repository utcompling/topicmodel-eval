sgu = read.csv("sgu.csv", row.names=1, header=FALSE)
sgu = t(sgu)
colnames(sgu) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")

gutenberg = read.csv("gutenberg.csv", row.names=1, header=FALSE)
gutenberg = t(gutenberg)
colnames(gutenberg) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")

twentynews = read.csv("twentynews.csv", row.names=1, header=FALSE)
twentynews = t(twentynews)
colnames(twentynews) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")

reuters = read.csv("reuters.csv", row.names=1, header=FALSE)
reuters = t(reuters)
colnames(reuters) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")

pcltravel = read.csv("pcltravel.csv", row.names=1, header=FALSE)
pcltravel = t(pcltravel)
colnames(pcltravel) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")

nyt1 = read.csv("nyt1.csv", row.names=1, header=FALSE)
nyt1 = t(nyt1)
nyt2 = read.csv("nyt2.csv", row.names=1, header=FALSE)
nyt2 = t(nyt2)
nyt = rbind(nyt1,nyt2)
colnames(nyt) = c("Filtering", "L2R (1)", "L2R (50)", "Mallet L2R (50)")



par(mar=c(4,6,2,1), mfrow=c(2,3))
boxplot(sgu/100, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="Skeptic's Guide", horizontal=TRUE)
mtext("x 10^2", 1, line=3, cex=0.6)

boxplot(gutenberg[,c(1,3,4)]/10000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="Project Gutenberg", horizontal=TRUE)
mtext("x 10^4", 1, line=3, cex=0.6)

boxplot(reuters[,c(1,3,4)]/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="Reuters", horizontal=TRUE)
mtext("x 10^3", 1, line=3, cex=0.6)	

boxplot(pcltravel[,c(1,3,4)]/10000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="Travel Narratives", horizontal=TRUE)
mtext("x 10^4", 1, line=3, cex=0.6)

boxplot(twentynews[,c(1,3,4)]/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="Newsgroups", horizontal=TRUE)
mtext("x 10^3", 1, line=3, cex=0.6)

boxplot(nyt[,c(1,3,4)]/10000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main="New York Times", horizontal=TRUE)
mtext("x 10^4", 1, line=3, cex=0.6)



sim50 = read.csv("sim50.csv", row.names=1, header=FALSE)
sim50 = t(sim50)
colnames(sim50) = c("Filtering", "L2R (1)", "L2R (50)")


sim200 = read.csv("sim200.csv", row.names=1, header=FALSE)
sim200 = t(sim200)
colnames(sim200) = c("Filtering", "L2R (1)", "L2R (50)")

par(mar=c(5,4,1,0), mfrow=c(1,3))
boxplot(sim50/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9, axes=FALSE,
	main="50 topics", horizontal=TRUE)
axis(1, las=2,cex.axis=0.8)
axis(2, las=1,cex.axis=0.8, at=1:3, tick=FALSE, labels=c("Filtering", "L2R (1)", "L2R (50)"))
mtext("x 10^3", 1, line=3, cex=0.7)

par(mar=c(5,3,1,1))
boxplot(sim200/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9, axes=FALSE,
	main="200 topics", horizontal=TRUE)
axis(1, las=2,cex.axis=0.8)
mtext("x 10^3", 1, line=3, cex=0.7)

par(mar=c(5,5,1,1))
plot(sim200[,3]/1000, sim200[,1]/1000, las=2,
	bty='n', xlab='L2R(50)', ylab="Filter-based estimate");
abline(0,1)
