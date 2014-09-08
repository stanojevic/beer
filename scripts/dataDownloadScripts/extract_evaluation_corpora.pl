#!/usr/bin/perl
#use warnings;
use strict;
use utf8; #maybe not necessary
use File::Copy qw/copy/;

binmode STDOUT, ":utf8";

die "usage: $0 manual_eval.csv wmt-data-dir" unless @ARGV == 2;

sub process{
    my ($srcLang,
        $tgtLang,
        $srcIndex,
        $docId,
        $segId,
        $judgeId,
        $sys1Num,
        $sys1Id,
        $sys2Num,
        $sys2Id,
        $sys3Num,
        $sys3Id,
        $sys4Num,
        $sys4Id,
        $sys5Num,
        $sys5Id,
        $sys1Rank,
        $sys2Rank,
        $sys3Rank,
        $sys4Rank,
        $sys5Rank,
        $system) = @_;
    my @sysRanks = ($sys1Rank, $sys2Rank, $sys3Rank, $sys4Rank, $sys5Rank ) ;
    my @sysIds   = ($sys1Id, $sys2Id, $sys3Id, $sys4Id, $sys5Id ) ;
    my @sysNums  = ($sys1Num, $sys2Num, $sys3Num, $sys4Num, $sys5Num ) ; #useless
    #output format : $srcLang, $tgtLang, $srcIndex, $sysLeftId, $sysRightId, $ref
    # left is always better than right i.e. left has lower number/rank
    my @resultingPairs;
    for my $i (0..3){
        for my $j ($i+1..4){
            next if $sysRanks[$i] == $sysRanks[$j];
            my ($winner, $looser);
            if($sysRanks[$i] < $sysRanks[$j]){
                $winner = $i;
                $looser = $j;
            }else{
                $winner = $j;
                $looser = $i;
            }
            my $winnerId = $sysIds[$winner];
            my $winnerSent = $system->{$srcLang}->{$tgtLang}->{$winnerId}->[$srcIndex];
            my $looserId = $sysIds[$looser];
            my $looserSent = $system->{$srcLang}->{$tgtLang}->{$looserId}->[$srcIndex];
            my $refSent = $system->{$srcLang}->{$tgtLang}->{_ref}->[$srcIndex];
            push @resultingPairs, [$srcLang,
                                   $tgtLang, 
                                   $srcIndex, 
                                   $winnerId, 
                                   $looserId,
                                   $winnerSent,
                                   $looserSent,
                                   $refSent];
        }
    }
    return @resultingPairs;
}

sub getRefFn{
    my ($srcLang, $tgtLang) = @_;
    my $path = "plain/references/.*-ref\.$tgtLang";
    return $path;
}

sub loadRefs{
    my ($sysOutDir) = @_;
    my $refs = {};

    my @refFiles = glob("$sysOutDir/plain/references/*-ref*");
	if($refFiles[0]=~/\...-..$/){
		#wmt 14 style
		for my $refFile (@refFiles){
			$refFile=~/(..)-(..)$/;
			my $srcLang=$1;
			my $tgtLang=$2;
			my $lineNo=1;
			open my $fh, "<:encoding(UTF-8)", $refFile or die $!;
			while(<$fh>){
				chomp;
				$refs->{$srcLang}->{$tgtLang}->[$lineNo]=$_;
				$lineNo++;
			}
			close $fh;
		}
	}else{
		my @langs = map {/(..)$/; $1} @refFiles;
		for my $refFile (@refFiles){
			$refFile=~/(..)$/;
			my $tgtLang=$1;
			my $lineNo=1;
			open my $fh, "<:encoding(UTF-8)", $refFile or die $!;
			while(<$fh>){
				chomp;
				for my $srcLang (@langs){
					next if $srcLang eq $tgtLang;
					$refs->{$srcLang}->{$tgtLang}->[$lineNo]=$_;
				}
				$lineNo++;
			}
			close $fh;
		}
	}

    return $refs;
}

