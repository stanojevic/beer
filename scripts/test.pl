#!/usr/bin/env perl
use strict;

use File::Path qw/ remove_tree /;
use File::Temp qw/ tempdir     /;
use File::Basename;
use Getopt::Long;
use Parallel::ForkManager;

our $MAX_PROCESSES = 1;
our $TMP_DIR = tempdir("TESTING_experiment_XXXXXXX", TMPDIR=>1);

binmode STDOUT, ":utf8";



my $pm;
my @allSentOutputFiles;
my @allCorpOutputFiles;

sub MAIN{
	my ($metricName, $dataDir, $corpusCmd, $sentCmd, $langPairsToTest) = @_;

	my @langPairsToTest = @$langPairsToTest;
	my @langPairDirs = findLangPairDirs($dataDir, @langPairsToTest);

	$pm = new Parallel::ForkManager($MAX_PROCESSES);

	for my $langPairDir (@langPairDirs){
		my $langPair = basename($langPairDir);
		evalLangPair($metricName, $langPair, $langPairDir, $corpusCmd, $sentCmd);
	}

	$pm->wait_all_children();
	
	if($corpusCmd){
		my @corpusContent;
		for my $file (@allCorpOutputFiles){
			push @corpusContent, loadContent($file);
		}
		@corpusContent = sort @corpusContent;
		foreach my $corpusEntry (@corpusContent){
			print $corpusEntry,"\n";
		}
	}


	if($sentCmd){
		my @sentContent;
		for my $file (@allSentOutputFiles){
			push @sentContent, loadContent($file);
		}
		@sentContent = sort @sentContent;
		foreach my $sentEntry (@sentContent){
			print $sentEntry,"\n";
		}
	}

}

sub evalLangPair{
	my ($metricName, $langPair, $langPairDir, $cmdTemplateCorpus, $cmdTemplateSent) = @_;

	my ($srcLang, $tgtLang) = split /-/, $langPair;

	my @systemTuples = findSystemTuples($langPairDir);
	my $refFn = findReference($langPairDir);

	for my $tuple ( @systemTuples ) {

		my $systemName = $tuple->[0];
		my $sysFn = $tuple->[1];
		my $testSet = $tuple->[2];

		if($cmdTemplateCorpus){
			my $corpCmd = createCommand($cmdTemplateCorpus, $sysFn, $refFn, $tgtLang);
			my $corpOutputFn = "$TMP_DIR/$testSet.$langPair.$systemName.corpusScores";
			push @allCorpOutputFiles, $corpOutputFn;

			unless($pm->start){
				my $score = `$corpCmd`+0.0;
				my $outputString = join("\t", $metricName, $langPair, $testSet, $systemName, $score);
				storeContent($corpOutputFn, $outputString);
				$pm->finish;
			}
		}

		if($cmdTemplateSent){
			my $sentCmd = createCommand($cmdTemplateSent, $sysFn, $refFn, $tgtLang);
			my $sentOutputFn = "$TMP_DIR/$testSet.$langPair.$systemName.sentScores";
			push @allSentOutputFiles, $sentOutputFn;
			unless($pm->start){

				my @contentForOutput;

				open my $pipe, "$sentCmd|" or die $!;
				my $segId = 1;
				while(<$pipe>){
					my $segScore = $_+0.0;

					my $outputString = join("\t", $metricName, $langPair, $testSet, $systemName, $segId, $segScore);
					push @contentForOutput, $outputString;
					$segId ++ ;
				}
				close $pipe;
				storeContent($sentOutputFn, @contentForOutput);
				$pm->finish;
			}
		}
	}
}

sub loadContent{
	my ($fn) = @_;
	my @content;
	open my $fh, "<encoding(utf-8)", $fn or die "problem with file $fn $!\n";
	while(<$fh>){
		chomp;
		push @content, $_;
	}
	close $fh;
	return @content;
}

