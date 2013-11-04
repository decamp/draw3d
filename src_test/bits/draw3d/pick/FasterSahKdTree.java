package bits.draw3d.pick;

import java.util.*;

import bits.draw3d.model.*;
import bits.math3d.*;
import bits.math3d.geom.*;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;



/**
 * You don't want to use this class.  It's just here for comparison and will
 * probably disappear at some point.
 * <p>
 * This kd tree uses a non-trivial Surface-Area-Heuristic for partitioning the space,
 * and uses a reasonable construction algorithm that runs in O(n^2) time.
 * 
 * @deprecated
 * @author decamp
 */
public final class FasterSahKdTree implements RayPicker {

    
    private static final int TERM_ITEM_COUNT = 1;
    private static final int TERM_DEPTH = 20;
    
    private static final double COST_TRAVERSAL_STEP = 2.0;
    private static final double COST_INTERSECTION   = 1.0;
    
    private static final int LEFT   = -1;
    private static final int RIGHT  = 1;
    
    private static final int EVENT_START  = 2;
    private static final int EVENT_PLANAR = 1;
    private static final int EVENT_STOP   = 0;
    
    
    public static FasterSahKdTree build(List<Triangle> tris) {
        Aabb cube = Triangles.computeBounds(tris);
        double[] vox = cubeToVoxel(cube);
        Node root = build(0, tris, vox);
        
        return new FasterSahKdTree(root, vox);
    }
    
    
    
    private final Node mRoot;
    private final int mDepth;
    private final double[] mBounds; 
    
    
    private FasterSahKdTree(Node root, double[] bounds) {
        mRoot = root;
        mDepth = root.computeMaxDepth();
        mBounds = bounds;
    }
    
    
    
    public RayPickResult newRayPickResult() {
        return new Result();
    }
    
    
    public boolean pick(double[] rayPoint, double[] rayDir, int side, RayPickResult out) {
        if(!(out instanceof Result))
            throw new IllegalArgumentException("Improperly allocated PickResult.");

        Result result = (Result)out;
        result.mHasPick = false;
        result.mRayDist = Double.POSITIVE_INFINITY;
        System.arraycopy(rayPoint, 0, result.mStartPoint, 0, 3);
        
        //Compute intersections with bounds.
        double tMin = POSITIVE_INFINITY;
        double tMax = NEGATIVE_INFINITY;
        double[] bounds = mBounds;
        
        for(int i = 0; i < 3; i++) {
            if(rayDir[i] == 0.0)
                continue;
            
            final int i1 = (i + 1) % 3;
            final int i2 = (i + 2) % 3;
            
            for(int j = 0; j < 4; j += 3) {
                final double t = (bounds[i+j] - rayPoint[i]) / rayDir[i];
                
                double v1 = rayPoint[i1] + rayDir[i1] * t;
                double v2 = rayPoint[i2] + rayDir[i2] * t;
                
                if(v1 >= bounds[i1] - Tolerance.TOL && v1 <= bounds[i1+3] + Tolerance.TOL) {
                    if(v2 >= bounds[i2] - Tolerance.TOL && v2 <= bounds[i2+3] + Tolerance.TOL) {
                        if(t < tMin) {      
                            tMin = t;
                        }
                        
                        if(t > tMax) {
                            tMax = t;
                        }
                    }
                }
            }
        }
        
        if(tMin < 0.0) {
            if(tMax < 0.0)
                return false;
            
            tMin = 0.0;
            
        }else if(tMin == POSITIVE_INFINITY) {
            return false;
        }
        
        return intersectTree(mRoot, rayPoint, rayDir, tMin, tMax, side, (Result)out);
    }
    
    
    
    /***************
     * Traversal
     ***************/
    
