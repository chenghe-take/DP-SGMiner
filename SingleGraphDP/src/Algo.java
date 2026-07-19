import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Algo {

	/**
	 * the minimum support represented as a count (number of subgraph occurrences)
	 */
	private int minSup;

	/** The list of frequent subgraphs found by the last execution */
	private List<FrequentSubgraph> frequentSubgraphs;

	/** runtime of the most recent execution */
	private long runtime = 0;

	/** runtime of the most recent execution */
	private double maxmemory = 0;

	/** pattern count of the most recent execution */
	private int patternCount = 0;

    /** the maximum edge number of frequent patterns */
    private int patternMaxSize = 0;

	/** number of graph in the input database */
	private int graphCount = 1;

	/** frequent vertex labels */
	List<Integer> frequentVertexLabels;

	/** if true, debug mode is activated */
	private static final boolean DEBUG_MODE = false;

	/** eliminate infrequent labels from graphs */
	private static final boolean ELIMINATE_INFREQUENT_VERTICES = true;  // strategy in Gspan paper

	/** eliminate infrequent vertex pairs from graphs */
	private static final boolean ELIMINATE_INFREQUENT_VERTEX_PAIRS = true;

	/** eliminate infrequent labels from graphs */
	private static final boolean ELIMINATE_INFREQUENT_EDGE_LABELS = true;  // strategy in Gspan paper

	/** apply edge count pruning strategy */
	private static final boolean EDGE_COUNT_PRUNING = true;

	/** skip strategy */
	private static final boolean SKIP_STRATEGY = false;

	/** infrequent edges removed */
	int infrequentVertexPairsRemoved;

	/** infrequent edges removed */
	int infrequentVerticesRemovedCount;

	/** remove infrequent edge labels */
	int edgeRemovedByLabel;

	/** remove infrequent edge labels */
	int eliminatedWithMaxSize;

	/** empty graph removed by edge count pruning */
	int pruneByEdgeCountCount;

	/** skip strategy count */
	int skipStrategyCount;

	/** maximum number of edges in each frequent subgraph */
	int maxNumberOfEdges = Integer.MAX_VALUE;

	/** choose your support type **/
	private String supType;

	/** Output the ids of graph containing each frequent subgraph */
	boolean outputsubGraph = true;

    /** use the hypertree method */
	boolean getHypertree = false;

    /** the number of cadidate patterns */
    int candidateNum = 0;

	/**
	 * Run the GSpan algorithm
	 * 
	 * @param inPath               the input file
	 * @param outPath              the output file
	 * @param minSupport           a minimum support value (a percentage represented
	 *                             by a value between 0 and 1)
	 * @param outputSingleVertices if true, frequent subgraphs containing a single
	 *                             vertex will be output
	 * @param outputDotFile        if true, a graphviz DOT file will be generated to
	 *                             visualize the patterns
	 * @param maxNumberOfEdges     an integer indicating a maximum number of edges
	 *                             for each frequent subgraph
	 * @throws IOException            if error while writing to file
	 * @throws ClassNotFoundException
	 */
	public void runAlgorithm(String inPath, String outPath, int minSupport, String chooseSup, boolean outputSingleVertices,
			boolean outputDotFile, int maxNumberOfEdges, boolean getHypertree)
			throws IOException, ClassNotFoundException {

		// Calculate the minimum support as a number of graphs
		minSup = minSupport;

		supType = chooseSup;

		// if maximum size is 0
		if (maxNumberOfEdges <= 0) {
			return;
		}

		// Save the maximum number of edges
		this.maxNumberOfEdges = maxNumberOfEdges;

		this.getHypertree = getHypertree;

		// initialize variables for statistics
		infrequentVertexPairsRemoved = 0;
		infrequentVerticesRemovedCount = 0;
		edgeRemovedByLabel = 0;
		eliminatedWithMaxSize = 0;
		pruneByEdgeCountCount = 0;

		// initialize structure to store results
		frequentSubgraphs = new ArrayList<FrequentSubgraph>();

		// Initialize the tool to check memory usage
		MemoryLogger.getInstance().reset();

		// reset the number of patterns found
		patternCount = 0;

		// Record the start time
		Long t1 = System.currentTimeMillis();

		// read graphs
		Graph graphDB = readGraphs(inPath);

		// mining
		gSpan(graphDB, outputSingleVertices);

		// check the memory usage
		MemoryLogger.getInstance().checkMemory();

		// output
		writeResultToFile(outPath);

		Long t2 = System.currentTimeMillis();

		runtime = (t2 - t1) / 1000;

		maxmemory = MemoryLogger.getInstance().getMaxMemory();

		patternCount = frequentSubgraphs.size();

		// Find the maximum pattern size among all frequent subgraphs
		patternMaxSize = 0;
		for (FrequentSubgraph subgraph : frequentSubgraphs) {
			int size = subgraph.dfsCode.size();
			if (size > patternMaxSize) {
				patternMaxSize = size;
			}
		}

		if (outputDotFile) {
			outputDotFile(outPath);
		}
	}

	/**
	 * Output the DOT files to a given file path
	 * 
	 * @param outputPath the output file path
	 * @throws IOException if some exception when reading/writing the files
	 */
	private static void outputDotFile(String outputPath) throws IOException {
		String dirName = outputPath + "_dotfile";
		File dir = new File(dirName);
		if (!dir.exists())
			dir.mkdir();
		VizGraph.visulizeFromFile(outputPath, dirName);
	}

	/**
	 * Write the result to an output file
	 * 
	 * @param outputPath an output file path
	 **/
	private void writeResultToFile(String outputPath) throws IOException {
		// Create the output file
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));

		// For each frequent subgraph
		int i = 0;
		for (FrequentSubgraph subgraph : frequentSubgraphs) {
			StringBuilder sb = new StringBuilder();

			DFSCode dfsCode = subgraph.dfsCode;
			sb.append("g # ").append(i).append(" * ").append(subgraph.support).append(System.lineSeparator());
			if (dfsCode.size() == 1) {
				ExtendedEdge ee = dfsCode.getEeL().get(0);
				if (ee.getEdgeLabel() == -1) {
					sb.append("v 0 ").append(ee.getvLabel1()).append(System.lineSeparator());
				} else {
					sb.append("v 0 ").append(ee.getvLabel1()).append(System.lineSeparator());
					sb.append("v 1 ").append(ee.getvLabel2()).append(System.lineSeparator());
					sb.append("e 0 1 ").append(ee.getEdgeLabel()).append(System.lineSeparator());
				}
			} else {
				List<Integer> vLabels = dfsCode.getAllVLabels();
				for (int j = 0; j < vLabels.size(); j++) {
					sb.append("v ").append(j).append(" ").append(vLabels.get(j)).append(System.lineSeparator());
				}
				for (ExtendedEdge ee : dfsCode.getEeL()) {
					int startV = ee.getV1();
					int endV = ee.getV2();
					int eL = ee.edgeLabel;
					sb.append("e ").append(startV).append(" ").append(endV).append(" ").append(eL)
							.append(System.lineSeparator());
				}
			}
			// If the user choose to output the graph ids where the frequent subgraph
			// appears
			// We output it
			if (outputsubGraph) {
//				sb.append("x");
//				for (HyperEdge he : subgraph.hyperEdges) {
//					sb.append(" ").append(he.toString());
//				}
			}
			sb.append(System.lineSeparator()).append(System.lineSeparator());

			bw.write(sb.toString());

			i++;
		}
		bw.close();
	}

	/**
	 * Read graph from the input file
	 * 
	 * @param path the input file
	 * @return a list of input graph from the input graph database
	 * @throws IOException if error reading or writing to file
	 */
	private Graph readGraphs(String path) throws IOException {
		if (DEBUG_MODE) {
			System.out.println("start reading graphs...");
		}
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		Graph graphDatabase = null;

		String line = br.readLine();
		Boolean hasNextGraph = (line != null) && line.startsWith("t");

		// For each graph of the graph database
		while (hasNextGraph) {
			hasNextGraph = false;
			int gId = Integer.parseInt(line.split(" ")[2]);
			Map<Integer, Vertex> vMap = new HashMap<>();
			while ((line = br.readLine()) != null && !line.startsWith("t")) {

				String[] items = line.split(" ");

				if (line.startsWith("v")) {
					// If it is a vertex
					int vId = Integer.parseInt(items[1]);
					int vLabel = Integer.parseInt(items[2]);
					vMap.put(vId, new Vertex(vId, vLabel));
				} else if (line.startsWith("e")) {
					// If it is an edge
					int v1 = Integer.parseInt(items[1]);
					int v2 = Integer.parseInt(items[2]);
					Double tempeLabel = Double.parseDouble(items[3]);
					Integer eLabel = tempeLabel.intValue();
					Edge e = new Edge(v1, v2, eLabel);
//                    System.out.println(v1 + " " + v2 + " " + vMap.get(v1).id + " " + vMap.get(v2).id);
					vMap.get(v1).addEdge(e);
					vMap.get(v2).addEdge(e);
				}
			}
			graphDatabase = new Graph(gId, vMap);
			if (line != null) {
				hasNextGraph = true;
			}
		}


		br.close();

//		graphCount = graphDatabase.size();
		return graphDatabase;
	}

	/**
	 * Find all isomorphisms between graph described by c and graph g each
	 * isomorphism is represented by a map
	 * 
	 * @param c a dfs code representing a subgraph
	 * @param g a graph
	 * @return the list of all isomorphisms
	 */
	private List<Map<Integer, Integer>> subgraphIsomorphisms(DFSCode c, Graph g) {

		List<Map<Integer, Integer>> isoms = new ArrayList<>();

		// initial isomorphisms by finding all vertices with same label as vertex 0 in C
		int startLabel = c.getEeL().get(0).getvLabel1(); // only non-empty DFSCode will be real parameter
		for (int vID : g.findAllWithLabel(startLabel)) {
			Map<Integer, Integer> map = new HashMap<>();
			map.put(0, vID);
			isoms.add(map);
		}

		// each extended edge will update partial isomorphisms
		// for forward edge, each isomorphism will be either extended or discarded
		// for backward edge, each isomorphism will be either unchanged or discarded
		for (ExtendedEdge ee : c.getEeL()) {
			int v1 = ee.getV1();
			int v2 = ee.getV2();
			int v2Label = ee.getvLabel2();
			int eLabel = ee.getEdgeLabel();

			List<Map<Integer, Integer>> updateIsoms = new ArrayList<>();
			// For each isomorphism
			for (Map<Integer, Integer> iso : isoms) {

				// Get the vertex corresponding to v1 in the current edge
				int mappedV1 = iso.get(v1);

				// If it is a forward edge extension
				if (v1 < v2) {
					Collection<Integer> mappedVertices = iso.values();

					// For each neighbor of the vertex corresponding to V1
					for (Vertex mappedV2 : g.getAllNeighbors(mappedV1)) {

						// If the neighbor has the same label as V2 and is not already mapped and the
						// edge label is
						// the same as that between v1 and v2.
						if (v2Label == mappedV2.getLabel() && (!mappedVertices.contains(mappedV2.getId()))
								&& eLabel == g.getEdgeLabel(mappedV1, mappedV2.getId())) {

							// TODO: PHILIPPE: getEdgeLabel() in the above line could be precalculated in
							// Graph.java ...

							// because there may exist multiple extensions, need to copy original partial
							// isomorphism
							HashMap<Integer, Integer> tempM = new HashMap<>(iso.size() + 1);
							tempM.putAll(iso);
							tempM.put(v2, mappedV2.getId());

							updateIsoms.add(tempM);
						}
					}
				} else {
					// If it is a backward edge extension
					// v2 has been visited, only require mappedV1 and mappedV2 are connected in g
					int mappedV2 = iso.get(v2);
					if (g.isNeighboring(mappedV1, mappedV2) && eLabel == g.getEdgeLabel(mappedV1, mappedV2)) {
						updateIsoms.add(iso);
					}
				}
			}
			isoms = updateIsoms;
		}

		// Return the isomorphisms
		return isoms;
	}

	private Map<ExtendedEdge, Set<Integer>> rightMostPathExtensionsFromSingle(DFSCode c, Graph g) {
		int gid = g.getId();

		// Map of extended edges to graph ids
		Map<ExtendedEdge, Set<Integer>> extensions = new HashMap<>();

		if (c.isEmpty()) {
			// IF WE HAVE AN EMPTY SUBGRAPH THAT WE WANT TO EXTEND

			// find all distinct label tuples
			for (Vertex vertex : g.vertices) {
				for (Edge e : vertex.getEdgeList()) {
					int v1L = g.getVLabel(e.v1);
					int v2L = g.getVLabel(e.v2);
					ExtendedEdge ee1;
					if (v1L < v2L) {
						ee1 = new ExtendedEdge(0, 1, v1L, v2L, e.getEdgeLabel());
					} else {
						ee1 = new ExtendedEdge(0, 1, v2L, v1L, e.getEdgeLabel());
					}

					// Update the set of graph ids for this pattern
					Set<Integer> setOfGraphIDs = extensions.get(ee1);
					if (setOfGraphIDs == null) {
						setOfGraphIDs = new HashSet<>();
						extensions.put(ee1, setOfGraphIDs);
					}
					setOfGraphIDs.add(gid);
				}
			}
		} else {
			// IF WE WANT TO EXTEND A SUBGRAPH
			int rightMost = c.getRightMost();

			// Find all isomorphisms of the DFS code "c" in graph "g"
			List<Map<Integer, Integer>> isoms = subgraphIsomorphisms(c, g);

			// For each isomorphism
			for (Map<Integer, Integer> isom : isoms) {

				// backward extensions from rightmost child
				Map<Integer, Integer> invertedISOM = new HashMap<>();
				for (Entry<Integer, Integer> entry : isom.entrySet()) {
					invertedISOM.put(entry.getValue(), entry.getKey());
				}
				int mappedRM = isom.get(rightMost);
				int mappedRMlabel = g.getVLabel(mappedRM);
				for (Vertex x : g.getAllNeighbors(mappedRM)) {
					Integer invertedX = invertedISOM.get(x.getId());
					if (invertedX != null && c.onRightMostPath(invertedX) && c.notPreOfRM(invertedX)
							&& !c.containEdge(rightMost, invertedX)) {
						// rightmost and invertedX both have correspondings in g, so label of vertices
						// and edge all
						// can be found by correspondings
						ExtendedEdge ee = new ExtendedEdge(rightMost, invertedX, mappedRMlabel, x.getLabel(),
								g.getEdgeLabel(mappedRM, x.getId()));
						if (extensions.get(ee) == null)
							extensions.put(ee, new HashSet<>());
						extensions.get(ee).add(g.getId());
					}
				}
				// forward extensions from nodes on rightmost path
				Collection<Integer> mappedVertices = isom.values();
				for (int v : c.getRightMostPath()) {
					int mappedV = isom.get(v);
					int mappedVlabel = g.getVLabel(mappedV);
					for (Vertex x : g.getAllNeighbors(mappedV)) {
						if (!mappedVertices.contains(x.getId())) {
							ExtendedEdge ee = new ExtendedEdge(v, rightMost + 1, mappedVlabel, x.getLabel(),
									g.getEdgeLabel(mappedV, x.getId()));
							if (extensions.get(ee) == null)
								extensions.put(ee, new HashSet<>());
							extensions.get(ee).add(g.getId());
						}
					}
				}
			}
		}
		return extensions;
	}

	private Map<ExtendedEdge, Set<HyperEdge>> rightMostPathExtensions(DFSCode c, Set<HyperEdge> hyperEdges, Graph g) {
		// the key is extended edge pattern, and the value is the occurrence of this pattern
		Map<ExtendedEdge, Set<HyperEdge>> extensions = new HashMap<>();

		// if the DFS code is empty (WE START FROM AN EMPTY GRAPH)
		if (c.isEmpty()) {
			// 回来看一下对于每个ExtendedEdge是否Set<Edge>中的元素真的唯一。结论：唯一！
			// find all distinct label tuples
			for (Vertex vertex : g.vertices) {
				for (Edge e : vertex.getEdgeList()) {
					int v1L = g.getVLabel(e.v1);
					int v2L = g.getVLabel(e.v2);
					ExtendedEdge ee1;
					HyperEdge he;

					if (v1L < v2L) {
						ee1 = new ExtendedEdge(0, 1, v1L, v2L, e.getEdgeLabel());
						he = new HyperEdge(e.v1, e.v2);
						hyperEdges.add(he);
					} else {
						ee1 = new ExtendedEdge(0, 1, v2L, v1L, e.getEdgeLabel());
						he = new HyperEdge(e.v2, e.v1);
						hyperEdges.add(he);
					}

					// Update the list of edge occurrence for this pattern
					if (extensions.get(ee1) == null)
						extensions.put(ee1, new HashSet<>());
					extensions.get(ee1).add(he);

				}
			}
		}
		else {
			// IF THE DFS CODE IS NOT EMPTY (WE WANT TO EXTEND SOME EXISTING GRAPH)
			int rightMost = c.getRightMost();

			List<Map<Integer, Integer>> isoms = subgraphIsomorphisms(c, g);
			for (Map<Integer, Integer> isom : isoms) {

				// backward extensions from rightmost child
				Map<Integer, Integer> invertedISOM = new HashMap<>();
				for (Entry<Integer, Integer> entry : isom.entrySet()) {
					invertedISOM.put(entry.getValue(), entry.getKey());
				}
				int mappedRM = isom.get(rightMost);
				int mappedRMlabel = g.getVLabel(mappedRM);
				for (Vertex x : g.getAllNeighbors(mappedRM)) {
					Integer invertedX = invertedISOM.get(x.getId());
					Iterator<HyperEdge> iterator = hyperEdges.iterator();
					if (invertedX != null && c.onRightMostPath(invertedX) && c.notPreOfRM(invertedX) && !c.containEdge(rightMost, invertedX)) {

						HyperEdge he = new HyperEdge();

						while (iterator.hasNext()){
							he = iterator.next();
							List<Integer> vertices = he.getHyperEdge();
							// if the rightmost vertex of existed hyperedge is equal to the rightmost vertex of this pattern
							if (vertices.get(vertices.size()-1) == mappedRM) {
								List<Integer> newEdgeList = he.addHyperEdge(mappedRM,x.getId());
								HyperEdge newhe = new HyperEdge(newEdgeList);

								// rightmost and invertedX both have correspondings in g, so label of vertices
								// and edge all
								// can be found by correspondings
								ExtendedEdge ee = new ExtendedEdge(rightMost, invertedX, mappedRMlabel, x.getLabel(), g.getEdgeLabel(mappedRM, x.getId()));
								if (extensions.get(ee) == null)
									extensions.put(ee, new HashSet<>());
								if (newhe.hashCode() != 0){
									extensions.get(ee).add(newhe);
								}
							}
						}



					}
				}

				// forward extensions from nodes on rightmost path
				Collection<Integer> mappedVertices = isom.values();
				for (int v : c.getRightMostPath()) {
					int mappedV = isom.get(v);
					int mappedVlabel = g.getVLabel(mappedV);
					Iterator<HyperEdge> iterator = hyperEdges.iterator();
					for (Vertex x : g.getAllNeighbors(mappedV)) {
						if (!mappedVertices.contains(x.getId())) {
							HyperEdge he = new HyperEdge();

							while (iterator.hasNext()){
								he = iterator.next();
								List<Integer> vertices = he.getHyperEdge();
								// if the rightmost vertex of existed hyperedge is equal to the rightmost vertex of this pattern
								if (vertices.get(vertices.size()-1) == mappedV) {
									List<Integer> newEdge = he.addHyperEdge(mappedV,x.getId());
									HyperEdge newhe = new HyperEdge(newEdge);

									ExtendedEdge ee = new ExtendedEdge(v, rightMost + 1, mappedVlabel, x.getLabel(), g.getEdgeLabel(mappedV, x.getId()));
									if (extensions.get(ee) == null)
										extensions.put(ee, new HashSet<>());
									if (newhe.hashCode()!=0){
										extensions.get(ee).add(newhe);
									}
								}
							}

						}
					}
				}
			}
// 下面不能让extensions归零，也不能只注释extensions那一行，因为提前终止会导致新加入的边的对应图id集合不全，影响newGraphIDs.size()的sup计算，因此暂时将SKIP_STRATEGY改为false
//				if (SKIP_STRATEGY && (highestSupport + remaininggraphCount < minSup)) {
////            		System.out.println("BREAK2");
//					skipStrategyCount++;
//					extensions = null;
//					break;
//				}
//				remaininggraphCount--;
		}
		return extensions;
	}

	/**
	 * Initial call of the depth-first search
	 * 
	 * @param c                      the initial DFS code
	 * @param graphDB                a graph database
	 * @param outputFrequentVertices if true, include frequent subgraph with a
	 *                               single vertex in the output
	 * @throws IOException            exception if error writing/reading to file
	 * @throws ClassNotFoundException if error casting a class
	 */
	private void gSpan(Graph graphDB, boolean outputFrequentVertices) throws IOException, ClassNotFoundException {

		// If the user wants single vertex graph, we will output them
		if (outputFrequentVertices || ELIMINATE_INFREQUENT_VERTICES) {
			findAllOnlyOneVertex(graphDB, outputFrequentVertices);
		}

		// get the vertices of this graph
		graphDB.precalculateVertexList();


		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_EDGE_LABELS) {
			removeInfrequentVertexPairs(graphDB);
		}

		if (DEBUG_MODE) {
			System.out.println("Precalculating information...");
		}

		if (graphDB.vertices != null || graphDB.vertices.length != 0) {
			// If we deleted some vertices, we recalculate again the vertex list
			if (infrequentVerticesRemovedCount > 0) {
				graphDB.precalculateVertexList();
			}

			// Precalculate the list of neighbors of each vertex
			graphDB.precalculateVertexNeighbors();

			// Precalculate the list of vertices having each label
			graphDB.precalculateLabelsToVertices();
		} else {
			if (DEBUG_MODE) {
				System.out.println("EMPTY GRAPH!");
			}
		}

		if (frequentVertexLabels.size() != 0) {
			if (DEBUG_MODE) {
				System.out.println("Starting depth-first search...");

			}
			// Start the depth-first search

			gSpanDFS(new DFSCode(), new HashSet<HyperEdge>(), graphDB);
		}
	}

	/**
	 * Pair
	 */
	class Pair {
		/** a value */
		int x;
		/** another value */
		int y;

		Pair(int x, int y) {
			if (x < y) {
				this.x = x;
				this.y = y;
			} else {
				this.x = y;
				this.y = x;
			}
		}

		@Override
		public boolean equals(Object obj) {
			Pair other = (Pair) obj;
			return other.x == this.x && other.y == this.y;
		}

		@Override
		public int hashCode() {
			return x + 100 * y;
		}
	}

	/**
	 * Create the pruning matrix
	 */
	private void removeInfrequentVertexPairs(Graph graphDB) {

//		Set<Pair> alreadySeenPair;
		SparseTriangularMatrix matrix;
		if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
			if (DEBUG_MODE) {
				System.out.println("Calculating the pruning matrix...");
			}
			matrix = new SparseTriangularMatrix();
//			alreadySeenPair = new HashSet<Pair>();
		}

