/**
 * MIT Media Lab
 * Cognitive Machines Group
 */

package bits.draw3d.pick;

import java.util.*;


/**
 * Picks N-dimensional points. Uses euclidean distance to determine nearest
 * neighbor.
 * 
 * @param <P>
 *            Point object type.
 * @author Philip DeCamp
 */
public class KdPointTree<P> implements Collection<P>, PointPicker<P> {


    private final DimComparator<? super P> mComp;

    private Node mRoot = null;
    private int mSize = 0;

    private int mDimCount = 0;
    private Bounds mBounds = null;
    private BinSum mMinDist = null;

    private int mModCount = 0;


    public KdPointTree( DimComparator<? super P> comp ) {
        mComp = comp;
    }


    @Override
    public boolean add( P point ) {
        mModCount++;
        mSize++;

        if( mRoot == null ) {
            addFirst( point );
            return true;
        }

        mBounds.clear();
        Node node = mRoot;

        do {
            int dim = node.mDim;
            double val1 = mComp.value( point, dim );
            double val2 = mComp.value( node.mFeature, dim );

            if( val1 < val2 ) {
                mBounds.mMax[dim] = val2;
                dim = (dim + 1) % mDimCount;

                if( node.mLess != null ) {
                    node = node.mLess;
                } else {
                    node.mLess = new Node( node, point, dim, mBounds.mMin[dim], mBounds.mMax[dim] );
                    return true;
                }

            } else {
                mBounds.mMin[dim] = val1;
                dim = (dim + 1) % mDimCount;

                if( node.mMore != null ) {
                    node = node.mMore;

                } else {
                    node.mMore = new Node( node, point, dim, mBounds.mMin[dim], mBounds.mMax[dim] );
                    return true;
                }
            }
        } while( true );
    }


    @Override
    public boolean addAll( Collection<? extends P> c ) {
        boolean ret = false;
        for( P t : c ) {
            ret |= add( t );
        }
        return ret;
    }


