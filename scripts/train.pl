#!/usr/bin/perl 
use strict;
use warnings;

use File::Basename;
use File::Copy qw/copy/;
use File::Path qw/rmtree/;
use Cwd qw/abs_path/;
use Parallel::ForkManager;


my @langsToUseForTraining  = ("en", "cs", "fr", "de", "es", "ru", "hi");

my @langPairsAvailableForTraining = ("en-cs", "en-fr", "en-de", "en-es", "cs-en", "fr-en", "de-en", "es-en", "en-ru", "ru-en", "hi-en", "en-hi");

my $MAX_PROCESSES = 20;

my $experiments_dir = abs_path(".");
my $application_dir = abs_path(dirname($0));


my $BEER_HOME = "$application_dir/..";
my $BEER_CMD = "$BEER_HOME/beer";

my $wmt13    = "$experiments_dir/data/wmt13";
my $wmt14    = "$experiments_dir/data/wmt14";
my $training = "$experiments_dir/data/training";
my $unlabeledTraining = "$experiments_dir/data/unlabeled_training";
my $trainingFeatures = "$experiments_dir/data/training_features";
my $readyForTraining = "$experiments_dir/data/collected_features";


#                    [en] (exact, para, stem, syn)
#                     |
#                [fr, de, es, ru] (exact, para, stem)  
#        	          |     
#     (exact, para) [cs|   
#                     |
#                  [hi, other] (exact)
#

my %configTemplates = 
	(
	 en => ["func_exact_para_stem_syn_dep",    "func_exact_para_stem_syn"        , "func_exact_para_stem"   , "func_exact_para"   , "func_exact_stem"   , "exact"   ],
	 fr => [                                                                       "func_exact_para_stem"   , "func_exact_para"   , "func_exact_stem"   , "exact"   ],
	 de => [                                                                       "func_exact_para_stem"   , "func_exact_para"   , "func_exact_stem"   , "exact"   ],
	 es => [                                                                       "func_exact_para_stem"   , "func_exact_para"   , "func_exact_stem"   , "exact"   ],
	 ru => [                                                                       "func_exact_para_stem"   , "func_exact_para"   , "func_exact_stem"   , "exact"   ],
	 cs => [                                                                                                  "func_exact_para"   ,                       "exact"   ],
	 hi => [                                                                                                                                              "exact"   ],
	);


sub MAIN{
	mkdir "$experiments_dir/data";
	downloadDataPhase();
	prepareDataForTrainingPhase();

	featureExtractForTraining();
	featureCollectForTraining();

	trainingPhase();
}

sub trainingPhase{
	######## TRAINING PHASE ###########
	print STDERR "######## TRAINING PHASE ###########\n";

	my @dirs = glob ("$readyForTraining/*");

	my $pm = new Parallel::ForkManager($MAX_PROCESSES);

	for my $dir (@dirs){
		my $baseDir = basename($dir);
		$baseDir =~ /^(.+?)_(.+)$/;
		my $lang = $1;
		my $metaTemplate = $2;
		for my $algorithm ("weka"){#, "RankNet"){
#next unless($dir=~/other/ or ($algorithm eq 'RankNet'));
			my $template = $metaTemplate."_$algorithm.yaml";
			my $modelType = $lang."_$metaTemplate"."_$algorithm";
			$pm->start and next;

			my $winnerFile = "$dir/winnerFeatures";
			my $loserFile = "$dir/loserFeatures";
			my $unlabeledFile1 = "$dir/unlabeledFeatures1";
			my $unlabeledFile2 = "$dir/unlabeledFeatures2";

			my $cmd;
			$cmd = "$BEER_CMD -l $lang --modelType $modelType --workingMode train --trainingWinnerFeaturesFile $winnerFile --trainingLoserFeaturesFile $loserFile --modelDescFile $BEER_HOME/templates/$template >> $algorithm.subProcesses.std 2>> $algorithm.subProcesses.err";
			print STDERR "EXECUTING !!! $cmd !!!\n";
			`$cmd`;
			print STDERR "DONE !!! $cmd !!!\n";

			if(-e $unlabeledFile1 and -e $unlabeledFile2){
				$modelType = "tuning_$modelType";
				$cmd = "$BEER_CMD -l $lang --modelType $modelType --workingMode train --trainingWinnerFeaturesFile $winnerFile --trainingLoserFeaturesFile $loserFile --modelDescFile $BEER_HOME/templates/$template".
					"--trainingUnlabeledFeaturesFile1 $unlabeledFile1 --trainingUnlabeledFeaturesFile2 $unlabeledFile2 >> $algorithm.subProcesses.std 2>> $algorithm.subProcesses.err";
				print STDERR "EXECUTING !!! $cmd !!!\n";
				`$cmd`;
				print STDERR "DONE !!! $cmd !!!\n";
			}
			
			$pm->finish;
		}
	}
	$pm->wait_all_children;
	
}

