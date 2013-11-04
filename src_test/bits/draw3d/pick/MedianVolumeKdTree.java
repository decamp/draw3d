package bits.draw3d.pick;

import java.util.*;

import bits.draw3d.model.*;
import bits.math3d.*;
import bits.math3d.geom.*;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.NEGATIVE_INFINITY;


/**
 * You don't want to use this class.  It's just here for comparison and is likely
 * to disappear at some point.
 * <p>
 * This kd tree uses a trivial, median volume method for partitioning the space.  It's 
 * fast to build, but provides about the worst result you can get with a KD tree 
 * (without trying to get bad results).  In testing, this is still some
 * 30-odd times faster than brute force picking for 3000 triangles, and is obviously
 * more scalable.
 * 
 * @deprecated
 * @author decamp
 */
public final class MedianVolumeKdTree implements RayPicker {

    private static final int TERM_ITEM_COUNT = 3;
    private static final int TERM_DEPTH = 20;
    
    
    public static MedianVolumeKdTree build(List<Triangle> tris) {
        Aabb cube = Triangles.computeBounds(tris);
        double[] vox = cubeToVoxel(cube);
        Node root = build(0, tris, vox);
        
        return new MedianVolumeKdTree(root, vox);
    }
    
    
    
    private final Node mRoot;
    private final int mDepth;
    private final double[] mBounds; 
    
    
    private MedianVolumeKdTree(Node root, double[] bounds) {
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
        if(tris.size() <= TERM_ITEM_COUNT || depth >= TERM_DEPTH)
            return new Node(depth, tris);

        //Determine split dimension and location.
        final int splitAxis;
        final double splitPos;
        
        {
            Plane plane = computeSplitPlane(depth, tris, voxel);
            splitAxis = plane.mAxis;
            splitPos = plane.mPosition;
        }
            
        
        //Split triangles at split plane.
        double[] leftVox = new double[]{ POSITIVE_INFINITY,
                                         POSITIVE_INFINITY,
                                         POSITIVE_INFINITY,
                                         NEGATIVE_INFINITY,
                                         NEGATIVE_INFINITY,
                                         NEGATIVE_INFINITY };
        
        double[] rightVox = new double[]{ POSITIVE_INFINITY,
                                          POSITIVE_INFINITY,
                                          POSITIVE_INFINITY,
                                          NEGATIVE_INFINITY,
                                          NEGATIVE_INFINITY,
                                          NEGATIVE_INFINITY };

        List<Triangle> leftTris = new ArrayList<Triangle>();
        List<Triangle> rightTris = new ArrayList<Triangle>();
        int side;
                
        for(Triangle tri: tris) {
            //Determine if triangle falls on one side of split plane, or if its split.
            double[][] verts = tri.vertexRef();
            double vertPos = verts[0][splitAxis];
              
            if(vertPos < splitPos) {
                if( verts[1][splitAxis] < splitPos && 
                    verts[2][splitAxis] < splitPos) 
                {
                    leftTris.add(tri);
                    unionVoxelWithTriangle(leftVox, verts);
                }else{
                    leftTris.add(tri);
                    rightTris.add(tri);
                    unionVoxelWithTriangle(leftVox, verts);
                    unionVoxelWithTriangle(rightVox, verts);
                }
            }else if(vertPos > splitPos) {
                if( verts[1][splitAxis] > splitPos && 
                    verts[2][splitAxis] > splitPos) 
                {
                    rightTris.add(tri);
                    unionVoxelWithTriangle(rightVox, verts);
                }else{
                    leftTris.add(tri);
                    rightTris.add(tri);
                    unionVoxelWithTriangle(leftVox, verts);
                    unionVoxelWithTriangle(rightVox, verts);
                }
            }else{
                leftTris.add(tri);
                rightTris.add(tri);
                unionVoxelWithTriangle(leftVox, verts);
                unionVoxelWithTriangle(rightVox, verts);
            }
        }

        //Ensure voxel remains entirely within parent voxel.
        //This is necessary for certain cases where triangles span more than two nodes, and possibly others.

        clipVoxel(leftVox, voxel);
        clipVoxel(rightVox, voxel);
        
        if(leftVox[splitAxis + 3] > splitPos)
            leftVox[splitAxis + 3] = splitPos;
        
        if(rightVox[splitAxis] < splitPos)
            rightVox[splitAxis] = splitPos;
       
        Node left = build(depth + 1, leftTris, leftVox);
        Node right = build(depth + 1, rightTris, rightVox);
        return new Node(depth, left, right, splitAxis, splitPos);
    }
    
    
    private static Plane computeSplitPlane(int depth, List<Triangle> tris, double[] voxel) {
        Plane ret = new Plane();
        
        int dim = depth % 3;
        ret.mPosition = (voxel[dim] + voxel[dim+3]) * 0.5;
        ret.mAxis = dim;
        
        return ret;
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

    
    private static final class Plane {
        int mAxis;
        double mPosition;
    }

    
    private static final class Result implements RayPickResult {

        final TriangleIntersector mIntersector = new TriangleIntersector();
        final RayIntersection mRayIntersection = new RayIntersection();
        
        boolean mHasPick     = false;
        double mRayDist      = 0.0;
        Triangle mTriangle   = null;
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
            return Side.NONE;
        }
        
        public Object pickedData() {
            return null;
        }
            
    }
    
}
