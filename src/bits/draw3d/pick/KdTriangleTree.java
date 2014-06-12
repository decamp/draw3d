package bits.draw3d.pick;

import java.util.*;

import bits.draw3d.model.*;
import bits.math3d.*;
import bits.math3d.geom.*;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;


/**
 * KdTriangleTree indexes collections of Triangle objects and provides
 * fast geometry picking using the RayPicker interface.
 * <p>
 * This class is a complete implementation of the KD tree described in: <br/>
 * Wald, Ingo and Havran, Vlastimil. <i>On building fast kd-Trees for Ray
 * Tracing, and on doing that in O(N log N).</i> <br/>
 * Proceedings of the 2006 IEEE Symposium on Interactive Ray Tracing, 2006,
 * pages 61-69. <br/>
 * <p>
 * Building a tree occurs in O(N log N) time, and tree traversal runs in O(log N).
 * Although any tree implementation will run traversals in O(log N), in
 * practice, there are significant differences. This implementation uses
 * several heuristics to improve space partitioning:
 * <p>
 * A <i>Surface Area Heuristic</i> is used to generate reasonable probabilities
 * for the likelihood of rays intersecting candidate space partitions.
 * <p>
 * Partition costs are biased towards culling out empty spaces, which, according
 * to Monsieur Ingo Wald, improves traversal speed in most cases.
 * <p>
 * Triangles are perfectly split, which means that for triangles the span
 * partition planes, the triangles are clipped exactly to each partition space,
 * instead of the far simpler practice of clipping the <i>bounding
 * box</i> of the triangle to the partition box. This means that triangles are
 * never added to a leaf node that they do not actually intersect.
 * 
 * @author decamp
 */
public final class KdTriangleTree implements RayPicker {

    private static final int TERM_ITEM_COUNT = 1;
    private static final int TERM_DEPTH      = 20;

    private static final double COST_TRAVERSAL_STEP = 2.0;
    private static final double COST_INTERSECTION = 1.0;

    private static final int NONE  = 0;
    private static final int LEFT  = 1;
    private static final int RIGHT = 2;
    private static final int BOTH  = 3;

    private static final int EVENT_START = 2;
    private static final int EVENT_PLANAR = 1;
    private static final int EVENT_STOP = 0;


    public static KdTriangleTree build( List<? extends Triangle> triList ) {
        double[] vox = new double[6];
        Triangles.computeBounds( triList ).toArray( vox );
        
        // Generate events.
        final int triCount    = triList.size();
        final Triangle[] tris = triList.toArray( new Triangle[triCount] );
        final int[] mark      = new int[tris.length];
        Arrays.fill( mark, BOTH );
        
        SplitEvent[] events = new SplitEvent[triCount * 2 * 3];
        int eventCount = 0;
        
        // Compute "events" associated with every possible split point.
        for( int i = 0; i < triCount; i++ ) {
            final double[][] verts = tris[i].vertexRef();
            
            for( int axis = 0; axis < 3; axis++ ) {
                double v0 = verts[0][axis];
                double v1 = verts[1][axis];
                double v2 = verts[2][axis];

                double min = (v0 < v1 ? v0 : v1);
                if( v2 < min ) {
                    min = v2;
                }
                double max = (v0 > v1 ? v0 : v1);
                if( v2 > max ) {
                    max = v2;
                }
                
                if( min == max ) {
                    events[eventCount++] = new SplitEvent( axis, min, EVENT_PLANAR, i );
                } else {
                    events[eventCount++] = new SplitEvent( axis, min, EVENT_START, i );
                    events[eventCount++] = new SplitEvent( axis, max, EVENT_STOP, i );
                }
            }
        }

        Arrays.sort( events, 0, eventCount );

        Node root = build( 0,
                           tris,
                           triCount,
                           mark,
                           events,
                           eventCount,
                           new SplitEvent[eventCount * 2],
                           vox );

        return new KdTriangleTree( root, vox );
    }



    private final Node mRoot;
    private final int mDepth;
    private final double[] mBounds;


    private KdTriangleTree( Node root, double[] bounds ) {
        mRoot = root;
        mDepth = root.computeMaxDepth();
        mBounds = bounds;
    }



    @Override
    public RayPickResult newRayPickResult() {
        return new Result();
    }


