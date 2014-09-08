#!/bin/bash

SCRIPTS_DIR=`dirname $0`

MANUAL_LINK="http://www.statmt.org/wmt14/wmt14-metrics-task.tar.gz"
TMP_MANUAL_FILE="download.tar.gz"
RANKINGS_FILE="wmt14-metrics-task/judgements-2014-05-14.csv"

TARGET_DIRECTORY="."

mkdir $TARGET_DIRECTORY
cd    $TARGET_DIRECTORY


wget $MANUAL_LINK -O $TMP_MANUAL_FILE
tar xfvz $TMP_MANUAL_FILE
rm $TMP_MANUAL_FILE


SYSTEMS_DIR="wmt14-metrics-task/baselines/data"

"$SCRIPTS_DIR/extract_evaluation_corpora.pl" $RANKINGS_FILE $SYSTEMS_DIR

rm -rf wmt14-metrics-task

cd -

