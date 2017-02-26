package DiGraph_A5;

import java.util.*;

public class DiGraph implements DiGraph_Interface {

    private HashMap<String, Vertex> vertices;
    private HashSet<Long> edge_ids, vertex_ids;

    public DiGraph() {
        this.vertices = new HashMap<>();
        this.vertex_ids = new HashSet<>();
        this.edge_ids = new HashSet<>();
    }

    @Override
    public boolean addNode(long idNum, String label) {
        if (idNum >= 0 && label != null && !vertex_ids.contains(idNum) &&
                vertices.putIfAbsent(label, new Vertex(label, idNum)) == null) {
            vertex_ids.add(idNum);
            return true;
        } else return false;
    }

    @Override
    public boolean addEdge(long idNum, String sLabel, String dLabel, long weight, String eLabel) {
        if (idNum >= 0 && sLabel != null && dLabel != null && !edge_ids.contains(idNum)) {
            Vertex source = vertices.get(sLabel);
            Vertex dest = vertices.get(dLabel);
            Edge e = new Edge(idNum, weight, eLabel, dest, source);
            if (source != null && dest != null && source.getOutEdges().putIfAbsent(dLabel, e) == null) {
                dest.getInEdges().put(sLabel, e);
                edge_ids.add(idNum);
                return true;
            } else return false;
        } else return false;
    }

    @Override
    public boolean delNode(String label) {
        Vertex v = vertices.get(label);
        if (v == null) return false;
        vertices.remove(label);
        vertex_ids.remove(v.getId());
        v.getOutEdges().values().forEach(e -> {
            edge_ids.remove(e.getId());
            e.getDest().getInEdges().remove(label);
        });
        v.getInEdges().values().forEach(e -> {
            edge_ids.remove(e.getId());
            e.getSource().getOutEdges().remove(label);
        });
        return true;
    }

    @Override
    public boolean delEdge(String sLabel, String dLabel) {
        Vertex v = vertices.get(sLabel);
        if (v == null) return false;
        Edge e = v.getOutEdges().get(dLabel);
        if (e == null) return false;
        edge_ids.remove(e.getId());
        e.getDest().getInEdges().remove(sLabel);
        v.getOutEdges().remove(dLabel);
        return true;
    }

    @Override
    public long numNodes() {
        return vertices.size();
    }

    @Override
    public long numEdges() {
        return edge_ids.size();
    }

    public void print() {
        vertices.values().forEach(v -> {
            System.out.println("(" + v.getId() + ")" + v.getLabel());
            v.getOutEdges().values().forEach(e -> {
                if (e.getLabel() != null)
                    System.out.println("  (" + e.getId() + ")--" + e.getLabel() + "," + e.getWeight() + "--> " + e.getDest().getLabel());
                else System.out.println("  (" + e.getId() + ")--" + e.getWeight() + "--> " + e.getDest().getLabel());
            });
        });
    }

    @Override
    public String[] topoSort() {
        String[] res = new String[vertices.size()];
        HashMap<String, Vertex> visited = new HashMap<>();
        for (Vertex v : vertices.values())
            if (!visit(v, res, visited)) return null;
        return res;
    }

    private boolean visit(Vertex v, String[] res, HashMap<String, Vertex> visited) {
        if (visited.containsKey(v.getLabel())) return true;
        if (v.isMarked()) return false;
        v.setMarked(true);
        for (Edge e : v.getOutEdges().values())
            if (!visit(e.getDest(), res, visited)) return false;
        visited.put(v.getLabel(), v);
        v.setMarked(false);
        res[vertices.size() - visited.size()] = v.getLabel();
        return true;
    }

/*    public List<String> shortestPath(String label) {
        MinBinHeap visit = new MinBinHeap();
        //MinFibHeap visit = new MinFibHeap();
        List<String> res = new LinkedList<>();
        Vertex source = vertices.get(label);
        if (source == null) return null;
        source.setDist(0);

        vertices.values().forEach( v -> {
            if (v.getDist() != 0) {
                v.setDist(Double.POSITIVE_INFINITY);
                v.setParent(null);
            }
            //visit.offer(v); //for MinFibHeap (not working fully)
        });

        visit.build(vertices); //for MinBinHeap

        while (true) {
            Vertex v = visit.poll();
            if (v == null) break;
            v.getOutEdges().values().forEach( e -> {
                double alt = v.getDist() + e.getWeight();
                if (alt < e.getDest().getDist()) {
                    e.getDest().setParent(v);
                    visit.decreaseKey(alt, e.getDest());
                }
            });
            res.add(v.getLabel().concat(": " + v.getDist()));
        }
        return res;
    }*/
}
