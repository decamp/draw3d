/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

import bits.draw3d.model.DrawTri;
import bits.math3d.Vec;
import bits.math3d.Vec3;

import java.util.*;


/**
 * This is a refernce implemention of GeometryPicker that uses a simple brute
 * force algorithm. It should be used for testing purposes only.
 *
 * @author decamp
 */
public class BruteForcePicker implements RayPicker {


    public static BruteForcePicker build( List<DrawTri> trisRef ) {
        return new BruteForcePicker( trisRef );
    }


    private final List<DrawTri> mTris;


    private BruteForcePicker( List<DrawTri> tris ) {
        mTris = tris;
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
        Vec.put( rayPointVec, result.mStartPoint );

        TriangleIntersector checker = result.mIntersector;
        RayIntersection inter = result.mIntersection;
        float bestDist = Float.POSITIVE_INFINITY;

        for( DrawTri t : mTris ) {
            if( checker.intersect( rayPoint, rayDir, t, inter ) ) {
                if( (inter.mTargetSide & side) == 0 || inter.mRayDist > bestDist ) {
                    continue;
                }

                bestDist = inter.mRayDist;
                result.mHasPick = true;
                result.mTriangle = t;
                result.mRayDistance = bestDist;

                Vec.put( inter.mPoint, result.mStopPoint );
            }
        }

        return result.mHasPick;
    }


    private static final class Result implements RayPickResult {

        final TriangleIntersector mIntersector = new TriangleIntersector();
        final RayIntersection mIntersection = new RayIntersection();

        private boolean mHasPick;
        private final Vec3 mStartPoint = new Vec3();
        private final Vec3 mStopPoint  = new Vec3();
        private float mRayDistance;
        private DrawTri mTriangle;

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
            return mRayDistance;
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
            return Side.NONE;
        }

        @Override
        public Object pickedData() {
            return null;
        }

    }

}
