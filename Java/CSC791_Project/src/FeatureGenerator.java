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
	
	
	public static void main(String[] args) {
		try {
			System.out.println("Spliting Record...");
			FeatureGenerator.splitRecord(FeatureGenerator.DATA_DIR + "DT6_Cond6_ActionTable.csv");
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
