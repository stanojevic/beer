#!/usr/bin/perl -w
use strict;


my %convert = ( "English"=>"en",
                "Czech"  =>"cs",
                "German" =>"de",
                "French" =>"fr",
                "Spanish"=>"es");

while(<STDIN>){
    chomp;
    if(/^srclang,trglang/){
        print "$_\n";
    }else{
        my @vals = split /,/,$_;
        my $srcLang = $convert{$vals[0]};
        my $tgtLang = $convert{$vals[1]};
        my $lpair="$srcLang-$tgtLang";
        my @indicesToChange = (7,9,11,13,15);
        for my $index (@indicesToChange){
            $vals[$index]="newstest2012.$lpair.".$vals[$index];
        }
        print join(",", @vals),"\n";
    }
}

