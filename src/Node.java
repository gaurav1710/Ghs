import java.net.Socket;
import java.util.List;



/**
 * Node in a GHS MST
 *
 */
public class Node implements Runnable{

	//the fragment to which this node belongs to
	private Fragment fragment;
	
	//this node's children
	private List<Node> children;
	
	private NodeState state;
	
	private int nodeId;
	
	private MessageBus messageBus;
	
	public Node(int nodeId, MessageBus messageBus){
		this.nodeId = nodeId;
		this.messageBus = messageBus;
		this.state = NodeState.SLEEP;
	}
	
	
	@Override
	public void run() {
		if(Property.DEBUG){
			System.out.println("Node with id "+nodeId+" starting up..");
		}
		while(true){
			Message message = poll();
			if(Property.DEBUG){
				System.out.println("Got message from the message bus:"+message);
			}
			processMessage();
		}
	}
	
	private Message poll(){
		
		Message message = null;
		while(message == null){
			if(Property.DEBUG){
				System.out.println("Polling the bus for new messages (Node "+nodeId+")..");
			}
			message = messageBus.getMessage(nodeId);
		}
		return message;
		
	}
	
	private void processMessage(){
		
	}
	
	private void sendMessage(){
		
	}

}
