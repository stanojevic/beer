#!/usr/bin/env perl
use strict;
use warnings;

my $CPUs = 16;
my $WMT14_DATA_DIR = "data/wmt14-metrics-task/baselines/data/plain/system-outputs/newstest2014/";
my $OUT_DIR = "scores";

my @testingOrder = qw/ evaluation other en_func_exact_para_weka   fr_func_exact_para_weka   cs_func_exact_para_weka   de_func_exact_para_weka   es_func_exact_para_weka   ru_func_exact_para_weka   other_func_exact_para_weka /;

my %toTest = (
		evaluation => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		other => ["en-hi"],
		en_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		fr_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		cs_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		de_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		es_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		ru_func_exact_para_weka    => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en"],
		other_func_exact_para_weka => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		en_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		fr_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		cs_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		de_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		es_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
#		ru_exact_weka              => ["en-cs", "en-de", "en-ru", "en-fr", "cs-en", "de-en", "ru-en", "fr-en", "hi-en", "en-hi"],
		);

mkdir $OUT_DIR;

for my $modelType (@testingOrder){
	my @langPairs = @{$toTest{$modelType}};

	my $langPairs = join(" ", @langPairs);

	my $sentMetricCmd    = "./beer --printSentScores --modelType $modelType -l __LANG__ -s __S__ -r __R__ | grep -v   corpus             | sed \"s/best beer     : //\"      ";
	my $sentTestCmd = "./scripts/test.pl --sentCmd '$sentMetricCmd' --dataDir $WMT14_DATA_DIR --metricName sent_$modelType --CPUs $CPUs --langPairs $langPairs";
	print STDERR "EXECUTING = $sentTestCmd\n";
	`$sentTestCmd > $OUT_DIR/sent_$modelType.scores 2> $OUT_DIR/sent_$modelType.scores.errs`;
	print STDERR "DONE = $sentTestCmd\n";

	my $corpMetricCmd    = "./beer                   --modelType $modelType -l __LANG__ -s __S__ -r __R__ | grep    \"corpus beer\"      | sed \"s/corpus beer   : //\"      ";
	my $corpTestCmd = "./scripts/test.pl --corpusCmd '$corpMetricCmd' --dataDir $WMT14_DATA_DIR --metricName corp_$modelType --CPUs $CPUs --langPairs $langPairs";
	print STDERR "EXECUTING = $corpTestCmd\n";
	`$corpTestCmd > $OUT_DIR/corp_$modelType.scores 2> $OUT_DIR/corp_$modelType.scores.errs`;
	print STDERR "DONE = $corpTestCmd\n";

	my $corpLogMetricCmd = "./beer                   --modelType $modelType -l __LANG__ -s __S__ -r __R__ | grep    \"corpus logarithm\" | sed \"s/corpus logarithm   : //\" ";
	my $corpLogTestCmd = "./scripts/test.pl --corpusCmd '$corpLogMetricCmd' --dataDir $WMT14_DATA_DIR --metricName corpLog_$modelType --CPUs $CPUs --langPairs $langPairs";
	print STDERR "EXECUTING = $corpLogTestCmd\n";
	`$corpLogTestCmd > $OUT_DIR/corpLog_$modelType.scores 2> $OUT_DIR/corpLog_$modelType.scores.errs`;
	print STDERR "DONE = $corpLogTestCmd\n";

}



