#!/usr/bin/env perl
use strict;
use warnings;

use File::Path qw/ remove_tree /;
use File::Temp qw/ tempdir     /;
use Getopt::Long;
use Parallel::ForkManager;

our $MAX_PROCESSES = 1;
our $TMP_DIR = tempdir("MBR_experiment_XXXXXXX", TMPDIR=>1);
END{remove_tree($TMP_DIR)}

binmode STDOUT, ":utf8";

sub MAIN{
	my ($metricCMD, $nbestFile, $extractBest, $mbrSize, $dumpDir, @scalings) = @_;

	my @unfilteredNBest = parseMosesNBest($nbestFile);

	my @filteredNBest = selectTopN($mbrSize, @unfilteredNBest);

	my @uniqPairs = extractUniqPairs(@filteredNBest);

	my %cachedEvals = evaluate($metricCMD, @uniqPairs); # sys=>ref=>score

	for my $scaling (@scalings){
		my @nbest = computeScaledProbabilities($scaling, @filteredNBest);

		my @expectedBest = MBR(\@nbest, \%cachedEvals);

		open my $fh, ">encoding(utf-8)", "$dumpDir/result_scaling_$scaling" or die $!;

		if($extractBest){
			for my $id (0..$#expectedBest){
				my @sortedBest = sort {$b->{expectedScore} <=> $a->{expectedScore}} @{$expectedBest[$id]};
				print $fh $sortedBest[0]->{string}, "\n";
			}
		}else{
			for my $id (0..$#expectedBest){
				my @nbest = @{$expectedBest[$id]};
				for my $entry (@nbest){
					print $fh join(" ||| ", $id, $entry->{string}, $entry->{scaledProb}, $entry->{expectedScore}),"\n";
				}
			}
		}

		close $fh;
	}

}

sub selectTopN{
	my ($n, @unfiltered) = @_;
	my @filtered;
	for my $i (0..$#unfiltered){
		my @sorted = sort {$b->{rawScore} <=> $a->{rawScore}} @{$unfiltered[$i]};
		my @selection;
		for my $j (0..$n-1){
			last if $j > $#sorted;
			push @selection, $sorted[$j];
		}
		push @filtered, [@selection];
	}
	return @filtered;
}

