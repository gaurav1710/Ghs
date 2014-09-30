
/**
 * A message sent from one node to another 
 *
 */
public class Message {

	private MessageType type;
	private int from;
	private int to;	
	private int level;
	private String fragName;
	private NodeState state;
	private int bestWeight;
	
	public int getBestWeight() {
		return bestWeight;
	}
	public void setBestWeight(int bestWeight) {
		this.bestWeight = bestWeight;
	}
	public MessageType getType() {
		return type;
	}
	public void setType(MessageType type) {
		this.type = type;
	}
	public int getFrom() {
		return from;
	}
	
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public NodeState getState() {
		return state;
	}
	public void setState(NodeState state) {
		this.state = state;
	}

	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public String getFragName() {
		return fragName;
	}
	public void setFragName(String fragName) {
		this.fragName = fragName;
	}
	@Override
	public String toString() {
		return "Message [type=" + type + ", from=" + from + ", to=" + to
				+ ", level=" + level + ", fragName=" + fragName + ", state="
				+ state + ", bestWeight=" + bestWeight + "]";
	}
	

}
