BEER_VERSION=1.1
BEER_DESTINATION=beer_$(BEER_VERSION)

LIB=lib

SCALA_VERSION=2.11.2
https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.11/2.2.1/scalatest_2.11-2.2.1.jar
SCALA_DIR=scala_compiler
SCALA_COMPILER=./$(SCALA_DIR)/bin/scalac

beer: $(LIB) dist

$(SCALA_DIR):
	wget http://downloads.typesafe.com/scala/$(SCALA_VERSION)/scala-$(SCALA_VERSION).tgz -O scala.tgz
	tar xvfz scala.tgz
	rm -rf scala.tgz
#	rm -rf scala-2.10.3 # we need this for the compile step
	mv scala-$(SCALA_VERSION) $(SCALA_DIR)
#
	wget http://dl.bintray.com/sbt/native-packages/sbt/0.13.5/sbt-0.13.5.tgz -O sbt.tgz
	tar xvfz sbt.tgz
	rm -rf sbt.tgz
	mv sbt/bin/* $(SCALA_DIR)/bin
	rmdir sbt/bin
	mv sbt/* $(SCALA_DIR)
	rmdir sbt

$(LIB): $(SCALA_DIR)
	mkdir -p $(LIB)
#
	cp $(SCALA_DIR)/$(LIB)/scala-library.jar $(LIB)
#
	wget http://search.maven.org/remotecontent?filepath=junit/junit/4.11/junit-4.11.jar -O $(LIB)/junit-4.11.jar
#
	wget http://repo2.maven.org/maven2/org/yaml/snakeyaml/1.13/snakeyaml-1.13.jar -O $(LIB)/snakeyaml-1.13.jar
#
	wget http://downloads.sourceforge.net/project/weka/weka-3-7/3.7.11/weka-3-7-11.zip -O $(LIB)/weka.zip
	unzip $(LIB)/weka.zip
	cp weka-3-7-11/weka.jar $(LIB)/weka.jar
	rm -rf weka-3-7-11 $(LIB)/weka.zip
#
	git clone https://github.com/scopt/scopt.git
	cd scopt; ../$(SCALA_DIR)/bin/sbt package ; cd -
	mv scopt/target/scala-2.11/scopt_*.jar $(LIB)
	rm -rf scopt
#
	wget https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.11/2.2.1/scalatest_2.11-2.2.1.jar -O $(LIB)/scalatest_2.11-2.2.1.jar
#
	wget http://www.cs.cmu.edu/~alavie/METEOR/download/meteor-1.5.tar.gz -O $(LIB)/meteor.tar.gz
	tar xvfz $(LIB)/meteor.tar.gz
	mv meteor-1.5 $(LIB)
	rm $(LIB)/meteor.tar.gz
#
	wget http://nlp.stanford.edu/software/stanford-corenlp-full-2015-01-29.zip
	unzip stanford-corenlp*.zip
	rm stanford-corenlp*.zip
	mv stanford-corenlp* $(LIB)
#
	git clone https://github.com/jhclark/multeval.git multeval_dir
	rm -rf multeval_dir/.git
	mv multeval_dir $(LIB)/multeval
#
	wget http://downloads.sourceforge.net/project/lemur/lemur/RankLib-2.3/RankLib-2.3.jar
	mv RankLib-2.3.jar $(LIB)

multeval: jar $(LIB)
	cp -r src_multeval/* $(LIB)/multeval/
	cd $(LIB)/multeval; ant; cd -

jar: bin
	echo "Manifest-Version: 1.0" > Manifest.txt
	echo -n "Class-Path: " >> Manifest.txt
	find ./lib -name \*.jar | sed "s/^/ /" >> Manifest.txt
	echo Main-Class: beer.Evaluation >> Manifest.txt
	rm -f beer_$(BEER_VERSION).jar
	jar -cvfm beer_$(BEER_VERSION).jar Manifest.txt -C bin .
	rm Manifest.txt

clean:
	rm -rf $(BEER_DESTINATION) dist beer*.jar

bin:
	mkdir -p bin
	$(SCALA_COMPILER) -d bin -classpath `find $(LIB) -name \*.jar| tr "\n" :` `find src -name \*.scala`

dist: jar multeval models
	mkdir -p $(BEER_DESTINATION)
	cp -r beer*.jar              $(BEER_DESTINATION)
	cp -r beer                   $(BEER_DESTINATION)
	cp -r multeval               $(BEER_DESTINATION)
	cp -r README.md              $(BEER_DESTINATION)
	cp -r configuration.yaml     $(BEER_DESTINATION)
	cp -r templates              $(BEER_DESTINATION)
	mkdir $(BEER_DESTINATION)/$(LIB)
	for X in `ls $(LIB) -1 | grep -v meteor | grep -v stanford-corenlp`; do echo "copying $$X" ; cp -r $(LIB)/$$X $(BEER_DESTINATION)/$(LIB) ; done
	cp -r models                 $(BEER_DESTINATION)
	cp -r scripts                $(BEER_DESTINATION)
	cp -r src_moses              $(BEER_DESTINATION)
	if [ -e resources ] ; then \
		cp -r resources      $(BEER_DESTINATION); \
	fi
	chmod o+rw -R $(BEER_DESTINATION)
	tar -czvf $(BEER_DESTINATION).tar.gz $(BEER_DESTINATION)
	mkdir -p dist
	mv $(BEER_DESTINATION).tar.gz dist
	mv $(BEER_DESTINATION)/beer*.jar dist
	rm -rf $(BEER_DESTINATION)
	chmod o+rw -R dist

deploy: dist
	scp -p dist/* mstanoj1@staff.fnwi.uva.nl:/data/mstanoj1/WWW/beer/

models: jar
#nohup ./scripts/train.pl > std.log 2> err.log

