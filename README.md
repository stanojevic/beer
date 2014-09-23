BEER
==========

BEER is trained a machine translation evaluation metric which puts special attention on the sentence level evaluation. For details look at the paper  [BEER: BEtter Evaluation as Ranking](http://www.statmt.org/wmt14/pdf/W14-3354.pdf) ([bibtex](http://www.statmt.org/wmt14/bib/W14-3354.bib)). Its features include:

* Full evaluation metric (published in [1] and [2])
* Reordering evaluation metric (published in [3])
* Support for statistical testing trough multeval
* Permutation parser and visualizer

#Instalation

BEER 1.0 includes all its dependencies in the tar.gz file except METEOR. METEOR will be downloaded on the first run of BEER. Basic installation commands on a standard Unix terminal could be as follows:

    wget https://staff.fnwi.uva.nl/m.stanojevic/beer?version=1.0 -O beer.tar.gz
    tar xfvz beer.tar.gz
    ./beer_1.0/beer
    rm beer.tar.gz

After these commands the directory `beer_1.0` will contain all the necessary files. Even though this installation requires bash, BEER can run on Windows too. User can download BEER directly from the following link:

[BEER 1.0](https://staff.fnwi.uva.nl/m.stanojevic/beer?version=1.0)

and afterwords download meteor and extract it in the lib directory.

#Usage

## Full evaluation metric

The available parameters are:

Parameter  | Description
------------- | -------------
-l                                 | (required parameter) evaluated language (supported: en, fr, cs, de, es, ru, da, fi, hu, it, nl, no, pt, ro, se, tr, ur, other)
-s                                | system translation
-r                                | reference files separated by column :
--noPunct                    | do not use punctuation before evaluation
--noLower                  | do not lowercase before evaluation
--norm                        | tokenize before evaluation
--workingMode           | evaluation (default), factors, interactive, train, evaluateReordering
--printFeatureValues | prints values of all features for each sentence
--printSentScores      | prints score for each sentence
--reorderingMetric      | choose reordering metric (excludes full evaluation!) ; available: PEFrecursive, PETrecursiveViterbi, PEFsize, PETarity, PETsize, Kendall, Hamming, Spearman, Ulam, Fuzzy
--alpha                 | parameter that gives importance to the lexical scoring in the reordering metric (default is 0)
--verbose                    | prints additional, possibly helpful, messages on stderr
--version                     | prints current version
--help                          | prints available options

### Example usage for evaluation

The most basic usage is evaluation of some corpus translated by the system with location `system.en` with reference translation `reference.en` with target language being English:

    $BEER_HOME/beer -l en -s system.en -r reference.en

###Interactive working mode

BEER can also be used in interactive mode. After executing:

    $BEER_HOME/beer -l en --workingMode interactive

the BEER starts waiting for the users commands. Each command is separated from its arguments with `|||`. Example of commands are:

    EVAL ||| system translation ||| reference translation 1 ||| reference translation 2

The output will be probability of system translation for each of the references.

    FACTORS ||| system translation ||| reference translation 1 ||| reference translation 2

will output feature values for each reference translation.

## Statistical testing using multeval

BEER is distributed with the extended version of multeval that supports BEER. The only additional necessary argument is the one specifying the evaluation language. Example usage is:

    $BEER_HOME/multeval --beer.language en ...all the standard multeval params...

## (pre-)ordering evaluation metric

BEER also includes specialized reordering metrics published in [3]. Example usage is:

    $BEER_HOME/beer  --workingMode evaluateReordering --reorderingMetric PEFrecursive -l en -s system.en -r reference.en

Here system.en and reference.en should differ only in word order.
If that is not the case then you need to add some importance to the lexical part (F1 score).
That is balanced with the parameter --alpha which is by default 0 (ignores lexical translation).
The reason for that is that this reordering metric is meant to be used only for measuring the quality of preordering.

## How to report the results

If you use BEER as an MT evaluation metric in your research please cite [1].

If you use the part of BEER that is made only for measuring the quality of preordering cite [3]
and mention exactly the value of alpha parameter and the word order metric used (e.g. PEFrecursive )    

## Parser and Visualizer

BEER can be used as a permutation parser. 

    java -cp $BEER_HOME/beer_1.0.jar beer.PermutationParser

would open a terminal waiting for the permutation. After permutation is typed the program will generate code for GraphViz that would be compiled with dot and generate a png image that would be automatically openned. Naturally this requires Unix type of machine with GraphViz installed.

Running the same command with `--help` would show additional options that include other output formats (outputing forest of permutation trees, penn output format, qtree, dot...).

Libraries
----
BEER is published under [GPL v3](http://www.gnu.org/licenses/gpl-3.0.html) license, but it is possible to get a separate license for distribution with proprietary software. This license is compatible with the libraries that BEER uses:

- METEOR 1.5
- MultEval
- JUnit
- ScalaTest
- ScalaLibrary
- scopt
- snakeyaml
- Weka
- RankLib

Authors
----
Miloš Stanojević and Khalil Sima'an

Institute for Logic, Language and Computation

University of Amsterdam

#References

1. Miloš Stanojević and Khalil Sima’an -- BEER: BEtter Evaluation as Ranking - WMT 2014
2. Miloš Stanojević and Khalil Sima’an -- Fitting Sentence Level Translation Evaluation with Many Dense Features - EMNLP 2014
3. Miloš Stanojević and Khalil Sima’an -- Evaluating Word Order Recursively over Permutation-Forests -- SSST 2014
