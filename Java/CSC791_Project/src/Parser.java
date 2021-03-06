import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.*;


public class Parser {
	private static Map<String,Integer> headerMap;
	
	// Save record: first by studentID, then by currPrb, then order by interaction
	private static final Map<String,Map<String,List<Row>>> recordsByStudent = new HashMap<String,Map<String,List<Row>>>();

	public static void splitRecord(String file) throws IOException{
		String outputFolder = file.substring(0,file.lastIndexOf("."));
		new File(outputFolder).mkdirs();
		
		CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
		headerMap = parser.getHeaderMap();
		
		saveRecordsToMap(parser);
		
		parser.close();
	}
	
	public static void updateNumAttempts() {
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				double lastElTime = 0.0;
				int numAttempts = 0;
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					double currElTime = Double.parseDouble(row.list.get(headerMap.get("elTime")));
					
					if (currElTime < lastElTime) numAttempts++;
					
					row.list.set(headerMap.get("numAttempts"), Integer.toString(numAttempts));
					lastElTime = currElTime;
				}
			}
		}
	}
	
	public static void updateNumAction() {
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				int[] numActionCurr = new int[10]; //number of action for current attempt
				int[] numActionTotal = new int[10]; //total number of action for each problem
				
				// initialize numActionCurr and numActionTotal
				for (int i = 0; i < numActionCurr.length; i++) {
					numActionCurr[i] = 0;
					numActionTotal[i] = 0;
				}
				
				List<Row> recordsForCurrPrb = recordsByProblem.get(key1);
				double lastElTime = 0.0;
				for (int i = 0; i < recordsForCurrPrb.size(); i++) {
					Row row = recordsForCurrPrb.get(i);
					int action = Integer.parseInt(row.list.get(headerMap.get("action")));
					double currElTime = Double.parseDouble(row.list.get(headerMap.get("elTime")));
					
					// if restart attempt, reset numActionCurr, add 1 to numActionCurr[i]
					if (currElTime < lastElTime) {
						for (int j = 0; j < numActionCurr.length; j++) {
							numActionCurr[j] = 0;
						}
					}
					
					if (action < 1 || action >10) continue;
					
					numActionCurr[action-1]++;
					numActionTotal[action-1]++;
					lastElTime = currElTime;
					
					// update numActionCurr and numActionTotal columns
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
	
	public static void updateNumInteraction() {
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1:recordsByProblem.keySet()) {
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				double lastElTime = 0.0;
				int numInteractionCurr = 0;
				//count number of interactions and update for each row
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					
					// numInteractionCurr depends on if problem restart
					double currElTime =  Double.parseDouble(row.list.get(headerMap.get("elTime")));
					if (currElTime < lastElTime) {
						numInteractionCurr = 0;
					}
					numInteractionCurr++;
					row.list.set(headerMap.get("numInteractionCurr"), Integer.toString(numInteractionCurr));
					lastElTime = currElTime;
					
					// numInteractionTotal = i+1
					row.list.set(headerMap.get("numInteractionTotal"), Integer.toString(i+1));
				}
			}
		}
	}
	
	public static void updateNumProblemsCompleted() {
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			int numProblemsCompleted = 0;
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				for (int i = 0; i < recordForCurrPrb.size(); i++) {
					Row row = recordForCurrPrb.get(i);
					if (row.list.get(headerMap.get("action")).equals("98")) {
						numProblemsCompleted++;
					}
					row.list.set(headerMap.get("numProblemsCompleted"), Integer.toString(numProblemsCompleted));
				}
			}
		}
	}
	
	public static void updateStepTime() {
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordsForCurrPrb = recordsByProblem.get(key1);
				double lastElTime = 0.0;
				
				for (int i = 0; i < recordsForCurrPrb.size(); i++) {
					Row row = recordsForCurrPrb.get(i);
					double currElTime = Double.parseDouble(row.list.get(headerMap.get("elTime")));
					
					if (currElTime < lastElTime) row.list.set(headerMap.get("stepTime"), Double.toString(currElTime));
					else row.list.set(headerMap.get("stepTime"), Double.toString(currElTime-lastElTime));
					
					lastElTime = currElTime;
				}
			}
		}
	}
	
	public static void updateIsForced() {
		for (String key: recordsByStudent.keySet()) {
			Map<String, List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordsForCurrPrb = recordsByProblem.get(key1);
				
				for (int i = 0; i < recordsForCurrPrb.size(); i++) {
					Row row = recordsForCurrPrb.get(i);
					if (row.list.get(headerMap.get("action")).equals("7"))
						row.list.set(headerMap.get("isForced"), "0");
					else
						row.list.set(headerMap.get("isForced"), "1");
				}
			}
		}
	}
	
	public static void outputInOneFile(String filePath) throws IOException{
		if (headerMap == null) {
			System.out.println("No headerMap.");
			return;
		}
		
		String[] header = headerMap.keySet().toArray(new String[headerMap.keySet().size()]);
		CSVPrinter printer = new CSVPrinter(new FileWriter(filePath), CSVFormat.EXCEL.withHeader(header));
		
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
					if (row.list.get(headerMap.get("hintGiven")).startsWith("Try to derive")) {
						Object[] obj = row.list.toArray();
						printer.printRecord(obj);
					}
				}
				
			}
		}
		printer.close();
	}
	
	private static void orderRecordsByInteraction() {
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordForCurrPrb = recordsByProblem.get(key1);
				Collections.sort(recordForCurrPrb);
			}
		}
	}
	
	private static void saveRecordsToMap(CSVParser parser) {
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
		
		
	}
	
	public static void checkRecordNumber() {
		// Check record number
		int checkRecordNumber = 0;
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordForCurrProb = recordsByProblem.get(key1);
				checkRecordNumber += recordForCurrProb.size();
			}
		}
		System.out.println("checkRecordNumber: " + checkRecordNumber);
	}

	
	private static boolean isUnwantedRow(CSVRecord record) {
		if (record.get(headerMap.get("rule")).equals("login")) return true;
		if (record.get(headerMap.get("rule")).equals("#NAME?")) return true;
		if (record.get(headerMap.get("rule")).equals("next")) return true;
		return false;
	}

	public static void main(String[] args) {
		try {
			System.out.println("Spliting Record...");
			Parser.splitRecord("../../data/DT6_Cond5_ActionTable.csv");
			System.out.println();
			
			System.out.println("Updating number of Interaction...");
			Parser.updateNumInteraction();
			System.out.println();
			
			System.out.println("Updating number of problems completed...");
			Parser.updateNumProblemsCompleted();
			System.out.println();
			
			System.out.println("Updating number of attempts to current problem...");
			Parser.updateNumAttempts();
			System.out.println();
			
			System.out.println("Updating number of action both for current attempt and for the problem...");
			Parser.updateNumAction();
			System.out.println();
			
			System.out.println("Updating stepTime...");
			Parser.updateStepTime();
			System.out.println();
			
			System.out.println("Updating isForced...");
			Parser.updateIsForced();
			System.out.println();
			
			
			System.out.println("Outputing records into one file...");
			Parser.outputInOneFile("../../data/DT6_Cond5_ActionTable_Filled.csv");
			System.out.println();
			
			System.out.println("Outputing hint follow records into one file...");
			Parser.outputHintFollowRecords("../../data/DT6_Cond5_HintFollow.csv");
			System.out.println();
			
			System.out.println("Completed!");
			
			
			
			
//			DataRow d = new DataRow();
//			DataRow a = new DataRow();
//			d.interaction = 5;
//			a.interaction = 1;
//			List<DataRow> list = new LinkedList<DataRow>();
//			list.add(d);
//			list.add(a);
//			Collections.sort(list);
//			
//			for(int i = 0; i < list.size(); i++){
//				System.out.println(list.get(i).interaction);
//			}
//				
//			System.out.println((1==2)?false:true);
		}catch(IOException ex){
			System.out.print(ex);
		}
		
	}

}
