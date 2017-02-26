package DiGraph_A5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class DiGraph implements DiGraphInterface {
	public long numNodes;
	public long numEdges;
	public Node array[];

	// in here go all your data and methods for the graph
	// and the topo sort operation

	public DiGraph() { // default constructor
		// explicitly include this
		// we need to have the default constructor
		// if you then write others, this one will still be there
		numNodes = 0;
		numEdges = 0;
		array = new Node[1000];
	}

	HashMap<String, Node> digraph = new HashMap();
	HashSet<Long> nodeid = new HashSet();
	HashSet<Long> edgeid = new HashSet();

	@Override
	public boolean addNode(long idNum, String label) {
		// TODO Auto-generated method stub
		if (idNum < 0 || label == null || nodeid.contains(idNum) == true || digraph.containsKey(label) == true) {
			return false;
		}
		Node node = new Node(idNum, label);
		digraph.put(label, node);
		array[(int) idNum] = node;
		nodeid.add(idNum);
		numNodes++;
		return true;
	}

	@Override
	public boolean addEdge(long idNum, String sLabel, String dLabel, long weight, String eLabel) {
		if ( edgeid.contains(idNum) == true || idNum < 0
				|| digraph.containsKey(sLabel) == false || digraph.containsKey(dLabel) == false
				|| isIn(digraph.get(sLabel), dLabel) == true) {
			return false;
		}
		Edge edge = new Edge(idNum, 0, eLabel);
		edge.nextedge = digraph.get(sLabel).edge;
		digraph.get(sLabel).edge = edge;
		edge.points_to = digraph.get(dLabel);
		numEdges++;
		digraph.get(sLabel).out++;
		digraph.get(dLabel).in++;
		edgeid.add(idNum);
		return true;
	}

	@Override
	public boolean delNode(String label) {
		// TODO Auto-generated method stub
		if (digraph.containsKey(label) == false) {
			return false;
		}
		Node n = digraph.get(label);
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				this.delEdge(array[i].label, label);
			}
		}
		array[(int) n.idnum] = null;
		nodeid.remove(n.idnum);
		digraph.remove(label);
		numNodes--;

		return true;
	}

	@Override
	public boolean delEdge(String sLabel, String dLabel) {
		// TODO Auto-generated method stub
		if (digraph.get(sLabel) == null || digraph.get(dLabel) == null || digraph.containsKey(sLabel) == false
				|| digraph.containsKey(dLabel) == false || isIn(digraph.get(sLabel), dLabel) == false) {
			return false;
		}
		Edge tempedge = digraph.get(sLabel).edge;
		Edge prev_edge = null;

		while (tempedge != null) {
			if (digraph.get(dLabel) == tempedge.points_to) { // if destination
																// node == what
																// node tempedge
																// points to
				if (prev_edge == null) {
					digraph.get(sLabel).edge = tempedge.nextedge;
				} else {
					prev_edge.nextedge = tempedge.nextedge;
				}
				edgeid.remove(tempedge.idnum);
				numEdges--;
				digraph.get(sLabel).out--;
				digraph.get(dLabel).in--;
				return true;
			}
			prev_edge = tempedge;
			tempedge = tempedge.nextedge;
		}
		return false;
	}

	@Override
	public long numNodes() {
		// TODO Auto-generated method stub
		return numNodes;
	}

	@Override
	public long numEdges() {
		// TODO Auto-generated method stub
		return numEdges;
	}

	@Override
	public String[] topoSort() {
		// TODO Auto-generated method stub
		Edge elst;
		String fn, tn;
		int yo = 0;
	
		String[] topo = new String[1000];
		Stack<String> scratch_pad = new Stack<String>();
		Stack<String> stack = new Stack<String>();
		int counter = 0;
		
		for (int i = 0; i < array.length; i ++){
			if (array[i] != null){
			if (array[i].in == 0){
				stack.push(array[i].label);
			}
		}
	}
		
		
		while (stack.size() != 0){
			fn = stack.pop();
			topo[counter] = fn;
		//	System.out.println(topo[counter]);
			counter++;
			elst = this.array[(int)digraph.get(fn).idnum].edge;
			while (elst != null){
				scratch_pad.push(elst.points_to.label);
				elst = elst.nextedge;
			}
			while(scratch_pad.size() > 0){
				tn = scratch_pad.pop();
				this.delEdge(fn, tn);
				if (this.digraph.get(tn).in == 0){
					stack.push(tn);
				}
			}
		}
		for ( int i =0; i < topo.length; i++){
			if (topo[i] != null){
				yo += 1;
			}
		}
		if (yo != this.numNodes){
			return null;
		}

		String[] copy = new String[yo];
		for (int i = 0; i < yo; i ++){
			copy[i] = topo[i];
		}
		
		return copy;

	}

	// rest of your code to implement the various operations
	public boolean isIn(Node n, String s) {
		Edge temp = n.edge;
		// Edge temp2 = digraph.get(s).edge;

		while (temp != null) {
			if (temp.points_to == digraph.get(s)) {
				return true;
			}
			temp = temp.nextedge;
		}

		return false;
	}
	
}