sub loadSystem{
    my ($sysOutDir) = @_;
    my $systemSents={}; # src->tgt->system->line
    my @files = 
        glob("$sysOutDir/plain/system-outputs/newstest20*/*-*/*");
    for my $fn (@files){
        $fn=~/plain\/system-outputs\/newstest20..\/(.*)-(.*)\/(.*?)$/;
        my $srcLang = $1;
        my $tgtLang = $2;
        my $system  = $3;

        my $lineNo = 1;
        open my $fh, "<:encoding(UTF-8)", $fn or die $!;
        #warn "start $fn\n";
        while(<$fh>){
            chomp;
            $systemSents->{$srcLang}->{$tgtLang}->{$system}->[$lineNo]=$_;
            $lineNo++;
        }
        #warn "done $fn\n";
        close $fh;
    }

    return $systemSents;
}

sub addRefsToSystem{
    my ($system,$refs) = @_;
    for my $srcLang (keys %$system){
        for my $tgtLang (keys %{$system->{$srcLang}}){
            $system->{$srcLang}->{$tgtLang}->{_ref}=[@{$refs->{$srcLang}->{$tgtLang}}];
        }
    }
    return $system; #doesn't matter since $system is already changed
}

sub renameLanguages{
    my ($isCz, @vals) = @_;
    return map {
        if    ($_ eq 'Czech'){
            if($isCz){
                'cz'
            }else{
                'cs'
            }
        }elsif($_ eq 'Russian'){
            'ru';
        }elsif($_ eq 'English'){
            'en';
        }elsif($_ eq 'Hindi'){
            'hi';
        }elsif($_ eq 'German'){
            'de';
        }elsif($_ eq 'French'){
            'fr';
        }elsif($_ eq 'Spanish'){
            'es';
        }else{
            $_;
        } } @vals;
}

sub isCz{
    my ($sysOutDir) = @_;
    my @refFiles = glob("$sysOutDir/plain/references/*-ref*");
    my $isCz = 0;
    for my $refFile (@refFiles){
        $isCz=1 if /cz$/;
    }
    return $isCz;
}

sub getLangs{
    my ($sysOutDir) = @_;
    my @refFiles = glob("$sysOutDir/plain/references/*-ref*");
    my @langs = map {/(..)$/; $1} @refFiles;
    return @langs;
}


sub prepareDirectories{
    my @langPairs = @_;
    #my (@langs) = @_;

    #my @langPairs = makeLangPairs(@langs);

    for my $langPair (@langPairs){
        my ($srcLang, $tgtLang) = @$langPair;
        my $langPairStr = "$srcLang-$tgtLang";
        mkdir $langPairStr;
    }
}

sub makeLangPairs{
    my @langs = @_;
    my @langPairs;
    for my $lang (@langs){
        next if $lang eq 'en';
        push @langPairs, ['en' , $lang];
        push @langPairs, [$lang, 'en' ];
    }
    return @langPairs;
}

sub makeFhs{
    my @langPairs = @_;
    my %fhs;
    for my $langPair (@langPairs){
        my ($srcLang, $tgtLang) = @$langPair;
        my $strPair = "$srcLang-$tgtLang";
        open my $fhWinner, ">:encoding(UTF-8)", 
            "$strPair/SENTENCE_LEVEL_WINNER.$tgtLang" or die $!;
        open my $fhLooser, ">:encoding(UTF-8)", 
            "$strPair/SENTENCE_LEVEL_LOSER.$tgtLang" or die $!;
        open my $fhRef   , ">:encoding(UTF-8)", 
            "$strPair/SENTENCE_LEVEL_REFERENCE.$tgtLang" or die $!;
        open my $fhSystemHumanScores, ">:encoding(UTF-8)", 
            "$strPair/SYSTEM_RANKINGS.txt" or die $!;
        $fhs{$strPair}{winner} = $fhWinner;
        $fhs{$strPair}{looser} = $fhLooser;
        $fhs{$strPair}{ref}  = $fhRef;
        $fhs{$strPair}{humanRanks} = $fhSystemHumanScores;
    }
    return \%fhs;
}

sub copySystemAndRefCorpora{
    my ($systems_output_dir) = @_;

    for my $sysFn (glob("$systems_output_dir/plain/system-outputs/newstest20*/*-*/*")){
        $sysFn=~/plain\/system-outputs\/newstest20..\/(.*)-(.*)\/(.*?)$/;
        my $srcLang = $1;
        my $tgtLang = $2;
        my $sysName = $3;
        copy $sysFn, "$srcLang-$tgtLang/$sysName" or die $!;
    }
    my @refFiles = glob("$systems_output_dir/plain/references/*-ref*");
    my @langs = map {/(..)$/;$1} @refFiles;
    for my $refFn (@refFiles){
        $refFn=~/(..)$/;
        my $tgtLang = $1;
        if($tgtLang ne 'en'){
            copy $refFn, "en-$tgtLang/referenceSystem.$tgtLang" or die $!;
        }else{
            for my $srcLang (@langs){
                next if $srcLang eq 'en';
                copy $refFn, "$srcLang-en/reference_corpus.en" or die $!;
            }
        }
    }
}