sub storeContent{
	my ($outputFn, @content) = @_;
	open my $fh, ">$outputFn" or die $!;
	for my $entry (@content){
		print $fh $entry, "\n";
	}
	close $fh;
}

sub createCommand{
	my ($template, $sysFn, $refFn, $tgtLang) = @_;

	my $realCMD = $template;
	$realCMD =~ s/__S__/$sysFn/g;
	$realCMD =~ s/__R__/$refFn/g;
	$realCMD =~ s/__LANG__/$tgtLang/g;

	return $realCMD;
}

sub findReference{
#dirty ad-hoc solution
	my ($langPairDir) = @_;
	my $langPair = basename($langPairDir);
	my $ref = "$langPairDir/../../../references/newstest2014-ref.$langPair";
	return $ref;
#	my @refs = glob ("$langPairDir/reference*");
#	if(@refs == 1){
#		return $refs[0];
#	}else{
#		die "Too many references @refs\n";
#	}
}

sub findSystemTuples{
	my ($langPairDir) = @_;

	my @files = grep {!/(SENTENCE_LEVEL_REFERENCE.es|SENTENCE_LEVEL_WINNER.es|SENTENCE_LEVEL_LOSER.es|SYSTEM_RANKINGS.txt|referenceSystem.es)$/} glob("$langPairDir/*");
	my @tuples = map{
		my $fn = $_;
		my $tuple;
		my $baseName = basename($fn);
		if($baseName =~ /^(.+?)\.(.+)\.(.+?)/){
			my $testSet = $1;
			my $system = $2;
			my $langPair = $3;
			$tuple = [$system, $fn, $testSet];
		}else{
			die "can't finde all the fields I need in file name $fn\n";
		}

		$tuple
	} @files;

	if(@tuples == 0){
		die "No systems for $langPairDir\n";
	}

	@tuples
}

sub findLangPairDirs{
	my ($contentDir, @langPairs) = @_;
	my %langPairs;
	for my $langPair (@langPairs){
		$langPairs{$langPair} = 1;
	}
	grep {/\/(..-..)$/; exists $langPairs{$1} } glob ("$contentDir/*");
}


my $dataDir = undef;
my $corpusCmd = undef;
my $sentCmd = undef;
my $metricName = undef;
my @langPairsToTest;

GetOptions(
	"dataDir=s" => \$dataDir,
	"corpusCmd:s" => \$corpusCmd,
	"sentCmd:s" => \$sentCmd,
	"langPairs=s{1,}" => \@langPairsToTest,
	"metricName=s" => \$metricName,
	"CPUs:i" => \$MAX_PROCESSES) or
  	die "usage: $0 --corpusCmd ' ' --sentCmd 'metricCommand __S__ __R__ __LANG__' --dataDir /dir/data --metricName ime --CPUs 4 --langPairs cs-en en-cs\n";

die "lang pairs are obligatory argument\n" unless @langPairsToTest;
die "metricName is obligatory\n" unless $metricName;
#...
#example usage:
#
#nohup ./scripts/test.pl --dataDir ~/experiments/2015_ACL/wmt14/wmt14-metrics-task/baselines/data/plain/system-outputs/newstest2014/ --corpusCmd './beer -l __LANG__ -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs en-cs en-ru en-hi en-de en-fr cs-en ru-en hi-en de-en fr-en --metricName BEER_final --CPUs 10 > BEER_full.sys.score 2> BEER_full.sys.err &
#
#nohup ./scripts/test.pl --dataDir ~/experiments/2015_ACL/wmt14/wmt14-metrics-task/baselines/data/plain/system-outputs/newstest2014/ --sentCmd './beer -l __LANG__ -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs en-cs en-ru en-hi en-de en-fr cs-en ru-en hi-en de-en fr-en --metricName BEER_final --CPUs 10 > BEER_full.seg.score 2> BEER_full.seg.err &
#

MAIN($metricName, $dataDir, $corpusCmd, $sentCmd, \@langPairsToTest);
remove_tree($TMP_DIR)