//		Set<Integer> alreadySeenEdgeLabel;
		Map<Integer, Integer> mapEdgeLabelToSupport;
		if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
			mapEdgeLabelToSupport = new HashMap<Integer, Integer>();
//			alreadySeenEdgeLabel = new HashSet<Integer>();
		}

		// CALCULATE THE SUPPORT OF EACH ENTRY

		Vertex[] vertices = graphDB.getAllVertices();

		for (int i = 0; i < vertices.length; i++) {
			Vertex v1 = vertices[i];
			int labelV1 = v1.getLabel();

			for (Edge edge : v1.getEdgeList()) {
				int v2 = edge.another(v1.getId());
				int labelV2 = graphDB.getVLabel(v2);

				if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
					// Update vertex pair count
					Pair pair = new Pair(labelV1, labelV2);
//					boolean seen = alreadySeenPair.contains(pair);
//					if (!seen) {
//						matrix.incrementCount(labelV1, labelV2);
//						alreadySeenPair.add(pair);
//					}
					matrix.incrementCount(labelV1, labelV2);
				}

				if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
					// Update edge label count
					int edgeLabel = edge.getEdgeLabel();

					Integer edgeSupport = mapEdgeLabelToSupport.get(edgeLabel);
					if (edgeSupport == null) {
						mapEdgeLabelToSupport.put(edgeLabel, 1);
					} else {
						mapEdgeLabelToSupport.put(edgeLabel, edgeSupport + 1);
					}
				}
			}
		}

		// divided by 2, because edges and vertex pair are double counted
		matrix.updateSup();

		for (Entry<Integer,Integer> entry : mapEdgeLabelToSupport.entrySet()){
			int edgeLabel = entry.getKey();
			Integer edgeSupport = mapEdgeLabelToSupport.get(edgeLabel);
			mapEdgeLabelToSupport.put(edgeLabel, edgeSupport/2);
		}