sub MAIN{
    #my ($input_fn, $systems_outputs_dir, $pairs_out_fn, $system_out_fn) = @_;
    my ($input_fn, $systems_outputs_dir) = @_;

    my %system_wins; # srcLang->tgtLang->system->{win,loss}=number
                     # we do not care about ties because of Bojar et al 2011


    my @langs = getLangs($systems_outputs_dir); 
    my @langPairs = makeLangPairs(@langs);
    prepareDirectories(@langPairs);
    my $pairsFhs = makeFhs(@langPairs); # langPair->winner|looser|ref|humanRanks 
    copySystemAndRefCorpora($systems_outputs_dir, @langPairs); 

#open my $pairs_out_fh, ">:encoding(UTF-8)", $pairs_out_fn or die $!;
#open my $system_out_fh, ">:encoding(UTF-8)", $system_out_fn or die $!;




    my $refs = loadRefs($systems_outputs_dir);
    my $isCz = isCz($systems_outputs_dir);
    my $system = loadSystem($systems_outputs_dir);
    addRefsToSystem($system,$refs);
    open my $fh, "<:encoding(UTF-8)", $input_fn or die $!; #maybe not utf8?
    <$fh>; #ignore first line
    while(<$fh>){
        chomp;
        my (@vals) = split /,/;
        @vals = renameLanguages($isCz, @vals);
        my @pairs = process(@vals, $system);
        foreach my $pair (@pairs){
#print $pairs_out_fh join(" ||| ",@$pair), "\n";

            my $srcLang = $pair->[0];
            my $tgtLang = $pair->[1];
            my $winnerId= $pair->[3];
            my $looserId= $pair->[4];
            my $winnerSent = $pair->[5];
            my $looserSent = $pair->[6];
            my $refSent    = $pair->[7];

            my $winnerFh = $pairsFhs->{"$srcLang-$tgtLang"}->{winner};
            print $winnerFh "$winnerSent\n";
            my $looserFh = $pairsFhs->{"$srcLang-$tgtLang"}->{looser};
            print $looserFh "$looserSent\n";
            my $refFh = $pairsFhs->{"$srcLang-$tgtLang"}->{ref};
            print $refFh "$refSent\n";

            $system_wins{$srcLang}{$tgtLang}{$winnerId}{win}++;
            $system_wins{$srcLang}{$tgtLang}{$looserId}{loss}++;
        }
    }
    close $fh;

    for my $srcLang (keys %system_wins){
        for my $tgtLang (keys %{$system_wins{$srcLang}}){
            my $systems_scores = $system_wins{$srcLang}{$tgtLang};
            my %systems_ratio_of_wins;
            for my $system (keys %{$systems_scores}){
                $systems_ratio_of_wins{$system} = 
                    $systems_scores->{$system}->{win}  /
                    ($systems_scores->{$system}->{win}  +
                     $systems_scores->{$system}->{loss} );
            }
            for my $system (sort 
                    {$systems_ratio_of_wins{$a} <=> $systems_ratio_of_wins{$b}} 
                    keys %systems_ratio_of_wins){
                #print $system_out_fh join(" ||| ",$srcLang, $tgtLang, $system, $systems_ratio_of_wins{$system}), "\n";
                my $humanRanksFh = $pairsFhs->{"$srcLang-$tgtLang"}->{humanRanks};
                #print $humanRanksFh join(" ||| ",$srcLang, $tgtLang, $system, $systems_ratio_of_wins{$system}), "\n";
                print $humanRanksFh "$system $systems_ratio_of_wins{$system}\n";
            }
        }
    }
#close $system_out_fh;
#    close $pairs_out_fh;

    for my $langPair (keys %$pairsFhs){
        close $pairsFhs->{$langPair}->{winner};
        close $pairsFhs->{$langPair}->{looser};
        close $pairsFhs->{$langPair}->{ref};
        close $pairsFhs->{$langPair}->{humanRanks};
    }
}

MAIN(@ARGV);

