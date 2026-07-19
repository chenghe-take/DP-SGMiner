import java.util.*;

public class MaximumIndependentEdgeSet {

    public static int MIS(Set<HyperEdge> hyperEdgeSet) {
        List<HyperEdge> edgeList = new ArrayList<>(hyperEdgeSet);
        int n = edgeList.size();

        // Variable to store the size of the maximum independent edge set
        int maxIndepSetSize = 0;

        // Generate all subsets of edges
        for (int i = 0; i < (1 << n); i++) {
            Set<HyperEdge> candidateSet = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    candidateSet.add(edgeList.get(j));
                }
            }

            // Check if the candidate set is an independent edge set
            if (isIndependentSet(candidateSet)) {
                maxIndepSetSize = Math.max(maxIndepSetSize, candidateSet.size());
            }
        }

        return maxIndepSetSize;
    }

    private static boolean isIndependentSet(Set<HyperEdge> edgeSet) {
        Set<Integer> usedVertices = new HashSet<>();
        for (HyperEdge edge : edgeSet) {
            for (Integer vertex : edge.getHyperEdge()) {
                if (usedVertices.contains(vertex)) {
                    return false;
                }
                usedVertices.add(vertex);
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Set<HyperEdge> hyperEdgeSet = new HashSet<>();
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 1, 2)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 3, 4)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(4, 5, 6)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(2, 7, 9)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(10, 11, 12)));

        int maxIndepSetSize = MIS(hyperEdgeSet);
        System.out.println("Maximum Independent Edge Set size: " + maxIndepSetSize);
    }
}