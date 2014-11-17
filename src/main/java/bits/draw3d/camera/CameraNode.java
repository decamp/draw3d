/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.*;
import bits.draw3d.actors.Actor;
import bits.math3d.*;


/**
 * @author decamp
 */
public class CameraNode implements DrawNode, ReshapeListener {

    public static final int TRIGGER_RESHAPE = 1 << 0;
    public static final int TRIGGER_DRAW    = 1 << 1;

    public final Actor mCamera;

    public final Vec3 mCamPos    = new Vec3();
    public final Mat3 mCamRot    = new Mat3();
    public final Mat3 mCamRotInv = new Mat3();

    public final Mat4 mViewMat        = new Mat4();
    public final Mat4 mInvViewMat     = new Mat4();
    public final Mat4 mProjMat        = new Mat4();
    public final Mat4 mInvProjMat     = new Mat4();
    public final Mat4 mProjViewMat    = new Mat4();
    public final Mat4 mInvProjViewMat = new Mat4();
    public final Mat4 mViewportMat    = new Mat4();
    public final Mat4 mInvViewportMat = new Mat4();

    private final double[][] mRevert = new double[2][16];

    private final Rect mViewport             = new Rect( 0, 0, 1, 1 );
    private final Rect mTileViewport         = new Rect( 0, 0, 1, 1 );
    private       Rect mOverrideViewport     = null;
    private       Rect mOverrideTileViewport = null;

    private ViewFunc       mViewFunc;
    private ProjectionFunc mProjFunc;
    private ViewportFunc   mViewportFunc;

    private int mViewTriggers     = TRIGGER_RESHAPE | TRIGGER_DRAW;
    private int mProjTriggers     = TRIGGER_RESHAPE;
    private int mViewportTriggers = TRIGGER_RESHAPE;


    public CameraNode() {
        this( null, null, null, null );
    }


    public CameraNode( Actor optCamera ) {
        this( optCamera, null, null, null );
    }


    public CameraNode( Actor optCamera,
                       ViewFunc optViewFunc,
                       ProjectionFunc optProjFunc,
                       ViewportFunc optViewportFunc )
    {
        mCamera = optCamera != null ? optCamera : new Actor();
        mViewFunc     = optViewFunc     != null ? optViewFunc     : new BasicViewFunc();
        mProjFunc     = optProjFunc     != null ? optProjFunc     : new FovFunc();
        mViewportFunc = optViewportFunc != null ? optViewportFunc : new BasicViewportFunc();
    }
    
    
    
    public Actor camera() {
        return mCamera;
    }    
    

    public ViewFunc viewFunc() {
        return mViewFunc;
    }

    
    public void viewFunc( ViewFunc func ) {
        mViewFunc = func;
    }
    
    
    public ProjectionFunc projectionFunc() {
        return mProjFunc;
    }
    
    
    public void projectionFunc( ProjectionFunc func ) {
        mProjFunc = func;
    }
    
    
    public ViewportFunc viewportFunc() {
        return mViewportFunc;
    }
    
    
    public void viewportFunc( ViewportFunc func ) {
        mViewportFunc = func;
    }
    
    
    public float nearPlane() {
        return mProjFunc.nearPlane();
    }
    
    
    public void nearPlane( float dist ) {
        mProjFunc.nearPlane( dist );
    }
    
    
    public float farPlane() {
        return mProjFunc.farPlane();
    }
    
    
    public void farPlane( float dist ) {
        mProjFunc.farPlane( dist );
    }
    
    
    public float fov() {
        if( mProjFunc instanceof FovFunc ) {
            return ((FovFunc)mProjFunc).fov();
        } else {
            return 0f;
        }
    }
    
    
    public void fov( float fov ) {
        if( mProjFunc instanceof FovFunc ) {
           ((FovFunc)mProjFunc).fov( fov );
        } 
    }
    
    
    
    
    public int viewUpdateTriggers() {
        return mViewTriggers;
    }
    
    
    public void viewUpdateTriggers( int triggers ) {
        mViewTriggers = triggers;
    }
    

    public int projectionUpdateTriggers() {
        return mProjTriggers;
    }
    
    
    public void projUpdateTriggers( int triggers ) {
        mProjTriggers = triggers;
    }
    
    
    public int viewportUpdateTriggers() {
        return mViewTriggers;
    }
    
    
    public void viewportUpdateTriggers( int triggers ) {
        mViewTriggers = triggers;
    }
    
