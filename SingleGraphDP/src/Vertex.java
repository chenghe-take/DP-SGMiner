import java.util.ArrayList;
import java.util.List;

public class Vertex implements Comparable<Object>{
	
	/** the vertex id */
    private int id;
    
    /** the vertex label */
    private int vLabel;
    
    /** the list of edges starting from this vertex */
    private List<Edge> eList;

    /**
     * Constructor
     * @param id  the vertex id
     * @param vLabel the vertex label
     */
    public Vertex(int id, int vLabel) {
        this.id = id;
        this.vLabel = vLabel;
        eList = new ArrayList<>();
    }

    /**
     * Add an edge to this vertex
     * @param edge an edge
     */
    public void addEdge(Edge edge) {
        eList.add(edge);
    }

    /**
     * Get the id of this vertex
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * Get the label of this vertex
     * @return the label
     */
    public int getLabel() {
        return vLabel;
    }

    /**
     * Get the list of edges from this vertex
     * @return the list of edges
     */
    public List<Edge> getEdgeList() {
        return eList;
    }

	@Override
	public int compareTo(Object o) {
		Vertex vertex = (Vertex) o;
		return id - vertex.getId();
	}
	
	@Override
	/**
	 * Compare this vertex with another vertex
	 * @param obj another Vertex
	 * @return true if they have the same id.
	 */
	public boolean equals(Object obj) {
		if(!(obj instanceof Vertex)){
			return false;
		}
		Vertex vertex = (Vertex) obj;
		return id == vertex.id;
	}
}
