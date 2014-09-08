#!/usr/bin/perl 
use strict;
use warnings;

my ($srcCorp, $refCorp, $lang) = @ARGV;

print STDERR "srcCorp $srcCorp\n";
print STDERR "refCorp $refCorp\n";
print STDERR "lang $lang\n";

my $line = 0;

open my $fh, $srcCorp or die $!;
while(<$fh>){
	$line ++;
}
close $fh;
print STDERR "processed $line\n";
print "0.2\n";
