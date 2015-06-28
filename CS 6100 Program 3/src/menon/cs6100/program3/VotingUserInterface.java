package menon.cs6100.program3;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
 
@SuppressWarnings("serial")
public class VotingUserInterface extends JFrame {
	
    private static final String[] ELECTION_CANDIDATES = {"Alex", "Bart", "Cindy", "David", "Erik", "Frank", "Greg"};
    private static final String[] ELECTION_VOTERS = {"A", "B", "C", "D"};
    private static final String[] VOTER_WEIGHTS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    private static final String[] CANDIDATE_RANKS = {"1", "2", "3", "4", "5", "6", "7"};
    private static final String NEW_LINE = "\n";
    private static final String CANDIDATE_BEATS = " --beats--> ";
    private static final int MANUAL_VOTING_MODE = 1;
    private static final int RANDOM_VOTING_MODE = 2;
    private static final int SINGLE_PEAKED_VOTING_MODE = 3;
    private static final int UNIFORM_VOTING_MODE = 4;

    
    private static final Random randomWeightGenerator = new Random();
    private static final Random randomRankGenerator = new Random();
    
    private String[] randomCandidateRanks = null;
    private Map<String, String[]> votersRanks = null;
    private String removedCandidate = null;
    
    private Map<String, Integer> pluralityVoterWeights = null;
    private Map<String, Map<Integer, String>> pluralityVoteCandidateRanks = null;
    private Map<String, Map<String, Integer>> unvalidatedVoterCandidateRanks = null;
    private Map<String, Integer> singlePeakedPreferenceVoterChoices = null;
    private Map<CandidatePair, Integer> uniformVotingRanks = null;
    
    private int votingMode = 0;
    
    public VotingUserInterface(String name) {
        super(name);
        setResizable(false);
    }
    
    /**
     * @param majorityGraph
     * @return a string containing the majority graph as a list of "Candidate1 --> Candidate2" if the first beats the second
     */
    private String getMajorityGraphIllustration(Map<CandidatePair, Integer> majorityGraph) {
    	
    	StringBuffer returnValue = new StringBuffer();
    	
    	//Loop through all pairs in the majority graph
    	Set<CandidatePair> candidatePairsInMajorityGraph = majorityGraph.keySet();
    	for (CandidatePair candidatePair : candidatePairsInMajorityGraph) {
    		
    		//Check if pair needs to be inverted
			if (majorityGraph.get(candidatePair).intValue() > 0) {
				returnValue.append(candidatePair.getDominatingCandidate()).append(CANDIDATE_BEATS).append(candidatePair.getDominatedCandidate());
			} else {
				returnValue.append(candidatePair.getDominatedCandidate()).append(CANDIDATE_BEATS).append(candidatePair.getDominatingCandidate());
			}
			
    		returnValue.append(NEW_LINE);
   	
    	}
    	
    	return returnValue.toString();
    	
    }
    
    /**
     * @return true if user inputs are valid
     */
    private boolean manualVotesAreValid() {
    	
    	Map<String, Integer> unvalidatedVoterRankings = null;
    	Map<Integer, String> validatedVoterRankings = null;
    	Set<String> candidates = null;
    	StringBuffer errorMessage = new StringBuffer();
        this.pluralityVoteCandidateRanks = new HashMap<String, Map<Integer, String>>();
   	
    	for (String voter : ELECTION_VOTERS) {
    		
    		validatedVoterRankings = new HashMap<Integer, String>();
    		
    		//Get rank selections for each voter
    		unvalidatedVoterRankings = unvalidatedVoterCandidateRanks.get(voter);
    		candidates = unvalidatedVoterRankings.keySet();
    		
    		//Loop through each candidate in voter rankings
    		for (String candidate : candidates) {
    			
    			//If duplicate rank, show error message
    			if (validatedVoterRankings.containsKey(unvalidatedVoterRankings.get(candidate))) {
    				errorMessage.append("Duplicate rank given by voter ").append(voter).append(" for candidate ").append(candidate).append(".");
    				JOptionPane.showMessageDialog(this, errorMessage.toString(), "Duplicate Rank", JOptionPane.PLAIN_MESSAGE);
    				return false;
    			} else {
    				//Add the ranking to the validated ranking
    				validatedVoterRankings.put(unvalidatedVoterRankings.get(candidate), candidate);
    			}
    		}
    		
    		pluralityVoteCandidateRanks.put(voter, validatedVoterRankings);
    	}
    	
    	return true;
    }
    /**
     * @return true if user inputs are valid
     */
    private boolean userInputsAreValid() {
    	
    	if (this.votingMode == MANUAL_VOTING_MODE) {
    		return manualVotesAreValid();
    	} else {
    		return true;
    	}
    }
    
    /**
     * Show majority graph based on voting results
     */
    private void showMajorityGraph() {
    	
    	if ((this.votingMode == MANUAL_VOTING_MODE && userInputsAreValid()) || this.votingMode == RANDOM_VOTING_MODE) {
        	Map<CandidatePair, Integer> majorityGraph = getMajorityGraphFromPluralityVotes();
        	JOptionPane.showMessageDialog(this, getMajorityGraphIllustration(majorityGraph), "Majority Graph", JOptionPane.PLAIN_MESSAGE);
    	} else if (this.votingMode == SINGLE_PEAKED_VOTING_MODE) {
        	Map<CandidatePair, Integer> majorityGraph = getMajorityGraphFromSinglePeakedPreference();
        	JOptionPane.showMessageDialog(this, getMajorityGraphIllustration(majorityGraph), "Majority Graph", JOptionPane.PLAIN_MESSAGE);
    	} else if (this.votingMode == UNIFORM_VOTING_MODE) {
        	Map<CandidatePair, Integer> majorityGraph = getMajorityGraphFromUniformVoting();
        	JOptionPane.showMessageDialog(this, getMajorityGraphIllustration(majorityGraph), "Majority Graph", JOptionPane.PLAIN_MESSAGE);
    	}
    }
    
    /**
     * Show ranking table based on voting results
     */
    private void showRankingTable() {
    	
    	if (this.votingMode != MANUAL_VOTING_MODE && this.votingMode != RANDOM_VOTING_MODE && this.votingMode != SINGLE_PEAKED_VOTING_MODE && this.votingMode != UNIFORM_VOTING_MODE) {
    		return;
    	}
    	
    	List<String> slaterRanking = null, kemenyRanking = null;
    	Map<Integer, String> bucklinRanking = null, bordaRanking = null;
    	Map<CandidatePair, Integer> majorityGraph = null;
    	String secondOrderCopelandWinner = null, instantRunoffWinner = null, condorcetWinner = null;;
    	
    	if ((this.votingMode == MANUAL_VOTING_MODE && userInputsAreValid()) || this.votingMode == RANDOM_VOTING_MODE) {
        	majorityGraph = getMajorityGraphFromPluralityVotes();
    	} else if (this.votingMode == SINGLE_PEAKED_VOTING_MODE) {
        	majorityGraph = getMajorityGraphFromSinglePeakedPreference();
    	} else if (this.votingMode == UNIFORM_VOTING_MODE) {
        	majorityGraph = getMajorityGraphFromUniformVoting();
    	}

    	if ((this.votingMode == MANUAL_VOTING_MODE && userInputsAreValid()) || this.votingMode != MANUAL_VOTING_MODE) {
    		
	    	slaterRanking = getSlaterRanking(majorityGraph);
	    	kemenyRanking = getKemenyRanking(majorityGraph);
	    	bucklinRanking = getBucklinRanking(majorityGraph, kemenyRanking);
	    	secondOrderCopelandWinner = getSecondOrderCopelandWinner(majorityGraph);
	    	bordaRanking = getBordaRanking(majorityGraph);
	    	instantRunoffWinner = getInstantRunoffWinner(majorityGraph);
	    	condorcetWinner = getCondorcetWinner(majorityGraph);
	    	
	    	new RankingTable(this, slaterRanking, kemenyRanking, bucklinRanking, secondOrderCopelandWinner, instantRunoffWinner, bordaRanking, condorcetWinner, ELECTION_CANDIDATES.length);
    	}
    }
    
