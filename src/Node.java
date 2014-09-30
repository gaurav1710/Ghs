import java.util.List;

/**
 * Node in a GHS MST
 * 
 */
public class Node implements Runnable {

	// the fragment to which this node belongs to
	private Fragment fragment;

	// this node's children
	private List<Node> children;

	private NodeState state;

	private int nodeId;

	private MessageBus messageBus;

	private int distanceMatrix[][];

	private int noofnodes;

	private Status status[];

	private int rec;

	private int parent;

	// Temporary Variables
	private int bestNode = 0;
	private int bestWeight = Integer.MAX_VALUE;
	private int testNode = 0;

	public Node(int nodeId, MessageBus messageBus) {
		this.nodeId = nodeId;
		this.messageBus = messageBus;
		this.state = NodeState.SLEEP;
		this.status = new Status[Property.MAX_SIZE];
	}

	@Override
	public void run() {
		if (Property.DEBUG) {
			System.out.println("Node with id " + nodeId + " starting up..");
			System.out.println("Initializing..");
		}
		initialize();

		// while(true){
		Message message = poll();
		if (Property.DEBUG) {
			System.out.println("Got message from the message bus (Node "
					+ nodeId + "):" + message);
		}
		processMessage(message);
		// }
	}

	// Algo 1:Initialization
	private void initialize() {
		int min = Integer.MAX_VALUE;
		int nearestNode = nodeId;
		// find least weighted edge from distance matrix for this node
		for (int i = 0; i < noofnodes; i++) {
			if (min > distanceMatrix[nodeId][i]
					&& distanceMatrix[nodeId][i] != 0) {
				nearestNode = i;
				min = distanceMatrix[nodeId][i];

			}
		}

		if (nearestNode != nodeId) {
			// change state to FOUND
			state = NodeState.FOUND;
			status[nearestNode] = Status.BRANCH;
			rec = 0;
			// send a connect message to the nearest node id
			Message connectMessage = new Message();
			connectMessage.setFrom(nodeId);
			connectMessage.setTo(nearestNode);
			connectMessage.setType(MessageType.CONNECT);
			connectMessage.setLevel(fragment.getLevel());
			if (Property.DEBUG) {
				System.out.println("Sending CONNECT message (Node" + nodeId
						+ " )...");
			}
			sendMessage(connectMessage);
		}
	}

	private void processMessage(Message message) {

		if (message != null) {
			MessageType type = message.getType();
			switch (type) {
			case CONNECT:
				processConnectMessage(message);
				break;

			case INITIATE:
				processInitiateMessage(message);
				break;

			case TEST:
				processTestMessage(message);
				break;

			case ACCEPT:
				processAcceptMessage(message);
				break;

			case REJECT:
				processRejectMessage(message);
				break;

			case REPORT:
				processReportMessage(message);
				break;
				
			case CHANGEROOT:
				processChangeRootMessage(message);
				break;
			}

		}

	}

	private void processConnectMessage(Message message) {
		if (message.getLevel() < fragment.getLevel()) {
			// combine with LT rule
			status[message.getFrom()] = Status.BRANCH;
			Message initMessage = new Message();
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(fragment.getLevel());
			initMessage.setFragName(fragment.getName());
			initMessage.setState(state);
			initMessage.setType(MessageType.INITIATE);
			System.out.println("Sending INITIATE message (Node" + nodeId
					+ " )...");
			sendMessage(message);
		} else if (status[message.getFrom()] == Status.BASIC) {
			return;
		} else {
			// combine with EQ rule
			Message initMessage = new Message();
			initMessage.setType(MessageType.INITIATE);
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(fragment.getLevel() + 1);
			initMessage.setFragName(fragment.getName());
			initMessage.setState(state);
			System.out.println("Sending INITIATE message (Node" + nodeId
					+ " )...");
			sendMessage(message);
		}
	}

	private void processInitiateMessage(Message message) {
		fragment.setLevel(message.getLevel());
		fragment.setName(message.getFragName());
		state = message.getState();
		parent = message.getFrom();

		for (int i = 0; i < noofnodes; i++) {
			if (distanceMatrix[nodeId][i] > 0 && i != message.getFrom()
					&& status[i] == Status.BRANCH) {
				Message initMessage = new Message();
				initMessage.setFrom(nodeId);
				initMessage.setTo(i);
				initMessage.setLevel(message.getLevel());
				initMessage.setFragName(message.getFragName());
				initMessage.setState(message.getState());
				System.out.println("Sending INITIATE message (Node" + nodeId
						+ " )...");
				sendMessage(message);
			}
		}

		if (state == NodeState.FIND) {
			rec = 0;
			findMin();
		}

	}

