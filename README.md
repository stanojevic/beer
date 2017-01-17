BEER 2.0
==========

BEER 2.0 is a trained machine translation evaluation metric with high correlation with human judgment both on sentence and corpus level. For the papers that describe BEER in more detail look at the [References section](https://github.com/stanojevic/beer#references) on this page.

This pages is about the newest version of BEER which is 2.0 that was used on WMT16 metrics task. If you want to use old version from WMT15 metrics task which has some features not present in the new version (paraphrasing, reordering only evaluation, syntactic features...) then look at the branch for [BEER 1.1](https://github.com/stanojevic/beer/tree/BEER_1.1)


#Installation

BEER 2.0 includes all its dependencies in the tar.gz. Basic installation commands on a standard Unix terminal could be as follows:

    wget https://staff.fnwi.uva.nl/m.stanojevic/beer/beer_2.0.tar.gz
    tar xfvz beer_2.0.tar.gz

After these commands the directory `beer_2.0` will contain all the necessary files.

#Usage

In the installation directory there will be a shell script `beer` that should be used for evaluation. Its usage is straightforward:

    $BEER_HOME/beer -s system_translations.en -r reference_translations.en

That command will print only the corpus level score. If sentence level score is needed then additional parameter `--printSentScores` will print them out.

If you want to tune the Moses system with BEER you need to compile Moses with adding the files from `src_moses` directory.


License
----
BEER is published under [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) license.

This license is compatible with the libraries that BEER uses:
- liblinear-java
- snakeyaml
- scopt
- JUnit
- ScalaTest
- ScalaLibrary


Authors
----
[Miloš Stanojević](https://staff.fnwi.uva.nl/m.stanojevic) and [Khalil Sima'an](https://staff.fnwi.uva.nl/k.simaan/)

Institute for Logic, Language and Computation

University of Amsterdam



#References

1. Miloš Stanojević and Khalil Sima’an -- [BEER: BEtter Evaluation as Ranking](http://www.statmt.org/wmt14/pdf/W14-3354.pdf) - WMT 2014
2. Miloš Stanojević and Khalil Sima’an -- [Fitting Sentence Level Translation Evaluation with Many Dense Features](http://aclweb.org/anthology/D14-1025) - EMNLP 2014
3. Miloš Stanojević and Khalil Sima’an -- [Evaluating Word Order Recursively over Permutation-Forests](http://aclweb.org/anthology/W/W14/W14-4017.pdf) -- SSST 2014
6 lines yanked                                                                                                                              126,0-1       Bot
4. Miloš Stanojević and Khalil Sima’an -- [BEER 1.1: ILLC UvA submission to metrics and tuning task](http://www.statmt.org/wmt15/pdf/WMT50.pdf) -- WMT 2015
5. Miloš Stanojević and Khalil Sima’an -- [Hierarchical Permutation Complexity for Word Order Evaluation](http://www.statmt.org/wmt15/pdf/WMT50.pd://staff.fnwi.uva.nl/m.stanojevic/papers/2016_COLING_pets_evaluation.pdf) -- COLING 2016


