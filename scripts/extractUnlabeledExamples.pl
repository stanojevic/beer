#!/usr/bin/perl 
use strict;
use warnings;

die "usage: $0 nbestlist referenceFile sampleSize outputDir" unless @ARGV == 4;
binmode STDOUT, "encoding(UTF-8)";


sub MAIN{
	my ($nbestFn, $referenceFn, $sampleSize, $outputDir) = @_;
	my $nbest = loadNbest($nbestFn);
	my $references = loadSentences($referenceFn);
	my @sampledPairs = sample($nbest, $references, $sampleSize);

	saveSampledPairs(\@sampledPairs, $outputDir);
}

sub loadSentences{
	my ($fn) = @_;
	my @result;
	open my $fh, "<:encoding(UTF-8)", "$fn" or die $!;
	while(<$fh>){
		chomp;
		push @result, $_;
	}
	close $fh;
	return \@result;
}

sub saveSampledPairs{
	my ($sampledPairs, $outputDir) = @_;

	mkdir $outputDir;
	open my $fh1, ">:encoding(UTF-8)", "$outputDir/unlabeled1" or die $!;
	open my $fh2, ">:encoding(UTF-8)", "$outputDir/unlabeled2" or die $!;
	open my $fhRef, ">:encoding(UTF-8)", "$outputDir/unlabeled_reference" or die $!;

	foreach my $pair (@$sampledPairs){
		print $fh1 $pair->[0],"\n";
		print $fh2 $pair->[1],"\n";
		print $fhRef $pair->[2],"\n";
	}

	close $fh1;
	close $fh2;
	close $fhRef;
}

sub sample{
	my ($nbest, $references, $sampleSize) = @_;
	my $sentsTotal = scalar @$nbest;

	my $samplesPerInputSentence = $sampleSize/$sentsTotal;

	my @samples;

	for my $inputSentId (0..$sentsTotal-1){
		my $n = scalar @{$nbest->[$inputSentId]};
		for my $i (1..$samplesPerInputSentence){
			my $rand1 = int(rand($n));
			my $rand2 = int(rand($n));

			my $randBig   = ($rand1 > $rand2)? $rand1 : $rand2;
			my $randSmall = ($rand1 > $rand2)? $rand2 : $rand1;

			my $sent1 = $nbest->[$inputSentId]->[$randBig];
			my $sent2 = $nbest->[$inputSentId]->[$randSmall];
			my $ref = $references->[$inputSentId];
			push @samples, [$sent1, $sent2, $ref];
		}
	}

	return @samples;
}

sub loadNbest{
	my ($fn) = @_;

	my @nbest;
	my $currentSents = [];
	my $currentSentId = 0;

	open my $fh, "-|:encoding(UTF-8)", "zcat $fn" or die $!;
	while(<$fh>){
		chomp;
		my @fields = split / \|\|\| /;
		my $sentId   = $fields[0];
		my $sentText = $fields[1];
		if($currentSentId != $sentId){
			push @nbest, $currentSents;
			$currentSents = [];
			$currentSentId = $sentId;
		}
		push @$currentSents, $sentText;
	}

	close $fh;

	return \@nbest;
}

MAIN(@ARGV);