	private void findMin() {
		int min = Integer.MAX_VALUE;
		int nearestNode = nodeId;
		for (int i = 0; i < noofnodes; i++) {
			if (min > distanceMatrix[nodeId][i]
					&& distanceMatrix[nodeId][i] != 0
					&& status[i] == Status.BASIC) {
				nearestNode = i;
				min = distanceMatrix[nodeId][i];

			}
		}
		if (nearestNode != nodeId) {
			testNode = nearestNode;
			Message testMessage = new Message();
			testMessage.setType(MessageType.TEST);
			testMessage.setLevel(fragment.getLevel());
			testMessage.setFragName(fragment.getName());
			testMessage.setFrom(nodeId);
			testMessage.setTo(testNode);
		} else {
			testNode = 0;
			report();
		}
	}

	private void report() {

		if (status[rec] == Status.BRANCH && parent != rec && testNode == 0) {
			state = NodeState.FOUND;
			Message reportMessage = new Message();
			reportMessage.setType(MessageType.REPORT);
			reportMessage.setFrom(nodeId);
			reportMessage.setTo(parent);
			reportMessage.setBestWeight(bestWeight);
			sendMessage(reportMessage);
		}
	}

	private void processTestMessage(Message message) {
		if (fragment.getLevel() < message.getLevel())
			return;
		if (fragment.getName().compareToIgnoreCase(message.getFragName()) == 0) {
			// Internal edge
			if (status[message.getFrom()] == Status.BASIC)
				status[message.getFrom()] = Status.REJECT;

			if (testNode != message.getFrom()) {
				Message rejectMessage = new Message();
				rejectMessage.setType(MessageType.REJECT);
				rejectMessage.setFrom(nodeId);
				rejectMessage.setTo(message.getFrom());
				sendMessage(rejectMessage);
			} else {
				findMin();
			}
		} else {
			Message acceptMessage = new Message();
			acceptMessage.setType(MessageType.ACCEPT);
			acceptMessage.setFrom(nodeId);
			acceptMessage.setTo(message.getFrom());
			sendMessage(acceptMessage);
		}

	}

	private void processAcceptMessage(Message message) {
		testNode = 0;
		if (distanceMatrix[nodeId][message.getFrom()] < bestWeight) {
			bestWeight = distanceMatrix[nodeId][message.getFrom()];
			bestNode = message.getFrom();
		}
		report();
	}

	private void processRejectMessage(Message message) {
		if (status[message.getFrom()] == Status.BASIC) {
			status[message.getFrom()] = Status.REJECT;
		}
		findMin();
	}

	private void processReportMessage(Message message) {
		if (message.getFrom() == parent) {
			if (message.getBestWeight() < bestWeight) {
				bestWeight = message.getBestWeight();
				bestNode = message.getFrom();
			}
			rec = rec + 1;
			report();
		} else {
			if (state == NodeState.FIND) {
				return;
			} else if (message.getBestWeight() > bestWeight) {
				changeRoot();
			} else if (message.getBestWeight() == bestWeight
					&& bestWeight == Integer.MAX_VALUE) {
				System.exit(0);
			}
		}

	}

	private void changeRoot(){
		if(status[bestNode] == Status.BRANCH){
			Message crootMessage = new Message();
			crootMessage.setFrom(nodeId);
			crootMessage.setTo(bestNode);
			crootMessage.setType(MessageType.CHANGEROOT);
			sendMessage(crootMessage);
		}
		else{
			status[bestNode] = Status.BRANCH;
			Message connectMessage = new Message();
			connectMessage.setFrom(nodeId);
			connectMessage.setTo(bestNode);
			connectMessage.setLevel(fragment.getLevel());
			connectMessage.setType(MessageType.CONNECT);
			sendMessage(connectMessage);
		}
	}
	
	private void processChangeRootMessage(Message message){
		changeRoot();
	}
	
	private Message poll() {

		Message message = null;
		long start = System.currentTimeMillis();
		while (message == null) {
			if (Property.DEBUG) {
				System.out.println("Polling the bus for new messages (Node "
						+ nodeId + ")..");
			}
			message = messageBus.getMessage(nodeId);
			long end = System.currentTimeMillis() - start;
			if (end >= Property.POLL_TIMEOUT) {
				System.out.println("Message polling timed out...");
				break;
			}
		}
		return message;

	}

	private void sendMessage(Message message) {
		if (Property.DEBUG) {
			System.out.println("Putting Message on the bus:" + message);
		}
		messageBus.put(message);
	}

	public Fragment getFragment() {
		return fragment;
	}

	public void setFragment(Fragment fragment) {
		this.fragment = fragment;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public NodeState getState() {
		return state;
	}

	public void setState(NodeState state) {
		this.state = state;
	}

	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public MessageBus getMessageBus() {
		return messageBus;
	}

	public void setMessageBus(MessageBus messageBus) {
		this.messageBus = messageBus;
	}

	public int[][] getDistanceMatrix() {
		return distanceMatrix;
	}

	public void setDistanceMatrix(int[][] distanceMatrix) {
		this.distanceMatrix = distanceMatrix;
	}

	public int getNoofnodes() {
		return noofnodes;
	}

	public void setNoofnodes(int noofnodes) {
		this.noofnodes = noofnodes;
	}

}
