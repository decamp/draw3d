/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.actors;


import bits.math3d.*;


/**
 * For an actor: <br/>
 * Positive x-axis is considered forward, <br/>
 * Positive y-axis is left <br/>
 * Positive z-axis is up. <br/> 
 * 
 * @author decamp
 */
@Deprecated
 public class VelocityActor extends Actor {

    private static final Vec3 FORWARD = ActorCoords.newForwardAxis();
    private static final Vec3 UP      = ActorCoords.newUpAxis();


    /**
     * Linear velocity as 3-vector.
     */
    public final Vec3 mVel = new Vec3( 0, 0, 0 );

    /**
     * Angular velocity as 3-vector.
     */
    public final Vec3 mAngVel = new Vec3( 0, 0, 0 );

    /**
     * Velocity at which the object is moving within
     * its own coordinate system.
     */
    public final Vec3 mMoveVel = new Vec3( 0, 0, 0 );

    protected final Vec3 mWorkVec0 = new Vec3();
    protected final Vec3 mWorkVec1 = new Vec3();

    private double   mTime     = 0.0;
    private MoveMode mMode     = MoveMode.FLY;
    private boolean  mRollLock = false;

    /**
     * Changes time without updating position/rotation.
     */
    public void resetTime( double time ) {
        mTime = time;
    }

    /**
     * Changes time, updating position and rotation according to translation and rotation speeds.
     */
    public void updateTime( double time ) {
        if( time == mTime ) {
            return;
        }
        
        float delta = (float)( time - mTime );
        translate( delta * mVel.x, delta * mVel.y, delta * mVel.z );
        rotate( delta * mAngVel.x, 1, 0, 0 );
        rotate( delta * mAngVel.y, 0, 1, 0 );
        rotate( delta * mAngVel.z, 0, 0, 1 );
        move( delta * mMoveVel.x, delta * mMoveVel.y, delta * mMoveVel.z );

        mTime = time;
    }

    /**
     * @return current time for this object
     */
    public double time() {
        return mTime;
    }
    
    /**
     * Moves the actor.  Unlike translate(), which moves the
     * actor in global coordinates, this method is
     * defined relative to the avatar's perspective, 
     * where the x-axis comes out the right of the actor,
     * the y-axis comes out the top, and the z-axis comes out 
     * the back.
     */
    public void move( float mx, float my, float mz ) {
        if( mx == 0 && my == 0 && mz == 0 ) {
            return;
        }
        
        if( mMode == MoveMode.FLY ) {
            moveFlying( mx, my, mz );
        }else{
            moveWalking( mx, my, mz );
        }
    }


    public MoveMode moveMode() {
        return mMode;
    }


    public void moveMode( MoveMode mode ) {
        mMode = mode;
    }


    public boolean rollLock() {
        return mRollLock;
    }


    public void rollLock( boolean rollLock ) {
        mRollLock = rollLock;
    }


    public void removeRoll() {
        Vec3 x = mWorkVec0;
        Vec3 y = mWorkVec1;

        Mat.mult( mRot, FORWARD, x );
        Vec.normalize( x );
        Vec.cross( UP, x, y );
        float len = Vec.len( y );

        // Check if looking nearly straight up or down.
        if( len < 0.01f ) {
            return;
        }
        Vec.mult( 1f / len, y );
        Mat.basisVecsToRotation( x, y, mRot );
    }
    
    /**
     * For flying mode, motion of the actor not restricted.
     */
    private void moveFlying( float dx, float dy, float dz ) {
        Vec3 v = mWorkVec0;
        v.x = dx;
        v.y = dy;
        v.z = dz;
        Mat.mult( mRot, v, v );
        translate( v.x, v.y, v.z );
    }
    
    /**
     * For walking mode, the motion along the actor's z-axis is
     * mapped directly to the global z-axis, while motion
     * along the actor's x-axis and y-axis is not counted
     * towards translation in the global z-axis.
     */
    private void moveWalking( float dx, float dy, float dz ) {
        Vec3 v = mWorkVec0;
        v.x = dx;
        v.y = dy;
        v.z = dz;
        Mat.mult( mRot, v, v );
        translate( v.x, v.y, v.z );
   }


}