    public boolean intersectTree(Node node, double[] rayPoint, double[] rayDir, double minDist, double maxDist, int sides, Result result) {
        
        final int axis = node.mSplitAxis;
        final double splitPos = node.mSplitPos;
        final double tSplit = (splitPos - rayPoint[axis]) / rayDir[axis];
        
        if(node.isLeaf()) {
            TriangleIntersector tester = result.mIntersector;
            RayIntersection intersect = result.mRayIntersection;
            
            for(Triangle t: node.mTriangles) {
                if(!tester.intersect(rayPoint, rayDir, t, intersect))
                    continue;
                
                if((intersect.mTargetSide & sides) == 0)
                    continue;
                
                if(intersect.mRayDist >= result.mRayDist || intersect.mRayDist > maxDist + Tolerance.TOL || intersect.mRayDist < minDist - Tolerance.TOL)
                    continue;
        
                result.mHasPick  = true;
                result.mTriangle = t;
                result.mRayDist  = intersect.mRayDist;
                result.mSide     = intersect.mTargetSide;
                System.arraycopy(intersect.mPoint, 0, result.mStopPoint, 0, 3);
            }
            
            return result.mHasPick;
        }
        
        //Determine which node is closest.
        final Node nearNode;
        final Node farNode;

        if(rayPoint[axis] < splitPos) {
            nearNode = node.mLeft;
            farNode  = node.mRight;
        }else{
            nearNode = node.mRight;
            farNode  = node.mLeft;
        }
        
        if(tSplit > maxDist) {
            return intersectTree(nearNode, rayPoint, rayDir, minDist, maxDist, sides, result);
        
        }else if(tSplit < minDist) {
            if(tSplit > 0.0) {
                return intersectTree(farNode, rayPoint, rayDir, minDist, maxDist, sides, result);
                
            }else if(tSplit < 0.0) {
                return intersectTree(nearNode, rayPoint, rayDir, minDist, maxDist, sides, result);
                
            }else{
                if(rayDir[axis] < 0) {
                    return intersectTree(farNode, rayPoint, rayDir, minDist, maxDist, sides, result);
                }else{
                    return intersectTree(nearNode, rayPoint, rayDir, minDist, maxDist, sides, result);
                }
            }
            
        }else{
            if(tSplit > 0) {
                if(intersectTree(nearNode, rayPoint, rayDir, minDist, tSplit, sides, result))
                    return true;
                
                return intersectTree(farNode, rayPoint, rayDir, tSplit, maxDist, sides, result);
                
            }else{
                return intersectTree(nearNode, rayPoint, rayDir, tSplit, maxDist, sides, result);
            }
        }
        
    }
    
    
    
    /***************
     * Construction
     ***************/
    
