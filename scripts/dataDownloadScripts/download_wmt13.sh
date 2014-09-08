#!/bin/bash

SCRIPTS_DIR=`dirname $0`

MANUAL_LINK="http://www.statmt.org/wmt13/wmt13-manual-evaluation.tgz"
TMP_MANUAL_FILE="download.tgz"
RANKINGS_FILE="wmt13-manual-evaluation/wmt13-judgments.csv"

SYSTEM_LINK="http://www.statmt.org/wmt13/wmt13-data.tar.gz"
TMP_SYSTEM_FILE="download2.tgz"
SYSTEMS_DIR="wmt13-data"

TARGET_DIRECTORY="."

mkdir $TARGET_DIRECTORY
cd    $TARGET_DIRECTORY


wget $MANUAL_LINK -O $TMP_MANUAL_FILE
tar xfvz $TMP_MANUAL_FILE
rm $TMP_MANUAL_FILE

wget $SYSTEM_LINK -O $TMP_SYSTEM_FILE
tar xfz $TMP_SYSTEM_FILE
rm $TMP_SYSTEM_FILE


"$SCRIPTS_DIR/extract_evaluation_corpora.pl" $RANKINGS_FILE $SYSTEMS_DIR

rm -rf $SYSTEMS_DIR `dirname $RANKINGS_FILE`

cd ..

