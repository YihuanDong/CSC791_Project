import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;


public class FeatureGenerator {
	public static final String DATA_DIR = "../../data/DT6/";
	
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
			if (isUnwantedRow(record)) continue;
			
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
		
		for (String key: recordsByStudent.keySet()) {
			/**********************************************
			* DECLARE: features for a single student here.
			***********************************************/
			int numProblemsCompleted = 0;
		
			
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				/*********************************************
				* DECLARE: features for a single problem here.
				**********************************************/
				double lastElTime = 0.0; // used to determine the start of a new attempt
				int numInteractionCurr = 0; 
				int numInteractionTotal = 0;
				int numAttempts = 1;
				int[] numActionCurr = new int[10];
				int[] numActionTotal = new int[10];
				
				
				
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					int action = Integer.parseInt(row.list.get(headerMap.get("action"))); 
					double stepTime = 0.0;
					int isForced = 1;
					
					// check if start a new attempt
					double currElTime =  Double.parseDouble(row.list.get(headerMap.get("elTime")));
					if (currElTime < lastElTime) {
						/*****************************
						 * Reset or Update Current Counters here
						 *****************************/
						numInteractionCurr = 0;
						numAttempts++;
						stepTime = currElTime;
						
						//numActionCurr
						for (int j = 0; j < numActionCurr.length; j++) {
							numActionCurr[j] = 0;
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
					if (row.list.get(headerMap.get("action")).equals("7")) isForced = 0;
					else isForced = 1;
					
					/********************************
					 * Calculate Total feature here
					 ********************************/
					numInteractionTotal = i+1;
					
					//numActionTotal
					if (action >= 1 && action <= 10) numActionTotal[action-1]++;
					
					/*******************************
					 * Calculate Tutor feature here
					 *******************************/
					//numProblemsCompleted
					if (row.list.get(headerMap.get("action")).equals("98")) {
						numProblemsCompleted++;
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
					
					// numActionCurr and numActionTotal
					for (int j = 0; j < numActionCurr.length; j++) {
						String colNameCurr = "numAction" + Integer.toString(j+1) + "Curr";
						String colNameTotal = "numAction" + Integer.toString(j+1) + "Total";
						row.list.set(headerMap.get(colNameCurr), Integer.toString(numActionCurr[j]));
						row.list.set(headerMap.get(colNameTotal), Integer.toString(numActionTotal[j]));
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
					
					String studentID = row.list.get(headerMap.get("studentID"));
					String currPrb = row.list.get(headerMap.get("currPrb"));
					String elTime = row.list.get(headerMap.get("elTime"));
					int hintFollow = -1;
					
					for (int j = 0; j < statsRecords.size(); j++) {
						Row hintRow = statsRecords.get(j);
						
						if (studentID.equals(hintRow.list.get(header.get("studentID")))
								&& currPrb.equals(hintRow.list.get(header.get("currPrb")))
								&& elTime.equals(hintRow.list.get(header.get("elTime")))) {
							if (Boolean.parseBoolean(hintRow.list.get(header.get("lvl1HintFollowed"))))
								hintFollow = 1;
							else
								hintFollow = 0;
							//System.out.println(hintFollow);
							break;
						}
					}
					
					row.list.set(headerMap.get("hintFollow"), Integer.toString(hintFollow));
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			System.out.println("Spliting Record...");
			FeatureGenerator.splitRecord(FeatureGenerator.DATA_DIR + "DT6_Cond6_ActionTable.csv");
			System.out.println();
			
			System.out.println("Calculating Features...");
			FeatureGenerator.CalculateFeatures();
			System.out.println();
			
			System.out.println("Updating hintFollow...");
			FeatureGenerator.updateHintFollow(FeatureGenerator.DATA_DIR + "DT6_Cond6_Stat.csv");
			System.out.println();
			
			System.out.println("Outputing records into one file...");
			FeatureGenerator.outputRecords(FeatureGenerator.DATA_DIR + "/DT6_Cond6_ActionTable_Filled.csv");
			System.out.println();
			
			System.out.println("Completed!");
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}

}
