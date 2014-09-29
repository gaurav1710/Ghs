import java.util.ArrayList;
import java.util.List;


/**
 * A Message Bus
 * Nodes will poll this bus for new messages
 *
 */
public class MessageBus {
	
	List<Message> messageStore = new ArrayList<Message>(Property.MAX_SIZE);
	
	public synchronized void put(Message message){
		messageStore.add(message);
	}
	
	public synchronized Message getMessage(int nodeId){
		for(int i = 0 ; i < messageStore.size();i++){
			if(messageStore.get(i).getTo() == nodeId){
				Message message = messageStore.get(i);
				messageStore.remove(i);
				return message;
			}
		}
		
		return null;
	}
	
}
