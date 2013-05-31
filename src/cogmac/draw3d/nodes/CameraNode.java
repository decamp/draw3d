package cogmac.draw3d.nodes;

import javax.media.opengl.*;

import cogmac.draw3d.context.RenderTile;
import cogmac.draw3d.nodes.DrawNode;
import cogmac.math3d.*;
import cogmac.math3d.actors.SpatialObject;
import cogmac.math3d.camera.*;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class CameraNode implements DrawNode {

    public static final int TRIGGER_RESHAPE = 1 << 0;
    public static final int TRIGGER_DRAW    = 1 << 1;
    
    private final RenderTile mTile;
    
    private final double[] mCamPos         = new double[3];
    private final double[] mCamRot         = new double[16];
    private final double[] mCamRotInv      = new double[16];
    
    private final double[] mViewMat        = new double[16];
    private final double[] mViewMatInv     = new double[16];
    private final double[] mProjMat        = new double[16];
    private final double[] mProjMatInv     = new double[16];
    private final double[] mViewportMat    = new double[16];
    private final double[] mViewportMatInv = new double[16];
    private final double[] mViewProjMat    = new double[16];
    private final double[] mViewProjMatInv = new double[16];
    
    private final double[][] mRevert = new double[2][16];

    private final int[] mViewport       = { 0, 0, 1, 1 };
    private final int[] mTileViewport   = { 0, 0, 1, 1 };
    private int[] mOverrideViewport     = null;
    private int[] mOverrideTileViewport = null;
    
    private SpatialObject  mCamera;
    private ViewFunc       mViewFunc;
    private ProjectionFunc mProjFunc;
    private ViewportFunc   mViewportFunc;
    
    private int mViewTriggers     = TRIGGER_RESHAPE | TRIGGER_DRAW;
    private int mProjTriggers     = TRIGGER_RESHAPE;
    private int mViewportTriggers = TRIGGER_RESHAPE;
    
    
    public CameraNode() {
        this( null, null, null, null, null );
    }
    
    
    public CameraNode( SpatialObject optCamera ) {
        this( optCamera, null, null, null, null );
    }
    
    
    public CameraNode( SpatialObject optCamera,
                       RenderTile optTile, 
                       ViewFunc optViewFunc,
                       ProjectionFunc optProjFunc,
                       ViewportFunc optViewportFunc )
    {
        mCamera       = optCamera       != null ? optCamera       : new SpatialObject();
        mTile         = optTile;
        mViewFunc     = optViewFunc     != null ? optViewFunc     : new BasicViewFunc();
        mProjFunc     = optProjFunc     != null ? optProjFunc     : new FovFunc();
        mViewportFunc = optViewportFunc != null ? optViewportFunc : new BasicViewportFunc();
    }
    
    
    
    public SpatialObject camera() {
        return mCamera;
    }    
    

    public double[] camPosRef() {
        return mCamPos;
    }
    
    
    public double[] camRotRef() {
        return mCamRot;
    }

    
    public double[] camRotInvRef() {
        return mCamRotInv;
    }
    
    
    public double[] viewMatRef() {
        return mViewMat;
    }
    
    
    public double[] viewMatInvRef() {
        return mViewMatInv;
    }

    
    public double[] projectionMatRef() {
        return mProjMat;
    }
    
    
    public double[] projectionMatInvRef() {
        return mProjMatInv;
    }
    
    
    public double[] viewProjectionMatRef() {
        return mViewProjMat;
    }
    
    
    public double[] viewProjectionMatInvRef() {
        return mViewProjMatInv;
    }
    
    
    public double[] viewportMatRef() {
        return mViewportMat;
    }
    
    
    public double[] viewportMatInvRef() {
        return mViewportMatInv;
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
    public synchronized int[] viewportRef() {
        if( mOverrideViewport != null ) {
            return mOverrideViewport;
        }
        if( mTile != null ) {
            LongRect r = mTile.renderSpaceBounds();
            mViewport[0] = (int)r.minX();
            mViewport[1] = (int)r.minY();
            mViewport[2] = (int)r.maxX();
            mViewport[3] = (int)r.maxY();
        }
        
        return mViewport;
    }
    
    /**
     * @return Current viewport for tile of space.
     *         The array that is returned IS NOT a safe copy and MUST NOT be modified.
     * 
     */
    public synchronized int[] tileViewportRef() {
        if( mOverrideTileViewport != null ) {
            return mOverrideTileViewport;
        }
        if( mTile == null ) {
            return viewportRef();
        }
        
        LongRect r = mTile.tileBounds();
        mTileViewport[0] = (int)r.minX();
        mTileViewport[1] = (int)r.minY();
        mTileViewport[2] = (int)r.maxX();
        mTileViewport[3] = (int)r.maxY();
        return mTileViewport;
    }
        

    
    
    public int[] overrideViewport() {
        return mOverrideViewport;
    }
    
    
    public void overrideViewport( int[] box2 ) {
        if( box2 == null ) {
            mOverrideViewport = null;
        } else { 
            if( mOverrideViewport == null ) {
                mOverrideViewport = new int[4];
            }
            System.arraycopy( box2, 0, mOverrideViewport, 0, 4 );
        }
        
        doUpdateViewport();
        doUpdateComposites();
    }
    
    
    public int[] overrideTileViewport() {
        return mOverrideTileViewport;
    }
    
    
    public void overrideTileViewport( int[] box2 ) {
        if( box2 == null ) {
            mOverrideTileViewport = null;
        } else { 
            if( mOverrideTileViewport == null ) {
                mOverrideTileViewport = new int[4];
            }
            System.arraycopy( box2, 0, mOverrideTileViewport, 0, 4 );
        }

        doUpdateViewport();
        doUpdateComposites();
    }
    
    
    
    
    public void init(GLAutoDrawable gld) {}
    
    
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        mViewport[0] = x;
        mViewport[1] = y;
        mViewport[2] = x + w;
        mViewport[3] = y + h;
        triggerUpdates( TRIGGER_RESHAPE );
    }
    
    
    public void dispose( GLAutoDrawable gld ) {}
    
    
    public void pushDraw( GL gl ) {
        triggerUpdates( TRIGGER_DRAW );
        
        gl.glGetDoublev( GL_MODELVIEW_MATRIX, mRevert[0], 0 );
        gl.glGetDoublev( GL_PROJECTION_MATRIX, mRevert[1], 0 );
        
        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadMatrixd( mProjMat, 0 );
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadMatrixd( mViewMat, 0 );
    }
    
    
    public void popDraw( GL gl ) {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadMatrixd( mRevert[1], 0 );
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadMatrixd( mRevert[0], 0 );
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
        System.arraycopy( mCamera.mPos, 0, mCamPos, 0, 3 );
        System.arraycopy( mCamera.mRot, 0, mCamRot, 0, 16 );
        mViewFunc.computeViewMat( mCamera, mViewMat );
        Matrices.invert( mViewMat, mViewMatInv );
        Matrices.invert( mCamRot, mCamRotInv );
    }
    
    
    private void doUpdateProj() {
        int[] viewport = viewportRef();
        int[] tile     = tileViewportRef();
        if( tile == viewport ) {
            tile = null;
        }
        mProjFunc.computeProjectionMat( viewport, tile, mProjMat );
        Matrices.invert( mProjMat, mProjMatInv );
    }
    
    
    private void doUpdateViewport() {
        int[] viewport = viewportRef();
        int[] tile     = tileViewportRef();
        if( tile == viewport ) {
            tile = null;
        }
        mViewportFunc.computeViewportMat( viewportRef(), tileViewportRef(), mViewportMat );
        Matrices.invert( mViewportMat, mViewportMatInv );
    }
    
    
    private void doUpdateComposites() {
        Matrices.multMatMat( mProjMat, mViewMat, mViewProjMat );
        Matrices.multMatMat( mViewMatInv, mProjMatInv, mViewProjMatInv );
    }
    
}
