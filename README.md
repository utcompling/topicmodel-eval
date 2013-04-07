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

Five of the datasets discussed in the paper are included in their original format in the directory `data/orig`. These are:

```bash
$ ls data/orig 
20news.tar.bz2  gutenberg.tar.bz2  pcl-travel.tar.bz2  reuters21578.tar.bz2  sgu-2013-04-04.tar.bz2
```

To run the experiments, each of these datasets needs to be processed to form the input expected for training and evaluating models. To generate these, simply run:

```bash
$ bin/tmeval prepare
```

The New York Times corpus is not packaged with this repository as it requires having a license to [the English Gigaword corpus](http://www.ldc.upenn.edu/Catalog/catalogEntry.jsp?catalogId=LDC2003T05). If you have that corpus, you can process it for model training and evaluation by running:

```bash
$ bin/tmeval prepare <path-to-directory-containing-English-Gigaword>/english-gigaword-LDC2003T05/cdrom0/nyt
```

These steps will produce directories in `data/extracted` that are now ready with train and eval portions. Note that for some of the preparations, books or longer texts are cut into subdocuments---each of these is then a "document" for LDA to use in computing the topics.

To replicate the numbers from the paper, run the following:

```bash
$ bin/tmeval corpus-exp
```

This runs on all datasets in the data/extracted directory, with default options used in the paper.  Note that this will likely take several hours to complete since it is computing and evaluating 10 topic models for each of five to six corpora (six if you have the New York Times data).

There are options that let you choose a different number of topics, different numbers of draws from the posterior, run on just a single dataset, and also to output the results to a specified file. For example, to use 20 topics and 3 posterior draws for the SGU corpus, do this:

```bash
$ bin/tmeval corpus-exp -n 20 -r 3 -d sgu
```

Use `--help` with corpus-exp to see more details about the options.


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