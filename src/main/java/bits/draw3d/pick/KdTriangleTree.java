package bits.draw3d.pick;

import java.util.*;

import bits.draw3d.model.*;
import bits.math3d.*;
import bits.math3d.geom.*;

import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Float.NEGATIVE_INFINITY;


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

    private static final float COST_TRAVERSAL_STEP = 2;
    private static final float COST_INTERSECTION   = 1;

    private static final int NONE  = 0;
    private static final int LEFT  = 1;
    private static final int RIGHT = 2;
    private static final int BOTH  = 3;

    private static final int EVENT_START = 2;
    private static final int EVENT_PLANAR = 1;
    private static final int EVENT_STOP = 0;


    public static KdTriangleTree build( List<? extends DrawTri> triList ) {
        Box3 box = new Box3();
        GeomUtil.computeBounds( GeomUtil.vertIterator( triList ), box );
        float[] vox = { box.x0, box.y0, box.z0, box.x1, box.y1, box.z1 };

        // Generate events.
        final int triCount    = triList.size();
        final DrawTri[] tris = triList.toArray( new DrawTri[triCount] );
        final int[] mark      = new int[tris.length];
        Arrays.fill( mark, BOTH );

        SplitEvent[] events = new SplitEvent[triCount * 2 * 3];
        int eventCount = 0;

        // Compute "events" associated with every possible split point.
        for( int i = 0; i < triCount; i++ ) {
            final DrawVert[] verts = tris[i].mVerts;

            for( int axis = 0; axis < 3; axis++ ) {
                float v0 = verts[0].mPos.el( axis );
                float v1 = verts[1].mPos.el( axis );
                float v2 = verts[2].mPos.el( axis );

                float min = (v0 < v1 ? v0 : v1);
                if( v2 < min ) {
                    min = v2;
                }
                float max = (v0 > v1 ? v0 : v1);
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
    private final float[] mBounds;


    private KdTriangleTree( Node root, float[] bounds ) {
        mRoot = root;
        mDepth = root.computeMaxDepth();
        mBounds = bounds;
    }



    @Override
    public RayPickResult newRayPickResult() {
        return new Result();
    }


    @Override
    public boolean pick( Vec3 rayPointVec, Vec3 rayDirVec, int side, RayPickResult out ) {
        if( !(out instanceof Result) ) {
            throw new IllegalArgumentException( "Improperly allocated PickResult." );
        }

        float[] rayPoint = { rayPointVec.x, rayPointVec.y, rayPointVec.z };
        float[] rayDir   = { rayDirVec.x, rayDirVec.y, rayDirVec.z };

        Result result = (Result)out;
        result.mHasPick = false;
        result.mRayDist = POSITIVE_INFINITY;
        Vec.put( rayPointVec, result.mStartPoint );

        // Compute intersections with bounds.
        float tMin = POSITIVE_INFINITY;
        float tMax = NEGATIVE_INFINITY;
        float[] bounds = mBounds;

        for( int i = 0; i < 3; i++ ) {
            if( rayDir[i] == 0.0 ) {
                continue;
            }

            final int i1 = (i + 1) % 3;
            final int i2 = (i + 2) % 3;

            for( int j = 0; j < 4; j += 3 ) {
                final float t = (bounds[i + j] - rayPoint[i]) / rayDir[i];
                float v1 = rayPoint[i1] + rayDir[i1] * t;
                float v2 = rayPoint[i2] + rayDir[i2] * t;

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

        if( tMin < 0 ) {
            if( tMax < 0 ) {
                return false;
            }
            tMin = 0;
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
                                   float[] rayPoint,
                                   float[] rayDir,
                                   float minDist,
                                   float maxDist,
                                   int sides,
                                   Result result )
    {

        if( node.isEmpty() ) {
            return false;
        }

        final int axis = node.mSplitAxis;
        final float splitPos = node.mSplitPos;
        final float tSplit = (splitPos - rayPoint[axis]) / rayDir[axis];

        if( node.isLeaf() ) {
            TriangleIntersector tester = result.mIntersector;
            RayIntersection intersect = result.mRayIntersection;

            for( DrawTri t : node.mTriangles ) {
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
                Vec.put( intersect.mPoint, result.mStopPoint );
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
                               DrawTri[] tris,
                               int triCount,
                               int[] triMark,
                               SplitEvent[] events,
                               int eventCount,
                               SplitEvent[] work,
                               float[] voxel )
    {
        if( triCount == 0 ) {
            return new Node( depth );
        }

        // Compute optimal split plane.
        Plane plane = new Plane();

        // Return leaf node if termination condition is met.
        boolean havePlane = findSplitPlane( triCount, events, 0, eventCount, voxel, plane );

        if( !havePlane || triCount <= TERM_ITEM_COUNT || depth >= TERM_DEPTH ) {
            List<DrawTri> triList = new ArrayList<DrawTri>( triCount );

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
        final int splitAxis  = plane.mAxis;
        final float splitPos = plane.mPosition;
        final int splitSide  = plane.mSide; // Sorting preference for triangles
        // laying precisely on split plane

        // Split voxels.
        float[] leftVoxel  = new float[6];
        float[] rightVoxel = new float[6];
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

        PolyLine loop = new PolyLine();
        float[] triVoxel = new float[6];
        Vec3[] verts = new Vec3[3];
        Box3 tempBox = new Box3();

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
                DrawTri t = tris[e.mTriIndex];
                verts[0] = t.mVerts[0].mPos;
                verts[1] = t.mVerts[1].mPos;
                verts[2] = t.mVerts[2].mPos;

                put( leftVoxel, tempBox );
                if( Clip.clipPlanar( verts, 0, 3, tempBox, loop ) ) {
                    Box.pointUnion( loop.mVerts, 0, loop.mSize, tempBox );
                    put( tempBox, triVoxel );

                    for( int j = 0; j < 3; j++ ) {
                        if( triVoxel[j] == triVoxel[j + 3] ) {
                            work[lbmax++] = new SplitEvent( j, triVoxel[j], EVENT_PLANAR, e.mTriIndex );
                        } else {
                            work[lbmax++] = new SplitEvent( j, triVoxel[j], EVENT_START, e.mTriIndex );
                            work[lbmax++] = new SplitEvent( j, triVoxel[j + 3], EVENT_STOP, e.mTriIndex );
                        }
                    }
                }

                put( rightVoxel, tempBox );
                if( Clip.clipPlanar( verts, 0, 3, tempBox, loop ) ) {
                    for( int j = 0; j < loop.mSize; j++ ) {
                        if( Vec.isNaN( loop.mVerts[j] ) ) {
                            Clip.clipPlanar( verts, 0, 3, tempBox, loop );
                        }
                    }

                    Box.pointUnion( loop.mVerts, 0, loop.mSize, tempBox );
                    put( tempBox, triVoxel );

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
                                           float[] voxel,
                                           Plane out )
    {
        float[] rightVoxel = new float[6];
        float[] leftVoxel  = new float[6];

        SplitCost cost = new SplitCost();
        int[][] allCounts = new int[3][3];
        allCounts[0][2] = allCounts[1][2] = allCounts[2][2] = triCount;

        float bestCost = POSITIVE_INFINITY;
        int   bestSide = 0;
        int   bestSplitAxis = 0;
        float bestSplitPos = 0;


        for( int i = eventOff; i < eventOff + eventCount; ) {
            SplitEvent event = events[i];
            int splitAxis = event.mAxis;
            float splitPos = event.mPosition;

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
                                            float splitPos,
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


    private static float computePartialSplitCost( float leftHitProb,
                                                  float rightHitProb,
                                                  float leftCount,
                                                  float rightCount )
    {
        float cost = COST_TRAVERSAL_STEP + COST_INTERSECTION * ( leftHitProb * leftCount + rightHitProb * rightCount );
        // Bias for cutting out empty spaces.
        float lambda = (leftCount == 0.0 || rightCount == 0.0) ? 0.8f : 1.0f;
        return lambda * cost;
    }


    private static void computeSplitCost( float[] vox,
                                          float[] leftVox,
                                          float[] rightVox,
                                          int leftCount,
                                          int planeCount,
                                          int rightCount,
                                          SplitCost out )
    {
        // Compute surface areas of voxels.
        float area      = computeSurfaceArea( vox );
        float leftProb  = computeSurfaceArea( leftVox ) / area;
        float rightProb = computeSurfaceArea( rightVox ) / area;

        // Compute costs given surface areas and triangle counts.
        float costLeft  = computePartialSplitCost( leftProb, rightProb, leftCount + planeCount, rightCount );
        float costRight = computePartialSplitCost( leftProb, rightProb, leftCount, rightCount + planeCount );

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


    private static void splitVoxel( float[] vox, int splitAxis, float splitPos, float[] outLeft, float[] outRight ) {
        outLeft[0] = outRight[0] = vox[0];
        outLeft[1] = outRight[1] = vox[1];
        outLeft[2] = outRight[2] = vox[2];
        outLeft[3] = outRight[3] = vox[3];
        outLeft[4] = outRight[4] = vox[4];
        outLeft[5] = outRight[5] = vox[5];

        outLeft[splitAxis+3] = splitPos;
        outRight[splitAxis] = splitPos;
    }


    private static float computeSurfaceArea( float[] vox ) {
        float dx = vox[3] - vox[0];
        float dy = vox[4] - vox[1];
        float dz = vox[5] - vox[2];
        return 2 * (dx * dy + dx * dz + dy * dz);
    }


    private static float computeSurfaceArea( Box3 vox ) {
        float dx = vox.x1 - vox.x0;
        float dy = vox.y1 - vox.y0;
        float dz = vox.z1 - vox.z0;
        return 2 * (dx * dy + dx * dz + dy * dz);
    }



    private static final class Node {
        final int mDepth;
        final Node mLeft;
        final Node mRight;
        final List<DrawTri> mTriangles;

        final int mSplitAxis;
        final float mSplitPos;


        Node( int depth, Node left, Node right, int splitAxis, float splitPos ) {
            mDepth = depth;
            mLeft = left;
            mRight = right;
            mTriangles = null;
            mSplitAxis = splitAxis;
            mSplitPos = splitPos;
        }

        Node( int depth, List<DrawTri> triangles ) {
            mDepth     = depth;
            mLeft      = null;
            mRight     = null;
            mTriangles = triangles;
            mSplitAxis = 0;
            mSplitPos  = 0;
        }

        Node( int depth ) {
            mDepth     = depth;
            mLeft      = null;
            mRight     = null;
            mTriangles = null;
            mSplitAxis = -1;
            mSplitPos  = 0;
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
        final TriangleIntersector mIntersector     = new TriangleIntersector();
        final RayIntersection     mRayIntersection = new RayIntersection();

        boolean  mHasPick    = false;
        float    mRayDist    = 0;
        DrawTri  mTriangle   = null;
        int      mSide       = 0;
        Vec3     mStartPoint = new Vec3();
        Vec3     mStopPoint  = new Vec3();


        Result() {}


        @Override
        public boolean hasPick() {
            return mHasPick;
        }

        @Override
        public float pickedDistance() {
            return Vec.dist( mStartPoint, mStopPoint );
        }

        @Override
        public float pickedParamDistance() {
            return mRayDist;
        }

        @Override
        public Vec3 pickedPoint() {
            return new Vec3( mStopPoint );
        }

        @Override
        public Vec3 pickedPointRef() {
            return mStopPoint;
        }

        @Override
        public DrawTri pickedTriangle() {
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
        int   mAxis;
        float mPosition;
        int   mSide;
    }


    private static final class SplitCost {
        float  mCost;
        int    mSide;
    }


    private static final class SplitEvent implements Comparable<SplitEvent> {
        final int   mAxis;
        final float mPosition;
        final int   mType;

        int mTriIndex;

        SplitEvent( int axis, float position, int type, int triIndex ) {
            if( position != position ) {
                System.out.println( "??" );
            }
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


    private static void put( float[] b, Box3 box ) {
        box.x0 = b[0];
        box.y0 = b[1];
        box.z0 = b[2];
        box.x1 = b[3];
        box.y1 = b[4];
        box.z1 = b[5];
    }

    private static void put( Box3 box, float[] b ) {
        b[0] = box.x0;
        b[1] = box.y0;
        b[2] = box.z0;
        b[3] = box.x1;
        b[4] = box.y1;
        b[5] = box.z1;
    }


}
