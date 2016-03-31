import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.*;

public class Row implements Comparable<Row>{
	public CSVRecord record;
	public List<String> list;
	
	public Row(CSVRecord record) {
		this.record = record;
		list = new LinkedList<String>();
		for (int i = 0; i < record.size(); i++) {
			list.add(record.get(i));
		}
	}
	@Override
	public int compareTo(Row o) {
		// TODO Auto-generated method stub
		return Integer.parseInt(this.record.get(1))-Integer.parseInt(o.record.get(1));
	}

}
