package menon.cs6100.program3;

import java.util.Comparator;
import java.util.Map;

public class BordaRankingComparator implements Comparator<String> {
	
	private Map<String, Integer> bordaRanking;
	
	private static final int BEFORE = -1;
	private static final int EQUAL = 0;
	private static final int AFTER = 1;
	
	public BordaRankingComparator(Map<String, Integer> bordaRanking) {
		this.bordaRanking = bordaRanking;
	}
	
	@Override
	public int compare(String candidate1, String candidate2) {
		
		if (candidate1 == null || candidate2 == null) {
			throw new NullPointerException();
		} else if (this.bordaRanking.get(candidate1).intValue() > this.bordaRanking.get(candidate2).intValue()) {
			return BEFORE;
		} else if (this.bordaRanking.get(candidate1).intValue() < this.bordaRanking.get(candidate2).intValue()) {
			return AFTER;
		} else {
			return EQUAL;
		}
	}

}
