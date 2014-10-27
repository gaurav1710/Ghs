import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Node in a GHS MST
 * 
 */
public class Node implements Runnable {

	// the fragment to which this node belongs to
	private Fragment fragment;
        
    // this node's adjacent edges
    private List<Edge> adjsEdges;	

	private NodeState state;

	private int nodeId;

	private MessageBus messageBus;

	private int distanceMatrix[];

	private int noofnodes;

	private int rec;

	private int parent = -1;

	private int messagesReceivedNum = 0;

	private int messagesSentNum = 0;

	// Temporary Variables
	private int bestNode = -1;
	private int bestWeight = Integer.MAX_VALUE;
	private int testNode = -1;
        private int findCount = -1;

	public Node(int nodeId, MessageBus messageBus) {
		this.nodeId = nodeId;
		this.messageBus = messageBus;
		this.state = NodeState.SLEEP;
	}

	@Override
	public void run() {
		if (Property.DEBUG) {
			System.out.println("Node with id " + nodeId + " starting up..");
			System.out.println("Initializing..");
		}
		initialize();

		while (true) {
			Message message = poll();
			if (Property.DEBUG) {
				System.out.println("Got message from the message bus (Node "
						+ nodeId + "):" + message);
			}
			processMessage(message);
		}
	}

	// Algo 1:Initialization
	private void initialize() {
		int min = Integer.MAX_VALUE;
		int nearestNode = nodeId;
		// find least weighted edge from distance matrix for this node
		for (int i = 0; i < noofnodes; i++) {
			if (min > distanceMatrix[i]
					&& distanceMatrix[i] != 0) {
				nearestNode = i;
				min = distanceMatrix[i];

			}
		}

		if (nearestNode != nodeId) {
			// change state to FOUND
			state = NodeState.FOUND;
                        findCount = 0;
                         ListIterator<Edge> litr = adjsEdges.listIterator();
                         while(litr.hasNext()) 
                         {
                            Edge element = litr.next();
                            if(element.getW() == min)
                            {
                                element.setStatus(Status.BRANCH);
                                break;
                            }
                        }
			rec = -1;
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
			messagesReceivedNum++;
			MessageType type = message.getType();
			if(type == null)
				return;
			switch (type) {
			case CONNECT:
				if (Property.INFO) {
					System.out.println("CONNECT message received by (Node"
							+ nodeId + "):" + message);
				}
				processConnectMessage(message);
				break;

			case INITIATE:
				if (Property.INFO) {
					System.out.println("INITIATE message received by (Node"
							+ nodeId + "):" + message);
				}
				processInitiateMessage(message);
				break;

			case TEST:
				if (Property.INFO) {
					System.out.println("TEST message received by (Node"
							+ nodeId + "):" + message);
				}
				processTestMessage(message);
				break;

			case ACCEPT:
				if (Property.INFO) {
					System.out.println("ACCEPT message received by (Node"
							+ nodeId + "):" + message);
				}
				processAcceptMessage(message);
				break;

			case REJECT:
				if (Property.INFO) {
					System.out.println("REJECT message received by (Node"
							+ nodeId + "):" + message);
				}
				processRejectMessage(message);
				break;

			case REPORT:
				if (Property.INFO) {
					System.out.println("REPORT message received by (Node"
							+ nodeId + "):" + message);
				}
				processReportMessage(message);
				break;

			case CHANGEROOT:
				if (Property.INFO) {
					System.out.println("CHANGEROOT message received by (Node"
							+ nodeId + "):" + message);
				}
				processChangeRootMessage(message);
				break;
			}

		}

	}