    private String getCondorcetWinner(Map<CandidatePair, Integer> majorityGraph) {
    	
    	//Loop through each candidate
    	CandidatePair candidatePair = null;
    	boolean candidateDominates = false;
    	for (String dominatingCandidate : ELECTION_CANDIDATES) {
    	
    		candidateDominates = false;
    		
    		//Form pairs with each of the other candidates
    		for (String dominatedCandidate : ELECTION_CANDIDATES) {
    			
    			if (!dominatingCandidate.equals(dominatedCandidate)) {
    				
    				//If candidate does not dominate, break out of loop
	    			candidatePair = new CandidatePair(dominatingCandidate, dominatedCandidate);
	    			if (majorityGraph.containsKey(candidatePair)) {
	    				if (majorityGraph.containsKey(candidatePair)) {
	    					if (majorityGraph.get(candidatePair).intValue() > 0) {
	    						candidateDominates = true;
	    					} else {
	    						candidateDominates = false;
	    					}
	    				}
	    			} else {
	    				candidatePair = new CandidatePair(dominatedCandidate, dominatingCandidate);
	    				if (majorityGraph.containsKey(candidatePair)) {
	    					if (majorityGraph.get(candidatePair).intValue() < 0) {
	    						candidateDominates = true;
	    					} else {
	    						candidateDominates = false;
	    					}
	    				} else {
	    					candidateDominates = false;
	    				}
	    			}
	    			
	    			if (!candidateDominates) {
	    				break;
	    			}
	    			
    			}
    		}
    		
    		if (candidateDominates) {
    			return dominatingCandidate;
    		}
    	}
    	
    	return null;
    }
    
    private String getInstantRunoffWinner(Map<CandidatePair, Integer> majorityGraph) {
    	
    	if (this.votingMode == MANUAL_VOTING_MODE || this.votingMode == RANDOM_VOTING_MODE) {
    		return getInstantRunoffWinnerFromUserVotes();
    	} else if (this.votingMode == SINGLE_PEAKED_VOTING_MODE) {
    		return getInstantRunoffWinnerFromSinglePeakedSelection();
    	} else if (this.votingMode == UNIFORM_VOTING_MODE) {
    		return getInstantRunoffWinnerFromMajorityGraph(majorityGraph);
    	}
    	
    	return null;
    }
    
    private String getInstantRunoffWinnerFromUserVotes() {
    	
    	//First start with the Borda ranking 
    	Map<Integer, String> bordaRanking = getBordaRankingFromUserVotes();
    	
    	String lowestRankedCandidate = bordaRanking.get(Integer.valueOf(bordaRanking.size()));
    	Map<String, Map<Integer, String>> pluralityVoteCandidateRanksMinusLowestRank = null, rankingToWorkOn = this.pluralityVoteCandidateRanks;
    	
    	//Keep removing lowest ranked candidate till only one is left
    	while (true) {
    		
    		pluralityVoteCandidateRanksMinusLowestRank = getPluralityVoteCandidateRanksMinusLowestRank(rankingToWorkOn, lowestRankedCandidate);
			bordaRanking = getBordaRankingFromModifiedVotes(pluralityVoteCandidateRanksMinusLowestRank);
    		
    		if (bordaRanking.size() == 1) {
    			
    			return bordaRanking.get(Integer.valueOf(1));
    			
    		} else {
    			
    			rankingToWorkOn = pluralityVoteCandidateRanksMinusLowestRank;
    			lowestRankedCandidate = bordaRanking.get(Integer.valueOf(bordaRanking.size()));
    			
    		}
    	}
    
    }
    
    private Map<Integer, String> getBordaRankingFromModifiedVotes(Map<String, Map<Integer, String>> ranking) {
    	
    	//Loop through all the voters and assign Borda count according to rank.
    	//A rank of 1 will get a Borda count of 6 and a rank of 7 will get a Borda count of 0.
    	//Multiply this by the voter vote and store it in a Map for accrual.
    	Map<Integer, String> voterRankings = null;
    	Map<String, Integer> bordaRankings = new HashMap<String, Integer>();;
    	Set<Integer> voterRankNumbers = null;
    	int candidateBordaRank = 0;
    	for (String voter : ELECTION_VOTERS) {
    		
    		//Get the rankings for the voter
    		voterRankings = ranking.get(voter);
    		
    		//For each ranking calculate the Borda count and multiple it by the voter weight
    		voterRankNumbers = voterRankings.keySet();
    		for (Integer candidateRank : voterRankNumbers) {
    			candidateBordaRank = (ELECTION_CANDIDATES.length - candidateRank.intValue()) * pluralityVoterWeights.get(voter).intValue();
    			if (bordaRankings.containsKey(voterRankings.get(candidateRank))) {
    				bordaRankings.put(voterRankings.get(candidateRank), Integer.valueOf(bordaRankings.get(voterRankings.get(candidateRank)).intValue() + candidateBordaRank));
    			} else {
    				bordaRankings.put(voterRankings.get(candidateRank), Integer.valueOf(candidateBordaRank));
    			}
    		}
    	}
    	
    	//At this point there will be a Map<String, Integer> containing Borda scores. This needs to be sorted in descending order.
    	return getReverseSortedRanking(bordaRankings);

    }

    
    private Map<String, Map<Integer, String>> getPluralityVoteCandidateRanksMinusLowestRank(final Map<String, Map<Integer, String>> voterRankings, String candidateToRemove) {
    	
    	Map<String, Map<Integer, String>> returnValue = new HashMap<String, Map<Integer, String>>();
    	
    	//Loop through voter rankings and remove lowest ranked candidate. Transfer ranking for removed candidate to next highest candidate.
    	Map<Integer, String> candidateRanking = null, newRanking = null;
    	Set<String> voters = voterRankings.keySet();
    	int numberOfRankings = 0, rankingCounter = 0, candidateRemovedAtIndex = 0, newRankingSize = 0;
    	String candidate = null, candidateToBeMovedUp = null;;
    	boolean candidateRemoved = false;
    	for (String voter : voters) {
    		
    		newRanking = new HashMap<Integer, String>();
    		
    		//Get the candidate ranking for the voter
    		candidateRanking = voterRankings.get(voter);
    		
    		//Loop through the candidate rankings by the voter
    		numberOfRankings = candidateRanking.size();
    		candidateRemoved = false;
    		for (rankingCounter = 1; rankingCounter <= numberOfRankings; ++rankingCounter) {
    			
    			//Get the candidate corresponding to the rank
    			candidate = candidateRanking.get(Integer.valueOf(rankingCounter));
    			
	    		//remove candidate
	    		if (candidate.equals(candidateToRemove)) {
	    			
	    			candidateRemoved = true;
	    			candidateRemovedAtIndex = rankingCounter;
	    			
	    		//Copy ranking to return value
	    		} else {

	    			newRanking.put(Integer.valueOf(rankingCounter), candidate);
	    			
	    		}
    		}
    		
    		//Now move the rankings up in order to fill the gap created by the removed candidate
    		if (candidateRemoved && candidateRemovedAtIndex <= newRanking.size() + 1) {
    			
    			newRankingSize = newRanking.size();
    			for (int ranking = candidateRemovedAtIndex + 1; ranking <= newRankingSize + 1; ++ranking) {
    				
    				candidateToBeMovedUp = newRanking.get(Integer.valueOf(ranking));
    				newRanking.remove(Integer.valueOf(ranking));
    				newRanking.put(Integer.valueOf(ranking - 1), candidateToBeMovedUp);
    				
    			}
    		}

    		returnValue.put(voter, newRanking);
    		
    	}
    	
    	
    	return returnValue;
    }
    
