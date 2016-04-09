import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class FeatureGenerator {
	public static final String DATA_DIR = "../../data/Cond6/";
	
	// Save the header into csv, for getting the column number with header name
	private static Map<String,Integer> headerMap;
	
	// Save record: first by studentID, then by currPrb, then order by interaction
	private static final Map<String,Map<String,List<Row>>> recordsByStudent = new HashMap<String,Map<String,List<Row>>>();
	
	// Split records and save into recordsByStudent map
	// file is the path of input file
	public static void splitRecord(String file) throws IOException{
		String outputFolder = file.substring(0,file.lastIndexOf("."));
		
		CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
		headerMap = parser.getHeaderMap();
		
		for (CSVRecord record: parser) {
			// if the rule doesn't meet these requirements, discard the row
//			if (isUnwantedRow(record)) continue;
			
			String studentID = record.get(headerMap.get("studentID"));
			
			// if studentID does not exist in recordsByStudent, create a record for the studentID 
			if (!recordsByStudent.containsKey(studentID)) {
				Map<String, List<Row>> recordsByProblem = new HashMap<String, List<Row>>();
				recordsByStudent.put(studentID, recordsByProblem);
			}
			
			// Get recordByProblem for the studentID
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(studentID);
			String currPrb = record.get(headerMap.get("currPrb"));
			
			// if currProb does not exist in recordByProblem, create a record for the currProb
			if (!recordsByProblem.containsKey(currPrb)) {
				List<Row> recordForCurrPrb = new LinkedList<Row>();
				recordsByProblem.put(currPrb, recordForCurrPrb);
			}
			
			// Get recordsForCurrProb
			List<Row> recordForCurrPrb = recordsByProblem.get(currPrb);
			
			// Add new row to recordsForCurrProb
			Row newRow = new Row(record);
			recordForCurrPrb.add(newRow);
		}
		
		parser.close();
	}
	
	// Check if the row does not satisfy a certain criteria
	private static boolean isUnwantedRow(CSVRecord record) {
		if (record.get(headerMap.get("rule")).equals("login")) return true;
		if (record.get(headerMap.get("rule")).equals("#NAME?")) return true;
		if (record.get(headerMap.get("rule")).equals("next")) return true;
		if (record.get(headerMap.get("action")).equals("8")) return true;
//		if (!record.get(headerMap.get("collaborators")).equals("")) return true;
		return false;
	}
	
	// Output all records in recordsByStudent to a single csv file.
	public static void outputRecords(String file) throws IOException{
		if (headerMap == null) {
			System.out.println("No headerMap.");
			return;
		}
		
		String[] header = headerMap.keySet().toArray(new String[headerMap.keySet().size()]);
		CSVPrinter printer = new CSVPrinter(new FileWriter(file), CSVFormat.EXCEL.withHeader(header));
		
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					Object[] obj = row.list.toArray();
					printer.printRecord(obj);
				}
			}
		}
		printer.close();
	}
	
	// Calculate Feature
	public static void CalculateFeatures() {
		/********************************************
		* DECLARE: features for all students here.
		*********************************************/
		String[] strRuleNames = new String[] {"MP","DS", "SIMP", "MT", "ADD", "CONJ", "HS", "CD", "DN", 
				"DEM", "IMPL", "CP", "EQUIV", "NULL", "COM", "ASSOC", "DIST", "ABS", "EXP", "TAUT", "DEL"};
		List<String> RuleNames = new LinkedList<String>();
		
		for(int i = 0; i < strRuleNames.length; i++) RuleNames.add(strRuleNames[i]);
		
		for (String key: recordsByStudent.keySet()) {
			/**********************************************
			* DECLARE: features for a single student here.
			***********************************************/
			int numProblemsCompleted = 0;
			int hintRequestedTutor = 0;
			int hintForcedTutor = 0;
			int hintFollowTutor = 0;
			int hintRequestedFollowTutor = 0;
			int hintForcedFollowTutor = 0;
			
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				/*********************************************
				* DECLARE: features for a single problem here.
				**********************************************/
				double lastElTime = 0.0; // used to determine the start of a new attempt
				double elTimeTotal = 0.0;
				int numInteractionCurr = 0; 
				int numInteractionTotal = 0;
				int numAttempts = 1;
				int hintRequestedCurr = 0;
				int hintRequestedTotal = 0;
				int hintForcedCurr = 0;
				int hintForcedTotal = 0;
				int hintFollowCurr = 0;
				int hintFollowTotal = 0;
				int hintRequestedFollowCurr = 0;
				int hintRequestedFollowTotal = 0;
				int hintForcedFollowCurr = 0;
				int hintForcedFollowTotal = 0;
				
				
				
				int[] numActionCurr = new int[10];
				int[] numActionTotal = new int[10];
				Map<String,Integer> numCorrectRuleCurr = new HashMap<String,Integer>();
				Map<String,Integer> numCorrectRuleTotal = new HashMap<String,Integer>();
				Map<String,Integer> numWrongRuleCurr = new HashMap<String,Integer>();
				Map<String,Integer> numWrongRuleTotal = new HashMap<String,Integer>();
				
				// Initialize map
				for (int i = 0; i < RuleNames.size(); i++) {
					numCorrectRuleCurr.put(RuleNames.get(i), 0);
					numCorrectRuleTotal.put(RuleNames.get(i), 0);
					numWrongRuleCurr.put(RuleNames.get(i), 0);
					numWrongRuleTotal.put(RuleNames.get(i), 0);
				}
				
				// Loop through rows for current problem
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					int action = Integer.parseInt(row.list.get(headerMap.get("action"))); 
					double stepTime = 0.0;
					int isForced = 0;
					int hasCollaborator = 0;
					String currPrb = row.list.get(headerMap.get("currPrb"));
					int error = Integer.parseInt(row.list.get(headerMap.get("error")));
					String rule = row.list.get(headerMap.get("rule")).toUpperCase();
					boolean isRule = RuleNames.contains(rule);
					
					// check if start a new attempt
					double currElTime =  Double.parseDouble(row.list.get(headerMap.get("elTime")));
					if (currElTime < lastElTime) {
						/****************************************
						 * Reset or Update Current Counters here
						 ***************************************/
						numInteractionCurr = 0;
						numAttempts++;
						stepTime = currElTime;
						hintRequestedCurr = 0;
						hintForcedCurr = 0;
						hintFollowCurr = 0;
						hintRequestedFollowCurr = 0;
						hintForcedFollowCurr = 0;
						
						//numActionCurr
						for (int j = 0; j < numActionCurr.length; j++) numActionCurr[j] = 0;
						
						//numCorrectRuleCurr and numWrongRuleCurr
						for (int j = 0; j < RuleNames.size(); j++) {
							numCorrectRuleCurr.put(RuleNames.get(j), 0);
							numWrongRuleCurr.put(RuleNames.get(j), 0);
						}
						
					} else {
						stepTime = currElTime - lastElTime;
					}
					lastElTime = currElTime;
					
					/********************************
					 * Calculate Current feature here
					 ********************************/
					numInteractionCurr++;

					//numActionCurr
					if (action >= 1 && action <= 10) numActionCurr[action-1]++;
					
					//isForced
					if (action == 7) isForced = 1;
					
					//hasCollaborator
					if (!row.list.get(headerMap.get("collaborators")).equals("")) hasCollaborator = 1;
					
					//numCorrectRuleCurr
					if (isRule) {
						if (action == 3 && error == 0) numCorrectRuleCurr.put(rule, numCorrectRuleCurr.get(rule)+1);
						if (action == 5 && error != 0) numCorrectRuleCurr.put(rule, numCorrectRuleCurr.get(rule)-1);
					}
					
					//numWrongRuleCurr
					if (isRule) {
						if ((action == 3 || action == 5) && error != 0) numWrongRuleCurr.put(rule, numWrongRuleCurr.get(rule)+1);
					}
					
					// hintRequestedCurr
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedCurr++;
					
					// hintForcedCurr
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedCurr++;
					
					// hintFollowCurr, hintRequestedFollowCurr, hintForcedFollowCurr
					if (row.list.get(headerMap.get("hintFollow")).equals("1")) {
						hintFollowCurr++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedFollowCurr++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedFollowCurr++;
					}
					
					/********************************
					 * Calculate Total feature here
					 ********************************/
					numInteractionTotal = i+1;
					elTimeTotal += stepTime;
					
					//numActionTotal
					if (action >= 1 && action <= 10) numActionTotal[action-1]++;
					
					//numCorrectRuleTotal
					if (isRule) {
						if (action == 3 && error == 0) numCorrectRuleTotal.put(rule, numCorrectRuleTotal.get(rule)+1);
						if (action == 5 && error != 0) numCorrectRuleTotal.put(rule, numCorrectRuleTotal.get(rule)-1);
					}
					
					//numWrongRuleTotal
					if (isRule) {
						if ((action == 3 || action == 5) && error != 0) numWrongRuleTotal.put(rule, numWrongRuleTotal.get(rule)+1);
					}
					
					// hintRequestedTotal
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedTotal++;
					
					// hintForcedTotal
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedTotal++;
					
					// hintFollowTotal, hintRequestedFollowTotal, hintForcedFollowTotal
					if (row.list.get(headerMap.get("hintFollow")).equals("1")) {
						hintFollowTotal++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedFollowTotal++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedFollowTotal++;
					}
					
					/*******************************
					 * Calculate Tutor feature here
					 *******************************/
					//numProblemsCompleted
					if (row.list.get(headerMap.get("action")).equals("98")) numProblemsCompleted++;
					
					// hintRequestedTutor
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedTutor++;
					
					// hintForcedTutor
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedTutor++;
					
					// hintFollowTutor, hintRequestedFollowTutor, hintForcedFollowTutor
					if (row.list.get(headerMap.get("hintFollow")).equals("1")) {
						hintFollowTutor++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action == 7) hintRequestedFollowTutor++;
						if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive") && action != 7) hintForcedFollowTutor++;
					}
					
					
					/****************************
					 * Update feature value here
					 ****************************/
					row.list.set(headerMap.get("numInteractionCurr"), Integer.toString(numInteractionCurr));
					row.list.set(headerMap.get("numInteractionTotal"), Integer.toString(numInteractionTotal));
					row.list.set(headerMap.get("numProblemsCompleted"), Integer.toString(numProblemsCompleted));
					row.list.set(headerMap.get("numAttempts"), Integer.toString(numAttempts));
					row.list.set(headerMap.get("stepTime"), Double.toString(stepTime));
					row.list.set(headerMap.get("isForced"), Integer.toString(isForced));
					row.list.set(headerMap.get("hasCollaborator"), Integer.toString(hasCollaborator));
					row.list.set(headerMap.get("PPLevel"), getPPLevel(currPrb));
					row.list.set(headerMap.get("elTimeTotal"), Double.toString(elTimeTotal));
					row.list.set(headerMap.get("hintRequestedCurr"), Integer.toString(hintRequestedCurr));
					row.list.set(headerMap.get("hintRequestedTotal"), Integer.toString(hintRequestedTotal));
					row.list.set(headerMap.get("hintForcedCurr"), Integer.toString(hintForcedCurr));
					row.list.set(headerMap.get("hintForcedTotal"), Integer.toString(hintForcedTotal));
					row.list.set(headerMap.get("hintRequestedTutor"), Integer.toString(hintRequestedTutor));
					row.list.set(headerMap.get("hintForcedTutor"), Integer.toString(hintForcedTutor));
					row.list.set(headerMap.get("hintFollowCurr"), Integer.toString(hintFollowCurr));
					row.list.set(headerMap.get("hintFollowTotal"), Integer.toString(hintFollowTotal));
					row.list.set(headerMap.get("hintRequestedFollowCurr"), Integer.toString(hintRequestedFollowCurr));
					row.list.set(headerMap.get("hintRequestedFollowTotal"), Integer.toString(hintRequestedFollowTotal));
					row.list.set(headerMap.get("hintForcedFollowCurr"), Integer.toString(hintForcedFollowCurr));
					row.list.set(headerMap.get("hintForcedFollowTotal"), Integer.toString(hintForcedFollowTotal));
					row.list.set(headerMap.get("hintFollowTutor"), Integer.toString(hintFollowTutor));
					row.list.set(headerMap.get("hintRequestedFollowTutor"), Integer.toString(hintRequestedFollowTutor));
					row.list.set(headerMap.get("hintForcedFollowTutor"), Integer.toString(hintForcedFollowTutor));					
					
					row.list.set(headerMap.get("hintFollowRateCurr"), Double.toString((double)hintFollowCurr / (hintRequestedCurr + hintForcedCurr)));
					row.list.set(headerMap.get("hintFollowRateTotal"), Double.toString((double)hintFollowTotal / (hintRequestedTotal + hintForcedTotal)));
					row.list.set(headerMap.get("hintFollowRateTutor"), Double.toString((double)hintFollowTutor / (hintRequestedTutor + hintForcedTutor)));
					row.list.set(headerMap.get("hintForcedFollowRateCurr"), Double.toString((double)hintForcedFollowCurr / hintForcedCurr));
					row.list.set(headerMap.get("hintForcedFollowRateTotal"), Double.toString((double)hintForcedFollowTotal / hintForcedTotal));
					row.list.set(headerMap.get("hintForcedFollowRateTutor"), Double.toString((double)hintForcedFollowTutor / hintForcedTutor));
					row.list.set(headerMap.get("hintRequestedFollowRateCurr"), Double.toString((double)hintRequestedFollowCurr / hintRequestedCurr));
					row.list.set(headerMap.get("hintRequestedFollowRateTotal"), Double.toString((double)hintRequestedFollowTotal / hintRequestedTotal));
					row.list.set(headerMap.get("hintRequestedFollowRateTutor"), Double.toString((double)hintRequestedFollowTutor / hintRequestedTutor));
					
					
					// numActionCurr and numActionTotal
					for (int j = 0; j < numActionCurr.length; j++) {
						String colNameCurr = "numAction" + Integer.toString(j+1) + "Curr";
						String colNameTotal = "numAction" + Integer.toString(j+1) + "Total";
						row.list.set(headerMap.get(colNameCurr), Integer.toString(numActionCurr[j]));
						row.list.set(headerMap.get(colNameTotal), Integer.toString(numActionTotal[j]));
					}
					
					// PL[Rule]
					for (int j = 0; j < RuleNames.size(); j++) {
						String colName = "PL" + RuleNames.get(j);
						String ruleScore = getPL(row.list.get(headerMap.get("score" + RuleNames.get(j))));
						row.list.set(headerMap.get(colName), ruleScore);
					}
					
					// numCorrectRuleCurr, numCorrectRuleTotal, numWrongRuleCurr, numWrongRuleTotal, numRuleCurr, numRuleTotal          
					for (int j = 0; j < RuleNames.size(); j++) {
						int numRuleCurr = numCorrectRuleCurr.get(RuleNames.get(j)) + numWrongRuleCurr.get(RuleNames.get(j));
						int numRuleTotal = numCorrectRuleTotal.get(RuleNames.get(j)) + numWrongRuleTotal.get(RuleNames.get(j));
						
						row.list.set(headerMap.get("numCorrect" + RuleNames.get(j) + "Curr"), Integer.toString(numCorrectRuleCurr.get(RuleNames.get(j))));
						row.list.set(headerMap.get("numCorrect" + RuleNames.get(j) + "Total"), Integer.toString(numCorrectRuleTotal.get(RuleNames.get(j))));
						row.list.set(headerMap.get("numWrong" + RuleNames.get(j) + "Curr"), Integer.toString(numWrongRuleCurr.get(RuleNames.get(j))));
						row.list.set(headerMap.get("numWrong" + RuleNames.get(j) + "Total"), Integer.toString(numWrongRuleTotal.get(RuleNames.get(j))));
						row.list.set(headerMap.get("num" + RuleNames.get(j) + "Curr"), Integer.toString(numRuleCurr));
						row.list.set(headerMap.get("num" + RuleNames.get(j) + "Total"), Integer.toString(numRuleTotal));
					}
				}
			}
		}
	}
	
	public static void updateHintFollow(String file) throws IOException{
		CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
		Map<String, Integer> header = parser.getHeaderMap();
		
		List<Row> statsRecords = new LinkedList<Row>();
		for (CSVRecord record: parser) {
			statsRecords.add(new Row(record));
		}
		
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordsForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordsForCurrPrb.size(); i++) {
					Row row = recordsForCurrPrb.get(i);
					int hintFollow = -1;
					if (!row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive")) {
						row.list.set(headerMap.get("hintFollow"), Integer.toString(hintFollow));
						continue;
					}
					
					String studentID = row.list.get(headerMap.get("studentID"));
					String currPrb = row.list.get(headerMap.get("currPrb"));
					String elTime = row.list.get(headerMap.get("elTime"));
					
					for (int j = 0; j < statsRecords.size(); j++) {
						Row hintRow = statsRecords.get(j);
						
						if (studentID.equals(hintRow.list.get(header.get("studentID")))
								&& currPrb.equals(hintRow.list.get(header.get("currPrb")))
								&& elTime.equals(hintRow.list.get(header.get("elTime")))) {
							if (Boolean.parseBoolean(hintRow.list.get(header.get("lvl1HintFollowed"))))
								hintFollow = 1;
							else
								hintFollow = 0;
							break;
						}
					}
					
					row.list.set(headerMap.get("hintFollow"), Integer.toString(hintFollow));
				}
			}
		}
	}
	
	public static void outputHintFollowRecords(String filePath) throws IOException {
		String[] header = headerMap.keySet().toArray(new String[headerMap.keySet().size()]);
		CSVPrinter printer = new CSVPrinter(new FileWriter(filePath),CSVFormat.EXCEL.withHeader(header));
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordsForCurrPrb = recordsByProblem.get(key1);
				
				for (int i = 0; i < recordsForCurrPrb.size(); i++) {
					Row row = recordsForCurrPrb.get(i);
					
					// Change Restrictions on which lines to print here
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive")
							&& !row.list.get(headerMap.get("action")).equals("98")) {
						Object[] obj = row.list.toArray();
						printer.printRecord(obj);
					}
				}
			}
		}
		printer.close();
	}

	/*********************
	 * Helper Functions
	 ********************/
	/***************
	 * get probability of initial learning from KT scores
	 * @param KT: KT score in format: 0.01;0.01;0.3;0.1
	 * @return PL: First number of KT Score (Probability of initial leaning
	 */
	private static String getPL(String KT) {
		String PL = KT.substring(0, KT.indexOf(";"));
		return PL;
	}
	
	/****************
	 * Get Proficiency level from currPrb
	 * @param currPrb: format of 1.0.1.0, with second digit as proficiency level
	 * @return: second digit
	 */
	private static String getPPLevel(String currPrb) {
		return currPrb.replaceAll("\\d\\.(\\d|-\\d)\\.\\d\\.\\d", "$1");
	}
	
	
	/*****************
	 * Main Function
	 ****************/
	public static void main(String[] args) {
		try {
			System.out.println("Spliting Record...");
			FeatureGenerator.splitRecord(FeatureGenerator.DATA_DIR + "Cond6_Ready.csv");
			System.out.println();
			
			System.out.println("Updating hintFollow...");
			FeatureGenerator.updateHintFollow(FeatureGenerator.DATA_DIR + "Cond6_Tag.csv");
			System.out.println();

			System.out.println("Calculating Features...");
			FeatureGenerator.CalculateFeatures();
			System.out.println();

			System.out.println("Outputing records into one file...");
			FeatureGenerator.outputRecords(FeatureGenerator.DATA_DIR + "/Cond6_Filled.csv");
			System.out.println();
			
			// refactor to only output columns after currPrb
			System.out.println("Outputing hint follow records into one file...");
			FeatureGenerator.outputHintFollowRecords(FeatureGenerator.DATA_DIR + "Cond6_HintFollow.csv");
			System.out.println();
			
			
			System.out.println("Completed!");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
