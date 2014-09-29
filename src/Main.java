import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Main Class - GHS Simulator
 *
 */
public class Main {

	private final int MAX_NODES = 100;
	private final String SEPARATOR = " "; 
	private Node root;
	private int noofnodes;
	private int distanceMatrix[][];
	private MessageBus messageBus = new MessageBus();
	private List<Thread> nodeList;
	
	
	
	public static void main(String[] args) {
		Main simulator = new Main();
		//read and process input from input file
		simulator.processInput();
		simulator.printInfo();
		simulator.init();
	}
	
	public void processInput(){
		FileReader inputFileReader;
		try {
			inputFileReader = new FileReader("data/input.txt");
			BufferedReader br = new BufferedReader(inputFileReader);
			noofnodes = Integer.parseInt(br.readLine());
			distanceMatrix = new int[noofnodes][noofnodes];
			
			for(int i=0 ; i<noofnodes ; i++){
				String inputLine = br.readLine();
				String ints[] = inputLine.split(SEPARATOR);
				for(int j=0 ; j < noofnodes ; j++){
					distanceMatrix[i][j] = Integer.parseInt(ints[j]);
				}
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Not able to read from file."+e.getMessage());
			e.printStackTrace();
		}		
		
	}
	
	public void init(){
		
		nodeList = new ArrayList<Thread>();
		for(int i=0;i<noofnodes;i++){
			Node node = new Node(i,messageBus);
			Thread nodeThread = new Thread(node);
			nodeList.add(nodeThread);
			nodeThread.start();
		}
		
	}
	
	public void printInfo(){
		
		for(int i = 0; i< noofnodes;i++){
			for(int j=0;j<noofnodes;j++){
				System.out.println(distanceMatrix[i][j]);
			}
		}
		
	}
	
}
