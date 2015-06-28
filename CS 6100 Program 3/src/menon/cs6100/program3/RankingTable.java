package menon.cs6100.program3;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class RankingTable extends JDialog implements ActionListener {

	public RankingTable(JFrame frame, List<String> slaterRanking, List<String> kemenyRanking, Map<Integer, String> bucklinRanking, String secondOrderCopelandWinner, String instantRunoffWinner, Map<Integer, String> bordaRanking, String condorcetWinner, int numberOfCandidates) {
		
		super(frame, "Ranking Table", true);
		
		//Create the table headers
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("Ranking"); //1
		columnNames.add("Slater"); //2
		columnNames.add("Kemeny"); //3
		columnNames.add("Bucklin"); //4
		columnNames.add("2nd Order Copeland"); //5
		columnNames.add("Single Transferable Vote"); //6
		columnNames.add("Borda"); //7
		columnNames.add("Condorcet"); //8
		
		//Create the data rows
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		for (int rowIndex = 0; rowIndex < numberOfCandidates; ++ rowIndex) {
			rows.add(new Vector<String>());
		}
		
		//Slater ranking
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			rows.get(ranking).add(0, String.valueOf(ranking + 1));
			rows.get(ranking).add(1, ranking < slaterRanking.size() && slaterRanking.get(ranking) != null ? slaterRanking.get(ranking) : "");
		}
		
		//Kemeny Ranking
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			rows.get(ranking).add(2, kemenyRanking.get(ranking) != null ? kemenyRanking.get(ranking) : "");
		}
		
		//Bucklin Ranking
		int bucklinRank = Integer.MAX_VALUE;
		
		if (bucklinRanking != null) {
			Set<Integer> rankingScore = bucklinRanking.keySet();
			for (Integer rank : rankingScore) {
				bucklinRank = rank.intValue();
			}
		}
		
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			if (ranking == bucklinRank) {
				rows.get(ranking).add(3, bucklinRanking.get(bucklinRank) != null ? bucklinRanking.get(bucklinRank) : "");
			} else {
				rows.get(ranking).add(3, "");
			}
			
		}
		
		//2nd Order Copeland
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			if (ranking == 0) {
				rows.get(ranking).add(4, secondOrderCopelandWinner != null ? secondOrderCopelandWinner : "");
			} else {
				rows.get(ranking).add(4, "");
			}
		}
		
		//Single Transferable Vote aka Instant Runoff Winner
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			if (ranking == 0) {
				rows.get(ranking).add(5, instantRunoffWinner != null ? instantRunoffWinner : "");
			} else {
				rows.get(ranking).add(5, "");
			}
		}
		
		//Borda
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			if (bordaRanking.containsKey(Integer.valueOf(ranking + 1))) {
				rows.get(ranking).add(6, bordaRanking.get(Integer.valueOf(ranking + 1)) != null ? bordaRanking.get(Integer.valueOf(ranking + 1)) : "");
			} else {
				rows.get(ranking).add(6, "");
			}
		}

		//Condorcet
		for (int ranking = 0; ranking < numberOfCandidates; ++ranking) {
			if (ranking == 0) {
				rows.get(ranking).add(7, condorcetWinner != null ? condorcetWinner : "");
			} else {
				rows.get(ranking).add(7, "");
			}
		}
		
		JTable rankingTable = new JTable(rows, columnNames);
		rankingTable.setEnabled(false);
		Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(rankingTable.getTableHeader(), BorderLayout.NORTH);
		container.add(rankingTable, BorderLayout.CENTER);
		
		//Put an OK button at the bottom
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(new JLabel("                    "), BorderLayout.EAST);
		bottomPanel.add(okButton, BorderLayout.CENTER);
		bottomPanel.add(new JLabel("                    "), BorderLayout.WEST);
		container.add(bottomPanel, BorderLayout.SOUTH);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dispose();
	}
}
