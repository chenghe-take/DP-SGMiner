import java.io.*;
import java.util.*;

/**
 * Performance evaluation for frequent subgraph mining with differential privacy.
 * Compares true frequent subgraphs (output.txt) with DP-protected subgraphs (outputDP.txt).
 * Computes F-score and Relative Error.
 */
public class Performance {

    private static boolean isMatchSubgraph = false;

    private static int minSupport = 5;

    public static void main(String[] args) {
        String trueFile = "output.txt";
        String dpFile = "outputDP.txt";

        try {
            evaluate(trueFile, dpFile);
        } catch (IOException e) {
            System.err.println("Error reading files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void evaluate(String trueFilePath, String dpFilePath) throws IOException {
        // Parse both files
        List<Subgraph> trueGraphs = parseFile(trueFilePath);
        List<Subgraph> dpGraphs = parseFile(dpFilePath);

        System.out.println("=== Performance Evaluation ===");
        System.out.println("True frequent subgraphs: " + trueGraphs.size());
        System.out.println("DP frequent subgraphs: " + dpGraphs.size());
        System.out.println();

        // Build maps from canonical string to Subgraph for matching
        Map<String, Subgraph> trueMap = new HashMap<>();
        for (Subgraph sg : trueGraphs) {
            trueMap.put(sg.canonical, sg);
        }

        Map<String, Subgraph> dpMap = new HashMap<>();
        for (Subgraph sg : dpGraphs) {
            dpMap.put(sg.canonical, sg);
        }

        // Count matches
        int truePositive = 0;
        List<Double> relativeErrors = new ArrayList<>();
        List<Double> absoluteErrors = new ArrayList<>();

        for (Subgraph dpSg : dpGraphs) {
            Subgraph trueSg = trueMap.get(dpSg.canonical);
            if (trueSg != null) {
                truePositive++;

                // Relative Error: |true_support - dp_support| / true_support
                // Bounded Relative Error: |true_support - dp_support| / max{true_support,minSup}
                double denom = Math.max(trueSg.support, minSupport);
                double relError = Math.abs(trueSg.support - dpSg.support) / (double) trueSg.support;
                relativeErrors.add(relError);

                double absError = Math.abs(trueSg.support - dpSg.support);
                absoluteErrors.add(absError);
            }
        }

        // Calculate metrics
        int falsePositive = dpGraphs.size() - truePositive;
        int falseNegative = trueGraphs.size() - truePositive;

        double precision = dpGraphs.size() > 0 ? (double) truePositive / dpGraphs.size() : 0.0;
        double recall = trueGraphs.size() > 0 ? (double) truePositive / trueGraphs.size() : 0.0;
        double fScore = (precision + recall) > 0 ? 2 * precision * recall / (precision + recall) : 0.0;

        double avgRelativeError = 0.0;
        if (!relativeErrors.isEmpty()) {
            double sum = 0.0;
            for (double re : relativeErrors) {
                sum += re;
            }
            avgRelativeError = sum / relativeErrors.size();
        }

        double avgAbsoluteError = 0.0;
        if (!absoluteErrors.isEmpty()) {
            double sum = 0.0;
            for (double ae : absoluteErrors) {
                sum += ae;
            }
            avgAbsoluteError = sum / absoluteErrors.size();
        }

        // Print results
        System.out.println("=== Confusion Matrix ===");
        System.out.println("True Positives (matched): " + truePositive);
        System.out.println("False Positives (DP-only): " + falsePositive);
        System.out.println("False Negatives (missed):  " + falseNegative);
        System.out.println();

        System.out.println("=== Metrics ===");
        System.out.printf("Precision:         %.6f%n", precision);
        System.out.printf("Recall:            %.6f%n", recall);
        System.out.printf("F-score:           %.6f%n", fScore);
        System.out.printf("Avg Relative Error: %.6f%n", avgRelativeError);
        System.out.printf("Avg Absolute Error: %.6f%n", avgAbsoluteError);
        System.out.println();

        // Print matched subgraphs with their support values
        if (isMatchSubgraph){
            System.out.println("=== Matched Subgraphs (canonical form) ===");
            for (Subgraph dpSg : dpGraphs) {
                Subgraph trueSg = trueMap.get(dpSg.canonical);
                if (trueSg != null) {
                    double relError = Math.abs(trueSg.support - dpSg.support) / (double) trueSg.support;
                    System.out.printf("  True support: %5d | DP support: %5d | RelError: %.4f | %s%n",
                            trueSg.support, dpSg.support, relError, dpSg.canonical);
                }
            }
        }

    }

    /**
     * Parse a .txt file containing frequent subgraphs in the format:
     * g # <id> * <support>
     * v <index> <label>
     * e <from> <to> <label>
     */
    private static List<Subgraph> parseFile(String filePath) throws IOException {
        List<Subgraph> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        int currentSupport = 0;
        List<String> vertices = new ArrayList<>();
        List<String> edges = new ArrayList<>();
        boolean inGraph = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith("g #")) {
                // Save previous graph if exists
                if (inGraph) {
                    result.add(buildSubgraph(currentSupport, vertices, edges));
                }

                // Parse new graph header: g # <id> * <support>
                String[] parts = line.split("\\s+");
                currentSupport = Integer.parseInt(parts[4]);
                vertices = new ArrayList<>();
                edges = new ArrayList<>();
                inGraph = true;
            } else if (line.startsWith("v ")) {
                // v <index> <label>
                vertices.add(line);
            } else if (line.startsWith("e ")) {
                // e <from> <to> <label>
                edges.add(line);
            }
        }

        // Save last graph
        if (inGraph) {
            result.add(buildSubgraph(currentSupport, vertices, edges));
        }

        reader.close();
        return result;
    }

