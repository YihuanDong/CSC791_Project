import org.apache.commons.csv.*;

public class DataRow implements Comparable<DataRow> {
	public String studentID;
	public int interaction = -1;
	public int numInteractionInProblem = -1;
	public int representation = -1;
	public int direction = -1;
	public int proofDir = -1;
	public int action = -1;
	public String rule;
	public int num1Action = -1;
	public int num2Action = -1;
	public int num3Action = -1;
	public int num4Action = -1;
	public int num5Action = -1;
	public int num6Action = -1;
	public int num7Action = -1;
	public int num8Action = -1;
	public int num9Action = -1;
	public int num10Action = -1;
	public String application;
	public String derived;
	public String preState;
	public String postState;
	public String currProb;
	public int hintType = -1;
	public String hintGiven;
	public boolean isForced = true;
	public int hintCount = -1;
	public double stepTime = -1.0;
	public double elTime = -1.0;
	public String certificate;
	public String numProblemsCompleted;
	public String problemType;
	public int PPType = -1;
	public int numRulesApplied = -1;
	public int numRestart = -1;
	public int numSkipInLevel = -1;
	public int numSkipInTutor = -1;
	public int hintFollow = -1; //0: not followed; 1: immediate follow; 2: not immediate follow;
	
	public DataRow(){}
	
	public DataRow(CSVRecord record) {
		this.studentID = record.get(0); 
		this.interaction = record.get(1).equals("") ? -1 : Integer.parseInt(record.get(1)); 
		this.numInteractionInProblem = record.get(2).equals("") ? -1 : Integer.parseInt(record.get(2)); 
		this.representation = record.get(3).equals("") ? -1 : Integer.parseInt(record.get(3)); 
		this.direction = record.get(4).equals("") ? -1 : Integer.parseInt(record.get(4)); 
		this.proofDir = record.get(5).equals("") ? -1 : Integer.parseInt(record.get(5)); 
		this.action = record.get(6).equals("") ? -1 : Integer.parseInt(record.get(6)); 
		this.rule = record.get(7); 
		this.num1Action = record.get(8).equals("") ? -1 : Integer.parseInt(record.get(8)); 
		this.num2Action = record.get(9).equals("") ? -1 : Integer.parseInt(record.get(9)); 
		this.num3Action = record.get(10).equals("") ? -1 : Integer.parseInt(record.get(10)); 
		this.num4Action = record.get(11).equals("") ? -1 : Integer.parseInt(record.get(11)); 
		this.num5Action = record.get(12).equals("") ? -1 : Integer.parseInt(record.get(12)); 
		this.num6Action = record.get(13).equals("") ? -1 : Integer.parseInt(record.get(13)); 
		this.num7Action = record.get(14).equals("") ? -1 : Integer.parseInt(record.get(14)); 
		this.num8Action = record.get(15).equals("") ? -1 : Integer.parseInt(record.get(15)); 
		this.num9Action = record.get(16).equals("") ? -1 : Integer.parseInt(record.get(16)); 
		this.num10Action = record.get(17).equals("") ? -1 : Integer.parseInt(record.get(17)); 
		this.application = record.get(18); 
		this.derived = record.get(19); 
		this.preState = record.get(20); 
		this.postState = record.get(21); 
		this.currProb = record.get(22); 
		this.hintType = record.get(23).equals("") ? -1 : Integer.parseInt(record.get(23)); 
		this.hintGiven = record.get(24); 
		this.isForced = record.get(25).equals("") ? true : Boolean.parseBoolean(record.get(25));
		this.hintCount = record.get(26).equals("") ? -1 : Integer.parseInt(record.get(26)); 
		this.stepTime = record.get(27).equals("") ? -1 : Double.parseDouble(record.get(27)); 
		this.elTime = record.get(28).equals("") ? -1 : Double.parseDouble(record.get(28)); 
		this.certificate = record.get(29); 
		this.numProblemsCompleted = record.get(30); 
		this.problemType = record.get(31); 
		this.PPType = record.get(32).equals("") ? -1 : Integer.parseInt(record.get(32)); 
		this.numRulesApplied = record.get(33).equals("") ? -1 : Integer.parseInt(record.get(33)); 
		this.numRestart = record.get(34).equals("") ? -1 : Integer.parseInt(record.get(34)); 
		this.numSkipInLevel = record.get(35).equals("") ? -1 : Integer.parseInt(record.get(35)); 
		this.numSkipInTutor = record.get(36).equals("") ? -1 : Integer.parseInt(record.get(36)); 
		this.hintFollow = record.get(37).equals("") ? -1 : Integer.parseInt(record.get(37)); 
	}
	
	@Override
	public int compareTo(DataRow o) {
		return this.interaction - o.interaction;
	}
}