//		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
//			alreadySeenPair.clear();
//		}
//		if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
//			alreadySeenEdgeLabel.clear();
//		}

//		alreadySeenPair = null;

		// REMOVE INFREQUENT ENTRIES FROM THE MATRIX
//		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
//			if (DEBUG_MODE) {
//				System.out.println("Removing infrequent pairs...  minsup = " + minSup);
//			}
//			matrix.removeInfrequentEntriesFromMatrix(minSup);
//		}

		// Remove infrequent edge Labels and Vertex pairs
		if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_EDGE_LABELS) {
			// CALCULATE THE SUPPORT OF EACH ENTRY
//			Vertex[] vertices = graphDB.getAllVertices();

			for (int i = 0; i < vertices.length; i++) {
				Vertex v1 = vertices[i];
				int labelV1 = v1.getLabel();

				Iterator<Edge> iter = v1.getEdgeList().iterator();
				while (iter.hasNext()) {
					Edge edge = (Edge) iter.next();
					int v2 = edge.another(v1.getId());
					int labelV2 = graphDB.getVLabel(v2);

					int count = matrix.getSupportForItems(labelV1, labelV2);
                    // add noise to 'count' and 'mapEdgeLabelToSupport.get(edge.getEdgeLabel())'
                    // example for infrequent pair and edge label { pair:a-b], label: [A] }
                    // eliminating pairs and edge labels guarantees the removal of infrequent 2-subgraphs
                    if (ELIMINATE_INFREQUENT_VERTEX_PAIRS && count < minSup) {
						iter.remove();

						infrequentVertexPairsRemoved++;
					} else if (ELIMINATE_INFREQUENT_EDGE_LABELS && mapEdgeLabelToSupport.get(edge.getEdgeLabel()) < minSup) {
						iter.remove();
						edgeRemovedByLabel++;
					}
				}

			}
		}

		infrequentVertexPairsRemoved = infrequentVertexPairsRemoved/2;
		edgeRemovedByLabel = edgeRemovedByLabel/2;

		if (DEBUG_MODE) {
//			for (Vertex x: vertices){
//				for (Edge xe: x.getEdgeList()){
//					System.out.println("frequent edge label" + xe.getEdgeLabel());
//				}
//			}
//			System.out.println("vid:10, " + "vid:10 pairSup:" + matrix.getSupportForItems(10,10));
//			System.out.println("vid:10, " + "vid:11 pairSup:" + matrix.getSupportForItems(10,11));
//			for (Entry<Integer,Integer> entry : mapEdgeLabelToSupport.entrySet()){
//				System.out.println("edge label:" + entry.getKey() + ", " + "support:" + entry.getValue());
//			}

			for (int i = 0; i < vertices.length; i++) {
				Vertex v = vertices[i];
				for (Edge e: v.getEdgeList()) {
					System.out.println("frequent edge:" + e.v1 + " " + e.v2 + " " + e.getEdgeLabel());
				}
			}
		}
	}

	/**
	 * Recursive method to perform the depth-first search
	 *
	 * @param
	 * @param c          the current DFS code
	 * @param hyperEdges
	 * @param graphDB    the graph database
	 * @throws IOException            exception if error writing/reading to file
	 * @throws ClassNotFoundException if error casting a class
	 */
	private void gSpanDFS(DFSCode c, Set<HyperEdge> hyperEdges, Graph graphDB)
			throws IOException, ClassNotFoundException {
		// If we have reached the maximum size, we do not need to extend this graph
		if (c.size() == maxNumberOfEdges - 1) {
			return;
		}

		// Find all the extensions of this graph, with their support values
		// They are stored in a map where the key is an extended edge, and the value is
		// the list of vertex ids where this edge extends the current subgraph c.

		Map<ExtendedEdge, Set<HyperEdge>> extensions = rightMostPathExtensions(c, hyperEdges, graphDB);

        candidateNum = candidateNum + extensions.size();

		// For each extension
		if (extensions != null) {
			for (Entry<ExtendedEdge, Set<HyperEdge>> entry : extensions.entrySet()) {

				DFSCode newC = c.copy();
				ExtendedEdge extension = entry.getKey();
				Set<HyperEdge> hyperEdgeSet = entry.getValue();
				newC.add(extension);

//				for (HyperEdge temp : hyperEdges){
//					System.out.println(temp.toString());
//				}
//				System.out.println("============");

				if (hyperEdgeSet == null || hyperEdgeSet.size() ==0) {
					break;
				}

				int sup = 0;

				if (getHypertree) {
					Set<HyperEdge> hypertree = HypergraphPrim.Prim(hyperEdgeSet);
					sup = calculateSup(hypertree, newC, graphDB);
				} else {
					sup = calculateSup(hyperEdgeSet, newC, graphDB);
				}

				// if the support is enough
				if (sup >= minSup) {

					// Create the new DFS code of this graph
					// if the resulting graph is canonical (it means that the graph is nonredundant)
					if (isCanonical(newC)) {
						// Save the graph
						FrequentSubgraph subgraph = new FrequentSubgraph(newC, hyperEdgeSet, sup);
						frequentSubgraphs.add(subgraph);

						// Try to extend this graph to generate larger frequent subgraphs
						gSpanDFS(newC, hyperEdgeSet, graphDB);
					}
				}
			}
		}
		// check the memory usage
		MemoryLogger.getInstance().checkMemory();
	}

	/**
	 * Check if a DFS code is canonical
	 * 
	 * @param c a DFS code
	 * @return true if it is canonical, and otherwise, false.
	 */
	private boolean isCanonical(DFSCode c) {
		DFSCode canC = new DFSCode();
		for (int i = 0; i < c.size(); i++) {
			Map<ExtendedEdge, Set<Integer>> extensions = rightMostPathExtensionsFromSingle(canC, new Graph(c));
			ExtendedEdge minEE = null;
			for (ExtendedEdge ee : extensions.keySet()) {
				if (ee.smallerThan(minEE))
					minEE = ee;
			}

			if (minEE.smallerThan(c.getAt(i)))
				return false;
			canC.add(minEE);
		}
		return true;
	}

	public int calculateSup(Set<HyperEdge> hyper, DFSCode c, Graph graphDB) {
		int sup = 0;
		switch (supType) {
			case "MNI":
				sup = MNI(hyper);
				break;
			case "MI":
				sup = MI(hyper, c, graphDB);
				break;
			case "MVC":
				sup = MVC(hyper);
				break;
			case "MIS":
				sup = MIS(hyper);
				break;
		}
		return sup;
	}