    private String getInstantRunoffWinnerFromSinglePeakedSelection() {
    	return null;
    }
    
    private String getInstantRunoffWinnerFromMajorityGraph(Map<CandidatePair, Integer> majorityGraph) {
    	return null;
    }
    
    private Map<Integer, String> getBordaRanking(Map<CandidatePair, Integer> majorityGraph) {
    	
    	if (this.votingMode == MANUAL_VOTING_MODE || this.votingMode == RANDOM_VOTING_MODE) {
    		return getBordaRankingFromUserVotes();
    	} else if (this.votingMode == SINGLE_PEAKED_VOTING_MODE) {
    		return getBordaRankingFromSinglePeakedSelection();
    	} else if (this.votingMode == UNIFORM_VOTING_MODE) {
    		return getBordaRankingFromMajorityGraph(majorityGraph);
    	}
    	
    	return null;
    }
    
    private Map<Integer, String> getBordaRankingFromUserVotes() {
    	
    	//Loop through all the voters and assign Borda count according to rank.
    	//A rank of 1 will get a Borda count of 6 and a rank of 7 will get a Borda count of 0.
    	//Multiply this by the voter vote and store it in a Map for accrual.
    	Map<Integer, String> voterRankings = null;
    	Map<String, Integer> bordaRankings = new HashMap<String, Integer>();;
    	Set<Integer> voterRankNumbers = null;
    	int candidateBordaRank = 0;
    	for (String voter : ELECTION_VOTERS) {
    		
    		//Get the rankings for the voter
    		voterRankings = this.pluralityVoteCandidateRanks.get(voter);
    		
    		//For each ranking calculate the Borda count and multiple it by the voter weight
    		voterRankNumbers = voterRankings.keySet();
    		for (Integer candidateRank : voterRankNumbers) {
    			candidateBordaRank = (ELECTION_CANDIDATES.length - candidateRank.intValue()) * pluralityVoterWeights.get(voter).intValue();
    			if (bordaRankings.containsKey(voterRankings.get(candidateRank))) {
    				bordaRankings.put(voterRankings.get(candidateRank), Integer.valueOf(bordaRankings.get(voterRankings.get(candidateRank)).intValue() + candidateBordaRank));
    			} else {
    				bordaRankings.put(voterRankings.get(candidateRank), Integer.valueOf(candidateBordaRank));
    			}
    		}
    	}
    	
    	//At this point there will be a Map<String, Integer> containing Borda scores. This needs to be sorted in descending order.
    	return getReverseSortedRanking(bordaRankings);

    }
    
    private Map<Integer, String> getReverseSortedRanking(Map<String, Integer> bordaRankings) {
    	
    	//Use the sorted Borda rankings to return the Map used in display the values on screen
    	PriorityQueue<String> bordaRankPriorityQueue = new PriorityQueue<String>(ELECTION_CANDIDATES.length, new BordaRankingComparator(bordaRankings));
    	Set<String> candidates = bordaRankings.keySet();
    	for (String candidate : candidates) {
    		bordaRankPriorityQueue.add(candidate);
    	}
    	
    	int bordaRankToDisplay = 0;
    	String nextCandidate = null;
    	Map<Integer, String> returnValue = new HashMap<Integer, String>();
    	while(!bordaRankPriorityQueue.isEmpty()) {
    		nextCandidate = bordaRankPriorityQueue.remove();
    		returnValue.put(Integer.valueOf(++bordaRankToDisplay), nextCandidate);
    	}
    	return returnValue;    	
    }
    
    private Map<Integer, String> getBordaRankingFromSinglePeakedSelection() {
    	
    	//Get the selection for each voter and apply the voter weight
    	String selectedCandidate = null;
    	Map<String, Integer> bordaRankings = new HashMap<String, Integer>();
    	for (String voter : ELECTION_VOTERS) {
    		
    		selectedCandidate = ELECTION_CANDIDATES[singlePeakedPreferenceVoterChoices.get(voter)];
    		if (bordaRankings.containsKey(selectedCandidate)) {
    			bordaRankings.put(selectedCandidate, Integer.valueOf(bordaRankings.get(selectedCandidate).intValue() + pluralityVoterWeights.get(voter).intValue()));
    		} else {
    			bordaRankings.put(selectedCandidate, pluralityVoterWeights.get(voter));
	    	}
    	}
    	
    	
    	return getReverseSortedRanking(bordaRankings);
    }
    
    private Map<Integer, String> getBordaRankingFromMajorityGraph(Map<CandidatePair, Integer> majorityGraph) {
    	
    	Map<String, Integer> firstOrderCopelandScores = getFirstOrderCopelandScores(majorityGraph);
    	Map<String, Integer> filteredFirstOrderCopelandScores = new HashMap<String, Integer>();
    	
    	//Filter out candidates that have been removed
    	Set<String> candidates = firstOrderCopelandScores.keySet();
    	for (String candidate : candidates) {
			filteredFirstOrderCopelandScores.put(candidate, firstOrderCopelandScores.get(candidate));
    	}
    	
    	
    	return getReverseSortedRanking(filteredFirstOrderCopelandScores);
    	
    }
    
    private String getSecondOrderCopelandWinner(Map<CandidatePair, Integer> majorityGraph) {
    	
    	//The majority graph will already contain the first order Copeland ranking
    	Map<String, Integer> copelandScores = getFirstOrderCopelandScores(majorityGraph);
    	
    	Set<CandidatePair> majorityGraphPairs = majorityGraph.keySet();  	
    	Map<String, Integer> secondOrderCopelandScores = new HashMap<String, Integer>();
    	
    	//Loop through the majority graph and assign Copeland scores of dominated candidates to dominating candidates
    	for (CandidatePair candidatePair : majorityGraphPairs) {
    		
    		//Assign the Copeland score of the dominated candidate to the dominating candidate
    		if (copelandScores.containsKey(candidatePair.getDominatedCandidate())) {
    			
    			if (secondOrderCopelandScores.containsKey(candidatePair.getDominatingCandidate())) {
    				secondOrderCopelandScores.put(candidatePair.getDominatingCandidate(), Integer.valueOf(secondOrderCopelandScores.get(candidatePair.getDominatingCandidate()).intValue() + copelandScores.get(candidatePair.getDominatedCandidate()).intValue()));
    			} else {
    				secondOrderCopelandScores.put(candidatePair.getDominatingCandidate(), copelandScores.get(candidatePair.getDominatedCandidate()));
    			}
    			
    		}
    	
    	}
    	
    	if (secondOrderCopelandScores.isEmpty()) {
    		return null;
    	} else {
    		//Loop through the map and find the highest score entry
    		Set<String> candidates = secondOrderCopelandScores.keySet();
    		int highestScore = 0;
    		String secondOrderCopelandWinner = null;
    		for (String candidate : candidates) {
    			if (secondOrderCopelandScores.get(candidate).intValue() > highestScore) {
    				highestScore = secondOrderCopelandScores.get(candidate).intValue();
    				secondOrderCopelandWinner = candidate;
    			}
    		}
    		return secondOrderCopelandWinner;
    	}
    	
    }
    
