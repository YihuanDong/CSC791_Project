import java.io.File;
import java.io.FileReader;
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
		
		orderRecordsByInteraction();

		
		System.out.println("Record number: " + parser.getRecordNumber());
		System.out.println("Parser.splitRecord() Finished!");
		parser.close();
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
		
		// Check record number
		/*int checkRecordNumber = 0;
		for (String key: recordsByStudent.keySet()) {
			Map<String,List<Row>> recordsByProblem = recordsByStudent.get(key);
			for (String key1: recordsByProblem.keySet()) {
				List<Row> recordForCurrProb = recordsByProblem.get(key1);
				checkRecordNumber += recordForCurrProb.size();
			}
		}
		System.out.println("checkRecordNumber: " + checkRecordNumber);*/
	}
	public static void main(String[] args) {
		try {
			Parser.splitRecord("../../data/DT6_Cond5_ActionTable.csv");
			DataRow d = new DataRow();
			DataRow a = new DataRow();
			d.interaction = 5;
			a.interaction = 1;
			List<DataRow> list = new LinkedList<DataRow>();
			list.add(d);
			list.add(a);
			Collections.sort(list);
			
			for(int i = 0; i < list.size(); i++){
				System.out.println(list.get(i).interaction);
			}
				
			System.out.println((1==2)?false:true);
		}catch(IOException ex){
			System.out.print(ex);
		}
		
	}

}
