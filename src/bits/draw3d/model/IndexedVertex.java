package bits.draw3d.model;

import java.util.*;

import bits.draw3d.pick.*;




/** 
 * @author decamp
 */
public class IndexedVertex implements Comparable<IndexedVertex> {

    public static final Comparator<IndexedVertex> ORDER = new Comparator<IndexedVertex>() {
        public int compare(IndexedVertex a, IndexedVertex b) {
            return a.mIndex - b.mIndex;
        }
    };
    
    public static DimComparator<IndexedVertex> ACCESSOR = new DimComparator<IndexedVertex>() {
        
        public int dimCount(IndexedVertex v) {
            return 3;
        }
        
        public double value(IndexedVertex v, int idx) {
            return v.mVertex[idx];
        }
        
    };
    
    
    
    private final int mIndex;
    private final double[] mVertex;
    
    
    public IndexedVertex(int index, double[] vertex) {
        mIndex = index;
        mVertex = vertex;
    }
    
    
    
    public int index() {
        return mIndex;
    }

    
    public double[] vertex() {
        return mVertex;
    }
    
    
    public double[] vectorRef() {
        return mVertex;
    }

    
    public int compareTo(IndexedVertex v) {
        return mIndex - v.mIndex;
    }
    
    
}