sub featureCollectForTraining{
	######## COLLECT FOR TRAINING PHASE ###########
	print STDERR "######## COLLECT FOR TRAINING PHASE ###########\n";

	mkdir $readyForTraining;

	for my $lang (keys %configTemplates){
		my @models = @{$configTemplates{$lang}};
		for my $modelDesc (@models){
			my $dir = "$readyForTraining/$lang"."_$modelDesc";
			mkdir $dir;
			for my $features (qw/winnerFeatures loserFeatures unlabeledFeatures1 unlabeledFeatures2/){
				my $inputFileName  = "$trainingFeatures/$lang/$modelDesc/$features";
				next unless -e $inputFileName;
				my $targetFileName = "$dir/$features";
				my @content = loadContent($inputFileName);
				append($targetFileName, @content);
			}
		}
	}

	my %reverseConfigTemplates;
	for my $lang (keys %configTemplates){
		my @models = @{$configTemplates{$lang}};
		for my $modelDesc (@models){
			push @{$reverseConfigTemplates{$modelDesc}}, $lang;
		}
	}

	for my $modelDesc (keys %reverseConfigTemplates){
		my $dir = "$readyForTraining/other_$modelDesc";
		mkdir $dir;
		my @langs = @{$reverseConfigTemplates{$modelDesc}};
		for my $lang (@langs){
			for my $features (qw/winnerFeatures loserFeatures unlabeledFeatures1 unlabeledFeatures2/){
				my $inputFileName  = "$trainingFeatures/$lang/$modelDesc/$features";
				next unless -e $inputFileName;
				my $targetFileName = "$dir/$features";
				my @content = loadContent($inputFileName);
				append($targetFileName, @content);
			}
		}
	}
}

