package bits.draw3d.pick;

import java.util.*;

import bits.draw3d.model.Triangle;
import bits.math3d.Vectors;


/**
 * This is a refernce implemention of GeometryPicker that uses a simple brute
 * force algorithm. It should be used for testing purposes only.
 * 
 * @author decamp
 */
public class BruteForcePicker implements RayPicker {


    public static BruteForcePicker build( List<Triangle> trisRef ) {
        return new BruteForcePicker( trisRef );
    }


    private final List<Triangle> mTris;

    
    private BruteForcePicker( List<Triangle> tris ) {
        mTris = tris;
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
        System.arraycopy( rayPoint, 0, result.mStartPoint, 0, 3 );

        TriangleIntersector checker = result.mIntersector;
        RayIntersection inter = result.mIntersection;
        double bestDist = Double.POSITIVE_INFINITY;

        for( Triangle t : mTris ) {
            if( checker.intersect( rayPoint, rayDir, t, inter ) ) {
                if( (inter.mTargetSide & side) == 0 || inter.mRayDist > bestDist ) {
                    continue;
                }

                bestDist = inter.mRayDist;
                result.mHasPick = true;
                result.mTriangle = t;
                result.mRayDistance = bestDist;

                System.arraycopy( inter.mPoint, 0, result.mStopPoint, 0, 3 );
            }
        }

        return result.mHasPick;
    }


    private static final class Result implements RayPickResult {

        final TriangleIntersector mIntersector = new TriangleIntersector();
        final RayIntersection mIntersection = new RayIntersection();

        private boolean mHasPick;
        private final double[] mStartPoint = new double[3];
        private final double[] mStopPoint = new double[3];
        private double mRayDistance;
        private Triangle mTriangle;
        
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
            return mRayDistance;
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
            return Side.NONE;
        }

        @Override
        public Object pickedData() {
            return null;
        }

    }

}