    @Override
    public boolean pick( double[] rayPoint, double[] rayDir, int side, RayPickResult out ) {
        if( !(out instanceof Result) ) {
            throw new IllegalArgumentException( "Improperly allocated PickResult." );
        }

        Result result = (Result)out;
        result.mHasPick = false;
        result.mRayDist = Double.POSITIVE_INFINITY;
        System.arraycopy( rayPoint, 0, result.mStartPoint, 0, 3 );

        // Compute intersections with bounds.
        double tMin = POSITIVE_INFINITY;
        double tMax = NEGATIVE_INFINITY;
        double[] bounds = mBounds;

        for( int i = 0; i < 3; i++ ) {
            if( rayDir[i] == 0.0 ) {
                continue;
            }

            final int i1 = (i + 1) % 3;
            final int i2 = (i + 2) % 3;

            for( int j = 0; j < 4; j += 3 ) {
                final double t = (bounds[i + j] - rayPoint[i]) / rayDir[i];
                double v1 = rayPoint[i1] + rayDir[i1] * t;
                double v2 = rayPoint[i2] + rayDir[i2] * t;
                
                if( Tol.approxComp( v1, bounds[i1] ) >= 0 && Tol.approxComp( v1, bounds[i1+3] ) <= 0 ) {
                    if( Tol.approxComp( v2, bounds[i2] ) >= 0 && Tol.approxComp( v2, bounds[i2+3] ) <= 0 ) {
                        if( t < tMin ) {
                            tMin = t;
                        }

                        if( t > tMax ) {
                            tMax = t;
                        }
                    }
                }
            }
        }

        if( tMin < 0.0 ) {
            if( tMax < 0.0 ) {
                return false;
            }

            tMin = 0.0;

        } else if( tMin == POSITIVE_INFINITY ) {
            return false;
        }

        return intersectTree( mRoot, rayPoint, rayDir, tMin, tMax, side, (Result)out );
    }


    public int treeDepth() {
        return mDepth;
    }
    
    

    /***************
     * Traversal
     ***************/

    private boolean intersectTree( Node node,
                                   double[] rayPoint,
                                   double[] rayDir,
                                   double minDist,
                                   double maxDist,
                                   int sides,
                                   Result result )
    {

        if( node.isEmpty() ) {
            return false;
        }

        final int axis = node.mSplitAxis;
        final double splitPos = node.mSplitPos;
        final double tSplit = (splitPos - rayPoint[axis]) / rayDir[axis];

        if( node.isLeaf() ) {
            // if(node.isEmpty())
            // return false;

            TriangleIntersector tester = result.mIntersector;
            RayIntersection intersect = result.mRayIntersection;

            for( Triangle t : node.mTriangles ) {
                if( !tester.intersect( rayPoint, rayDir, t, intersect ) ) {
                    continue;
                }

                if( (intersect.mTargetSide & sides) == 0 ) {
                    continue;
                }

                if( intersect.mRayDist >= result.mRayDist ||
                    Tol.approxComp( intersect.mRayDist, maxDist ) > 0 ||
                    Tol.approxComp( intersect.mRayDist, minDist ) < 0 )  
                {
                    continue;
                }

                result.mHasPick = true;
                result.mTriangle = t;
                result.mRayDist = intersect.mRayDist;
                result.mSide = intersect.mTargetSide;
                System.arraycopy( intersect.mPoint, 0, result.mStopPoint, 0, 3 );
            }

            return result.mHasPick;
        }

        // Determine which node is closest.
        final Node nearNode;
        final Node farNode;

        if( rayPoint[axis] < splitPos ) {
            nearNode = node.mLeft;
            farNode = node.mRight;
        } else {
            nearNode = node.mRight;
            farNode = node.mLeft;
        }

        if( tSplit > maxDist ) {
            return intersectTree( nearNode, rayPoint, rayDir, minDist, maxDist, sides, result );

        } else if( tSplit < minDist ) {
            if( tSplit > 0.0 ) {
                return intersectTree( farNode, rayPoint, rayDir, minDist, maxDist, sides, result );

            } else if( tSplit < 0.0 ) {
                return intersectTree( nearNode, rayPoint, rayDir, minDist, maxDist, sides, result );

            } else {
                if( rayDir[axis] < 0 ) {
                    return intersectTree( farNode, rayPoint, rayDir, minDist, maxDist, sides, result );
                } else {
                    return intersectTree( nearNode, rayPoint, rayDir, minDist, maxDist, sides, result );
                }
            }
        
        } else {
            if( tSplit > 0 ) {
                if( intersectTree( nearNode, rayPoint, rayDir, minDist, tSplit, sides, result ) ) {
                    return true;
                }
                return intersectTree( farNode, rayPoint, rayDir, tSplit, maxDist, sides, result );

            } else {
                return intersectTree( nearNode, rayPoint, rayDir, tSplit, maxDist, sides, result );
            }
        }
    }