    /**
     * Forces update of current view transform.
     */
    public synchronized void updateViewMat() {
        doUpdateView();
        doUpdateComposites();
    }
    
    /**
     * Forces update of current projection transform.
     */
    public synchronized void updateProjectionMat() {
        doUpdateProj();
        doUpdateComposites();
    }

    /**
     * Forces update of current viewport transform.
     */
    public synchronized void updateViewportMat() {
        doUpdateViewport();
        doUpdateComposites();
    }
    
    /**
     * @return Current viewport for entire space. 
     *         The array that is returned IS NOT a safe copy and MUST NOT be modified.
     */
    public synchronized Rect viewportRef() {
        if( mOverrideViewport != null ) {
            return mOverrideViewport;
        }
        return mViewport;
    }
    
    /**
     * @return Current viewport for tile of space.
     *         The array that is returned IS NOT a safe copy and MUST NOT be modified.
     * 
     */
    public synchronized Rect tileViewportRef() {
        if( mOverrideTileViewport != null ) {
            return mOverrideTileViewport;
        }
        return viewportRef();
    }
        


    public Rect overrideViewport() {
        return mOverrideViewport;
    }
    
    
    public void overrideViewport( Rect box2 ) {
        if( box2 == null ) {
            mOverrideViewport = null;
        } else {
            if( mOverrideViewport == null ) {
                mOverrideViewport = new Rect();
            }
            mOverrideViewport.set( box2 );
        }

        doUpdateViewport();
        doUpdateProj();
        doUpdateComposites();
    }
    
    
    public Rect overrideTileViewport() {
        return mOverrideTileViewport;
    }
    
    
    public void overrideTileViewport( Rect box2 ) {
        if( box2 == null ) {
            mOverrideTileViewport = null;
        } else { 
            if( mOverrideTileViewport == null ) {
                mOverrideTileViewport = new Rect();
            }
            mOverrideTileViewport.set( box2 );
        }
        doUpdateViewport();
        doUpdateProj();
        doUpdateComposites();
    }




    public void init( DrawEnv env ) {}


    public void reshape( DrawEnv env ) {
        mViewport.set( env.mContextViewport );
        triggerUpdates( TRIGGER_RESHAPE );
    }


    public void dispose( DrawEnv d ) {}


    public void pushDraw( DrawEnv e ) {
        triggerUpdates( TRIGGER_DRAW );
        e.mProj.push();
        e.mProj.set( mProjMat );
        e.mView.push();
        e.mView.set( mViewMat );
    }


    public void popDraw( DrawEnv e ) {
        e.mView.pop();
        e.mProj.pop();
    }




    private void triggerUpdates( int triggers ) {
        boolean updated = false;

        if( ( mViewTriggers & triggers ) != 0 ) {
            doUpdateView();
            updated = true;
        }

        if( ( mProjTriggers & triggers ) != 0 ) {
            doUpdateProj();
            updated = true;
        }

        if( ( mViewportTriggers & triggers ) != 0 ) {
            doUpdateViewport();
            updated = true;
        }

        if( updated ) {
            doUpdateComposites();
        }
    }


    private void doUpdateView() {
        Vec.put( mCamera.mPos, mCamPos );
        Mat.put( mCamera.mRot, mCamRot );
        mViewFunc.computeViewMat( mCamera, mViewMat );
        Mat.invert( mViewMat, mInvViewMat );
        Mat.invert( mCamRot, mCamRotInv );
    }


    private void doUpdateProj() {
        Rect viewport = viewportRef();
        Rect tile     = tileViewportRef();
        if( tile == viewport ) {
            tile = null;
        }
        mProjFunc.computeProjectionMat( viewport, tile, mProjMat );
        Mat.invert( mProjMat, mInvProjMat );
    }


    private void doUpdateViewport() {
        Rect viewport = viewportRef();
        Rect tile     = tileViewportRef();
        if( tile == viewport ) {
            tile = null;
        }
        mViewportFunc.computeViewportMat( viewportRef(), tileViewportRef(), mViewportMat );
        Mat.invert( mViewportMat, mInvViewportMat );
    }


    private void doUpdateComposites() {
        Mat.mult( mProjMat, mViewMat, mProjViewMat );
        Mat.mult( mInvViewMat, mInvProjMat, mInvProjViewMat );
    }

}