    private Map<String, Integer> getFirstOrderCopelandScores(Map<CandidatePair, Integer> majorityGraph) {
    	
    	Map<String, Integer> copelandScores = new HashMap<String, Integer>();
    	
    	//Get the first order Copeland scores
    	Set<CandidatePair> majorityGraphPairs = majorityGraph.keySet();
    	int copelandScore = 0;
    	for (CandidatePair candidatePair : majorityGraphPairs) {
    		
    		copelandScore = majorityGraph.get(candidatePair).intValue();
    		if (copelandScore > 0) {
    			if (copelandScores.containsKey(candidatePair.getDominatingCandidate())) {
    				copelandScores.put(candidatePair.getDominatingCandidate(), Integer.valueOf(copelandScores.get(candidatePair.getDominatingCandidate()).intValue() + copelandScore));
    			} else {
    				copelandScores.put(candidatePair.getDominatingCandidate(), copelandScore);
    			}
    		} else if (copelandScore < 0) {
    			if (copelandScores.containsKey(candidatePair.getDominatedCandidate())) {
    				copelandScores.put(candidatePair.getDominatedCandidate(), Integer.valueOf(copelandScores.get(candidatePair.getDominatedCandidate()).intValue() + copelandScore * -1));
    			} else {
    				copelandScores.put(candidatePair.getDominatedCandidate(), copelandScore * -1);
    			}
    			
    		}
    		
    	}
    	
    	return copelandScores;
    }
    
    private Map<Integer, String> getBucklinRanking(Map<CandidatePair, Integer> majorityGraph, List<String> kemenyRanking) {
    	
    	Map<Integer, String> returnValue = new HashMap<Integer, String>();
    	
		//Find the total votes by adding up the voter weights
    	int totalVotes = 0;
    	if (this.votingMode != UNIFORM_VOTING_MODE) {
			Set<String> voters = this.pluralityVoterWeights.keySet();
			for (String voter : voters) {
				totalVotes += this.pluralityVoterWeights.get(voter).intValue();
			}
    	}
		
		
    	if (this.votingMode == MANUAL_VOTING_MODE || this.votingMode == RANDOM_VOTING_MODE) {
    		
    		//Loop through all the ranks till you have a winner with more than half the votes
    	   	int voterWeight = 0, numberOfCandidates = ELECTION_CANDIDATES.length;
    	   	Map<String, Integer> candidateVotes = new HashMap<String, Integer>();
    	   	
    		for (int rankToConsider = 1; rankToConsider < numberOfCandidates + 1; ++rankToConsider) {
    			
    			//For each voter, consider upto the rank in the outer loop
            	for (String voter : ELECTION_VOTERS) {
                	
            		//Get the voter weight
            		voterWeight = this.pluralityVoterWeights.get(voter).intValue();
            		
            		//Get the candidate rankings for the voter
            		Map<Integer, String> voterCandidateRanking = pluralityVoteCandidateRanks.get(voter);
            		
            		if (candidateVotes.containsKey(voterCandidateRanking.get(Integer.valueOf(rankToConsider)))) {
            			candidateVotes.put(voterCandidateRanking.get(Integer.valueOf(rankToConsider)), Integer.valueOf(candidateVotes.get(voterCandidateRanking.get(Integer.valueOf(rankToConsider))).intValue() + voterWeight));
            		} else {
            			candidateVotes.put(voterCandidateRanking.get(Integer.valueOf(rankToConsider)), Integer.valueOf(voterWeight));
            		}
    			
            	}
            	
            	//Check if any candidate has more than half the votes
            	double maximumVoteFraction = 0.0, rankAppliedToTotalVotes = totalVotes * rankToConsider, maximumVotes = 0;
            	String candidateWithMaximumVotes = null;
            	Set<String> candidates = candidateVotes.keySet();
            	
            	//Find the candidate with maximum votes
            	for (String candidate : candidates) {
            		if (candidateVotes.get(candidate).intValue() > maximumVotes) {
            			maximumVotes = candidateVotes.get(candidate).intValue();
            			candidateWithMaximumVotes = candidate;
            		}
            	}
            	
            	//Check if the candidate has more than half the votes
            	maximumVoteFraction = maximumVotes / rankAppliedToTotalVotes;
            	if (maximumVoteFraction > 0.5) {
            		returnValue.put(Integer.valueOf(rankToConsider), candidateWithMaximumVotes);
            		return returnValue;
            	}
            	
    		}
    		
    		return null;
    		
    	} else if (this.votingMode == SINGLE_PEAKED_VOTING_MODE) {
    		
    		//Check if there is a choice with more than half the votes
    		int voterChoice = 0, candidateVotes = 0;
    		double maximumVoteFraction = 0.0, maximumVotes = 0;
    		String candidateWithMaximumVotes = null;
    		for (String voter : ELECTION_VOTERS) {
    			
    			voterChoice = singlePeakedPreferenceVoterChoices.get(voter).intValue();
    			candidateVotes = this.pluralityVoterWeights.get(voter).intValue();
    			if (candidateVotes > maximumVotes) {
    				maximumVotes = candidateVotes;
    				candidateWithMaximumVotes = ELECTION_CANDIDATES[voterChoice];
    			}
    			
    			maximumVoteFraction = maximumVotes / (double) totalVotes;
            	if (maximumVoteFraction > 0.5) {
            		returnValue.put(Integer.valueOf(0), candidateWithMaximumVotes);
            		return returnValue;
            	}
    			
    		}
    		
    		return null;
    	
    	} else if (this.votingMode == UNIFORM_VOTING_MODE) {
    		
    		//Since there is only one voter, return the candidate on top of the majority graph.
    		//It will be the top rank in the Kemeny ranking
    		returnValue.put(Integer.valueOf(0), kemenyRanking.get(0));
    		return returnValue;
    		
    	}
    	
    	return null;
    }
    
    /**
     * Show popup that lets the user select the candidate to be removed
     */
    private void doRemoveCandidatePopup() {
    	
    	String removedCandidate = (String) JOptionPane.showInputDialog(this, 
    			                                                       "Which candidate do you want to remove?\n",
                                                                       "Remove Candidate",
                                                                       JOptionPane.PLAIN_MESSAGE,
                                                                       null,
                                                                       ELECTION_CANDIDATES,
                                                                       this.removedCandidate == null ? ELECTION_CANDIDATES[0] : this.removedCandidate);
    	
    	this.removedCandidate = removedCandidate;
    	
    }
    
    private List<String> getSlaterRanking(Map<CandidatePair, Integer> majorityGraph) {
    	
    	List<String> candidates = new ArrayList<String>();
    	for (String candidate : ELECTION_CANDIDATES) {
    		if (this.removedCandidate == null || (this.removedCandidate != null && !this.removedCandidate.equals(candidate))) {
    			candidates.add(candidate);
    		}
    	}
    	
    	//Get all permutations of the candidates
    	List<List<String>> candidatePermutations = getCandidatePermutations(candidates);
    	
    	//Calculate Slater score for each permutation
    	int maximumSlaterScore = Integer.MIN_VALUE, currentSlaterScore = 0;
    	List<String> permutationWithMaxScore = null;
    	for (List<String> permutation : candidatePermutations) {
    		currentSlaterScore = getSlaterScore(permutation, majorityGraph) ;
    		if (currentSlaterScore > maximumSlaterScore) {
    			maximumSlaterScore = currentSlaterScore;
    			permutationWithMaxScore = permutation;
    		}
    	} 
    	
    	return permutationWithMaxScore;
    	
    }
    
    private int getSlaterScore(List<String> permutation, Map<CandidatePair, Integer> majorityGraph) {
    	
    	//Iterate through the permutation of the candidates 
    	int slaterScore = 0, currentScore = 0;
    	CandidatePair candidatePair = null;
    	for (int index = 0; index < permutation.size() - 1; ++index) {
    		candidatePair = new CandidatePair(permutation.get(index), permutation.get(index + 1));
    		if (majorityGraph.containsKey(candidatePair)) {
    			currentScore = majorityGraph.get(candidatePair).intValue();
    			if (currentScore > 0) {
    				slaterScore += 1;
    			} else if (currentScore < 0) {
    				slaterScore -= 1;
    			}
    		} else {
    			candidatePair = new CandidatePair(permutation.get(index + 1), permutation.get(index));
        		if (majorityGraph.containsKey(candidatePair)) {
        			currentScore = majorityGraph.get(candidatePair).intValue();
        			if (currentScore > 0) {
        				slaterScore -= 1;
        			} else if (currentScore < 0) {
        				slaterScore += 1;
        			}
        		}
    		}
    			
    	}
    	
    	return slaterScore;
    	
    }
    
