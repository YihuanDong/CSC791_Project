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
		double result = Double.parseDouble(this.record.get(23))-Double.parseDouble(o.record.get(23));
		if (result > 0) return 1;
		else if (result < 0) return -1;
		else return 0;
	}

}