//

	public int MNI(Set<HyperEdge> hyperEdgeSet) {
		if (hyperEdgeSet == null || hyperEdgeSet.isEmpty()) {
			return 0;
		}

		List<Set<Integer>> elementSets = new ArrayList<>();

		// Initialize elementSets with empty sets
		for (int i = 0; i < hyperEdgeSet.iterator().next().getHyperEdge().size(); i++) {
			elementSets.add(new HashSet<>());
		}

		// Populate elementSets with unique elements from each position in the hyperedges
		for (HyperEdge edge : hyperEdgeSet) {
			List<Integer> elements = edge.getHyperEdge();
			for (int i = 0; i < elements.size(); i++) {
				elementSets.get(i).add(elements.get(i));
			}
		}

		// Find the minimum size among the sets
		int minSize = Integer.MAX_VALUE;
		for (Set<Integer> set : elementSets) {
			minSize = Math.min(minSize, set.size());
		}

		return minSize;
	}

	public int MI(Set<HyperEdge> hyperEdgeSet, DFSCode c, Graph graphDB) {
		List<Integer> labels = c.getAllVLabels();

		// key: vertex label, value: the vertex id set with this label. For each set, if the label exists in mutiple vertex in pattern, the corresponding vertex occurrences are in this list
		Map<Integer,Set<List<Integer>>> labelMapVidSet = new HashMap<>();

		// key: vid in pattern, value, the corresponding vertex id in occurrences
		Map<Integer,List<Integer>> patternVMapOccurV = new HashMap<>();

		List<Integer> occurVid0 = new ArrayList<>();

		int MI = MNI(hyperEdgeSet);

		int k = 0;

		// vid in pattern
		int patternVid = 0;

		for (HyperEdge he : hyperEdgeSet){
			int vertex = he.getHyperEdge().get(0);
			k = he.getHyperEdgeSize();
			occurVid0.add(vertex);
		}
		patternVMapOccurV.put(patternVid,occurVid0);

		for (int i = 1; i < k; i++){
			List<Integer> occurVid = new ArrayList<>();
			for (HyperEdge he : hyperEdgeSet) {
				int vertex = he.getHyperEdge().get(i);
				occurVid.add(vertex);
			}
			patternVMapOccurV.put(i,occurVid);
		}

		// for i-th vertex in the pattern
		for (int i = 0; i < k; i++){
			// Ensure i is within bounds of labels
			if (i >= labels.size()) {
				break;  // or continue; depending on your logic
			}

			// Find j-th vertex has the same label with i-th vertex in the pattern
			for (int j = i+1; j < k; j++) {
				// Ensure j is within bounds of labels
				if (j >= labels.size()) {
					break;  // or continue; depending on your logic
				}

				Set<List<Integer>> vidSet = new HashSet<>();
				if (graphDB.getVLabel(patternVMapOccurV.get(i).get(0)) == graphDB.getVLabel(patternVMapOccurV.get(j).get(0))) {
					for (int l = 0; l < hyperEdgeSet.size(); l++) {
						List<Integer> vids = new ArrayList<>();
						int v1 = patternVMapOccurV.get(i).get(l);
						int v2 = patternVMapOccurV.get(j).get(l);
						vids.add(v1);
						vids.add(v2);
						vidSet.add(vids);

						// Check if labelMapVidSet already contains the key
						// Ensure labels.get(i) is safe to access
						if (i < labels.size() && !labelMapVidSet.containsKey(labels.get(i))) {
							labelMapVidSet.put(labels.get(i), new HashSet<>());
						}

						if (i < labels.size()) {
							labelMapVidSet.get(labels.get(i)).addAll(vidSet);
						}
					}
				}
			}
		}


		if (labelMapVidSet == null) {
			return MI;
		}

		int minimum = 9999999;
		for (Entry<Integer,Set<List<Integer>>> entry: labelMapVidSet.entrySet()){
			int temp = entry.getValue().size();
			if (temp < minimum){
				minimum = temp;
			}
		}

		if (minimum < MI) {
			MI = minimum;
			return MI;
		} else {
			return MI;
		}
	}



