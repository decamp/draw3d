//package bits.draw3d.model;
//
//import bits.draw3d.pick.DimComparator;
//
//import java.util.Comparator;
//
//
///**
// * @author decamp
// */
//public class LabelVert implements Comparable<LabelVert> {
//
//    public static final Comparator<LabelVert> ORDER = new Comparator<LabelVert>() {
//        public int compare( LabelVert a, LabelVert b ) {
//            return a.mLabel - b.mLabel;
//        }
//    };
//
//    public static DimComparator<LabelVert> ACCESSOR = new DimComparator<LabelVert>() {
//
//        public int dimCount( LabelVert v ) {
//            return 3;
//        }
//
//        public double value( LabelVert v, int idx ) {
//            return v.mVert[idx];
//        }
//
//    };
//
//
//    public int      mLabel;
//    public double[] mVert;
//
//
//    public LabelVert( int label, double[] vert ) {
//        mLabel = label;
//        mVert = vert;
//    }
//
//
//    public int compareTo( LabelVert v ) {
//        return mLabel - v.mLabel;
//    }
//
//}
