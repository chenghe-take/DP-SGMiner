import java.util.Set;

public class FrequentSubgraph implements Comparable<FrequentSubgraph>{
	
	/** dfs code */
    public DFSCode dfsCode;
    
    /** the ids of graphs where the subgraph appears */
    public Set<HyperEdge> hyperEdges;
    
    /** the support of the subgraph */
    public int support;
    
    /**
     * Constructor
     * @param dfsCode a dfs code
     * @param hyperedges the ids of graphs where the subgraph appears
     * @param support the support of the subgraph
     */
    public FrequentSubgraph(DFSCode dfsCode, Set<HyperEdge> hyperedges, int support){
    	this.dfsCode = dfsCode;
    	this.hyperEdges = hyperedges;
    	this.support = support;
    }


    /**
     * Compare this subgraph with another subgraph
     * @param o another subgraph
     * @return 0 if equal, -1 if smaller, 1 if larger (in terms of support).
     */
    public int compareTo(FrequentSubgraph o) {
		if(o == this){
			return 0;
		}
		long compare =  this.support - o.support;
		if(compare > 0){
			return 1;
		}
		if(compare < 0){
			return -1;
		}
		return 0;
	}
}