//	public int MI(Set<HyperEdge> hyperEdgeSet, DFSCode c, Graph graphDB) {
//		// Step 1: Retrieve all vertex labels from the DFSCode.
//		List<Integer> labels = c.getAllVLabels();
//
//		// Step 2: Create a map to group sets of vertices by their labels and positions.
//		Map<Integer, Set<Set<Integer>>> labelToVerticesMap = new HashMap<>();
//
//		for (HyperEdge he : hyperEdgeSet) {
//			List<Integer> vertices = he.getHyperEdge();
//			int size = Math.min(vertices.size(), labels.size());
//
//			for (int i = 0; i < size; i++) {
//				int label = labels.get(i);
//
//				// Create the set of vertex positions for this label if it doesn't exist
//				labelToVerticesMap.putIfAbsent(label, new HashSet<>());
//
//				// Create a set of vertex positions for this label at the i-th position
//				Set<Integer> vertexPositions = new HashSet<>();
//				for (int j = 0; j < size; j++) {
//					if (labels.get(j) == label) {
//						vertexPositions.add(vertices.get(j));
//					}
//				}
//
//				// Add the vertex positions to the set for this label
//				labelToVerticesMap.get(label).add(vertexPositions);
//			}
//		}
//
//		// Step 3: Calculate the support number for each label.
//		List<Integer> supportNumbers = new ArrayList<>();
//		for (Entry<Integer, Set<Set<Integer>>> entry : labelToVerticesMap.entrySet()) {
//			int supportNumber = entry.getValue().size();
//			supportNumbers.add(supportNumber);
//		}
//
//		// Step 4: Determine the minimum support number.
//		return Collections.min(supportNumbers);
//	}


