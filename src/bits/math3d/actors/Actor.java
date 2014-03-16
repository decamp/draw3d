package bits.math3d.actors;

import bits.math3d.*;


/**
 * For an actor: <br/>
 * Positive x-axis is considered forward, <br/>
 * Positive y-axis is left <br/>
 * Positive z-axis is up. <br/> 
 * 
 * @author decamp
 */
public class Actor extends SpatialObject {
    
    /**
     * Linear velocity as 3-vector.
     */
    public final double[] mVel = {0,0,0};
    
    /**
     * Angular velocity as 3-vector.
     */
    public final double[] mAngVel = {0,0,0};

    /**
     * Velocity at which the object is moving within
     * its own coordinate system.
     */
    public double[] mMoveVel = {0,0,0};
    
    private double mTime       = 0.0;
    private MoveMode mMode     = MoveMode.FLY;
    private boolean mRollLock  = false;
    
    
    /**
     * Changes time without updating position/rotation.
     *  
     * @param time
     */
    public void resetTime( double time ) {
        mTime = time;
    }
    
    /**
     * Changes time, updating position and rotation according to translation and rotation speeds.
     * 
     * @param time
     */
    public void updateTime( double time ) {
        if( time == mTime ) {
            return;
        }
        
        double delta = time - mTime;
        
        translate( delta * mVel[0], delta * mVel[1], delta * mVel[2] );
        rotate( delta * mAngVel[0], 1.0, 0.0, 0.0 ); 
        rotate( delta * mAngVel[1], 0.0, 1.0, 0.0 );
        rotate( delta * mAngVel[2], 0.0, 0.0, 1.0 );
        
        double mx = mMoveVel[0] * delta;
        double my = mMoveVel[1] * delta;
        double mz = mMoveVel[2] * delta;
        move( mx, my, mz );
        
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
     * 
     * @param mx
     * @param my
     * @param mz
     */
    public void move( double mx, double my, double mz ) {
        if( mx == 0.0 && my == 0.0 && mz == 0.0 ) {
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
        if( rollLock == mRollLock )
            return;
        
        mRollLock = rollLock;
    }
    
    public void removeRoll() {
        double[] x = mRot;
        double[] y = mWork[0];
        double[] z = mWork[1];
        
        // Compute x-axis
        Matrices.multMatVec( mRot, FORWARD, x );
        Vectors.normalize( x, 1.0 );
        
        // Compute y-axis.
        Vectors.cross( UP, x, y );
        double len = Vectors.hypot( y );
        
        // Check if looking nearly straight up or down.
        if( len < 0.01 ) {
            return;
        }
        
        Vectors.scale( y, 1.0 / len );
        // Compute z-axis
        Vectors.cross( x, y, z );
        
        x[ 3] = 0;
        x[ 4] = y[0];
        x[ 5] = y[1];
        x[ 6] = y[2];
        x[ 7] = 0;
        x[ 8] = z[0];
        x[ 9] = z[1];
        x[10] = z[2];
        x[11] = 0;
        x[12] = 0;
        x[13] = 0;
        x[14] = 0;
        x[15] = 1;
    }
    
    /**
     * For flying mode, motion of the actor not restricted.
     */
    private void moveFlying( double dx, double dy, double dz ) {
        double[] w0 = mWork[0];
        double[] w1 = mWork[1];
        w0[0] = dx;
        w0[1] = dy;
        w0[2] = dz;
        
        Matrices.multMatVec( mRot, w0, w1 );
        translate( w1[0], w1[1], w1[2] );
    }
    
    /**
     * For walking mode, the motion along the actor's z-axis is
     * mapped directly to the global z-axis, while motion
     * along the actor's x-axis and y-axis is not counted
     * towards translation in the global z-axis.
     */
    private void moveWalking( double dx, double dy, double dz ) {
        double[] w0 = mWork[0];
        double[] w1 = mWork[1];
        w0[0] = dx;
        w0[1] = dy;
        w0[2] = 0.0;
        
        Matrices.multMatVec( mRot, w0, w1 );
        translate( w1[0], w1[1], dz );
    }
    

    private static final double[] FORWARD = ActorCoords.newForwardAxis();
    private static final double[] UP      = ActorCoords.newUpAxis();

}
