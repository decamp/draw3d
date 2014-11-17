//package bits.draw3d.model;
//
//import java.util.*;
//
//import bits.draw3d.pick.*;
//
//
///**
// * @author decamp
// */
//@Deprecated public class IndexedVertex implements Comparable<IndexedVertex> {
//
//    public static final Comparator<IndexedVertex> ORDER = new Comparator<IndexedVertex>() {
//        public int compare( IndexedVertex a, IndexedVertex b ) {
//            return a.mIndex - b.mIndex;
//        }
//    };
//
//    public static DimComparator<IndexedVertex> ACCESSOR = new DimComparator<IndexedVertex>() {
//
//        public int dimCount( IndexedVertex v ) {
//            return 3;
//        }
//
//        public double value( IndexedVertex v, int idx ) {
//            return v.mVert[idx];
//        }
//
//    };
//
//
//    private final int      mIndex;
//    private final double[] mVert;
//
//
//    public IndexedVertex( int index, double[] vertex ) {
//        mIndex = index;
//        mVert = vertex;
//    }
//
//
//    public int index() {
//        return mIndex;
//    }
//
//
//    public double[] vertex() {
//        return mVert;
//    }
//
//
//    public double[] vectorRef() {
//        return mVert;
//    }
//
//
//    public int compareTo( IndexedVertex v ) {
//        return mIndex - v.mIndex;
//    }
//
//
//}
