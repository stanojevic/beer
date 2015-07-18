#!/bin/bash

# nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --corpusCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l __LANG__ -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs cs-en  de-en  en-cs  en-de  en-ru  fi-en  ru-en --metricName BEER --CPUs 10 > BEER.sys.score 2> BEER.sys.err &

# nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --corpusCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l other -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs en-fi --metricName BEER --CPUs 10 > BEER.sys.score 2> BEER.sys.err &

# nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --sentCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l __LANG__ -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs cs-en  de-en  en-cs  en-de  en-ru  fi-en  ru-en  --metricName BEER --CPUs 10 > BEER.seg.score 2> BEER.seg.err &

# nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --sentCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l other -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs en-fi  --metricName BEER --CPUs 10 > BEER.seg.score 2> BEER.seg.err &


#nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newsdiscusstest2015/ --corpusCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l __LANG__ -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs en-fr fr-en --metricName BEER --CPUs 10 > BEER_discourse.sys.score 2> BEER_discourse.sys.err &

#nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newsdiscusstest2015/ --sentCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer -l __LANG__ -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs en-fr fr-en  --metricName BEER --CPUs 10 > BEER_discourse.seg.score 2> BEER_discourse.seg.err &


# no syntax english

nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newsdiscusstest2015/ --corpusCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer  --modelType evaluationNoSyntax -l __LANG__ -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs fr-en --metricName BEER_no_syntax --CPUs 10 > BEER_no_syntax_discourse.sys.score 2> BEER_no_syntax_discourse.sys.err &

nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newsdiscusstest2015/ --sentCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer  --modelType evaluationNoSyntax -l __LANG__ -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs fr-en  --metricName BEER_no_syntax --CPUs 10 > BEER_no_syntax_discourse.seg.score 2> BEER_no_syntax_discourse.seg.err &

nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --corpusCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer  --modelType evaluationNoSyntax -l __LANG__ -s __S__ -r __R__ | sed "sBcorpus beer        : BB"'  --langPairs cs-en  de-en  fi-en  ru-en --metricName BEER_no_syntax --CPUs 10 > BEER_no_syntax.sys.score 2> BEER_no_syntax.sys.err &

nohup /home/mstanoj1/experiments/2015_WMT_metrics_task/beer_1.1_alpha_dep/scripts/test.pl --dataDir /home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/wmt15-metrics-task/txt/system-outputs/newstest2015/ --sentCmd '/home/mstanoj1/experiments/2015_WMT_metrics_task/wmt15/../beer_1.1_alpha_dep/beer  --modelType evaluationNoSyntax -l __LANG__ -s __S__ -r __R__ --printSentScores | sed "sBbest beer     : BB" | grep -v corpus' --langPairs cs-en  de-en  fi-en  ru-en  --metricName BEER_no_syntax --CPUs 10 > BEER_no_syntax.seg.score 2> BEER_no_syntax.seg.err &

