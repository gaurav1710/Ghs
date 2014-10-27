import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * A Message Bus
 * Nodes will poll this bus for new messages
 *
 */
public class MessageBus {
	
	private Vector<Message> messageStore = new Vector<Message>(Property.MAX_SIZE);
	private String lock = "lock1";
	
	public void put(Message message){
		if(Property.DEBUG){
			System.out.println("Adding message to bus.");
		}
		synchronized(lock){
			messageStore.add(message);
		}
		
	}
	
	public Message getMessage(int nodeId){
		if(Property.DEBUG){
			System.out.println("Reading message from bus (Node "+nodeId+")");
		}
		synchronized(lock){
			for(int i = 0 ; i < messageStore.size();i++){
				if(messageStore.get(i).getTo() == nodeId){
					Message message = messageStore.get(i);
					messageStore.remove(i);
					return message;
				}
			}
		}
		
		return null;
	}
	
	public void printMessageBus(){
		System.out.println("Message Store:"+messageStore);
	}
	
}
