import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FrequentSubgraphStatistics {

    public static void main(String[] args) {
        String filePath = "output.txt"; // 确保路径正确

        int graphCount = 0;
        long totalSupport = 0;
        long totalVertices = 0;
        long totalEdges = 0;

        int currentVertices = 0;
        int currentEdges = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                // 新的频繁子图开始
                if (line.startsWith("g #")) {
                    // 若不是第一个图，先结算上一个图
                    if (graphCount > 0) {
                        totalVertices += currentVertices;
                        totalEdges += currentEdges;
                    }

                    graphCount++;
                    currentVertices = 0;
                    currentEdges = 0;

                    // 解析支持度
                    // 格式：g # a * b
                    String[] parts = line.split("\\*");
                    int support = Integer.parseInt(parts[1].trim());
                    totalSupport += support;
                }
                // 顶点行
                else if (line.startsWith("v ")) {
                    currentVertices++;
                }
                // 边行
                else if (line.startsWith("e ")) {
                    currentEdges++;
                }
            }

            // 结算最后一个图
            if (graphCount > 0) {
                totalVertices += currentVertices;
                totalEdges += currentEdges;
            }

            // 计算平均值
            double avgSupport = (double) totalSupport / graphCount;
            double avgVertices = (double) totalVertices / graphCount;
            double avgEdges = (double) totalEdges / graphCount;

            // 输出结果
            System.out.println("Number of frequent subgraphs: " + graphCount);
            System.out.println("Average support: " + avgSupport);
            System.out.println("Average number of vertices: " + avgVertices);
            System.out.println("Average number of edges: " + avgEdges);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
