import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.*;

public class Row{
	public CSVRecord record;
	public List<String> list;
	
	public Row(CSVRecord record) {
		this.record = record;
		//list = new LinkedList<String>();
		list = new ArrayList<String>();
		for (int i = 0; i < record.size(); i++) {
			list.add(record.get(i));
		}
	}
	
}
