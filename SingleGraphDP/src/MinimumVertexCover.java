import java.util.*;

public class MinimumVertexCover {

    public static int MVC(Set<HyperEdge> hyperEdgeSet) {
        // Find all vertices
        Set<Integer> vertices = new HashSet<>();
        for (HyperEdge hyperEdge : hyperEdgeSet) {
            vertices.addAll(hyperEdge.getHyperEdge());
        }

        int n = vertices.size();
        List<Integer> vertexList = new ArrayList<>(vertices);

        // Check all subsets of vertices
        int minCoverSize = n;
        for (int i = 0; i < (1 << n); i++) {
            Set<Integer> cover = new HashSet<>();
            for (int j = 0; j < n; j++) {
                if ((i & (1 << j)) != 0) {
                    cover.add(vertexList.get(j));
                }
            }

            if (isVertexCover(cover, hyperEdgeSet)) {
                minCoverSize = Math.min(minCoverSize, cover.size());
            }
        }

        return minCoverSize;
    }

    private static boolean isVertexCover(Set<Integer> cover, Set<HyperEdge> hyperEdgeSet) {
        for (HyperEdge hyperEdge : hyperEdgeSet) {
            boolean covered = false;
            for (Integer vertex : hyperEdge.getHyperEdge()) {
                if (cover.contains(vertex)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Set<HyperEdge> hyperEdgeSet = new HashSet<>();
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(0, 1, 2)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(3, 4, 5)));
        hyperEdgeSet.add(new HyperEdge(Arrays.asList(6, 7, 8)));

        int minCover = MVC(hyperEdgeSet);
        System.out.println("Minimum Vertex Cover size: " + minCover);
    }
}

