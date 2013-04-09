# Topic Model Evaluation

Authors: 
* **James Scott** (james.scott@mccombs.utexas.edu)
* **Jason Baldridge** (jbaldrid@mail.utexas.edu)

This repository contains code for replicating the topic model evaluation experiments in Scott &amp; Baldridge 2013, "A recursive estimate for the predictive likelihood in a topic model." (Link to paper to be posted soon.)

The code in this repository contains implementations for the evaluation methods discussed in the paper, data management code for the corpora we used, and glue code to train topic models using [the Mallet toolkit](http://mallet.cs.umass.edu/). There is code in R that is used for the smaller simulated experiments (section 4.1) and Scala code for the larger simulations (section 4.2) and experiments with actual text corpora (seciton 4.3). Instructions for using the code to reproduce the tables and and figures in the paper are given on wiki page](https://github.com/utcompling/topicmodel-eval/wiki).

You can see [the topics computed for the six corpora](https://github.com/utcompling/topicmodel-eval/wiki/Example-Topics). If you don't know what a topic model is and are curious, check out the resources on David Blei's [topic modeling page](http://www.cs.princeton.edu/~blei/topicmodeling.html) and [the Mallet page on topic modeling](http://mallet.cs.umass.edu/topics.php).

If you have any problems running the code, please create [an issue](https://github.com/utcompling/topicmodel-eval/issues). 