//	public int MVC(Set<HyperEdge> hyperEdgeSet) {
//		// Find all vertices
//		Set<Integer> vertices = new HashSet<>();
//		for (HyperEdge hyperEdge : hyperEdgeSet) {
//			vertices.addAll(hyperEdge.getHyperEdge());
//		}
//
//		int n = vertices.size();
//		List<Integer> vertexList = new ArrayList<>(vertices);
//
//		// Check all subsets of vertices
//		int minCoverSize = n;
//		for (int i = 0; i < (1 << n); i++) {
//			Set<Integer> cover = new HashSet<>();
//			for (int j = 0; j < n; j++) {
//				if ((i & (1 << j)) != 0) {
//					cover.add(vertexList.get(j));
//				}
//			}
//
//			if (isVertexCover(cover, hyperEdgeSet)) {
//				minCoverSize = Math.min(minCoverSize, cover.size());
//			}
//		}
//
//		return minCoverSize;
//	}
//
//	private static boolean isVertexCover(Set<Integer> cover, Set<HyperEdge> hyperEdgeSet) {
//		for (HyperEdge hyperEdge : hyperEdgeSet) {
//			boolean covered = false;
//			for (Integer vertex : hyperEdge.getHyperEdge()) {
//				if (cover.contains(vertex)) {
//					covered = true;
//					break;
//				}
//			}
//			if (!covered) {
//				return false;
//			}
//		}
//		return true;
//	}

	// approximate MVC algotithm
	public static int MVC(Set<HyperEdge> hyperEdgeSet) {
		if (hyperEdgeSet == null || hyperEdgeSet.isEmpty()) {
			return 0;
		}

		// Map to keep track of the frequency of each vertex
		Map<Integer, Integer> vertexFrequency = new HashMap<>();
		for (HyperEdge edge : hyperEdgeSet) {
			for (Integer vertex : edge.getHyperEdge()) {
				vertexFrequency.put(vertex, vertexFrequency.getOrDefault(vertex, 0) + 1);
			}
		}

		Set<Integer> coverSet = new HashSet<>();
		Set<HyperEdge> uncoveredEdges = new HashSet<>(hyperEdgeSet);

		// Greedily add vertices to the cover set based on frequency
		while (!uncoveredEdges.isEmpty()) {
			// Find the vertex that covers the most remaining hyperedges
			int maxCover = -1;
			int vertexToAdd = -1;
			for (Map.Entry<Integer, Integer> entry : vertexFrequency.entrySet()) {
				int vertex = entry.getKey();
				if (!coverSet.contains(vertex)) {
					int coverCount = 0;
					for (HyperEdge edge : uncoveredEdges) {
						if (edge.getHyperEdge().contains(vertex)) {
							coverCount++;
						}
					}
					if (coverCount > maxCover) {
						maxCover = coverCount;
						vertexToAdd = vertex;
					}
				}
			}

			// Add the selected vertex to the cover set
			coverSet.add(vertexToAdd);

			// Remove the hyperedges covered by this vertex using an iterator
			Iterator<HyperEdge> iterator = uncoveredEdges.iterator();
			while (iterator.hasNext()) {
				HyperEdge edge = iterator.next();
				if (edge.getHyperEdge().contains(vertexToAdd)) {
					iterator.remove();
				}
			}
		}

		return coverSet.size();
	}



	// approximate MIS algorithm
	public static int MIS(Set<HyperEdge> hyperEdgeSet) {
		if (hyperEdgeSet == null || hyperEdgeSet.isEmpty()) {
			return 0;
		}

		Set<HyperEdge> independentSet = new HashSet<>();
		Set<Integer> usedVertices = new HashSet<>();

		for (HyperEdge edge : hyperEdgeSet) {
			boolean independent = true;
			for (Integer vertex : edge.getHyperEdge()) {
				if (usedVertices.contains(vertex)) {
					independent = false;
					break;
				}
			}
			if (independent) {
				independentSet.add(edge);
				usedVertices.addAll(edge.getHyperEdge());
			}
		}

		return independentSet.size();
	}

