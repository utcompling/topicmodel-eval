# Topic Model Evaluation

Authors: 
* **James Scott** (james.scott@mccombs.utexas.edu)
* **Jason Baldridge** (jbaldrid@mail.utexas.edu)

# Introduction

This repository contains code for replicating the topic model evaluation experiments in Scott &amp; Baldridge 2013, "A recursive estimate for the predictive likelihood in a topic model." (Link to paper to be posted soon.)

The code in this repository contains implementations for the evaluation methods discussed in the paper, data management code for the corpora we used, and glue code to train topic models using [the Mallet toolkit](http://mallet.cs.umass.edu/). There is code in R that is used for the smaller simulated experiments (section 4.1) and Scala code for the larger simulations (section 4.2) and experiments with actual text corpora (seciton 4.3). Instructions for using the code to reproduce the tables and and figures in the paper are included below.

If you don't know what a topic model is and are curious, check out the resources on David Blei's [topic modeling page](http://www.cs.princeton.edu/~blei/topicmodeling.html) and [the Mallet page on topic modeling](http://mallet.cs.umass.edu/topics.php).

# Compiling the code

Go to the top-level directory `topicmodel-eval` and run:

```bash
$ ./build compile
```

This may take a while as dependencies are downloaded and the code is compiled. If this succeeds, you are ready to move on.

# Reproducing results from the paper

Keep in mind that there is randomness in the algorithms, so the precise numbers you get won't be the same as ours. (Keep in mind that this paper is about the broad patterns observed with likelihoods, not specific values.)

All commands given below assume you are in the top-level directory `topicmodel-eval`.

# Other stuff

To get some measurements for the different corpora (Table 1 in the paper), run the following:

```bash
$ bin/tmeval corpus-stats
```

You'll see information pass by for each corpus, followed at the end by the following output:

```
pcl-travel,188765,4780051,469,367.51598604686575
sgu,26851,421621,472,678.2764922949932
20news,114547,2743124,145,353.43882073139616
reuters,43153,1528617,70,47.916594202843754
gutenberg,78556,2953834,377,55.51576352712804
nyt,182942,21836689,405,209.63539777432626
```

This provides: *dataset name, vocabulary size, number of (non-stopword) tokens, average document length, and standard deviation of the document length*.

Though this paper is focused on evaluting predictive likelihood of topic models and doesn't consider the topics themselves, it is of course usually interesting to see them. We've computed them for all six datasets and [posted them on the wiki](https://github.com/utcompling/topicmodel-eval/wiki/Example-Topics). If you'd like to compute them yourself (so that you can play around with different numbers of topics for all or a specific dataset, use the `output-topics` command. For example, do the following to get the topics for all datasets (using the default 100 topics):

```bash
$ bin/tmeval output-topics -o all-topics.txt
```

The output goes to the file `all-topics.txt`. (It will take a while, so be patient.)

The following computes 25 topics for the SGU transcripts data, outputting them to `sgu-topics.txt`:


```bash
$ bin/tmeval output-topics -d sgu -n 25 -o sgu-topics.txt
```

Note: the topics you see are computed from both the train and eval portions of each corpus.
