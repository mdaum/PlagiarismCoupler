package DiGraph_A5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class DiGraph implements DiGraphInterface {
	public Node array[];
	HashMap<String, Node> dimap = new HashMap();
	public long edgeNums;

	// in here go all your data and methods for the graph
	// and the topo sort operation

	HashSet<Long> edges = new HashSet();

	public long nodeNums;
	HashSet<Long> nodes = new HashSet();
	public DiGraph() { // default constructor
		// explicitly include this
		// we need to have the default constructor
		// if you then write others, this one will still be there
		nodeNums = 0;
		edgeNums = 0;
		array = new Node[1000];
	}

	@Override
	public boolean addEdge(long idNum, String sLabel, String dLabel, long weight, String eLabel) {
		if (edges.contains(idNum) == true || idNum < 0 || dimap.containsKey(sLabel) == false
				|| dimap.containsKey(dLabel) == false || isIn(dimap.get(sLabel), dLabel) == true) {
			return false;
		}
		Edge edge = new Edge(idNum, 0, eLabel);
		edge.nextedge = dimap.get(sLabel).edge;
		dimap.get(sLabel).edge = edge;
		edge.points_to = dimap.get(dLabel);
		edgeNums++;
		dimap.get(sLabel).out++;
		dimap.get(dLabel).in++;
		edges.add(idNum);
		return true;
	}

	@Override
	public boolean addNode(long idNum, String label) {
		// TODO Auto-generated method stub
		if (idNum < 0 || label == null || nodes.contains(idNum) == true || dimap.containsKey(label) == true) {
			return false;
		}
		Node node = new Node(idNum, label);
		dimap.put(label, node);
		array[(int) idNum] = node;
		nodes.add(idNum);
		nodeNums++;
		return true;
	}

	@Override
	public boolean delEdge(String sLabel, String dLabel) {
		// TODO Auto-generated method stub
		if (dimap.get(sLabel) == null || dimap.get(dLabel) == null || dimap.containsKey(sLabel) == false
				|| dimap.containsKey(dLabel) == false || isIn(dimap.get(sLabel), dLabel) == false) {
			return false;
		}
		Edge temporary = dimap.get(sLabel).edge;
		Edge previous = null;

		while (temporary != null) {
			if (dimap.get(dLabel) == temporary.points_to) { // if destination
															// node == what
															// node tempedge
															// points to
				if (previous == null) {
					dimap.get(sLabel).edge = temporary.nextedge;
				} else {
					previous.nextedge = temporary.nextedge;
				}
				edges.remove(temporary.idnum);
				edgeNums--;
				dimap.get(sLabel).out--;
				dimap.get(dLabel).in--;
				return true;
			}
			previous = temporary;
			temporary = temporary.nextedge;
		}
		return false;
	}

	@Override
	public boolean delNode(String label) {
		// TODO Auto-generated method stub
		if (dimap.containsKey(label) == false) {
			return false;
		}
		Node n = dimap.get(label);
		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				this.delEdge(array[i].label, label);
			}
		}
		array[(int) n.idnum] = null;
		nodes.remove(n.idnum);
		dimap.remove(label);
		nodeNums--;

		return true;
	}

	// rest of your code to implement the various operations
	public boolean isIn(Node n, String s) {
		Edge temp = n.edge;
		// Edge temp2 = digraph.get(s).edge;

		while (temp != null) {
			if (temp.points_to == dimap.get(s)) {
				return true;
			}
			temp = temp.nextedge;
		}

		return false;
	}

	@Override
	public long numEdges() {
		// TODO Auto-generated method stub
		return edgeNums;
	}

	@Override
	public long numNodes() {
		// TODO Auto-generated method stub
		return nodeNums;
	}

	@Override
	public String[] topoSort() {
		// TODO Auto-generated method stub
		Edge element;
		String n, t;
		int hi = 0;

		String[] topo = new String[1000];
		Stack<String> sStack = new Stack<String>();
		Stack<String> stack = new Stack<String>();
		int counter = 0;

		for (int i = 0; i < array.length; i++) {
			if (array[i] != null) {
				if (array[i].in == 0) {
					stack.push(array[i].label);
				}
			}
		}

		while (stack.size() != 0) {
			n = stack.pop();
			topo[counter] = n;
			// System.out.println(topo[counter]);
			counter++;
			element = this.array[(int) dimap.get(n).idnum].edge;
			while (element != null) {
				sStack.push(element.points_to.label);
				element = element.nextedge;
			}
			while (sStack.size() > 0) {
				t = sStack.pop();
				this.delEdge(n, t);
				if (this.dimap.get(t).in == 0) {
					stack.push(t);
				}
			}
		}
		for (int i = 0; i < topo.length; i++) {
			if (topo[i] != null) {
				hi += 1;
			}
		}
		if (hi != this.nodeNums) {
			return null;
		}

		String[] copy = new String[hi];
		for (int i = 0; i < hi; i++) {
			copy[i] = topo[i];
		}

		return copy;

	}

}