    private List<String> getKemenyRanking(Map<CandidatePair, Integer> majorityGraph) {
    	
    	List<String> candidates = new ArrayList<String>();
    	for (String candidate : ELECTION_CANDIDATES) {
    		candidates.add(candidate);
    	}
    	
    	//Get all permutations of the candidates
    	List<List<String>> candidatePermutations = getCandidatePermutations(candidates);
    	
    	//Calculate Slater score for each permutation
    	int maximumKemenyScore = Integer.MIN_VALUE, currentKemenyScore = 0;
    	List<String> permutationWithMaxScore = null;
    	for (List<String> permutation : candidatePermutations) {
    		currentKemenyScore = getKemenyScore(permutation, majorityGraph) ;
    		if (currentKemenyScore > maximumKemenyScore) {
    			maximumKemenyScore = currentKemenyScore;
    			permutationWithMaxScore = permutation;
    		}
    	} 
    	
    	return permutationWithMaxScore;
    	
    }
    
    private int getKemenyScore(List<String> permutation, Map<CandidatePair, Integer> majorityGraph) {
    	
    	//Iterate through the permutation of the candidates 
    	int kemenyScore = 0;
    	CandidatePair candidatePair = null;
    	for (int index = 0; index < permutation.size() - 1; ++index) {
    		candidatePair = new CandidatePair(permutation.get(index), permutation.get(index + 1));
    		if (majorityGraph.containsKey(candidatePair)) {
    			kemenyScore += majorityGraph.get(candidatePair).intValue();
    		} else {
    			candidatePair = new CandidatePair(permutation.get(index + 1), permutation.get(index));
        		if (majorityGraph.containsKey(candidatePair)) {
        			kemenyScore -= majorityGraph.get(candidatePair).intValue();
        		}
    		}
    			
    	}
    	
    	return kemenyScore;
    	
    }
    
    private List<List<String>> getCandidatePermutations(List<String> candidates) {
    	
    	List<List<String>> returnValue = new ArrayList<List<String>>();
    	
    	//Terminating condition of the recursion
    	if (candidates.size() == 2) {
    		List<String> onlyList = new ArrayList<String>();
    		onlyList.add(0, candidates.get(0));
    		onlyList.add(1, candidates.get(1));
    		returnValue.add(onlyList);
    		return returnValue;
    	}
    	
    	//Take each candidate at a time
    	List<String> candidateList = null;
    	for (String candidate : candidates) {
    		
    		//Get a list of other candidates by removing current candidate
    		List<String> listOfOtherCandidates = getListWithoutCandidate(candidates, candidate);
    		
    		//Using this list of other candidates, get all its permutations
    		List<List<String>> permutationsOfOtherCandidates = getCandidatePermutations(listOfOtherCandidates);
    		
    		for (List<String> list : permutationsOfOtherCandidates) {
    			
    			//Create lists with current candidate at head with permutations of other candidates after it
        		candidateList = new ArrayList<String>();
        		candidateList.add(candidate);
        		candidateList.addAll(list);
        		returnValue.add(candidateList);
        		
    		}
    	}
    	
    	return returnValue;

    }
    
    private List<String> getListWithoutCandidate(List<String> candidates, String candidateToExclude) {
    	
    	List<String> returnValue = new ArrayList<String>();
    	
    	if (candidates.size() == 0) {
    		return null;
    	}
    	
    	for (String candidate : candidates) {
    		if (!candidate.equals(candidateToExclude)) {
    			returnValue.add(candidate);
    		}
    	}
    	
    	return returnValue;
    }
    
    private Map<CandidatePair, Integer> getMajorityGraphFromUniformVoting() {
    	
    	Map<CandidatePair, Integer> returnValue = new HashMap<CandidatePair, Integer>();    	
    	Set<CandidatePair> candidatePairs = uniformVotingRanks.keySet();
    	int preferenceCount = 0;
    	
    	for (CandidatePair pair : candidatePairs) {
    		preferenceCount = uniformVotingRanks.get(pair);
    		if (preferenceCount > 0) {
    			returnValue.put(pair, Integer.valueOf(preferenceCount));
    		} else if (preferenceCount < 0) {
    			returnValue.put(new CandidatePair(pair.getDominatedCandidate(), pair.getDominatingCandidate()), Integer.valueOf(preferenceCount * -1));
    		}
    	}
    	
    	return returnValue;   	
    }
    
    private Map<CandidatePair, Integer> getMajorityGraphFromSinglePeakedPreference() {
    	
    	int voterWeight = 0, voterChoice = 0;
    	Map<CandidatePair, Integer> returnValue = new HashMap<CandidatePair, Integer>();
    	CandidatePair candidatePair = null, reverseCandidatePair = null;
    	
    	for (String voter : ELECTION_VOTERS) {
    		
    		voterWeight = pluralityVoterWeights.get(voter).intValue();
    		   		
    		voterChoice = singlePeakedPreferenceVoterChoices.get(voter).intValue();
    		
    		for (int candidateIndex = 0; candidateIndex < ELECTION_CANDIDATES.length; ++candidateIndex) {
    			
    			if (voterChoice != candidateIndex) {
    				candidatePair = new CandidatePair(ELECTION_CANDIDATES[voterChoice], ELECTION_CANDIDATES[candidateIndex]);
    				if (returnValue.containsKey(candidatePair)) {
    					returnValue.put(candidatePair, Integer.valueOf(returnValue.get(candidatePair).intValue() + voterWeight));
    				} else {
    					reverseCandidatePair = new CandidatePair(ELECTION_CANDIDATES[candidateIndex], ELECTION_CANDIDATES[voterChoice]);
    					if (returnValue.containsKey(reverseCandidatePair)) {
    						returnValue.put(reverseCandidatePair, Integer.valueOf(returnValue.get(reverseCandidatePair).intValue() - voterWeight));
    					} else {
    						returnValue.put(candidatePair, Integer.valueOf(voterWeight));
    					}
    				}
    			}
    		}
    		
    	}
    	
    	return returnValue;
    }
    		
    private Map<CandidatePair, Integer> getMajorityGraphFromPluralityVotes() {
    	
    	//Map to store majority graph pairs and their counts
    	Map<CandidatePair, Integer> majorityGraphPairs = new HashMap<CandidatePair, Integer>();
    	
    	//Loop through all the voters
    	int voterWeight = 0, numberOfCandidates = ELECTION_CANDIDATES.length;
    	for (String voter : ELECTION_VOTERS) {
    	
    		//Get the voter weight
    		voterWeight = this.pluralityVoterWeights.get(voter).intValue();
    		
    		//Get the candidate rankings for the voter
    		Map<Integer, String> voterCandidateRanking = pluralityVoteCandidateRanks.get(voter);
    		CandidatePair candidatePair = null, reversedCandidatePair = null;
    		
    		//Use the rankings of a voter to form pairs of candidates by using candidates having sequential ranks 
    		for (int candidateRank = 1; candidateRank < numberOfCandidates; ++candidateRank) {
    			
    			candidatePair = new CandidatePair(voterCandidateRanking.get(Integer.valueOf(candidateRank)), voterCandidateRanking.get(Integer.valueOf(candidateRank + 1)));
    			if (majorityGraphPairs.containsKey(candidatePair)) {
    				majorityGraphPairs.put(candidatePair, Integer.valueOf(majorityGraphPairs.get(candidatePair).intValue() + voterWeight));
    			} else {
    				reversedCandidatePair = new CandidatePair(voterCandidateRanking.get(Integer.valueOf(candidateRank + 1)), voterCandidateRanking.get(Integer.valueOf(candidateRank)));
    				if (majorityGraphPairs.containsKey(reversedCandidatePair)) {
        				majorityGraphPairs.put(reversedCandidatePair, Integer.valueOf(majorityGraphPairs.get(reversedCandidatePair).intValue() - voterWeight));
    				} else {
    					majorityGraphPairs.put(candidatePair, Integer.valueOf(voterWeight));
    				}
    			}
    			
    		}
    	
    	}
    	
    	return majorityGraphPairs;
    }
    
