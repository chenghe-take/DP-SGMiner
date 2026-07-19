import java.io.*;

public class VizGraph {

	/**
	 * Convert a result file of gSpan to a DOT file for GraphViz visualization
	 * @param gPath the path to a result file
	 * @param outDir the resulting DOT files
	 * @throws IOException exception if error while reading/writing to file
	 */
    public static void visulizeFromFile(String gPath, String outDir) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(gPath));
        String line = br.readLine();
        while (line != null) {
            if (line.startsWith("t")) {
                int gId = Integer.parseInt(line.split(" ")[2]);
                StringBuilder sb = new StringBuilder();
                sb.append("graph G {").append(System.lineSeparator());
                while ((line = br.readLine()) != null && !line.startsWith("t")) {
                    if (line.startsWith("v")) {
                        String[] items = line.split(" ");
                        int v = Integer.parseInt(items[1]);
                        int vLabel = Integer.parseInt(items[2]);
                        sb.append(v).append("[label=").append("\"").append(v).append(":").append(vLabel).append("\"]");
                        sb.append(System.lineSeparator());
                    }
                    else if (line.startsWith("e")) {
                        String[] items = line.split(" ");
                        int v1 = Integer.parseInt(items[1]);
                        int v2 = Integer.parseInt(items[2]);
                        int eLabel = Integer.parseInt(items[3]);
                        sb.append(v1).append("--").append(v2).append("[label=\"").append(eLabel).append("\"]");
                        sb.append(System.lineSeparator());
                    }
                }
                sb.append("}").append(System.lineSeparator());
                String outPath = outDir + "/g" + gId + ".dot";
                BufferedWriter bw = new BufferedWriter(new FileWriter(outPath));
                bw.write(sb.toString());
                bw.close();
            }
        }
        br.close();
    }
}
