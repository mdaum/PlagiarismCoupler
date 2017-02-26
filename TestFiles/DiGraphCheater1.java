package DiGraph_A5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class DiGraph implements DiGraphInterface {

	// in here go all your data and methods for the graph
	// and the topo sort operation
	public long numNodes;
	public long numEdges;
	public DiGraph_Node nodeArray[];
	HashMap<String, DiGraph_Node> hashmap = new HashMap();
	HashSet<Long> IDNode = new HashSet();
	HashSet<Long> IDEdge = new HashSet();


	public DiGraph() { // default constructor
		numNodes = 0;
		numEdges = 0;
		nodeArray = new DiGraph_Node[1000];
	}

	public boolean addNode(long idNum, String label) {
		if(idNum < 0 || label == null || IDNode.contains(idNum) == true || hashmap.containsKey(label) == true){
			return false;
		}
		
		DiGraph_Node node = new DiGraph_Node(idNum, label);
		hashmap.put(label, node);
		nodeArray[(int) idNum] = node;
		IDNode.add(idNum);
		numNodes++;
		
		return true;
		// adds node and returns true, increments number of nodes and puts it in hashmap/array
	}

	public boolean addEdge(long idNum, String sLabel, String dLabel, long weight, String eLabel) {
		if( IDEdge.contains(idNum) == true || idNum < 0 || hashmap.containsKey(sLabel) == false 
				|| hashmap.containsKey(dLabel) == false || 
				isIn(hashmap.get(sLabel),dLabel) == true){
			return false;
		}
		
		DiGraph_Edges edge = new DiGraph_Edges(idNum, 1, eLabel);
		edge.nextEdge = hashmap.get(sLabel).edgeOne;
		hashmap.get(sLabel).edgeOne = edge;
		edge.thisNode = hashmap.get(dLabel);
		numEdges++;
		hashmap.get(sLabel).out++;
		hashmap.get(dLabel).in++;
		IDEdge.add(idNum);
		return true;
		
		// adds edge and returns true, increments number of edges
	}

	public boolean delNode(String label) {
		if(hashmap.containsKey(label) == false){
			return false;
		}
		
		for(int i = 0; i < nodeArray.length; i++){
			if(nodeArray[i] != null){
				this.delEdge(nodeArray[i].label, label);
			}
		}
		
		DiGraph_Node node = hashmap.get(label);
		nodeArray[(int) node.number] = null;
		IDNode.remove(node.number);
		hashmap.remove(label);
		numNodes--;
		
		return true;
	}

	public boolean delEdge(String sLabel, String dLabel) {
		if(hashmap.containsKey(sLabel) == false || hashmap.containsKey(dLabel) == false ||
				hashmap.get(sLabel) == null || hashmap.get(dLabel) == null || 
				isIn(hashmap.get(sLabel), dLabel) == false){
			return false;
		}

		DiGraph_Edges temporaryEdge = hashmap.get(sLabel).edgeOne;
		DiGraph_Edges previousEdge = null;
		
		while(temporaryEdge != null){
			if(hashmap.get(dLabel) == temporaryEdge.thisNode){
				if(previousEdge == null){
					hashmap.get(sLabel).edgeOne = temporaryEdge.nextEdge;
				} else {
					previousEdge.nextEdge = temporaryEdge.nextEdge;
				}

				IDEdge.remove(temporaryEdge.number);
				numEdges--;
				hashmap.get(sLabel).out--;
				hashmap.get(dLabel).in--;
				return true;
			}
			previousEdge = temporaryEdge;
			temporaryEdge = temporaryEdge.nextEdge;
		}
		return false;
	}

	public long numNodes() {
		return numNodes;
	}

	public long numEdges() {
		return numEdges;
	}

	public String[] topoSort() {
		DiGraph_Edges newEdge;
		String fn, tn;
		int yayInt = 0;
		
		String[] topoArray = new String[1000];
		Stack<String> extra = new Stack<String>();
		Stack<String> stack = new Stack<String>();
		int count = 0;
		
		for(int i = 0; i < nodeArray.length; i++){
			if(nodeArray[i] != null){
				if(nodeArray[i].in == 0){
					stack.push(nodeArray[i].label);
				}
			}
		}
		while(stack.size() != 0){
			fn = stack.pop();
			topoArray[count] = fn;
			count++;
			newEdge = this.nodeArray[(int) hashmap.get(fn).number].edgeOne;
			while(newEdge != null){
				extra.push(newEdge.thisNode.label);
				newEdge = newEdge.nextEdge;
			}
			while(extra.size() > 0){
				tn = extra.pop();
				this.delEdge(fn, tn);
				if(this.hashmap.get(tn).in == 0){
					stack.push(tn);
				}
			}
		}
		
		for(int i = 0; i < topoArray.length; i++){
			if(topoArray[i] != null){
				yayInt += 1;
			}
		}
		
		if(yayInt != this.numNodes){
			return null;
		}
		
		String[] copy = new String[yayInt];
		for(int i = 0; i < yayInt; i++){
			copy[i] = topoArray[i];
		}
		return copy;
	}

	public boolean isIn(DiGraph_Node n, String s){
		DiGraph_Edges temp = n.edgeOne;
		
		while(temp != null){
			if(temp.thisNode == hashmap.get(s)){
				return true;
			} 
			temp = temp.nextEdge;
		}
		return false;
		
		// this method checks if it is in the hashmap
	}
	
}