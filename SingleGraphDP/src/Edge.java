public class Edge {
	
	/** vertex id */
    public int v1;
    
	/** vertex id */
    public int v2;
    
    /** edge label */
    private int edgeLabel;
    
    /** the hashcode */
    private int hashcode;

    /**
     * Constructor
     * @param v1 vertex id
     * @param v2 vertex id
     * @param eLabel edge label
     */
    public Edge(int v1, int v2, int eLabel) {
        this.v1 = v1;
        this.v2 = v2;
        this.edgeLabel = eLabel;
        
        this.hashcode = (v1 + 1) * 100 + (v2 + 1) * 10 + edgeLabel;
    }

    /**
     * Given a vertex id in this edge, this method returns the id of the
     * other vertex connected by this edge.
     * 
     * @param v one of the two vertices appearing in this edge
     * @return the other vertex
     */
    public int another(int v) {
        return v == v1 ? v2 : v1;
    }

    /** Get the edge label */
    public int getEdgeLabel() {
        return edgeLabel;
    }

    public int getV1() {
        return v1;
    }

    public int getV2() {
        return v2;
    }

    @Override
    /**
     * Get the hashCode of this edge label
     * @return a hash code
     */
    public int hashCode() {
        return hashcode;
    }

    @Override
    /**
     * Check if this edge is equal to another edge
     * @param obj another edge or Object
     * @return true if equal
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Edge)) return false;
        Edge that = (Edge) (obj);
        return this.hashcode == that.hashcode && this.v1 == that.v1 && this.v2 == that.v2 && this.edgeLabel == that.edgeLabel;
    }
}
