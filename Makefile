BEER_VERSION=2.0
BEER_DESTINATION=beer_$(BEER_VERSION)

LIB=lib

SCALA_VERSION=2.11.2
https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.11/2.2.1/scalatest_2.11-2.2.1.jar
SCALA_DIR=$(LIB)/scala_compiler
SCALA_COMPILER=./$(SCALA_DIR)/bin/scalac

beer: $(LIB) dist

transport_source:
	scp -r src src_moses scripts Makefile feature_templates mstanoj1@laco12.science.uva.nl:/home/mstanoj1/experiments/2016_Ninkasi
	scp -r src src_moses scripts Makefile feature_templates mstanoj1@laco5.science.uva.nl:/home/mstanoj1/experiments/2016_Ninkasi
	scp -r src src_moses scripts Makefile feature_templates mstanoj1@laco11.science.uva.nl:/home/mstanoj1/experiments/2016_Ninkasi

models: jar data
	CP=`ls ./*.jar ./lib/*.jar -1 | tr "\n" :` ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 0 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default.yaml \
		--threads 30 \
		--out_dir models/linear ; \
	cp -r models/linear models/default ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 0 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default_exponential_length_disbalance.yaml \
		--threads 30 \
		--out_dir models/linear_exponential_length_disbalance ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 0 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default_no_length_disbalance.yaml \
		--threads 30 \
		--out_dir models/linear_no_length_disbalance 

models_self_train: jar data
	CP=`ls ./*.jar ./lib/*.jar -1 | tr "\n" :` ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 3 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default.yaml \
		--threads 30 \
		--out_dir models/linear_self_train ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 3 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default_exponential_length_disbalance.yaml \
		--threads 30 \
		--out_dir models/linear_self_train_exponential_length_disbalance ; \
	java -Xmx230G -cp $$CP beer.TrainApp \
 		--train_data_type wmt14 \
 		--train_data_loc data/wmt14-metrics-task \
 		--valid_data_type wmt11 \
 		--valid_data_loc data/wmt11-metrics-task \
		\
 		--nbest_type wmt09 \
 		--nbest_loc data/wmt09-nbest \
		\
 		--self_train_training_size 60000 \
 		--self_train_iters 3 \
 		\
		--kernel_degree 1 \
		\
		--absolute_judgments_loc data/wmt13-absolute-judgments \
		--absolute_judgments_type wmt13 \
		--absolute_data_loc data/wmt13-metrics-task \
		\
		--features_desc feature_templates/default_no_length_disbalance.yaml \
		--threads 30 \
		--out_dir models/linear_self_train_no_length_disbalance 

$(LIB):
	mkdir -p $(LIB)
#
	wget http://www.work.caltech.edu/~htlin/program/libsvm/doc/platt.py -O $(LIB)/platt_TMP.py
	cat $(LIB)/platt_TMP.py | sed "s/from svm /#from svm /" > $(LIB)/platt.py
	rm $(LIB)/platt_TMP.py
	chmod +x $(LIB)/platt.py
	echo "deci = []" >> $(LIB)/platt.py
	echo "label = []" >> $(LIB)/platt.py
	echo "with open(argv[1]) as f:" >> $(LIB)/platt.py
	echo "  for line in f:" >> $(LIB)/platt.py
	echo "    fields = line.split()" >> $(LIB)/platt.py
	echo "    deci.append(float(fields[0]))" >> $(LIB)/platt.py
	echo "    label.append(int(fields[1]))" >> $(LIB)/platt.py
	echo "[A,B] = SigmoidTrain(deci, label)" >> $(LIB)/platt.py
	echo "print(A,B)" >> $(LIB)/platt.py
#
	wget http://downloads.typesafe.com/scala/$(SCALA_VERSION)/scala-$(SCALA_VERSION).tgz -O scala.tgz
	tar xvfz scala.tgz
	rm -rf scala.tgz
	mv scala-$(SCALA_VERSION) $(SCALA_DIR)
