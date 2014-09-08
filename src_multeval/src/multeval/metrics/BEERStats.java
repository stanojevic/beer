package multeval.metrics;

import java.util.ArrayList;
import java.util.List;

public class BEERStats extends SuffStats<BEERStats> {
	
	public final List<Object> importances = new ArrayList<Object>();
	public final List<scala.collection.immutable.Map<String, Object>> sentStats = new ArrayList<scala.collection.immutable.Map<String, Object>>() ;
	
	public BEERStats(){
	}

	public BEERStats(double importance, scala.collection.immutable.Map<String, Object> sentStats){
		this.importances.add(new Double(importance));
		this.sentStats.add(sentStats);
	}

	@Override
	public void add(BEERStats other) {
		for(Object importance : other.importances)
			this.importances.add(importance);
		for(scala.collection.immutable.Map<String, Object> sentStats : other.sentStats)
			this.sentStats.add(sentStats);
	}

	@Override
	public SuffStats<BEERStats> create() {
		return new BEERStats();
	}

}
