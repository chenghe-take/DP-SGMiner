import org.omg.CORBA.INTERNAL;

import java.io.IOException;

public class MainTestDP {

	public static void main(String [] arg) throws IOException, ClassNotFoundException{

		// set the input and output file path
		String input = "Data/facebook.lg";
		String output = ".//outputDP.txt";

		// set the minimum support threshold
		int minSupport = 60;

		/**
		 * set support type
		 * there are four support measures: "MNI", "MI", "MVC", "MIS"
		 * the MVC and MIS algorithm are approximate algorithms
		 * the exact algotithms will take too much runtime
		 */
		String supType = "MVC";
		
		// The maximum number of edges for frequent subgraph patterns
        // for DP-SGMiner, it can be adapted: int maxNumberOfEdges = Integer.MAX_VALUE
		int maxNumberOfEdges = 1;
		
		// If true, single frequent vertices will be output
		boolean outputSingleFrequentVertices = false;
		
		// If true, a dot file will be output for visualization using GraphViz
		boolean outputDotFile = false;

        //If true, use the naive method
        boolean isNaive = false;

		// If true, use the hypertree method
        boolean getHypertree = false;

        double epsilon = 1;
		
		// Apply the algorithm 
		AlgoDP algo = new AlgoDP();
		algo.runAlgorithmDP(input, output, minSupport, supType, outputSingleFrequentVertices,
				outputDotFile, maxNumberOfEdges, isNaive, getHypertree, epsilon);
		
		// Print statistics about the algorithm execution
		algo.printStats();
	}
}