    private static Node build(int depth, List<Triangle> tris, double[] voxel) {
        
        //Return leaf node if termination condition is met.
        if(tris.size() <= TERM_ITEM_COUNT || depth >= TERM_DEPTH) {
            return new Node(depth, tris);
        }

        //Determine split dimension and location.
        final int splitAxis;
        final double splitPos;
        final int splitSide;       //Sorting preference for triangles laying precisely on split plane
        
        final double[] leftVox = new double[6];
        final double[] rightVox = new double[6];
        
        List<Triangle> leftTris;
        List<Triangle> rightTris;
        Loop loop = new Loop();
        
        {
            Plane plane = new Plane();
            
            //Compute optimal split pane.
            if(!computeSplitPlane(depth, tris, voxel, plane))
                return new Node(depth, tris);
            
            //Split voxes.
            splitVoxel(voxel, plane.mAxis, plane.mPosition, leftVox, rightVox);
            
            //Split triangles.
            splitAxis = plane.mAxis;
            splitPos = plane.mPosition;
            splitSide = plane.mSide;   
            
            leftTris = new ArrayList<Triangle>();
            rightTris = new ArrayList<Triangle>();
            
            for(Triangle t: tris) {
                double[][] verts = t.vertexRef();
                double v0 = verts[0][splitAxis];
                double v1 = verts[1][splitAxis];
                double v2 = verts[2][splitAxis];

                
                if(v0 < splitPos) {
                    if(v1 < splitPos && v2 < splitPos) {
                        leftTris.add(t);
                    
                    }else{
                        double[][] vv = new double[][]{verts[0], verts[1], verts[2]};
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, leftVox, loop)) {
                            leftTris.add(t);
                        }
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, rightVox, loop)) {
                            rightTris.add(t);
                        }
                    }
                
                }else if(v0 > splitPos) {
                    if(v1 > splitPos && v2 > splitPos) {
                        rightTris.add(t);
                        
                    }else{
                        double[][] vv = new double[][]{verts[0], verts[1], verts[2]};
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, leftVox, loop)) {
                            leftTris.add(t);
                        }
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, rightVox, loop)) {
                            rightTris.add(t);
                        }
                    }
                    
                }else{
                    if(v1 == splitPos && v2 == splitPos) {
                        if(splitSide < 0) {
                            leftTris.add(t);
                        }else{
                            rightTris.add(t);
                        }
                        
                    }else{
                        double[][] vv = new double[][]{verts[0], verts[1], verts[2]};
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, leftVox, loop)) {
                            leftTris.add(t);
                        }
                        
                        if(Aabbs.clipPlanarToAabb(vv, 0, 3, rightVox, loop)) {
                            rightTris.add(t);
                        }
                    }
                }
            }
        }
        
        Node left  = build(depth + 1, leftTris, leftVox);
        Node right = build(depth + 1, rightTris, rightVox);
        return new Node(depth, left, right, splitAxis, splitPos);
    }
    
    
    private static boolean computeSplitPlane(int depth, List<Triangle> tris, double[] voxel, Plane out) {
        
        double bestCost     = POSITIVE_INFINITY;
        int bestSide        = 0;
        int bestSplitAxis   = 0;
        double bestSplitPos = 0.0;
        
        SplitCost cost = new SplitCost();
        
        double[] rightVoxel = new double[6];
        double[] leftVoxel  = new double[6];

        SplitEvent[] eventArr = new SplitEvent[tris.size() * 2];
        int eventCount = 0;
        
        //Check each axis separately.
        for(int splitAxis = 0; splitAxis < 3; splitAxis++) {
            eventCount = 0;
                        
            //Compute "events" associated with every possible split point.
            for(Triangle t: tris) {
                double[][] verts = t.vertexRef();
                double v0 = verts[0][splitAxis];
                double v1 = verts[1][splitAxis];
                double v2 = verts[2][splitAxis];
                
                double min = (v0 < v1 ? v0 : v1);
                if(v2 < min)
                    min = v2;
                
                double max = (v0 > v1 ? v0 : v1);
                if(v2 > max)
                    max = v2;
                
                if(min == max) {
                    eventArr[eventCount++] = new SplitEvent(t, min, EVENT_PLANAR);
                }else{
                    eventArr[eventCount++] = new SplitEvent(t, min, EVENT_START);
                    eventArr[eventCount++] = new SplitEvent(t, max, EVENT_STOP);
                }
            }
            
            //Sort events.
            Arrays.sort(eventArr, 0, eventCount);
            
            //Sweep plane over split candidates.
            int leftCount  = 0;
            int planeCount = 0;
            int rightCount = tris.size();
            
            for(int i = 0; i < eventCount;) {
                double splitPos = eventArr[i].mPosition;
                int newStarts   = 0;
                int newPlanars  = 0;
                int newStops    = 0;
                
                while(i < eventCount && eventArr[i].mPosition == splitPos && eventArr[i].mType == EVENT_STOP) {
                    newStops++;
                    i++;
                }
                
                while(i < eventCount && eventArr[i].mPosition == splitPos && eventArr[i].mType == EVENT_PLANAR) {
                    newPlanars++;
                    i++;
                }
                
                while(i < eventCount && eventArr[i].mPosition == splitPos && eventArr[i].mType == EVENT_START) {
                    newStarts++;
                    i++;
                }
                
                //Move plane onto pos
                planeCount = newPlanars;
                rightCount -= newPlanars + newStops;
                
                splitVoxel(voxel, splitAxis, splitPos, leftVoxel, rightVoxel);
                computeSplitCost(voxel, leftVoxel, rightVoxel, leftCount, planeCount, rightCount, cost);
                
                if(cost.mCost < bestCost) {
                    bestCost = cost.mCost;
                    bestSide = cost.mSide;
                    bestSplitAxis = splitAxis;
                    bestSplitPos  = splitPos;
                }
                
                //Move plane past pos.
                leftCount += newStarts + newPlanars;
                planeCount = 0;
            }
        }
        
        if(bestCost > COST_INTERSECTION * tris.size())
            return false;
        
        out.mAxis     = bestSplitAxis;
        out.mPosition = bestSplitPos;
        out.mSide     = bestSide;
        
        return true;
    }
    
    
    private static double computePartialSplitCost(double leftHitProb, double rightHitProb, double leftCount, double rightCount) {
        double cost = COST_TRAVERSAL_STEP + COST_INTERSECTION * (leftHitProb * leftCount + rightHitProb * rightCount);
        //Bias for cutting out empty spaces.
        double lambda = (leftCount == 0.0 || rightCount == 0.0) ? 0.8 : 1.0;
        return lambda * cost;
    }
    
    
    private static void computeSplitCost( double[] vox,
                                          double[] leftVox, 
                                          double[] rightVox,
                                          int leftCount,
                                          int planeCount,
                                          int rightCount,
                                          SplitCost out)
    {
        //Compute surface areas of voxels.
        double area  = computeSurfaceArea(vox);
        double leftProb = computeSurfaceArea(leftVox) / area;
        double rightProb = computeSurfaceArea(rightVox) / area;
        
        //Compute costs given surface areas and triangle counts.
        double costLeft = computePartialSplitCost(leftProb, rightProb, leftCount + planeCount, rightCount);
        double costRight = computePartialSplitCost(leftProb, rightProb, leftCount, rightCount + planeCount);
        
        //Determine side of split.
        if(costLeft < costRight) {
            out.mCost = costLeft;
            out.mSide = LEFT;
        }else{
            out.mCost = costRight;
            out.mSide = RIGHT;
        }
    }

    
    private static void splitVoxel(double[] vox, int splitAxis, double splitPos, double[] outLeft, double[] outRight) {
        for(int i = 0; i < 6; i++) {
            outLeft[i] = outRight[i] = vox[i];
        }
        
        outLeft[splitAxis + 3] = splitPos;
        outRight[splitAxis    ] = splitPos;
    }
    
    
    private static void unionVoxelWithTriangle(double[] vox, double[][] verts) {
        for(int i = 0; i < 3; i++) {
            double[] arr = verts[i];
            
            for(int j = 0; j < 3; j++) {
                if(arr[j] < vox[j]) {
                    vox[j] = arr[j];
                }
            }
            
            for(int j = 0; j < 3; j++) {
                if(arr[j] > vox[j+3]) {
                    vox[j+3] = arr[j];
                }
            }
        }
    }
    

    private static void clipVoxel(double[] targetVoxel, double[] clipVoxel) {
        for(int i = 0; i < 3; i++) {
            targetVoxel[i  ] = Math.max(targetVoxel[i  ], clipVoxel[i  ]);
            targetVoxel[i+3] = Math.min(targetVoxel[i+3], clipVoxel[i+3]);
        }
    }
    

    private static double[] cubeToVoxel(Aabb cube) {
        return new double[]{cube.minX(), cube.minY(), cube.minZ(), cube.maxX(), cube.maxY(), cube.maxZ()};
    }
    
    
    private static Aabb voxelToCube(double[] vox) {
        return Aabb.fromEdges(vox[0], vox[1], vox[2], vox[3], vox[4], vox[5]);
    }

    
    private static double computeSurfaceArea(double[] vox) {
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

        
        Node(int depth, Node left, Node right, int splitAxis, double splitPos) {
            mDepth = depth;
            mLeft = left;
            mRight = right;
            mTriangles = null;
            mSplitAxis = splitAxis;
            mSplitPos = splitPos;
        }
        
        Node(int depth, List<Triangle> triangles) {
            mDepth = depth;
            mLeft = null;
            mRight = null;
            mTriangles = triangles;
            mSplitAxis = 0;
            mSplitPos = 0.0;
        }

        

        public boolean isLeaf() {
            return mTriangles != null;
        }

        public int objectCount() {
            return 0;
        }

        public int computeMaxDepth() {
            int max = mDepth;
            
            if(mLeft != null) {
                max = Math.max(max, mLeft.computeMaxDepth());
                max = Math.max(max, mRight.computeMaxDepth());
            }
            
            return max;
        }
        
    }

    
    
    private static final class Result implements RayPickResult {

        final TriangleIntersector mIntersector = new TriangleIntersector();
        final RayIntersection mRayIntersection = new RayIntersection();
        
        boolean mHasPick     = false;
        double mRayDist      = 0.0;
        Triangle mTriangle   = null;
        int mSide            = 0;
        double[] mStartPoint = new double[3];
        double[] mStopPoint  = new double[3];
        
        
        Result() {}

        
        public boolean hasPick() {
            return mHasPick;
        }

        public double pickedDistance() {
            return Vectors.dist(mStartPoint, mStopPoint);
        }
        
        public double pickedParamDistance() {
            return mRayDist;
        }
        
        public double[] pickedPoint() {
            return mStopPoint.clone();
        }
        
        public double[] pickedPointRef() {
            return mStopPoint;
        }
        
        public Triangle pickedTriangle() {
            return mTriangle;
        }

        public int pickedSide() {
            return mSide;
        }
        
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
        final Triangle mTriangle;
        final double mPosition;
        final int mType;
        
        SplitEvent(Triangle triangle, double position, int type) {
            mTriangle = triangle;
            mPosition = position;
            mType = type;
        }


        
        public int compareTo(SplitEvent e) {
            if(this == e)
                return 0;
            
            if(mPosition < e.mPosition)
                return -1;
            
            if(mPosition > e.mPosition)
                return 1;
            
            if(mType != e.mType)
                return mType - e.mType;
            
            return -1;
        }
        
    }


    
    
    private static boolean near(double x, double y) {
        return Math.abs(y - x) < 0.1;
    }
    
}
