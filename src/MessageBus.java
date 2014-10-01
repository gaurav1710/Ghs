import java.util.ArrayList;
import java.util.List;


/**
 * A Message Bus
 * Nodes will poll this bus for new messages
 *
 */
public class MessageBus {
	
	List<Message> messageStore = new ArrayList<Message>(Property.MAX_SIZE);
	String lock = "lock1";
	
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
	
}
