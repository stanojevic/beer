package multeval.metrics;

import jannopts.*;

import java.io.File;
import java.net.URL;
import java.util.List;
import multeval.util.*;
import com.google.common.primitives.*;
import com.google.common.collect.*;
import com.google.common.base.*;

import scala.collection.JavaConversions;

import com.google.common.base.Stopwatch;

import multeval.util.LibUtil;

public class BEER extends Metric<BEERStats>{
	
	public beer.BEER scorer;

    @Option(shortName = "l", longName = "beer.language", usage = "language code of a supported BEER language (e.g. 'en')", required = true)
	String language;
	
    @Option(shortName = "n", longName = "beer.norm", usage = "should the input be normalized (tokenized)", defaultValue = "true")
	boolean norm;
	
    @Option(shortName = "u", longName = "beer.lowercase", usage = "should the input be lowercased", defaultValue = "true")
	boolean lower;
	
    @Option(shortName = "a", longName = "beer.arguments", usage = "other arguments for beer", defaultValue = "")
    String arguments;
    
    String beerHome = getBeerHome();
    String beerArguments = "";
    String beerConfigFile = beerHome+"/configuration.yaml";
    
    private String getBeerHome(){
		Class<?> clazz = null;
		try {
			clazz = Class.forName("beer.BEER");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URL where = clazz.getProtectionDomain().getCodeSource().getLocation();
		String home = new File(where.toString()).getParent().replaceFirst("file:", "");
		return home;
    }
    
    private String createArguments(){
    	String normPart = "";
    	if(!norm){
    		normPart = "--noNorm";
         	normPart += " ";
    	}
    	
    	String lowerPart = "";
    	if(!lower){
    		lowerPart = "--noLower";
        	lowerPart += " ";
    	}

    	return "-l "+language+" "+normPart+lowerPart+arguments;
    }
    
	@Override
	public BEERStats stats(String sentence, List<String> refs) {
		double avgLen = 0;
		for (String ref : refs) {
			int wordCount = ref.split(" +").length;
			avgLen += wordCount;
		}
		avgLen /= refs.size();

		scala.collection.immutable.Map<String, Object> chosenFeatures = null;
		double currentMaxScore = 0;
		for (String ref : refs) {
			scala.collection.immutable.Map<String, Object> sentFeatures =
					scorer.factors(sentence, ref); // JavaConversions.asScalaBuffer(refs).toList());
			double score = scorer.evaluate(sentFeatures);
			if(score>currentMaxScore){
				currentMaxScore = score;
				chosenFeatures = sentFeatures;
			}
		}
		return new BEERStats(avgLen, chosenFeatures);
	}

	@Override
	public double score(BEERStats suffStats) {
		double score = scorer.evaluateCorpus(
				JavaConversions.asScalaBuffer(suffStats.importances).toList(),
				JavaConversions.asScalaBuffer(suffStats.sentStats).toList());
		return score*1000;
	}

	@Override
	public void configure(Configurator opts) throws ConfigurationException {
		opts.configure(this);
		beerArguments = createArguments();
		////////////////////////////////////////////////////////////////
    	System.err.println("beer.lowercase "+lower);
    	System.err.println("beer.norm "+norm);
    	System.err.println("beer.language "+language);
    	System.err.println("beer.arguments "+arguments);

    	System.err.println("beerArguments "+beerArguments);
    	System.err.println("beerHome "+beerArguments);
    	System.err.println("beerConfigFile "+beerConfigFile);
		////////////////////////////////////////////////////////////////

	    LibUtil.checkLibrary("beer.BEER", "BEER");
	    System.err.println("Using BEER Version " + beer.Constants.VERSION());
	    
	    System.err.println("Loading BEER ...");
	    Stopwatch watch = new Stopwatch();
	    watch.start();
	    scorer = new beer.BEER(beerArguments, beerHome, beerConfigFile);
	    watch.stop();
	    System.err.println("Loaded BEER in " + watch.toString(3));
	}

	@Override
	public boolean isBiggerBetter() {
		return true;
	}

	@Override
	public String getMetricDescription() {
		StringBuilder builder = new StringBuilder();
		builder.append("BEER V"+beer.Constants.VERSION());
		builder.append(" arguments="+arguments);
				
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return "BEER";
	}

	@Override
	public Metric<?> threadClone() {
		BEER metric = new BEER();
		String arguments = scorer.arguments();
		String beerHome = scorer.beerHome(); 
		String configurationFile = scorer.configurationFile();
		metric.scorer = new beer.BEER(arguments, beerHome, configurationFile);
		return metric;
	}

}