	private void processConnectMessage(Message message) {
                if(state == NodeState.SLEEP)
                {
                    initialize();
                }
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                        element = litr.next();
                        if(element.getVI() == message.getFrom())
                        {
                                break;
                        }
                }
		if (message.getLevel() < fragment.getLevel()) {
			// combine with LT rule
                        element.setStatus(Status.BRANCH);
			Message initMessage = new Message();
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(fragment.getLevel());
			initMessage.setFragName(fragment.getName());
			initMessage.setState(state);
			initMessage.setType(MessageType.INITIATE);
			if(Property.DEBUG){
				System.out.println("Sending INITIATE message (Node" + nodeId
						+ " )...");
			}
			sendMessage(initMessage);
                        if(state == NodeState.FIND)
                        {
                            findCount++;
                        }
		} else if (element.getStatus() == Status.BASIC) {
			try {
				//Thread.sleep(Property.WAIT);
				messageBus.put(message);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (fragment.getLevel() == message.getLevel()) {
			// combine with EQ rule
			//fragment.setLevel(fragment.getLevel()+1);
			
			Message initMessage = new Message();
			initMessage.setType(MessageType.INITIATE);
			initMessage.setFrom(nodeId);
			initMessage.setTo(message.getFrom());
			initMessage.setLevel(fragment.getLevel() + 1);
			initMessage.setFragName(element.getW());
			//initMessage.setFragName(fragment.getName());
			initMessage.setState(NodeState.FIND);
			if(Property.DEBUG){
				System.out.println("Sending INITIATE message (Node" + nodeId
						+ " )...");
			}
			sendMessage(initMessage);
		}
	}

	private void processInitiateMessage(Message message) {
		fragment.setLevel(message.getLevel());
		fragment.setName(message.getFragName());
		state = message.getState();
		parent = message.getFrom();
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                        element = litr.next();
                        if(element.getVI() != message.getFrom() && element.getStatus() == Status.BRANCH)
                        {
                                Message initMessage = new Message();
                                initMessage.setType(MessageType.INITIATE);
				initMessage.setFrom(nodeId);
				initMessage.setTo(element.getVI());
				initMessage.setLevel(message.getLevel());
				initMessage.setFragName(message.getFragName());
				initMessage.setState(message.getState());
				if(Property.DEBUG){
					System.out.println("Sending INITIATE message (Node" + nodeId
							+ " )...");
				}
				sendMessage(initMessage);
                                if(state == NodeState.FIND)
                                {
                                    findCount++;
                                }
                        }
                }

		

		if (state == NodeState.FIND) {
			rec = -1;
			findMin();
		}

	}

	private void findMin() {
		int min = Integer.MAX_VALUE;
		int nearestNode = nodeId;
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                        element = litr.next();
                        if(element.getStatus() == Status.BASIC && element.getW() < min)
                        {
                            min = element.getW();
                            nearestNode = element.getVI();
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
			sendMessage(testMessage);
		} else {
			testNode = -1;
			report();
		}
	}

