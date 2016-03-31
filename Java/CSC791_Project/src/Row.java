import java.util.Map;

import org.apache.commons.csv.*;

public class Row implements Comparable<Row>{
	public CSVRecord record;
	
	public Row(CSVRecord record) {
		this.record = record;
		
	}
	@Override
	public int compareTo(Row o) {
		// TODO Auto-generated method stub
		return Integer.parseInt(this.record.get(1))-Integer.parseInt(o.record.get(1));
	}

}
