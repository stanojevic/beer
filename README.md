BEER
====

BEER is trained a machine translation evaluation metric which puts special attention on the sentence level evaluation. For details look at the paper  [BEER: BEtter Evaluation as Ranking](http://www.statmt.org/wmt14/pdf/W14-3354.pdf) ([bibtex](http://www.statmt.org/wmt14/bib/W14-3354.bib)).

An example usage is:

    ./beer -l ru -s systemTranslation -r referenceTranslation

That command will give a detailed output (feature values for every input sentence, score for each sentence and the final score) for translated file systemTranslation and reference file referenceTranslation both being in Russian language. The description of more advanced options is on the way.

Download
----
Compiled version of BEER together with all its dependencies (except METEOR which will be automatically downloaded on the first run of BEER) can be downloaded from:

[BEER 1.0](https://staff.fnwi.uva.nl/m.stanojevic/beer?version=1.0)


Documentation
----
On the way. In case you have any doubts email me.

Libraries
----
BEER depends on the following libraries:

- METEOR 1.5
- MultEval
- JUnit
- ScalaTest
- ScalaLibrary
- scopt
- snakeyaml
- Weka

License
----
BEER is published under [GPL v3](http://www.gnu.org/licenses/gpl-3.0.html) license, but it is possible to get a separate license for distribution with proprietary software.

Authors
----
Miloš Stanojević and Khalil Sima'an

Institute for Logic, Language and Computation

University of Amsterdam

