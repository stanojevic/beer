#!/bin/bash

SCRIPTS_DIR=`dirname $0`

MANUAL_LINK="http://www.statmt.org/wmt12/manual-eval-judgments-2012.tgz"
TMP_MANUAL_FILE="download.tgz"
RANKINGS_FILE="manual-eval-judgments-2012/wmt12.RNK_results.csv"

SYSTEM_LINK="http://www.statmt.org/wmt12/wmt12-data.tar.gz"
TMP_SYSTEM_FILE="download2.tgz"
SYSTEMS_DIR="wmt12-data"

TARGET_DIRECTORY="."

mkdir $TARGET_DIRECTORY
cd    $TARGET_DIRECTORY


wget $MANUAL_LINK -O $TMP_MANUAL_FILE
tar xfvz $TMP_MANUAL_FILE
rm $TMP_MANUAL_FILE

wget $SYSTEM_LINK -O $TMP_SYSTEM_FILE
tar xfz $TMP_SYSTEM_FILE
rm $TMP_SYSTEM_FILE


"$SCRIPTS_DIR/repair_wmt12.pl" < $RANKINGS_FILE > $RANKINGS_FILE.new

"$SCRIPTS_DIR/extract_evaluation_corpora.pl" $RANKINGS_FILE.new $SYSTEMS_DIR

rm -rf $SYSTEMS_DIR `dirname $RANKINGS_FILE`

cd ..
