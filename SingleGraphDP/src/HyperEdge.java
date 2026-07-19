import java.util.*;

/*
对应每一个DFScode的超边
 */
public class HyperEdge {
    //contain vertex id
    private List<Integer> hyperEdge = new ArrayList<>();

    private int hashcode;

    private int hyperEdgeSize;

    public HyperEdge(int v1, int v2) {
        this.hyperEdge.add(v1);
        this.hyperEdge.add(v2);
        this.hashcode = (v1+1)*1+(v2+1)*10;
    }

    public HyperEdge(int singleVertex) {
        this.hyperEdge.add(singleVertex);
        this.hashcode = (singleVertex+1);
    }

    public HyperEdge() {
    }

    public HyperEdge(List<Integer> hyperEdge) {
        this.hyperEdge = hyperEdge;
        this.hashcode = 0;
        if (hyperEdge.size() !=0) {
            for (int i = 0; i < hyperEdge.size(); i++) {
                this.hashcode = (int) (hashcode + (hyperEdge.get(i)+1) * Math.pow(10,i));
            }
        }
    }

    public List<Integer> getHyperEdge() {
        return hyperEdge;
    }

    public List<Integer> addHyperEdge(int v1, int v2){
        List<Integer> copy = new ArrayList<>();
        for (Integer i : hyperEdge){
            copy.add(i);
        }
        copy.add(v2);
        for (int i = 0; i < hyperEdge.size(); i++) {
            hashcode = (int) (hashcode + (hyperEdge.get(i)+1) * Math.pow(10,i));
        }
        return copy;
    }

    public int getHyperEdgeSize() {
        hyperEdgeSize = hyperEdge.size();
        return hyperEdgeSize;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof HyperEdge)) return false;
        HyperEdge that = (HyperEdge) (obj);
        return this.hashcode == that.hashcode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : hyperEdge){
            sb.append(i);
            sb.append(" ");
        }
        sb.append("&");
        return sb.toString();
    }
}