    /***************
     * Construction
     ***************/

    private static Node build( int depth,
                               Triangle[] tris,
                               int triCount,
                               int[] triMark,
                               SplitEvent[] events,
                               int eventCount,
                               SplitEvent[] work,
                               double[] voxel )
    {
        if( triCount == 0 ) {
            return new Node( depth );
        }

        // Compute optimal split plane.
        Plane plane = new Plane();

        // Return leaf node if termination condition is met.
        boolean havePlane = findSplitPlane( triCount, events, 0, eventCount, voxel, plane );

        if( !havePlane || triCount <= TERM_ITEM_COUNT || depth >= TERM_DEPTH ) {
            List<Triangle> triList = new ArrayList<Triangle>( triCount );

            for( int i = 0; i < eventCount; i++ ) {
                int idx = events[i].mTriIndex;

                if( triMark[idx] == BOTH ) {
                    triMark[idx] = NONE;
                    triList.add( tris[idx] );
                }
            }

            for( int i = 0; i < eventCount; i++ ) {
                int idx = events[i].mTriIndex;
                triMark[idx] = BOTH;
            }

            return new Node( depth, triList );
        }

        // Determine split dimension and location.
        final int splitAxis = plane.mAxis;
        final double splitPos = plane.mPosition;
        final int splitSide = plane.mSide; // Sorting preference for triangles
                                           // laying precisely on split plane

        // Split voxels.
        double[] leftVoxel = new double[6];
        double[] rightVoxel = new double[6];
        splitVoxel( voxel, splitAxis, splitPos, leftVoxel, rightVoxel );

        // Reclassify split candidates.
        int[] triCounts = new int[BOTH + 1];

        classifySplitSides( events,
                            eventCount,
                            triCount,
                            splitAxis,
                            splitPos,
                            splitSide,
                            triMark,
                            triCounts );

        //final int bothCap = triCounts[BOTH] * 2 * 3;
        
        final int lbmin = 0;
        final int lomin = lbmin + triCounts[BOTH] * 2 * 3;
        final int rbmin = (triCounts[LEFT] + triCounts[BOTH]) * 2 * 3;
        final int romin = rbmin + triCounts[BOTH] * 2 * 3;

        int lomax = lomin;
        int lbmax = lbmin;
        int romax = romin;
        int rbmax = rbmin;

        SplitEvent[] leftEvents = new SplitEvent[(triCounts[LEFT] + triCounts[BOTH]) * 2 * 3];
        final SplitEvent[] rightEvents = events;

        Loop loop = new Loop();
        double[] triVoxel = new double[6];
        double[][] verts = new double[3][];

        for( int i = 0; i < eventCount; i++ ) {
            SplitEvent e = events[i];

            switch( triMark[e.mTriIndex] ) {
            case LEFT:
                work[lomax++] = e;
                break;

            case RIGHT:
                work[romax++] = e;
                break;

            case BOTH:
            // Reclip triangles.
            {
                triMark[e.mTriIndex] = NONE; // Leave indication that new events
                                             // have now been generated.
                Triangle t = tris[e.mTriIndex];
                verts[0] = t.mVerts[0];
                verts[1] = t.mVerts[1];
                verts[2] = t.mVerts[2];

                if( Box3.clipPlanar( verts, 0, 3, leftVoxel, loop ) ) {
                    
                    Box3.boundPoints( loop.mVerts, 0, loop.mLength, triVoxel );
                    for( int j = 0; j < 3; j++ ) {
                        if( triVoxel[j] == triVoxel[j + 3] ) {
                            work[lbmax++] = new SplitEvent( j, triVoxel[j], EVENT_PLANAR, e.mTriIndex );
                        } else {
                            work[lbmax++] = new SplitEvent( j, triVoxel[j], EVENT_START, e.mTriIndex );
                            work[lbmax++] = new SplitEvent( j, triVoxel[j + 3], EVENT_STOP, e.mTriIndex );
                        }
                    }
                }

                if( Box3.clipPlanar( verts, 0, 3, rightVoxel, loop ) ) {
                    
                    Box3.boundPoints( loop.mVerts, 0, loop.mLength, triVoxel );
                    for( int j = 0; j < 3; j++ ) {
                        if( triVoxel[j] == triVoxel[j + 3] ) {
                            work[rbmax++] = new SplitEvent( j, triVoxel[j], EVENT_PLANAR, e.mTriIndex );
                        } else {
                            work[rbmax++] = new SplitEvent( j, triVoxel[j], EVENT_START, e.mTriIndex );
                            work[rbmax++] = new SplitEvent( j, triVoxel[j + 3], EVENT_STOP, e.mTriIndex );
                        }
                    }
                }

                break;
            }
            }
        }

        // Clear mark array.
        for( int i = 0; i < eventCount; i++ ) {
            triMark[events[i].mTriIndex] = BOTH;
        }

        // Merge sort newly generated events.
        Arrays.sort( work, lbmin, lbmax );
        Arrays.sort( work, rbmin, rbmax );

        // Perform sortless merge of events from work array into children event arrays.
        sortlessMerge( work, lbmin, lbmax, work, lomin, lomax, leftEvents, 0 );
        sortlessMerge( work, rbmin, rbmax, work, romin, romax, rightEvents, 0 );

        // Removed empty space in array.
        System.arraycopy( work, lomin, leftEvents,  0,             lomax - lomin );
        System.arraycopy( work, lbmin, leftEvents,  lomax - lomin, lbmax - lbmin );
        System.arraycopy( work, romin, rightEvents, 0,             romax - romin );
        System.arraycopy( work, rbmin, rightEvents, romax - romin, rbmax - rbmin );
        
        lbmax = lomax - lomin + lbmax - lbmin;
        rbmax = romax - romin + rbmax - rbmin;

        // Sort new events.
        Arrays.sort( leftEvents, 0, lbmax );
        Arrays.sort( rightEvents, 0, rbmax );

        Node left = build( depth + 1,
                           tris,
                           triCounts[LEFT] + triCounts[BOTH],
                           triMark,
                           leftEvents,
                           lbmax,
                           work,
                           leftVoxel );

        // Clear left event array.
        leftEvents = null;

        Node right = build( depth + 1,
                            tris,
                            triCounts[RIGHT] + triCounts[BOTH],
                            triMark,
                            rightEvents,
                            rbmax,
                            work,
                            rightVoxel );

        return new Node( depth, left, right, splitAxis, splitPos );
    }


