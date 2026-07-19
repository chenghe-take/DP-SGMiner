import java.util.*;

public class HypergraphPrim {
    public static Set<HyperEdge> Prim(Set<HyperEdge> hyperEdgeSet) {
        Set<HyperEdge> hypertree = new HashSet<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, List<HyperEdge>> graph = new HashMap<>();
        List<HyperEdge> edgeList = new ArrayList<>();
        Random random = new Random();

        // Construct the graph
        for (HyperEdge edge : hyperEdgeSet) {
            for (Integer vertex : edge.getHyperEdge()) {
                graph.putIfAbsent(vertex, new ArrayList<>());
                graph.get(vertex).add(edge);
            }
        }

        // Get a random start node
        List<Integer> vertices = new ArrayList<>(graph.keySet());
        int startNode = vertices.get(random.nextInt(vertices.size()));

        visited.add(startNode);
        edgeList.addAll(graph.get(startNode));
        Collections.shuffle(edgeList, random);

        while (!edgeList.isEmpty()) {
            HyperEdge edge = edgeList.remove(random.nextInt(edgeList.size()));
            boolean hasUnvisited = false;
            for (Integer vertex : edge.getHyperEdge()) {
                if (!visited.contains(vertex)) {
                    hasUnvisited = true;
                    break;
                }
            }

            if (!hasUnvisited) continue;

            hypertree.add(edge);
            for (Integer vertex : edge.getHyperEdge()) {
                if (!visited.contains(vertex)) {
                    visited.add(vertex);
                    for (HyperEdge adjacentEdge : graph.get(vertex)) {
                        if (!hypertree.contains(adjacentEdge) && !edgeList.contains(adjacentEdge)) {
                            edgeList.add(adjacentEdge);
                        }
                    }
                }
            }
            Collections.shuffle(edgeList, random);
        }

        return hypertree;
    }

//    public static void main(String[] args) {
//        Set<HyperEdge> hyperEdgeSet = new HashSet<>();
//
//        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 1)));
//        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 2)));
//        hyperEdgeSet.add(new HyperEdge(Arrays.asList(1, 2)));
//        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 1)));
//
//        Set<HyperEdge> hypertree = Prim(hyperEdgeSet);
//        for (HyperEdge edge : hypertree) {
//            System.out.println(edge);
//        }
//    }
}