sub	featureExtractForTraining{
	######## FEATURE EXTRACTION PHASE ###########
	print STDERR "######## FEATURE EXTRACTION PHASE ###########\n";

	my $pm = new Parallel::ForkManager($MAX_PROCESSES);

	mkdir $trainingFeatures;

	for my $lang (@langsToUseForTraining){
		mkdir "$trainingFeatures/$lang";
		for my $modelDesc (@{$configTemplates{$lang}}){
			my $modelDescFile = "$BEER_HOME/templates/$modelDesc"."_weka.yaml";

			my $featuresDir = "$trainingFeatures/$lang/$modelDesc";
			mkdir $featuresDir;

			my $winnerTextFile = "$training/$lang/SENTENCE_LEVEL_WINNER";
			my $loserTextFile = "$training/$lang/SENTENCE_LEVEL_LOSER";
			my $referenceTextFile = "$training/$lang/SENTENCE_LEVEL_REFERENCE";
			my $unlabeledTextFile1 = "$unlabeledTraining/$lang/unlabeled1";
			my $unlabeledTextFile2 = "$unlabeledTraining/$lang/unlabeled2";
			my $unlabeledReferenceFile = "$unlabeledTraining/$lang/unlabeled_reference";

			my $args = "";

			my $pid = $pm->start and next;

			my $winCmd = "$BEER_CMD $args -l $lang --workingMode factors -s $winnerTextFile -r $referenceTextFile --modelDescFile $modelDescFile";
			print STDERR "EXECUTING !!! $winCmd !!!\n";
			`$winCmd > $featuresDir/winnerFeatures`;
			print STDERR "DONE !!! $winCmd !!!\n";

			my $losCmd = "$BEER_CMD $args -l $lang --workingMode factors -s $loserTextFile -r $referenceTextFile --modelDescFile $modelDescFile";
			print STDERR "EXECUTING !!! $losCmd !!!\n";
			`$losCmd > $featuresDir/loserFeatures`;
			print STDERR "DONE !!! $losCmd !!!\n";

			if(-e $unlabeledTextFile1 and -e $unlabeledTextFile2 and -e $unlabeledReferenceFile){
				my $unlabeledCmd1 = "$BEER_CMD $args -l $lang --workingMode factors -s $unlabeledTextFile1 -r $unlabeledReferenceFile --modelDescFile $modelDescFile";
				print STDERR "EXECUTING !!! $unlabeledCmd1 !!!\n";
				`$unlabeledCmd1 > $featuresDir/unlabeledFeatures1`;
				print STDERR "DONE !!! $unlabeledCmd1 !!!\n";

				my $unlabeledCmd2 = "$BEER_CMD $args -l $lang --workingMode factors -s $unlabeledTextFile2 -r $unlabeledReferenceFile --modelDescFile $modelDescFile";
				print STDERR "EXECUTING !!! $unlabeledCmd2 !!!\n";
				`$unlabeledCmd2 > $featuresDir/unlabeledFeatures2`;
				print STDERR "DONE !!! $unlabeledCmd2 !!!\n";
			}

			print STDERR "***** FINISHED feature extraction for $lang *****\n";


			$pm->finish;
		}
	}
	$pm->wait_all_children;
}

sub downloadDataPhase{
	######## DOWNLOADING DATA PHASE ###########
	print STDERR "######## DOWNLOADING DATA PHASE ###########\n";
	
	# download_WMT14("$experiments_dir/data/wmt14");
	download_WMT13($wmt13);
	download_WMT14($wmt14);
}

sub prepareDataForTrainingPhase{
	######## PREPARING TRAINING DATA PHASE ###########
	print STDERR "######## PREPARING TRAINING DATA PHASE ###########\n";
	mkdir $training;

	for my $lp (@langPairsAvailableForTraining){
		my ($srcLang, $tgtLang) = split /-/, $lp;

		mkdir "$training/$tgtLang";

		for my $type ("WINNER", "LOSER", "REFERENCE"){
			for my $wmtDir ($wmt13, $wmt14){
				my $fileName = "$wmtDir/$lp/SENTENCE_LEVEL_$type.$tgtLang";
				if(-e dirname($fileName)){
					my @content = loadContent($fileName);
					append("$training/$tgtLang/SENTENCE_LEVEL_$type", @content);
				}
			}
		}
	}

}

sub append{
	my ($fn, @content) = @_;
	open my $fh, ">>:encoding(utf-8)", $fn or die "$fn $!";
	for my $line (@content){
		print $fh "$line\n";
	}
	close $fh;
}

sub loadContent{
	my ($fn) = @_;
	my @result;
	open my $fh , "<:encoding(utf-8)", $fn or die "coud not open $fn $!";
	while(<$fh>){
		chomp;
		push @result, $_;
	}
	close $fh;
	return @result;
}

sub download_WMT14{
	my ($targetDir) = @_;
	mkdir $targetDir;
	chdir $targetDir;
	`$application_dir/dataDownloadScripts/download_wmt14.sh`;
	chdir $experiments_dir;
}

sub download_WMT13{
	my ($targetDir) = @_;
	mkdir $targetDir;
	chdir $targetDir;
	`$application_dir/dataDownloadScripts/download_wmt13.sh`;
	chdir $experiments_dir;
}

MAIN(@ARGV);