sub MBR{
	my ($nbests, $cachedEvals) = @_;

	my @nbests = @$nbests;
	my %cachedEvals = %$cachedEvals;

	for my $id (0..$#nbests){

		my $currNbest = $nbests->[$id];

		for my $entry (@$currNbest){

			my $expectedScore = 0;

			my $sysStr = $entry->{string};

			for my $otherEntry (@$currNbest){
				my $refStr = $otherEntry->{string};
				my $risk = $cachedEvals{$sysStr}{$refStr};

				my $scaledProb = $otherEntry->{scaledProb};

				$expectedScore += $risk*$scaledProb;
			}

			$entry->{expectedScore} = $expectedScore;
		}
	}

	return @$nbests;
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

sub evaluate{
	my ($metricCMD, @pairs) = @_;

	my $evalsPerProcess = int(scalar(@pairs)/$MAX_PROCESSES);
	$evalsPerProcess++ if scalar(@pairs) % $MAX_PROCESSES != 0;

	my @sysFiles;
	my @refFiles;
	my @resultFiles;

	my $currentPairId = 0;
	foreach my $process_id (0..$MAX_PROCESSES-1){
		print STDERR "preparing data for process $process_id\n";

		my $outputFn = "$TMP_DIR/scores$process_id";
		push @resultFiles, $outputFn;

		my $sysFn = "$TMP_DIR/system$process_id";
		push @sysFiles, $sysFn;

		my $refFn = "$TMP_DIR/reference$process_id";
		push @refFiles, $refFn;

		open my $sysFh, ">encoding(utf-8)", $sysFn or die $!;
		open my $refFh, ">encoding(utf-8)", $refFn or die $!;

		while($currentPairId<($process_id+1)*$evalsPerProcess &&
			$currentPairId < scalar(@pairs)){
			my $pair = $pairs[$currentPairId];
			print $sysFh $pair->[0], "\n";
			print $refFh $pair->[1], "\n";
			$currentPairId++;
		}

		close $sysFh;
		close $refFh;
	}

	@pairs = ();
	my $pm = new Parallel::ForkManager($MAX_PROCESSES);

	foreach my $process_id (0..$MAX_PROCESSES-1){

		my $sysFn = $sysFiles[$process_id];
		my $refFn = $refFiles[$process_id];
		my $outputFn = $resultFiles[$process_id];

		my $realCMD = $metricCMD;
		$realCMD =~ s/__S__/$sysFn/;
		$realCMD =~ s/__R__/$refFn/;
		$pm->start and next;
		print STDERR "starting process $process_id\n";
		exec "$realCMD > $outputFn" or die $!;
		$pm->finish;
	}

	$pm->wait_all_children;
	print STDERR "all processes finished\n";

	my %cachedEvals;
	foreach my $process_id (0..$MAX_PROCESSES-1){
		my $sysFn = $sysFiles[$process_id];
		my $refFn = $refFiles[$process_id];
		my $resultFn = $resultFiles[$process_id];
		my @currSysSents = loadContent($sysFn);
		my @currRefSents = loadContent($refFn);
		my @currScores = loadContent($resultFn);
		for my $i (0..$#currSysSents){
			my $sys = $currSysSents[$i];
			my $ref = $currRefSents[$i];
			my $score = $currScores[$i];
			$cachedEvals{$sys}{$ref} = $score ;
		}
	}

	return %cachedEvals;
}

sub extractUniqPairs{
	my (@nbests) = @_;
	my @evaluationPairs;

	for my $id (0..$#nbests){
		my @nbest = @{$nbests[$id]};
		my %uniqs;
		for my $entry (@nbest){
			$uniqs{$entry->{string}} = 0;
		}
		foreach my $string1 (keys %uniqs){
			foreach my $string2 (keys %uniqs){
				push @evaluationPairs, [$string1, $string2];
				if($string1 ne $string2){
					push @evaluationPairs, [$string2, $string1];
				}
			}
		}
		print STDERR $id,"\n" if $id % 500 == 0;
	}

	return @evaluationPairs;
}

sub parseMosesNBest{
	my ($nbestFile) = @_;
	my @nBest;
	open my $fh, "<encoding(utf-8)", $nbestFile or die $!;
	while(<$fh>){
		chomp;
		my @fields = split / \|\|\| /;
		my $id = $fields[0];
		my $string = $fields[1];
		my $modelScore = $fields[3];
		push @{$nBest[$id]}, {string=>$string, rawScore => $modelScore};
	}
	close $fh;
	return @nBest;
}

sub computeScaledProbabilities{
	my ($alpha, @nbests) = @_;

	my @scaledBest;

	for my $i (0..$#nbests){
		my @nbest = @{$nbests[$i]};
		my $normalization = eval join '+', map {exp($_->{rawScore}*$alpha)} @nbest;
		for my $entry (@nbest){
			if($normalization == 0){
				#everything is uniform then (we will assume that)
				$entry->{scaledProb} = 1/scalar(@nbest);
			}else{
				$entry->{scaledProb} = exp($entry->{rawScore}*$alpha)/$normalization;
			}
			push @{$scaledBest[$i]}, $entry;
		}
	}

	return @scaledBest;
}

my $metricCMD = undef;
my $nbestFile = undef;
my $extractMBR = 0;
my $help = undef;
my $mbrSize = 100000;
my @scaling;
my $dumpDir;

GetOptions(
	"metricCMD=s" => \$metricCMD,
	"nbest=s" => \$nbestFile,
	"extractMBR" => \$extractMBR,
	"scaling:f{0,}" => \@scaling,
	"dumpDir:s" => \$dumpDir,
	"help|h" => \$help,
	"mbrSize:i" => \$mbrSize,
	"CPUs:i" => \$MAX_PROCESSES) or
	die "usage: $0 --metricCMD 'metricCommand __S__ __R__' --nbest nbest --extractMBR --scaling 1.0 --CPUs 4\n";

if($help){
	print "usage: $0 --metricCMD 'metricCommand __S__ __R__' --nbest nbest --extractMBR --scaling 1.0 --CPUs 4\n";
	exit;
}

@scaling = (1.0) unless @scaling;

if(not defined $metricCMD or not defined $nbestFile or not defined $dumpDir){
	print STDERR "metricCMD and nbestFile are required arguments\n";
	die "usage: $0 --metricCMD 'metricCommand __S__ __R__' --nbest nbest --dumpDir --extractMBR --scaling 1.0 --CPUs 4\n";
}

MAIN($metricCMD, $nbestFile, $extractMBR, $mbrSize, $dumpDir, @scaling);