    private static boolean findSplitPlane( int triCount,
                                           SplitEvent[] events,
                                           int eventOff,
                                           int eventCount,
                                           double[] voxel,
                                           Plane out )
    {
        double[] rightVoxel = new double[6];
        double[] leftVoxel = new double[6];

        SplitCost cost = new SplitCost();
        int[][] allCounts = new int[3][3];
        allCounts[0][2] = allCounts[1][2] = allCounts[2][2] = triCount;

        double bestCost = POSITIVE_INFINITY;
        int bestSide = 0;
        int bestSplitAxis = 0;
        double bestSplitPos = 0.0;


        for( int i = eventOff; i < eventOff + eventCount; ) {
            SplitEvent event = events[i];
            int splitAxis = event.mAxis;
            double splitPos = event.mPosition;

            int newStops = 0;
            int newPlanars = 0;
            int newStarts = 0;

            while( i < eventCount && 
                   splitAxis == events[i].mAxis && 
                   splitPos == events[i].mPosition &&
                   events[i].mType == EVENT_STOP )
            {
                newStops++;
                i++;
            }

            while( i < eventCount && 
                   splitAxis == events[i].mAxis && 
                   splitPos == events[i].mPosition &&
                   events[i].mType == EVENT_PLANAR )
            {
                newPlanars++;
                i++;
            }

            while( i < eventCount && 
                   splitAxis == events[i].mAxis && 
                   splitPos == events[i].mPosition &&
                   events[i].mType == EVENT_START )
            {
                newStarts++;
                i++;
            }

            int[] counts = allCounts[splitAxis];
            counts[1] = newPlanars;
            counts[2] -= newPlanars + newStops;

            splitVoxel( voxel, splitAxis, splitPos, leftVoxel, rightVoxel );
            computeSplitCost( voxel, leftVoxel, rightVoxel, counts[0], counts[1], counts[2], cost );

            if( cost.mCost < bestCost ) {
                bestCost = cost.mCost;
                bestSide = cost.mSide;
                bestSplitAxis = splitAxis;
                bestSplitPos = splitPos;
            }

            counts[0] += newStarts + newPlanars;
            counts[1] = 0;
        }

        if( bestCost > COST_INTERSECTION * triCount ) {
            return false;
        }

        out.mAxis = bestSplitAxis;
        out.mPosition = bestSplitPos;
        out.mSide = bestSide;

        return true;
    }


