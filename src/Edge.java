
/**
 *An edge from vertex u to vertex v having weight w 
 *
 */
public class Edge {
	private Node u;
		
	private Node v;
	
	private int w;
	
	public Node getU() {
		return u;
	}

	public void setU(Node u) {
		this.u = u;
	}

	public Node getV() {
		return v;
	}

	public void setV(Node v) {
		this.v = v;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	
}