    /**
     * Return list of candidates after excluding the one passed in
     * 
     * @param candidateToExclude
     * @return
     */
    private String[] getListOfOtherCandidates(String candidateToExclude) {
    	
    	int returnArrayLength = ELECTION_CANDIDATES.length - 1, returnArrayIndex = 0;
    	String[] returnArray = new String[returnArrayLength];
    	
    	for (String candidate : ELECTION_CANDIDATES) {
    		if (!candidate.equals(candidateToExclude)) {
    			returnArray[returnArrayIndex] = candidate;
    			if (++returnArrayIndex >= returnArrayLength) {
    				break;
    			}
    		}
    	}
    	
    	return returnArray;
    	
    }
    
    /**
     * All voters will have the same preference
     */
    private void doUniformVotingUILayout() {
        
    	this.votingMode = UNIFORM_VOTING_MODE;
    	
    	this.uniformVotingRanks = new HashMap<CandidatePair, Integer>();
    	
     	//Create the panel on which to place voter grid components
     	Container contentPane = this.getContentPane();
     	JPanel votingGrid = new JPanel();
     	GridLayout voterGridLayout = new GridLayout(ELECTION_CANDIDATES.length + 1, 5);
     	voterGridLayout.setHgap(2);
     	voterGridLayout.setVgap(2);
     	votingGrid.setLayout(voterGridLayout);
     	
     	//Create the table header for the voter grid
     	votingGrid.add(new JLabel("Choice/Agent"));
     	StringBuffer voterList = new StringBuffer();
     	boolean firstTime = true;
 		for (String voter : ELECTION_VOTERS) {
 			if (firstTime) {
 				firstTime = false;
 				voterList.append(voter);
 			} else {
 				voterList.append(", ").append(voter);
 			}
 		}
 		votingGrid.add(new JLabel(voterList.toString()));
 		
 		//Dummy labels
 		votingGrid.add(new JLabel(""));
 		votingGrid.add(new JLabel(""));
 		votingGrid.add(new JLabel(""));
 		
     	//Loop through all the election candidates
     	for (String candidate : ELECTION_CANDIDATES) {
     		
     		//Create label for candidate
     		votingGrid.add(new JLabel("  " + candidate));
     		
     		//Create preference label for candidate
     		votingGrid.add(new JLabel(">"));
     		
     		//Create drop down for comparison candidate
     		DefaultListModel<String> listModel1 = new DefaultListModel<String>();
     		String[] candidates = getListOfOtherCandidates(candidate);
     		for (String listItem : candidates) {
     			listModel1.addElement(listItem);
     		}
     		JList<String> list1 = new JList<String>(listModel1);
     		list1.setLayoutOrientation(JList.HORIZONTAL_WRAP);
     		list1.setVisibleRowCount(2);
     		ListSelectionListener selectionListener1 = this.new UniformVotingCandidateSelectionHandler(candidate, true);
     		list1.getSelectionModel().addListSelectionListener(selectionListener1);
     		votingGrid.add(list1);
     		
     		//Create preference label for candidate
     		votingGrid.add(new JLabel("     <"));
     		
     		//Create drop down for comparison candidate
     		DefaultListModel<String> listModel2 = new DefaultListModel<String>();
     		for (String listItem : candidates) {
     			listModel2.addElement(listItem);
     		}
     		JList<String> list2 = new JList<String>(listModel2);
     		list2.setLayoutOrientation(JList.HORIZONTAL_WRAP);
     		list2.setVisibleRowCount(2);
     		ListSelectionListener selectionListener2 = this.new UniformVotingCandidateSelectionHandler(candidate, false);
     		list2.getSelectionModel().addListSelectionListener(selectionListener2);
     		votingGrid.add(list2);
     	}
     	
     	contentPane.removeAll();
     	contentPane.add(votingGrid);
     	this.pack();
     	this.setVisible(true);
    	
    }
    
    /**
     * Select a candidate based on single peaked preference
     */
    private void doSinglePeakedPreferenceVotingUILayout() {
        
    	this.votingMode = SINGLE_PEAKED_VOTING_MODE;
       
    	//Maps to hold voter weights and candidate ranks
        this.pluralityVoterWeights = new HashMap<String, Integer>();
    	singlePeakedPreferenceVoterChoices = new HashMap<String, Integer>();
    	
    	//Create the panel on which to place voter grid components
    	Container contentPane = this.getContentPane();
    	JPanel votingGrid = new JPanel();
    	GridLayout voterGridLayout = new GridLayout(ELECTION_CANDIDATES.length + 2, ELECTION_VOTERS.length + 1);
    	voterGridLayout.setHgap(2);
    	voterGridLayout.setVgap(2);
    	votingGrid.setLayout(voterGridLayout);
    	
    	//Create the table header for the voter grid
    	votingGrid.add(new JLabel("Choice/Agent"));
		for (String voter : ELECTION_VOTERS) {
			votingGrid.add(new JLabel("      " + voter));
			this.pluralityVoterWeights.put(voter, Integer.valueOf(1));
		}
		
		//Create weight controls and radio button groups for each voter
		votingGrid.add(new JLabel("  Weights"));
		
		Map<String, ButtonGroup> voterRadioButtonGroups = new HashMap<String, ButtonGroup>();
		VotingUserInterface.VoterWeightActionListener actionListener = null;
		for (String voter : ELECTION_VOTERS) {
			JComboBox<String> voterWeightSelector = new JComboBox<String>(VOTER_WEIGHTS);
			votingGrid.add(voterWeightSelector);
			actionListener = this.new VoterWeightActionListener(voter);
			voterWeightSelector.addActionListener(actionListener);
			voterRadioButtonGroups.put(voter, new ButtonGroup());
		}
		
    	//Loop through all the election candidates
    	for (String candidate : ELECTION_CANDIDATES) {
    		
    		//Create label for candidate
    		votingGrid.add(new JLabel("  " + candidate));
    		
    		//Create candidate rank drop downs for each voter
    		JRadioButton radioButton = null;
    		for (String voter : ELECTION_VOTERS) {
    			radioButton = new JRadioButton();
    			radioButton.addActionListener(this.new SinglePeakedPreferenceActionListener(voter, candidate));
    			voterRadioButtonGroups.get(voter).add(radioButton);
    			votingGrid.add(radioButton);
    		}
    	}
    	
    	contentPane.removeAll();
    	contentPane.add(votingGrid);
    	this.pack();
    	this.setVisible(true);
   	
    }
    
    /**
     * Shrink the size of the array and return a random candidate rank
     * @param currentCandidateRanks
     * @return
     */
    private String nextRandomCandidateRank(String[] currentCandidateRanks) {
    	
    	if (currentCandidateRanks == null || currentCandidateRanks.length == 0) {
    		return String.valueOf(0);
    	}
    	
    	int randomIndex = randomRankGenerator.nextInt(currentCandidateRanks.length);
    	String returnValue = String.valueOf(currentCandidateRanks[randomIndex]);
    	
    	String[] compactedRanks = new String[currentCandidateRanks.length - 1];
    	int compactedRanksIndex = 0;
    	for (int index = 0; index < currentCandidateRanks.length; ++index) {
    		if (index != randomIndex) {
    			compactedRanks[compactedRanksIndex++] = currentCandidateRanks[index];
    		}
    	}
    	
    	randomCandidateRanks = compactedRanks;
    	return returnValue;
    		
    }
   