    private static void classifySplitSides( SplitEvent[] events,
                                            int eventCount,
                                            int triCount,
                                            int splitAxis,
                                            double splitPos,
                                            int splitSide,
                                            int[] outMark,
                                            int[] outTriCounts )
    {
        int leftCount = 0;
        int rightCount = 0;

        for( int i = 0; i < eventCount; i++ ) {
            SplitEvent e = events[i];
            if( e.mAxis != splitAxis || outMark[e.mTriIndex] != BOTH ) {
                continue;
            }

            if( e.mType == EVENT_STOP && e.mPosition <= splitPos ) {
                outMark[e.mTriIndex] = LEFT;
                leftCount++;

            } else if( e.mType == EVENT_START && e.mPosition >= splitPos ) {
                outMark[e.mTriIndex] = RIGHT;
                rightCount++;

            } else if( e.mType == EVENT_PLANAR ) {
                if( e.mPosition < splitPos || (e.mPosition == splitPos && splitSide == LEFT) ) {
                    outMark[e.mTriIndex] = LEFT;
                    leftCount++;

                } else if( e.mPosition > splitPos || (e.mPosition == splitPos && splitSide == RIGHT) ) {
                    outMark[e.mTriIndex] = RIGHT;
                    rightCount++;
                }
            }
        }

        outTriCounts[LEFT] = leftCount;
        outTriCounts[RIGHT] = rightCount;
        outTriCounts[BOTH] = triCount - leftCount - rightCount;
    }


    private static double computePartialSplitCost( double leftHitProb,
                                                   double rightHitProb,
                                                   double leftCount,
                                                   double rightCount )
    {
        double cost = COST_TRAVERSAL_STEP + COST_INTERSECTION * ( leftHitProb * leftCount + rightHitProb * rightCount );
        // Bias for cutting out empty spaces.
        double lambda = (leftCount == 0.0 || rightCount == 0.0) ? 0.8 : 1.0;
        return lambda * cost;
    }


    private static void computeSplitCost( double[] vox,
                                          double[] leftVox,
                                          double[] rightVox,
                                          int leftCount,
                                          int planeCount,
                                          int rightCount,
                                          SplitCost out )
    {
        // Compute surface areas of voxels.
        double area = computeSurfaceArea( vox );
        double leftProb = computeSurfaceArea( leftVox ) / area;
        double rightProb = computeSurfaceArea( rightVox ) / area;

        // Compute costs given surface areas and triangle counts.
        double costLeft = computePartialSplitCost( leftProb, rightProb, leftCount + planeCount, rightCount );
        double costRight = computePartialSplitCost( leftProb, rightProb, leftCount, rightCount + planeCount );

        // Determine side of split.
        if( costLeft < costRight ) {
            out.mCost = costLeft;
            out.mSide = LEFT;
        } else {
            out.mCost = costRight;
            out.mSide = RIGHT;
        }
    }


    private static <T extends Comparable<? super T>> void sortlessMerge( T[] a,
                                                                         int aStart,
                                                                         int aStop,
                                                                         T[] b,
                                                                         int bStart,
                                                                         int bStop,
                                                                         T[] out,
                                                                         int outOff )
    {

        if( aStart < aStop && bStart < bStop ) {
            while( true ) {
                if( a[aStart].compareTo( b[bStart] ) < 0 ) {
                    out[outOff++] = a[aStart++];
                    if( aStart == aStop ) {
                        break;
                    }

                } else {
                    out[outOff++] = b[bStart++];
                    if( bStart == bStop ) {
                        break;
                    }
                }
            }
        }

        if( aStart < aStop ) {
            System.arraycopy( a, aStart, out, outOff, aStop - aStart );
        } else {
            System.arraycopy( b, bStart, out, outOff, bStop - bStart );
        }
    }


