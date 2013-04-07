# Topic Model Evaluation

Authors: 
* **James Scott** (james.scott@mccombs.utexas.edu)
* **Jason Baldridge** (jbaldrid@mail.utexas.edu)

# Introduction

This repository contains code for replicating the topic model evaluation experiments in Scott &amp; Baldridge 2013, "A recursive estimate for the predictive likelihood in a topic model."

# Reproducing results from the paper

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

To run replicate the numbers from the paper, run the following:

```bash
$ bin/tmeval corpus-exp
```

This runs on all datasets in the data/extracted directory, with default options used in the paper. There are options that let you choose a different number of topics, different numbers of draws from the posterior, and also run on just a single dataset. For example, to use 20 topics and 3 draws for the SGU corpus, do this:

```bash
$ bin/tmeval corpus-exp -n 20 -r 3 -d sgu
```

Use `--help` with corpus-exp to see more details about the options.


# Other stuff

TBA: get the corpus stats

TBA: output the topics for each corpus

