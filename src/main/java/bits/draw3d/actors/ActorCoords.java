/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.actors;

import bits.math3d.*;


/**
 * @author decamp
 */
public class ActorCoords {

    private static final Mat3 AXIS_MAT = new Mat3(  0,  0, -1,
                                                   -1,  0,  0,
                                                    0,  1,  0 );

    private static final Mat3 INV_AXIS_MAT = new Mat3();

    static {
        Mat.invert( AXIS_MAT, INV_AXIS_MAT );
    }


    /**
     * Gets the homographic transform for converting actor coordinates to gaze/view coordinates.
     * IE, where x is right, y is up, and -z is forward.
     */
    public static void actorToViewMat( Mat4 out ) {
        Mat.put( AXIS_MAT, out );
    }
    
    /**
     * Gets the homographic transform for converting actor coordinates to view coordinates.
     * IE, where x is right, y is up, and -z is forward.
     */
    public static Mat3 newActorToViewMat() {
        return new Mat3( AXIS_MAT );
    }
 
    /**
     * Gets the homographic transform for converting view coordinates to actor coordinates.
     */
    public static void viewToActorMat( Mat4 out ) {
        Mat.put( INV_AXIS_MAT, out );
    }

    /**
     * Gets the homographic transform for converting GL coordinates to actor coordinates. 
     * @return new 4x4 matrix
     */
    public static Mat3 newViewToActorMat() {
        return new Mat3( INV_AXIS_MAT );
    }

    
    public static void forwardAxis( Vec3 out ) {
        out.x = 1;
        out.y = 0;
        out.z = 0;
    }

    public static void backwardAxis( Vec3 out ) {
        out.x = -1;
        out.y =  0;
        out.z =  0;
    }

    public static void leftAxis( Vec3 out ) {
        out.x = 0;
        out.y = 1;
        out.z = 0;
    }

    public static void rightAxis( Vec3 out ) {
        out.x = 0;
        out.y = -1;
        out.z = 0;
    }

    public static void upAxis( Vec3 out ) {
        out.x = 0;
        out.y = 0;
        out.z = 1;
    }

    public static void downAxis( Vec3 out ) {
        out.x = 0;
        out.y = 0;
        out.z = -1;
    }
    
    public static Vec3 newForwardAxis() {
        return new Vec3( 1, 0, 0 );
    }
    
    public static Vec3 newBackwardAxis() {
        return new Vec3( -1, 0, 0 );
    }
    
    public static Vec3 newLeftAxis() {
        return new Vec3( 0, 1, 0 );
    }
    
    public static Vec3 newRightAxis() {
        return new Vec3( 0, -1, 0 );
    }
    
    public static Vec3 newUpAxis() {
        return new Vec3( 0, 0, 1 );
    }
    
    public static Vec3 newDownAxis() {
        return new Vec3( 0, 0, -1 );
    }


}