    @Override
    public void clear() {
        mModCount++;
        mRoot = null;
        mSize = 0;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public boolean contains( Object o ) {
        if( mRoot == null ) {
            return false;
        }

        Node node = mRoot;
        try {
            while( node != null ) {
                double val = mComp.value( (P)o, node.mDim );
                if( val < node.mVal ) {
                    node = node.mLess;

                } else if( val > node.mVal ) {
                    node = node.mMore;

                } else if( node.mFeature.equals( o ) ) {
                    return true;

                }
            }

        } catch( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }

        return false;
    }


    @Override
    public boolean containsAll( Collection<?> c ) {
        for( Object o : c ) {
            if( !contains( o ) ) {
                return false;
            }
        }
        return true;
    }


    @Override
    public boolean isEmpty() {
        return mSize == 0;
    }


    @Override
    public Iterator<P> iterator() {
        return new KDTreeIterator();
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public boolean remove( Object o ) {
        Node node = mRoot;

        try {
            while( node != null ) {
                double val = mComp.value( (P)o, node.mDim );

                if( val < node.mVal ) {
                    node = node.mLess;

                } else if( val > node.mVal ) {
                    node = node.mMore;

                } else if( o.equals( node.mFeature ) ) {
                    mModCount++;

                    if( node.mParent == null ) {
                        mRoot = null;

                    } else if( node.mParent.mLess == node ) {
                        node.mParent.mLess = null;

                    } else {
                        node.mParent.mMore = null;
                    }

                    List<P> featureList = new ArrayList<P>();

                    if( node.mLess != null ) {
                        node.mLess.listFeatures( featureList );
                    }

                    if( node.mMore != null ) {
                        node.mMore.listFeatures( featureList );
                    }

                    if( featureList.size() > 0 ) {
                        mSize -= (1 + featureList.size());
                        addAll( featureList );
                    } else {
                        mSize--;
                    }

                    return true;

                } else {
                    node = node.mMore;
                }
            }
        } catch( ArrayIndexOutOfBoundsException ex ) {}

        return false;
    }


    @Override
    public boolean removeAll( Collection<?> c ) {
        boolean ret = false;

        for( Object o : c ) {
            ret |= remove( o );
        }

        return ret;
    }


    @Override
    public boolean retainAll( Collection<?> c ) {
        if( mRoot == null ) {
            return false;
        }

        int size = mSize;
        List<P> list = new ArrayList<P>();

        for( P t : this ) {
            if( c.contains( t ) ) {
                list.add( t );
            }
        }

        clear();
        addAll( list );

        return mSize != size;
    }


    @Override
    public int size() {
        return mSize;
    }


    @Override
    public Object[] toArray() {
        Object[] array = new Object[mSize];
        if( mRoot != null ) {
            mRoot.toArray( array, 0 );
        }
        return array;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public <S> S[] toArray( S[] arr ) {
        if( arr.length < mSize ) {
            arr = (S[])java.lang.reflect.Array.newInstance( arr.getClass(), mSize );
        }

        if( mRoot != null ) {
            mRoot.toArray( arr, 0 );
        }

        if( arr.length > mSize ) {
            arr[mSize] = null;
        }

        return arr;
    }



    @Override
    public PointPickResult<P> newPointPickResult() {
        return new Result<P>();
    }


    @Override
    public boolean pick( P point, PointPickResult<P> out ) {
        if( !(out instanceof Result) ) {
            throw new IllegalArgumentException( "Improperly allocated PointPickResult." );
        }

        Result<P> result = (Result<P>)out;
        result.mHasPick = false;
        result.mPickDist = Double.POSITIVE_INFINITY;

        if( mRoot == null ) {
            return false;
        }

        mMinDist.clear();

        if( !mRoot.findNearest( point, mMinDist, result ) ) {
            return false;
        }

        result.mHasPick = true;
        result.mPickDist = Math.sqrt( result.mPickDist );
        return true;
    }


    public boolean approximatePick( P point, PointPickResult<P> out ) {
        if( !(out instanceof Result) ) {
            throw new IllegalArgumentException( "Improperly allocated PointPickResult." );
        }

        Result<P> result = (Result<P>)out;
        result.mHasPick = false;
        result.mPickDist = Double.POSITIVE_INFINITY;

        if( mRoot == null ) {
            return false;
        }

        PriorityQueue<Choice<Node>> queue = new PriorityQueue<Choice<Node>>( 1000 );
        mMinDist.clear();

        mRoot.findApproximateNearest( point, mMinDist, result, queue );

        for( int i = 0; i < 1000 && !queue.isEmpty(); i++ ) {
            Choice<Node> choice = queue.remove();
            if( choice.mDistance > result.mPickDist ) {
                break;
            }
            mRoot.findApproximateNearest( choice.mNode.mFeature, point, mMinDist, result, queue );
        }

        result.mHasPick = true;
        result.mPickDist = Math.sqrt( result.mPickDist );
        return true;
    }


    public void optimize() {
        if( mRoot == null ) {
            return;
        }

        mModCount++;
        List<P> list = new ArrayList<P>( mSize );
        Bounds range = new Bounds( mDimCount );
        mRoot.listFeatures( list );
        mBounds.clear();

        mRoot = balance( null, mBounds, list, range );
    }



    private void addFirst( P feature ) {
        mRoot = new Node( null, feature, 0, Float.MIN_VALUE, Float.MAX_VALUE );
        mDimCount = mComp.dimCount( feature );
        mBounds = new Bounds( mDimCount );
        mMinDist = new BinSum( mDimCount );
    }


    private Node balance( Node parent, Bounds bounds, List<P> list, Bounds range ) {
        if( list.size() == 0 ) {
            return null;
        }

        // Determine variances.
        Arrays.fill( range.mMin, 0f );
        Arrays.fill( range.mMax, 0f );
        double scale = 1f / list.size();

        for( P f : list ) {
            for( int i = 0; i < mDimCount; i++ ) {
                range.mMin[i] += mComp.value( f, i );
            }
        }

        for( int i = 0; i < mDimCount; i++ ) {
            range.mMin[i] *= scale;
        }

        for( P f : list ) {
            for( int i = 0; i < mDimCount; i++ ) {
                double d = mComp.value( f, i ) - range.mMin[i];
                range.mMax[i] += d * d;
            }
        }

        for( int i = 0; i < mDimCount; i++ ) {
            range.mMax[i] *= scale;
        }


        // Get the dimension with the maximum variance.
        int maxIndex = 0;
        double maxVar = Float.MIN_VALUE;

        for( int i = 0; i < mDimCount; i++ ) {
            if( range.mMax[i] > maxVar ) {
                maxVar = range.mMax[i];
                maxIndex = i;
            }
        }

        // Sort list according to this dimension and dived list into left,
        // center, and right portions.
        Collections.sort( list, new DimComp( maxIndex ) );

        int size = list.size();
        List<P> rightList = new ArrayList<P>( size );
        for( int i = size - 1; i > size / 2; i-- ) {
            rightList.add( list.remove( i ) );
        }

        P nodeFeature = list.remove( size / 2 );
        // double[] nodeVector = nodeFeature.vectorRef();
        Node newNode = new Node( parent, nodeFeature, maxIndex, mBounds.mMin[maxIndex], mBounds.mMax[maxIndex] );

        bounds.mMin[maxIndex] = mComp.value( nodeFeature, maxIndex );
        newNode.mLess = balance( newNode, bounds, list, range );
        bounds.mMin[maxIndex] = newNode.mMin;

        bounds.mMax[maxIndex] = mComp.value( nodeFeature, maxIndex );
        newNode.mMore = balance( newNode, bounds, rightList, range );
        bounds.mMax[maxIndex] = newNode.mMax;

        // Return list to previous state.
        list.add( nodeFeature );
        list.addAll( rightList );

        return newNode;
    }



    private final class Node {

        public final Node mParent;
        public final P mFeature;
        public final int mDim;
        public final double mVal;
        public final double mMin;
        public final double mMax;

        public Node mLess = null;
        public Node mMore = null;


        public Node( Node parent, P feature, int dim, double min, double max ) {
            mParent = parent;
            mFeature = feature;
            mDim = dim;
            mVal = mComp.value( feature, dim );
            mMin = min;
            mMax = max;
        }



        public boolean findNearest( P feature, BinSum minDist, Result<P> result ) {
            final int dimCount = mComp.dimCount( mFeature );
            double dist = 0f;

            for( int i = 0; i < dimCount; i++ ) {
                double d = mComp.value( feature, i ) - mComp.value( mFeature, i );
                dist += d * d;
            }

            if( dist < result.mPickDist ) {
                result.mPickDist = dist;
                result.mPickPoint = mFeature;
            }

            double val = mComp.value( feature, mDim );

            if( val < mVal ) {
                if( mLess != null ) {
                    mLess.findNearest( feature, minDist, result );
                }

                // Check if the other side of the tree can possible contain vec.
                if( mMore != null ) {
                    double prevDist = minDist.getBin( mDim );
                    double newDist = minDist.setBin( mDim, (mVal - val) * (mVal - val) );

                    if( newDist < result.mPickDist ) {
                        mMore.findNearest( feature, minDist, result );
                    }

                    minDist.setBin( mDim, prevDist );
                }

            } else {
                if( mMore != null ) {
                    mMore.findNearest( feature, minDist, result );
                }

                if( mLess != null ) {
                    double prevDist = minDist.getBin( mDim );
                    double newDist = minDist.setBin( mDim, (mVal - val) * (mVal - val) );

                    if( newDist < result.mPickDist ) {
                        mLess.findNearest( feature, minDist, result );
                    }

                    minDist.setBin( mDim, prevDist );
                }
            }

            return true;
        }


        public void listFeatures( List<P> list ) {
            list.add( mFeature );

            if( mLess != null ) {
                mLess.listFeatures( list );
            }
            if( mMore != null ) {
                mMore.listFeatures( list );
            }
        }


        public void findApproximateNearest( P startVec,
                                            P vec,
                                            BinSum minDist,
                                            Result<P> result,
                                            PriorityQueue<Choice<Node>> queue )
        {
            if( mFeature == startVec ) {
                findApproximateNearest( vec, minDist, result, queue );

            } else if( mComp.value( startVec, mDim ) < mVal ) {
                if( mLess == null ) {
                    return;
                }

                double val = mComp.value( vec, mDim );

                if( val > mVal ) {
                    double d = minDist.getBin( mDim );
                    minDist.setBin( mDim, (val - mVal) * (val - mVal) );
                    mLess.findApproximateNearest( startVec, vec, minDist, result, queue );
                    minDist.setBin( mDim, d );

                } else {
                    mLess.findApproximateNearest( startVec, vec, minDist, result, queue );

                }

            } else {
                if( mMore == null ) {
                    return;
                }

                double val = mComp.value( vec, mDim );
                if( val < mVal ) {
                    minDist.setBin( mDim, (val - mVal) * (val - mVal) );
                    mMore.findApproximateNearest( startVec, vec, minDist, result, queue );
                } else {
                    mMore.findApproximateNearest( startVec, vec, minDist, result, queue );
                }
            }
        }


        public void findApproximateNearest( P feature,
                                            BinSum minDist,
                                            Result<P> result,
                                            PriorityQueue<Choice<Node>> queue )
        {
            int dimCount = mComp.dimCount( mFeature );
            double dist = 0f;
            double val = mComp.value( feature, mDim );

            for( int i = 0; i < dimCount; i++ ) {
                double d = mComp.value( feature, i ) - mComp.value( mFeature, i );
                dist += d * d;
            }

            if( dist < result.mPickDist ) {
                result.mPickDist = dist;
                result.mPickPoint = mFeature;
            }

            if( val < mVal ) {
                if( mLess != null ) {
                    mLess.findApproximateNearest( feature, minDist, result, queue );
                }

                if( mMore != null ) {
                    double prevDist = minDist.getBin( mDim );
                    double newDist = minDist.setBin( mDim, (mVal - val) * (mVal - val) );

                    if( newDist < result.mPickDist ) {
                        queue.offer( new Choice<Node>( newDist, mMore ) );
                    }

                    minDist.setBin( mDim, prevDist );
                }

            } else {
                if( mMore != null ) {
                    mMore.findApproximateNearest( feature, minDist, result, queue );
                }

                if( mLess != null ) {
                    double prevDist = minDist.getBin( mDim );
                    double newDist = minDist.setBin( mDim, (val - mVal) * (val - mVal) );

                    if( newDist < result.mPickDist ) {
                        queue.offer( new Choice<Node>( newDist, mLess ) );
                    }

                    minDist.setBin( mDim, prevDist );
                }
            }
        }
        

        @Override
        public int hashCode() {
            int code = mFeature.hashCode();

            if( mLess != null ) {
                code += mLess.hashCode();
            }

            if( mMore != null ) {
                code += mMore.hashCode();
            }

            return code;
        }


        public int toArray( Object[] arr, int index ) {
            arr[index++] = mFeature;

            if( mLess != null ) {
                index = mLess.toArray( arr, index );
            }

            if( mMore != null ) {
                index = mMore.toArray( arr, index );
            }

            return index;
        }

    }



    private final class KDTreeIterator implements Iterator<P> {
        private final int mSize;
        private final int mModCount;
        private int mIndex;
        private Node mCurrent;

        public KDTreeIterator() {
            mSize = KdPointTree.this.mSize;
            mModCount = KdPointTree.this.mModCount;
            mIndex = 0;
            mCurrent = null;
        }


        @Override
        public boolean hasNext() {
            return mIndex < mSize;
        }

        @Override
        public P next() {
            if( mModCount != KdPointTree.this.mModCount ) {
                throw new ConcurrentModificationException();
            }

            if( ++mIndex > mSize ) {
                throw new NoSuchElementException();
            }

            if( mIndex == 1 ) {
                mCurrent = mRoot;

                while( mCurrent.mLess != null ) {
                    mCurrent = mCurrent.mLess;
                }

                return mCurrent.mFeature;
            }

            if( mCurrent.mMore != null ) {
                mCurrent = mCurrent.mMore;

                while( mCurrent.mLess != null ) {
                    mCurrent = mCurrent.mLess;
                }

                return mCurrent.mFeature;
            }


            do {
                if( mCurrent == mCurrent.mParent.mLess ) {
                    mCurrent = mCurrent.mParent;
                    return mCurrent.mFeature;
                }

                mCurrent = mCurrent.mParent;
            } while( true );
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }



    private final class DimComp implements Comparator<P> {

        private final int mDim;


        DimComp( int dim ) {
            mDim = dim;
        }


        @Override
        public int compare( P a, P b ) {
            double va = mComp.value( a, mDim );
            double vb = mComp.value( b, mDim );

            if( va < vb ) {
                return -1;
            } else if( va > vb ) {
                return 1;
            } else {
                return 0;
            }
        }
    }



    private static final class BinSum {
        private final double[] mBin;
        private double mTotal = 0f;

        public BinSum( int numBins ) {
            mBin = new double[numBins];
        }


        public void clear() {
            Arrays.fill( mBin, 0f );
            mTotal = 0f;
        }

        public double getBin( int dim ) {
            return mBin[dim];
        }
        
        public double setBin( int i, double val ) {
            mTotal -= mBin[i];
            mBin[i] = val;
            mTotal += val;

            return mTotal;
        }
    }



    private static final class Bounds {
        final double[] mMin;
        final double[] mMax;

        public Bounds( int dimCount ) {
            mMin = new double[dimCount];
            mMax = new double[dimCount];
        }


        public void clear() {
            Arrays.fill( mMin, 0f );
            Arrays.fill( mMax, 0f );
        }

    }



    private static final class Choice<N> implements Comparable<Choice<?>> {
        public final double mDistance;
        public final N mNode;

        public Choice( double distance, N node ) {
            mDistance = distance;
            mNode = node;
        }


        @Override
        public int compareTo( Choice<?> c ) {
            if( this == c ) {
                return 0;
            }

            if( mDistance < c.mDistance ) {
                return -1;
            }

            return 1;
        }
    }

    

    private static final class Result<P> implements PointPickResult<P> {

        boolean mHasPick = false;
        double mPickDist = 0.0;
        P mPickPoint = null;

        @Override
        public boolean hasPick() {
            return mHasPick;
        }


        @Override
        public double pickedDistance() {
            return mPickDist;
        }


        @Override
        public P pickedPoint() {
            return mPickPoint;
        }


        @Override
        public Object pickedData() {
            return null;
        }

    }

}