    /**
     * Generate random weights and candidate ranks
     */
    private void doRandomWeightsAndRanksVotingUILayout() {
        
    	this.votingMode = RANDOM_VOTING_MODE;

    	//Maps to hold voter weights and candidate ranks
        pluralityVoterWeights = new HashMap<String, Integer>();
        pluralityVoteCandidateRanks = new HashMap<String, Map<Integer, String>>();
        
    	//Create the panel on which to place voter grid components
    	Container contentPane = this.getContentPane();
    	JPanel votingGrid = new JPanel();
    	GridLayout voterGridLayout = new GridLayout(ELECTION_CANDIDATES.length + 2, ELECTION_VOTERS.length + 1);
    	voterGridLayout.setHgap(2);
    	voterGridLayout.setVgap(2);
    	votingGrid.setLayout(voterGridLayout);
    	
    	//Create the table header for the voter grid
    	votingGrid.add(new JLabel("Choice/Agent"));
		for (String voter : ELECTION_VOTERS) {
			votingGrid.add(new JLabel("      " + voter));
		}
		
		int numberOfVoters = ELECTION_VOTERS.length;
    	
		//Create weight controls for each voter
		votingGrid.add(new JLabel("  Weights"));
		int voterWeight = 0;
		for (int voterIndex = 0; voterIndex < numberOfVoters; ++voterIndex) {
			voterWeight = randomWeightGenerator.nextInt(VOTER_WEIGHTS.length - 1) + 1;
			votingGrid.add(new JLabel("      " + voterWeight));
			//Save the voter weight
			pluralityVoterWeights.put(ELECTION_VOTERS[voterIndex], Integer.valueOf(voterWeight));
		}
		
    	//Loop through all the election voters and generate random candidate rankings
		votersRanks = new HashMap<String, String[]>();
		
    	for (String voter : ELECTION_VOTERS) {
    		   		
    		//Create candidate rank for each voter
    		String[] voterRanks = new String[ELECTION_CANDIDATES.length];
    		int voterRanksIndex = 0;
    		String randomRank = null;
    		randomCandidateRanks = CANDIDATE_RANKS;
    		for (String candidate : ELECTION_CANDIDATES) {
    			randomRank = nextRandomCandidateRank(randomCandidateRanks);
    			voterRanks[voterRanksIndex++] = randomRank;
    		}
    		
    		//Save the voter ranks for later use
    		votersRanks.put(voter, voterRanks);
    	}
		
    	//Loop through all the election candidates and put the saved ranks on the screen
    	int rowNumber = 0;
    	for (String candidate : ELECTION_CANDIDATES) {
    		
    		//Create label for candidate
    		votingGrid.add(new JLabel("  " + candidate));
    		
    		//Create candidate rank drop downs for each voter
    		for (String voter : ELECTION_VOTERS) {
    			
    			votingGrid.add(new JLabel("      " + votersRanks.get(voter)[rowNumber]));
    			
    		}
    		
    		++rowNumber;
    	}
    	
    	//Store candidate rank per voter for use in calculating results
    	Map<Integer, String> voterRanksForAllCandidates = null; 
    	int candidateIndex = 0;
    	for (String voter : ELECTION_VOTERS) {
    		
    		voterRanksForAllCandidates = new HashMap<Integer, String>();
    		
    		candidateIndex = 0;
    		for (String candidate : ELECTION_CANDIDATES) {
    			
    	    	voterRanksForAllCandidates.put(Integer.valueOf(votersRanks.get(voter)[candidateIndex++]), candidate);
    		
    		}

    		pluralityVoteCandidateRanks.put(voter, voterRanksForAllCandidates);
    	}
    	
    	contentPane.removeAll();
    	contentPane.add(votingGrid);
    	this.pack();
    	this.setVisible(true);
   	
    }
    
    /**
     * Fill the voting grid with controls that let the user specify the weights and ranks manually
     * 
     */
    private void doManualVotingUILayout() {
 
    	this.votingMode = MANUAL_VOTING_MODE;

    	//Maps to hold voter weights and candidate ranks
        pluralityVoterWeights = new HashMap<String, Integer>();
        this.unvalidatedVoterCandidateRanks = new HashMap<String, Map<String, Integer>>();
    
    	//Create the panel on which to place voter grid components
    	Container contentPane = this.getContentPane();
    	JPanel votingGrid = new JPanel();
    	GridLayout voterGridLayout = new GridLayout(ELECTION_CANDIDATES.length + 2, ELECTION_VOTERS.length + 1);
    	voterGridLayout.setHgap(2);
    	voterGridLayout.setVgap(2);
    	votingGrid.setLayout(voterGridLayout);
    	
    	//Create the table header for the voter grid
    	votingGrid.add(new JLabel("Choice/Agent"));
		for (String voter : ELECTION_VOTERS) {
			votingGrid.add(new JLabel("      " + voter));
			pluralityVoterWeights.put(voter, Integer.valueOf(1));
		}
		
		int numberOfVoters = ELECTION_VOTERS.length;
    	
		//Create weight controls for each voter
		votingGrid.add(new JLabel("  Weights"));
		//VoterWeightActionListener
		JComboBox<String> voterWeightSelector = null;
		VotingUserInterface.VoterWeightActionListener actionListener = null;
		for (int voterIndex = 0; voterIndex < numberOfVoters; ++voterIndex) {
			voterWeightSelector = new JComboBox<String>(VOTER_WEIGHTS);
			actionListener = this.new VoterWeightActionListener(ELECTION_VOTERS[voterIndex]);
			voterWeightSelector.addActionListener(actionListener);
			votingGrid.add(voterWeightSelector);
		}
		
    	//Loop through all the election candidates
		JComboBox<String> candidateRankSelector = null;
		VotingUserInterface.VoterCandidateRankActionListener rankActionListener = null;
    	for (String candidate : ELECTION_CANDIDATES) {
    		
    		//Create label for candidate
    		votingGrid.add(new JLabel("  " + candidate));
    		
    		//Create candidate rank drop downs for each voter
    		for (String voter : ELECTION_VOTERS) {
    			candidateRankSelector = new JComboBox<String>(CANDIDATE_RANKS);
    			rankActionListener = this.new VoterCandidateRankActionListener(voter, candidate);
    			candidateRankSelector.addActionListener(rankActionListener);
    			votingGrid.add(candidateRankSelector);
    		}
    	}
    	
    	//Save default voter ranks
    	Map<String, Integer> candidateRanks = null;
    	for (String voter : ELECTION_VOTERS) {
    		candidateRanks = new HashMap<String, Integer>();
    		for (String candidate : ELECTION_CANDIDATES) {
    			candidateRanks.put(candidate,Integer.valueOf(1));
    		}
    		this.unvalidatedVoterCandidateRanks.put(voter, candidateRanks);
    	}
    	
    	contentPane.removeAll();
    	contentPane.add(votingGrid);
    	this.pack();
    	this.setVisible(true);
    }
    
    /**
     * Create the menu for user interaction
     * @param frame
     */
    private void createMenu() {
    	
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);   
        
        //Create the menus for exiting, voting and showing results
        JMenu fileMenu, votingMenu, resultsMenu;
        JMenuItem exitMenuItem, manualVoteMenuItem, randomVoteMenuItem, singlePeakedPreferenceMenuItem, uniformVoteMenuItem, removeCandidateVoteMenuItem, majorityGraphMenuItem, rankingTableMenuItem;
        
