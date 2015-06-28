package menon.cs6100.program3;

public class CandidatePair {
	
	private String dominatingCandidate;
	private String dominatedCandidate;
	
	public CandidatePair(String dominatingCandidate, String dominatedCandidate) {
		this.dominatingCandidate = dominatingCandidate;
		this.dominatedCandidate = dominatedCandidate;
	}

	public String getDominatingCandidate() {
		return dominatingCandidate;
	}

	public void setDominatingCandidate(String dominatingCandidate) {
		this.dominatingCandidate = dominatingCandidate;
	}

	public String getDominatedCandidate() {
		return dominatedCandidate;
	}

	public void setDominatedCandidate(String dominatedCandidate) {
		this.dominatedCandidate = dominatedCandidate;
	}

	public int hashCode() {
		
		String concatenatedCandidates = this.dominatingCandidate + this.dominatedCandidate;
		return concatenatedCandidates.hashCode();
		
	}
	
	public boolean equals(Object anotherPair) {
		
		if (anotherPair instanceof CandidatePair) {
			if (((CandidatePair) anotherPair).dominatingCandidate.equals(this.dominatingCandidate) &&
			    ((CandidatePair) anotherPair).dominatedCandidate.equals(this.dominatedCandidate)) {
				return true;
			}
		}
		
		return false;
		
	}
}