    /**
     * Build a Subgraph object with a canonical string representation.
     * The canonical form sorts vertices by label and edges lexicographically,
     * making it possible to match subgraphs across different index assignments.
     */
    private static Subgraph buildSubgraph(int support, List<String> vertices, List<String> edges) {
        // Build canonical form: sorted vertices + sorted edges
        // Vertex format: "v(label)" sorted by label
        // Edge format: "e(label1,label2,edgeLabel)" sorted lexicographically

        // First, extract vertex labels with their indices
        // v <index> <label>
        Map<Integer, Integer> indexToLabel = new TreeMap<>();
        for (String v : vertices) {
            String[] parts = v.split("\\s+");
            int idx = Integer.parseInt(parts[1]);
            int label = Integer.parseInt(parts[2]);
            indexToLabel.put(idx, label);
        }

        // Build canonical vertex string: sorted by label
        List<Integer> sortedLabels = new ArrayList<>(indexToLabel.values());
        Collections.sort(sortedLabels);
        StringBuilder sb = new StringBuilder();
        for (int label : sortedLabels) {
            sb.append("v").append(label);
        }

        // Build canonical edge strings using labels instead of indices
        List<String> canonicalEdges = new ArrayList<>();
        for (String e : edges) {
            String[] parts = e.split("\\s+");
            int fromIdx = Integer.parseInt(parts[1]);
            int toIdx = Integer.parseInt(parts[2]);
            int edgeLabel = Integer.parseInt(parts[3]);
            int fromLabel = indexToLabel.get(fromIdx);
            int toLabel = indexToLabel.get(toIdx);
            // Normalize: smaller label first
            int first = Math.min(fromLabel, toLabel);
            int second = Math.max(fromLabel, toLabel);
            canonicalEdges.add("e" + first + "," + second + "," + edgeLabel);
        }
        Collections.sort(canonicalEdges);

        for (String ce : canonicalEdges) {
            sb.append(ce);
        }

        return new Subgraph(support, sb.toString());
    }

    private static class Subgraph {
        int support;
        String canonical;

        Subgraph(int support, String canonical) {
            this.support = support;
            this.canonical = canonical;
        }
    }
}