        //Create menu item for ending the program
        fileMenu = new JMenu("File");
        exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {System.exit(0);} });
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        //Create voting options menu
        votingMenu = new JMenu("Vote");

        manualVoteMenuItem = new JMenuItem("Manual Voting Preferences");
        final VotingUserInterface votingUserInterfaceReference = this;
        manualVoteMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.doManualVotingUILayout();} });
        votingMenu.add(manualVoteMenuItem);
        
        randomVoteMenuItem = new JMenuItem("Random Voting Preferences");
        randomVoteMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.doRandomWeightsAndRanksVotingUILayout();} });
        votingMenu.add(randomVoteMenuItem);
        
        singlePeakedPreferenceMenuItem = new JMenuItem("Single Peaked Preference");
        singlePeakedPreferenceMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.doSinglePeakedPreferenceVotingUILayout();} });
        votingMenu.add(singlePeakedPreferenceMenuItem);
        
        uniformVoteMenuItem = new JMenuItem("Uniform Voting");
        uniformVoteMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.doUniformVotingUILayout();} });
        votingMenu.add(uniformVoteMenuItem);
        
        removeCandidateVoteMenuItem = new JMenuItem("Remove Candidate for Slater");
        removeCandidateVoteMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.doRemoveCandidatePopup();} });
        votingMenu.add(removeCandidateVoteMenuItem);
        menuBar.add(votingMenu);
        
        //Create the menu to show voting results
        resultsMenu = new JMenu("Results");
        majorityGraphMenuItem = new JMenuItem("Majority Graph");
        majorityGraphMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.showMajorityGraph();} });
        resultsMenu.add(majorityGraphMenuItem);
       
        rankingTableMenuItem = new JMenuItem("Ranking Table");
        rankingTableMenuItem.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent e) {votingUserInterfaceReference.showRankingTable();} });
        resultsMenu.add(rankingTableMenuItem);
        menuBar.add(resultsMenu);
        
        //Create the menu bar
        this.setJMenuBar(menuBar);
        
    }
     
    /**
     * Create the GUI and show it.  For thread safety,
     * this method is invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
    	VotingUserInterface frame = new VotingUserInterface("Social Choice");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create the menu
        frame.createMenu();
        //Display the window.
        frame.getContentPane().setPreferredSize(new Dimension(600, 300));
        frame.pack();
        frame.setVisible(true);
    }
     
    public static void main(String[] args) {
        /* Use an appropriate Look and Feel */
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
         
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
    private class VoterWeightActionListener implements ActionListener {
    	
    	private String voter;
    	
    	public VoterWeightActionListener(String voter) {
    		this.voter = voter;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			
			@SuppressWarnings("unchecked")
			JComboBox<String> voterWeightSelector = (JComboBox<String>) e.getSource();
			String voterWeight = (String) voterWeightSelector.getSelectedItem();
			VotingUserInterface.this.pluralityVoterWeights.put(voter, Integer.valueOf(voterWeight));
		}
    	
    }
    
    private class VoterCandidateRankActionListener implements ActionListener {
    	
    	private String voter;
    	private String candidate;
    	
    	public VoterCandidateRankActionListener(String voter, String candidate) {
    		this.voter = voter;
    		this.candidate = candidate;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			
			@SuppressWarnings("unchecked")
			JComboBox<String> voterCandidateRankSelector = (JComboBox<String>) e.getSource();
			String voterCandidateRank = (String) voterCandidateRankSelector.getSelectedItem();
			Map<String, Integer> unvalidatedVoterRankings = VotingUserInterface.this.unvalidatedVoterCandidateRanks.get(this.voter);
			unvalidatedVoterRankings.put(this.candidate, Integer.valueOf(voterCandidateRank));
			VotingUserInterface.this.unvalidatedVoterCandidateRanks.put(this.voter, unvalidatedVoterRankings);
		}
    	
    }
    
    private class SinglePeakedPreferenceActionListener implements ActionListener {
    	
    	private String voter;
    	private String candidate;
    	
    	public SinglePeakedPreferenceActionListener(String voter, String candidate) {
    		this.voter = voter;
    		this.candidate = candidate;
    	}
    	
		@Override
		public void actionPerformed(ActionEvent e) {
			VotingUserInterface.this.singlePeakedPreferenceVoterChoices.put(this.voter, getCandidateNumber(this.candidate));
		}
		
		private Integer getCandidateNumber(String selectedCandidate) {
			int candidateIndex = 0;
			for (String candidate : VotingUserInterface.ELECTION_CANDIDATES) {
				if (candidate.equals(selectedCandidate)) {
					return Integer.valueOf(candidateIndex);
				}
				++candidateIndex;
			}
			return 0;
		}
    	
    }
    
    private class UniformVotingCandidateSelectionHandler implements ListSelectionListener {

    	private String comparisonWithCandidate;
    	private boolean firstSelector;
    	private String[] candidateList;
    	
    	public UniformVotingCandidateSelectionHandler(String comparisonWithCandidate, boolean firstSelector) {
    		this.comparisonWithCandidate = comparisonWithCandidate;
    		this.firstSelector = firstSelector;
    		this.candidateList = getListOfOtherCandidates(comparisonWithCandidate);
    	}
    	
		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			ListSelectionModel listSelectionModel = (ListSelectionModel)e.getSource();
		    
		    if (!listSelectionModel.isSelectionEmpty()) {
		    	
		    	int minIndex = listSelectionModel.getMinSelectionIndex();
	            int maxIndex = listSelectionModel.getMaxSelectionIndex();
		    	CandidatePair candidatePair = null, reversedCandidatePair = null;
	            for (int candidateIndex = minIndex; candidateIndex <= maxIndex; ++candidateIndex) {
	            	if (listSelectionModel.isSelectedIndex(candidateIndex)) {
	            		candidatePair = new CandidatePair(comparisonWithCandidate, this.candidateList[candidateIndex]);
	            		if (VotingUserInterface.this.uniformVotingRanks.containsKey(candidatePair)) {
	            			if (this.firstSelector) {
	            				VotingUserInterface.this.uniformVotingRanks.put(candidatePair, Integer.valueOf(Integer.valueOf(VotingUserInterface.this.uniformVotingRanks.get(candidatePair)) + 1));
	            			} else {
	            				VotingUserInterface.this.uniformVotingRanks.put(candidatePair, Integer.valueOf(Integer.valueOf(VotingUserInterface.this.uniformVotingRanks.get(candidatePair)) - 1));
	            			}
	            		} else {
	            			reversedCandidatePair = new CandidatePair(this.candidateList[candidateIndex], comparisonWithCandidate);
	            			if (VotingUserInterface.this.uniformVotingRanks.containsKey(reversedCandidatePair)) {
		            			if (this.firstSelector) {
		            				VotingUserInterface.this.uniformVotingRanks.put(reversedCandidatePair, Integer.valueOf(Integer.valueOf(VotingUserInterface.this.uniformVotingRanks.get(reversedCandidatePair)) - 1));
		            			} else {
		            				VotingUserInterface.this.uniformVotingRanks.put(reversedCandidatePair, Integer.valueOf(Integer.valueOf(VotingUserInterface.this.uniformVotingRanks.get(reversedCandidatePair)) + 1));
		            			}
	            			} else {
		            			if (this.firstSelector) {
		            				VotingUserInterface.this.uniformVotingRanks.put(candidatePair, Integer.valueOf(1));
		            			} else {
		            				VotingUserInterface.this.uniformVotingRanks.put(reversedCandidatePair, Integer.valueOf(1));
		            			}
	            			}
	            		}
	            	}
	            }
		    }
		     
		}
    	
    }
}