//	public static int MIS(Set<HyperEdge> hyperEdgeSet) {
//	if (hyperEdgeSet == null || hyperEdgeSet.isEmpty()) {
//		return 0;
//	}
//
//	List<HyperEdge> edgeList = new ArrayList<>(hyperEdgeSet);
//	int n = edgeList.size();
//
//	// Variable to store the size of the maximum independent edge set
//	int maxIndepSetSize = 0;
//
//	// Generate all subsets of edges
//	for (int i = 0; i < (1 << n); i++) {
//		Set<HyperEdge> candidateSet = new HashSet<>();
//		for (int j = 0; j < n; j++) {
//			if ((i & (1 << j)) != 0) {
//				candidateSet.add(edgeList.get(j));
//			}
//		}
//
//		// Check if the candidate set is an independent edge set
//		if (isIndependentSet(candidateSet)) {
//			maxIndepSetSize = Math.max(maxIndepSetSize, candidateSet.size());
//		}
//	}
//
//	return maxIndepSetSize;
//	}
//
//	// This method checks if a set of edges forms an independent edge set
//	private static boolean isIndependentSet(Set<HyperEdge> edgeSet) {
//		Set<Integer> usedVertices = new HashSet<>();
//		for (HyperEdge edge : edgeSet) {
//			for (Integer vertex : edge.getHyperEdge()) {
//				if (usedVertices.contains(vertex)) {
//					return false; // Shared vertex, not independent
//				}
//				usedVertices.add(vertex);
//			}
//		}
//		return true;
//	}



	/**
	 * This method finds all frequent vertex labels from a graph database.
	 * 
	 * @param graphDB                a graph database
	 * @param outputFrequentVertices if true, the frequent vertices will be output
	 */
	private void findAllOnlyOneVertex(Graph graphDB, boolean outputFrequentVertices) {

		frequentVertexLabels = new ArrayList<Integer>();

		// Create a map (key = vertex label, value = vertex ids)
		// to count the support of each vertex
		Map<Integer, Set<Integer>> labelM = new HashMap<>();

		// For each vertex
		for (Vertex v : graphDB.getNonPrecalculatedAllVertices()) {

			// if it has some edges
			if (!v.getEdgeList().isEmpty()) {

				// Get the vertex label
				Integer vLabel = v.getLabel();

				// Store the vertex id in the map entry for this label
				// if it is not there already
				Set<Integer> set = labelM.get(vLabel);
				if (set == null) {
					set = new HashSet<>();
					labelM.put(vLabel, set);
				}
				set.add(v.getId());
			}
		}

		// For each vertex label
		for (Entry<Integer, Set<Integer>> entry : labelM.entrySet()) {
			int label = entry.getKey();

			// if it is a frequent vertex, then record that as a frequent subgraph
			Set<Integer> tempSupG = entry.getValue();
			Set<HyperEdge> tempHyper = new HashSet<>();
			for (Integer i : tempSupG){
				HyperEdge he = new HyperEdge(i);
				tempHyper.add(he);
			}
			int sup = tempSupG.size();
			if (sup >= minSup) {
				frequentVertexLabels.add(label);

				// if the user wants to output one vertex frequent subgraph
				if (outputFrequentVertices) {
					DFSCode tempD = new DFSCode();
					tempD.add(new ExtendedEdge(0, 0, label, label, -1));

					frequentSubgraphs.add(new FrequentSubgraph(tempD, tempHyper, sup));
				}
			} else if (ELIMINATE_INFREQUENT_VERTICES) {
				// for each graph
				for (Integer graphid : tempSupG) {

					graphDB.removeInfrequentLabel(label);
					infrequentVerticesRemovedCount++;
				}
			}
		}

		if (DEBUG_MODE) {
			for (Integer i : frequentVertexLabels) {
				System.out.println("Frequent vetex:" + i);
			}
		}
	}

	/**
	 * Print statistics about the algorithm execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  GraphMining v2.40 - STATS =============");
		System.out.println(" Number of graph in the input database: " + graphCount);
		System.out.println(" Frequent subgraph count : " + patternCount);
        System.out.println(" Maximum size (edge number) of frequent subagraphs: " + patternMaxSize);
		System.out.println(" Total time ~ " + runtime + " s");
		System.out.println(" Minsup : " + minSup + " graphs");
		System.out.println(" Memory usage : " + maxmemory + " mb");
        System.out.println(" Number of candidates : " + candidateNum);

		if(DEBUG_MODE) {
			if (ELIMINATE_INFREQUENT_VERTEX_PAIRS || ELIMINATE_INFREQUENT_VERTICES) {
				System.out.println("  -------------------");
			}
			if (ELIMINATE_INFREQUENT_VERTICES) {
				System.out.println("  Number of infrequent vertices pruned : " + infrequentVerticesRemovedCount);
			}
			if (ELIMINATE_INFREQUENT_VERTEX_PAIRS) {
				System.out.println("  Number of infrequent vertex pairs pruned : " + infrequentVertexPairsRemoved);
			}
			if (ELIMINATE_INFREQUENT_EDGE_LABELS) {
				System.out.println("  Number of infrequent edge labels pruned : " + edgeRemovedByLabel);
			}
			if (EDGE_COUNT_PRUNING) {
				System.out.println("  Extensions skipped (edge count pruning) : " + pruneByEdgeCountCount);
			}
			if (SKIP_STRATEGY) {
				System.out.println("  Skip strategy count : " + skipStrategyCount);
			}
		}
		System.out.println("===================================================");
	}
}