#
	wget http://dl.bintray.com/sbt/native-packages/sbt/0.13.5/sbt-0.13.5.tgz -O sbt.tgz
	tar xvfz sbt.tgz
	rm -rf sbt.tgz
	mv sbt/bin/* $(SCALA_DIR)/bin
	rmdir sbt/bin
	mv sbt/* $(SCALA_DIR)
	rmdir sbt
#
	cp $(SCALA_DIR)/$(LIB)/scala-library.jar $(LIB)
#
	wget http://search.maven.org/remotecontent?filepath=junit/junit/4.11/junit-4.11.jar -O $(LIB)/junit-4.11.jar
#
	wget http://repo2.maven.org/maven2/org/yaml/snakeyaml/1.13/snakeyaml-1.13.jar -O $(LIB)/snakeyaml-1.13.jar
#
	git clone https://github.com/scopt/scopt.git
	cd scopt; ../$(SCALA_DIR)/bin/sbt package ; cd -
	mv scopt/target/scala-2.11/scopt_*.jar $(LIB)
	rm -rf scopt
#
	wget https://oss.sonatype.org/content/groups/public/org/scalatest/scalatest_2.11/2.2.1/scalatest_2.11-2.2.1.jar -O $(LIB)/scalatest_2.11-2.2.1.jar
#
	wget http://www.bwaldvogel.de/liblinear-java/liblinear-java-1.95.jar -O $(LIB)/liblinear-java-1.95.jar
#
	git clone https://github.com/jhclark/multeval.git multeval_dir
	rm -rf multeval_dir/.git
	mv multeval_dir $(LIB)/multeval

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
	for X in `ls $(LIB) -1 | grep -v meteor | grep -v stanford-corenlp`; do \
	  echo "copying $$X" ; \
	  cp -r $(LIB)/$$X $(BEER_DESTINATION)/$(LIB) ; \
	done
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


data:
	mkdir data ; \
	cd data ; \
	\
	wget http://www.statmt.org/wmt15/metrics-task/wmt15-metrics-results.tgz ; \
	tar xfvz *.tgz ; \
	rm *.tgz ; \
	zcat wmt15-metrics-task/judgements.20150817.csv.gz \
		> wmt15-metrics-task/judgements.20150817.csv ; \
	\
	wget http://www.statmt.org/wmt14/wmt14-metrics-task.tar.gz ; \
	tar xfvz *.tar.gz ; \
	rm *.tar.gz ; \
	\
	wget http://statmt.org/wmt13/wmt13-metrics-task.tar.gz ; \
	tar xfvz wmt13-metrics-task.tar.gz ; \
	rm wmt13-metrics-task.tar.gz ; \
	\
	mkdir -p wmt12-metrics-task ; \
	cd wmt12-metrics-task ; \
	wget http://www.statmt.org/wmt12/wmt12-data.tar.gz ; \
	tar xfvz *.tar.gz ; \
	rm *.tar.gz ; \
	wget http://www.statmt.org/wmt12/manual-eval-judgments-2012.tgz ; \
	tar xfvz *.tgz ; \
	rm *.tgz  ; \
	cd .. ; \
	\
	mkdir wmt11-metrics-task ; \
	cd wmt11-metrics-task ; \
	wget http://www.statmt.org/wmt11/manual-eval-judgments.zip ; \
	unzip manual-eval-judgments.zip ; \
	rm manual-eval-judgments.zip ; \
	wget http://www.statmt.org/wmt11/wmt11-data.tar.gz ; \
	tar xfvz wmt11-data.tar.gz ; \
	rm wmt11-data.tar.gz ; \
	cd .. ; \
	\
	wget https://github.com/ygraham/segment-mteval/raw/master/seg-mteval-data.tar.gz; \
	tar xfvz seg-mteval-data.tar.gz ; \
	rm seg-mteval-data.tar.gz ; \
	mv seg-mteval-data wmt13-absolute-judgments ; \
	\
	mkdir wmt09-nbest; \
	cd wmt09-nbest; \
	wget http://www.statmt.org/wmt09/syscomb-test.tgz; \
	tar xfvz syscomb-test.tgz; \
	rm syscomb-test.tgz; \
	for X in submissions-nbest/newssyscomb2009/*.sgm.gz; do S=`echo $$X | sed "s/.gz$$//"` ; zcat $$X > $$S ; done ; \
	cd .. ; \
	\
	mkdir wmt10-nbest; \
	cd wmt10-nbest; \
	wget http://www.statmt.org/wmt10/syscomb-n-best.tar ; \
	tar xfv syscomb-n-best.tar ; \
	rm syscomb-n-best.tar ; \
	cd ..

clean:
	rm -rf bin *.jar

