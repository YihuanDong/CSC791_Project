
public class DataRow implements Comparable<DataRow> {
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
	public int hintType;
	public String hintGiven;
	public boolean isForced;
	public int hintCount;
	public double stepTime;
	public double elTime;
	public String certificate;
	public String numProblemsCompleted;
	public String problemType;
	public int numRulesApplied;
	public int numRestart;
	public int numSkipInLevel;
	public int numSkipInTutor;
	public int hintFollow; //0: not followed; 1: immediate follow; 2: not immediate follow;
	
	@Override
	public int compareTo(DataRow o) {
		return this.interaction - o.interaction;
	}
}
