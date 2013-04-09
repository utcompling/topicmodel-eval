outputs = read.csv("corpus-experiment.csv", header=FALSE)


colnames(outputs) = c("corpus", "method", "averageloglike", 1:{ncol(outputs)-3})
corpnames = unique(outputs$corpus)

pdf("realcorpora.pdf", width=6.5, height=3)
par(mar=c(4,6,2,1), mfrow=c(2,3))
for(i in seq_along(corpnames))
{
	mysub = subset(outputs, corpus==corpnames[i])
	methodnames = mysub$method
	mysub = t(mysub[, 4:ncol(mysub)])
	colnames(mysub) = methodnames
	boxplot(mysub/100, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9,
	main=corpnames[i], horizontal=TRUE)
	mtext("x 10^2", 1, line=3, cex=0.6)
}
dev.off()



sim50 = read.csv("largesim50.csv", header=FALSE)
colnames(sim50) = c("corpus", "method", "averageloglike", 1:{ncol(sim50)-3})

sim200 = read.csv("largesim200.csv", header=FALSE)
colnames(sim200) = c("corpus", "method", "averageloglike", 1:{ncol(sim200)-3})


pdf("largesims.pdf", width=5, height=2)
par(mar=c(5,4,3,0), mfrow=c(1,3))

methodnames = sim50$method
mysub50 = t(sim50[, 4:ncol(sim50)])
colnames(mysub50) = methodnames
boxplot(mysub50/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9, axes=FALSE,
	main="50 topics", horizontal=TRUE)
axis(1, las=2,cex.axis=0.8)
axis(2, las=1,cex.axis=0.8, at=1:3, tick=FALSE, labels=c("Filtering", "L2R (1)", "L2R (50)"))
mtext("x 10^3", 1, line=3, cex=0.7)

par(mar=c(5,3,3,1))
mysub200 = t(sim200[, 4:ncol(sim200)])
colnames(mysub200) = methodnames
boxplot(mysub200/1000, las=2, range=Inf,
	cex.axis=0.8, cex.main=0.9, axes=FALSE,
	main="200 topics", horizontal=TRUE)
axis(1, las=2,cex.axis=0.8)
mtext("x 10^3", 1, line=3, cex=0.7)

par(mar=c(5,5,3,1))
plot(mysub200[,3]/1000, mysub200[,1]/1000, las=2,
	main="Correspondence across \n simulated corpora",
	cex.main=0.9, cex.axis=0.8, 
	bty='n', xlab='L2R(50)', ylab="Filter-based estimate");
#abline(0,1)
dev.off()


