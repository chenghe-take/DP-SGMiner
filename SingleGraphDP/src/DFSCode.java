import java.io.*;
import java.util.*;

public class DFSCode implements Serializable{
	
    /** Serial UID */
	private static final long serialVersionUID = -3332379071310578036L;
	
	/**    maintain rightmost child and current rightmost path */
    private int rightMost;
    
    private Stack<Integer> rightMostPath;
    
    private List<ExtendedEdge> eeL;
    
    private int size;

    /**
     * Constructor
     */
    public DFSCode() {
        rightMost = -1;
        size = 0;
        rightMostPath = new Stack<>();
        eeL = new LinkedList<>();
    }

    /**
     * Make a copy of a DFS code
     * @return a copy
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public DFSCode copy() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DFSCode clonedObj = (DFSCode) ois.readObject();
        ois.close();
        return clonedObj;
    }

    public boolean notPreOfRM(int v) {
        if(rightMostPath.size() <= 1) return true;
        return v != rightMostPath.elementAt(rightMostPath.size() - 2);
    }

    /**
     * Get all vertex labels
     * @return a list of vertex label
     */
    public List<Integer> getAllVLabels() {
        List<Integer> labels = new ArrayList<>();
        Map<Integer, Integer> map = new LinkedHashMap<>();
        for (ExtendedEdge ee : eeL) {
            int v1 = ee.getV1();
            int v1L = ee.getvLabel1();
            int v2 = ee.getV2();
            int v2L = ee.getvLabel2();
            map.put(v1, v1L);
            map.put(v2, v2L);
        }
        int count = 0;
        Set<Integer> vSet = map.keySet();
        while (vSet.contains(count)) {
            labels.add(map.get(count));
            count++;
        }
        return labels;
    }

    /**
     * Add an edge to the DFS code
     * @param ee the edge
     */
    public void add(ExtendedEdge ee) {
        if (size == 0) {
            rightMost = 1;
            rightMostPath.push(0);
            rightMostPath.push(1);
        }
        else {
            int v1 = ee.getV1();
            int v2 = ee.getV2();
            if (v1 < v2) {
                //if forward edge, need to modify right most vertex and right most path
                rightMost = v2;
                while (!rightMostPath.isEmpty() && rightMostPath.peek() > v1) {
                    rightMostPath.pop();
                }
                rightMostPath.push(v2);
            }
            else {
                //if backward edge, no change
            }
        }
        eeL.add(ee);
        this.size++;
    }


    public ExtendedEdge getAt(int i) {
        return eeL.get(i);
    }

    public boolean onRightMostPath(int v) {
        return rightMostPath.contains(v);
    }

    public boolean containEdge(int v1, int v2) {
        for (ExtendedEdge ee : eeL) {
            int eeV1 = ee.getV1();
            int eeV2 = ee.getV2();
            if ((eeV1 == v1 && eeV2 == v2) || (eeV1 == v2 && eeV2 == v1))
                return true;
        }
        return false;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return eeL.isEmpty();
    }

    public int getRightMost() {
        return rightMost;
    }

    public Iterable<Integer> getRightMostPath() {
        return rightMostPath;
    }

    public List<ExtendedEdge> getEeL() {
        return eeL;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFSCode: ");
        for (ExtendedEdge ee : eeL)
            sb.append(ee).append(" ");
        return sb.toString();
    }
}
