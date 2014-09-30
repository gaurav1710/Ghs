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
	String lock2 = "lock2";
	
	public void put(Message message){
		synchronized(lock2){
			messageStore.add(message);
		}
	}
	
	public Message getMessage(int nodeId){
		synchronized(lock){
			System.out.println("List Size:"+messageStore.size());
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