    private static void splitVoxel( double[] vox, int splitAxis, double splitPos, double[] outLeft, double[] outRight ) {
        for( int i = 0; i < 6; i++ ) {
            outLeft[i] = outRight[i] = vox[i];
        }
        outLeft[splitAxis + 3] = splitPos;
        outRight[splitAxis] = splitPos;
    }


    private static double computeSurfaceArea( double[] vox ) {
        double dx = vox[3] - vox[0];
        double dy = vox[4] - vox[1];
        double dz = vox[5] - vox[2];
        return 2.0 * (dx * dy + dx * dz + dy * dz);
    }



    private static final class Node {
        final int mDepth;
        final Node mLeft;
        final Node mRight;
        final List<Triangle> mTriangles;

        final int mSplitAxis;
        final double mSplitPos;


        Node( int depth, Node left, Node right, int splitAxis, double splitPos ) {
            mDepth = depth;
            mLeft = left;
            mRight = right;
            mTriangles = null;
            mSplitAxis = splitAxis;
            mSplitPos = splitPos;
        }

        Node( int depth, List<Triangle> triangles ) {
            mDepth = depth;
            mLeft = null;
            mRight = null;
            mTriangles = triangles;
            mSplitAxis = 0;
            mSplitPos = 0.0;
        }

        Node( int depth ) {
            mDepth = depth;
            mLeft = null;
            mRight = null;
            mTriangles = null;
            mSplitAxis = -1;
            mSplitPos = 0.0;
        }


        public boolean isLeaf() {
            return mLeft == null;
        }

        public boolean isEmpty() {
            return mSplitAxis == -1;
        }

        public int computeMaxDepth() {
            int max = mDepth;
            if( mLeft != null ) {
                max = Math.max( max, mLeft.computeMaxDepth() );
                max = Math.max( max, mRight.computeMaxDepth() );
            }
            return max;
        }
    }



    private static final class Result implements RayPickResult {
        final TriangleIntersector mIntersector = new TriangleIntersector();
        final RayIntersection mRayIntersection = new RayIntersection();

        boolean mHasPick = false;
        double mRayDist = 0.0;
        Triangle mTriangle = null;
        int mSide = 0;
        double[] mStartPoint = new double[3];
        double[] mStopPoint = new double[3];


        Result() {}


        @Override
        public boolean hasPick() {
            return mHasPick;
        }

        @Override
        public double pickedDistance() {
            return Vectors.dist( mStartPoint, mStopPoint );
        }

        @Override
        public double pickedParamDistance() {
            return mRayDist;
        }

        @Override
        public double[] pickedPoint() {
            return mStopPoint.clone();
        }

        @Override
        public double[] pickedPointRef() {
            return mStopPoint;
        }

        @Override
        public Triangle pickedTriangle() {
            return mTriangle;
        }

        @Override
        public int pickedSide() {
            return mSide;
        }

        @Override
        public Object pickedData() {
            return null;
        }

    }



    private static final class Plane {
        int mAxis;
        double mPosition;
        int mSide;
    }



    private static final class SplitCost {
        double mCost;
        int mSide;
    }



    private static final class SplitEvent implements Comparable<SplitEvent> {
        final int mAxis;
        final double mPosition;
        final int mType;

        int mTriIndex;

        SplitEvent( int axis, double position, int type, int triIndex ) {
            mAxis = axis;
            mPosition = position;
            mType = type;
            mTriIndex = triIndex;
        }


        @Override
        public int compareTo( SplitEvent e ) {
            if( this == e ) {
                return 0;
            }

            if( mPosition < e.mPosition ) {
                return -1;
            }

            if( mPosition > e.mPosition ) {
                return 1;
            }

            if( mAxis < e.mAxis ) {
                return -1;
            }

            if( mAxis > e.mAxis ) {
                return 1;
            }

            if( mType != e.mType ) {
                return mType - e.mType;
            }

            return -1;
        }
    }

}