	private void report() {
		if (findCount == 0 && testNode == -1) 
                {
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
                if(state == NodeState.SLEEP)
                {
                    initialize();
                }
		if (fragment.getLevel() < message.getLevel()) {
			try {
				//Thread.sleep(Property.WAIT);
				messageBus.put(message);
				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (fragment.getName() == message.getFragName())    // Internal edge
                {
                            ListIterator<Edge> litr = adjsEdges.listIterator();
                            Edge element = null;
                            while(litr.hasNext()) 
                            {
                                element = litr.next();
                                if(element.getVI() == message.getFrom())
                                {
                                    break;
                                }
                            }
			
			if (element.getStatus() == Status.BASIC)
                        {
				 element.setStatus(Status.REJECT);
                        }

			if (testNode != message.getFrom()) {
				Message rejectMessage = new Message();
				rejectMessage.setType(MessageType.REJECT);
				rejectMessage.setFrom(nodeId);
				rejectMessage.setTo(message.getFrom());
				sendMessage(rejectMessage);
			} else {
				findMin();
			}
		}
                else 
                {
			Message acceptMessage = new Message();
			acceptMessage.setType(MessageType.ACCEPT);
			acceptMessage.setFrom(nodeId);
			acceptMessage.setTo(message.getFrom());
			sendMessage(acceptMessage);
		}

	}

	private void processAcceptMessage(Message message) {
		testNode = -1;
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                    element = litr.next();
                    if(element.getVI() == message.getFrom())
                    {
                        break;
                    }
                }
		if (element.getW() < bestWeight) {
			bestWeight = element.getW();
			bestNode = message.getFrom();
		}
		report();
	}

	private void processRejectMessage(Message message) {
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                        element = litr.next();
                        if(element.getVI() == message.getFrom())
                        {
                                    break;
                        }
                }                        
		if (element.getStatus() == Status.BASIC) 
                {
			 element.setStatus(Status.REJECT);
		}
		findMin();
	}

	private void processReportMessage(Message message) {
		if (message.getFrom() != parent) {
                        findCount--;
			if (message.getBestWeight() < bestWeight) {
				
				bestWeight = message.getBestWeight();
				bestNode = message.getFrom();
			}
			rec = rec + 1;
			report();
		} else {
			if (state == NodeState.FIND) {
				try {
					//Thread.sleep(Property.WAIT);
					messageBus.put(message);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (message.getBestWeight() > bestWeight) {
				changeRoot();
			} else if (message.getBestWeight() == bestWeight
					&& bestWeight == Integer.MAX_VALUE) {
				if (Property.DEBUG) {
					System.out.println("Processing completed.");
				}
				Main.completed = true;
				return;
				//System.exit(0);
			}
		}

	}

	private void changeRoot() {
            
                ListIterator<Edge> litr = adjsEdges.listIterator();
                Edge element = null;
                while(litr.hasNext()) 
                {
                        element = litr.next();
                        if(element.getVI() == bestNode)
                        {
                                    break;
                        }
                }
		if (element.getStatus() == Status.BRANCH) {
			Message crootMessage = new Message();
			crootMessage.setFrom(nodeId);
			crootMessage.setTo(bestNode);
			crootMessage.setType(MessageType.CHANGEROOT);
			sendMessage(crootMessage);
		} else {
			element.setStatus(Status.BRANCH);
			Message connectMessage = new Message();
			connectMessage.setFrom(nodeId);
			connectMessage.setTo(bestNode);
			connectMessage.setLevel(fragment.getLevel());
			connectMessage.setType(MessageType.CONNECT);
			sendMessage(connectMessage);
		}
	}

	private void processChangeRootMessage(Message message) {
		changeRoot();
	}

	private Message poll() {

		Message message = null;
		long start = System.currentTimeMillis();
		while (message == null) {

			message = messageBus.getMessage(nodeId);
			long end = System.currentTimeMillis() - start;
			if (end >= Property.POLL_TIMEOUT) {
				if(Property.DEBUG){
					System.out.println("Message polling timed out...");
				}
				break;
			}

			try {
				// Wait for a few seconds before arbitrating the bus again for
				// messages
				Thread.sleep(Property.POLL_WAIT + 100 * nodeId);// every thread
																// will get a
																// chance on the
																// lock
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return message;

	}

	private void sendMessage(Message message) {
		messagesSentNum++;
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

	public int[] getDistanceMatrix() {
		return distanceMatrix;
	}

	public void setDistanceMatrix(int[] distanceMatrix) {
           this.distanceMatrix = new int[noofnodes];
           System.arraycopy(distanceMatrix, 0, this.distanceMatrix, 0, noofnodes);
	}
        
        // initialize this node's adjacent edges
        public void initAdsEdges()
        {
            adjsEdges = new ArrayList<Edge>();
            for(int i=0;i<noofnodes;i++)
            {
               if(distanceMatrix[i] != 0)
               {
                    Edge e = new Edge();
                    e.setUI(nodeId);
                    e.setVI(i);
                    //e.setU(this);
                    //e.setV();
                    e.setW(distanceMatrix[i]);
                    e.setStatus(Status.BASIC);
                    adjsEdges.add(e);
               }
                
            }
        }

	public int getNoofnodes() {
		return noofnodes;
	}

	public void setNoofnodes(int noofnodes) {
		this.noofnodes = noofnodes;
	}
	

	public int getParent() {
		return parent;
	}

	public void setParent(int parent) {
		this.parent = parent;
	}

	public List<Edge> getAdjsEdges() {
		return adjsEdges;
	}

	public void setAdjsEdges(List<Edge> adjsEdges) {
		this.adjsEdges = adjsEdges;
	}

	
	@Override
	public String toString() {
		return "Node [fragment=" + fragment + ", state=" + state + ", nodeId="
				+ nodeId + ", parent=" + parent + "]";
	}

	public void nodeStats() {
		System.out.println("Number of Messages sent (Node " + nodeId + ") = "
				+ messagesSentNum);
		System.out.println("Number of Messages received (Node " + nodeId
				+ " )= " + messagesReceivedNum);
	}

}
