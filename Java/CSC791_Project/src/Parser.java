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
	private static final String[] HEADER = new String[]{
		"studentID", "interaction", "numInteractionInProblem"
	};
	
	private static final Map<String,Map<String,DataRow>> records = new HashMap<String,Map<String,DataRow>>();
	
	private static final Map<String, CSVPrinter> csvPrinters = new HashMap<String, CSVPrinter>();
	
	public static void splitRecord(String file) throws IOException{
		String outputFolder = file.substring(0,file.lastIndexOf("."));
		new File(outputFolder).mkdirs();
		
		CSVParser parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT.withHeader());
		System.out.println("Record number: " + parser.getRecordNumber());
		System.out.println("Parser.splitRecord() Finished!");
		parser.close();
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
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
				
			
		}catch(IOException ex){
			System.out.print(ex);
		}
		
	}

}
