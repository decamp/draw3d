package cogmac.draw3d.nodes;

import javax.media.opengl.*;

import cogmac.draw3d.context.RenderTile;
import cogmac.draw3d.nodes.DrawNode;
import cogmac.math3d.*;
import cogmac.math3d.actors.SpatialObject;
import cogmac.math3d.camera.CameraTransform;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class CameraNode implements DrawNode {

    
    private final CameraTransform mTransform;
    private final RenderTile mTile;
    
    private final TransformPair mProjectionPair = new TransformPair();
    private final TransformPair mModelviewPair  = new TransformPair();
    private final double[][] mRevert = new double[2][16];

    private LongRect mViewport             = LongRect.fromBounds( 0, 0, 1, 1 );
    private LongRect mOverrideViewport     = null;
    private LongRect mOverrideTileViewport = null;
    
    private boolean mUpdateOnReshape = true;
    private boolean mUpdateOnDraw    = true;
    
    
    public CameraNode( CameraTransform transform, RenderTile tile ) {
        mTransform = transform;
        mTile      = tile;
    }
    
    
    
    public SpatialObject camera() {
        return mTransform.getCameraObject();
    }    

    
    public CameraTransform cameraTransform() {
        return mTransform;
    }
    
    
    
    public boolean updateModelviewOnDraw() {
        return mUpdateOnDraw;
    }

    /**
     * Specify whether the modelview transform should be updated
     * on draw events.  Default is <code>true</code>
     * 
     * @param updateOnDraw
     */
    public void updateModelviewOnDraw( boolean updateOnPush ) {
        mUpdateOnDraw = updateOnPush;
    }

    
    public boolean updateProjectionOnReshape() {
        return mUpdateOnReshape;
    }

    /**
     * Specify whether the projection transform should be updated on 
     * reshape events.  Default is <code>true</code>
     * 
     * @param updateOnReshape
     */
    public void updateProjectionOnReshape(boolean updateOnReshape) {
        mUpdateOnReshape = updateOnReshape;
    }

    /**
     * Returns a view of the projection transform.  The returned
     * TransformPair SHOULD NOT be modified in any way; results
     * are undefined.
     * 
     * @return a view of the projection transform
     */
    public TransformPair projectionTransform() {
        return mProjectionPair;
    }
    
    /**
     * Returns a view of the modelview transform.  The returned
     * TransformPair SHOULD NOT be modified in any way; results
     * are undefined.
     * 
     * @return a view of the modelview transform
     */
    public TransformPair modelviewTransform() {
        return mModelviewPair;
    }
    
    /**
     * Updates the current projection transform according to
     * the underlying CameraTranfsorm and current position of
     * the camera object.
     */
    public synchronized void updateProjectionTransform() {
        double[] mat = mProjectionPair.getTransformRef();
        
        mTransform.computeCameraToNormDeviceMatrix( viewport(), tileViewport(), mat );
        mProjectionPair.setTransformRef( mat );
    }

    /**
     * Updates the modelview transform according to the 
     * underlying CameraTransform and current positioning of
     * the camera object.
     */
    public synchronized void updateModelviewTransform() {
        double[] mat = mModelviewPair.getTransformRef();
        mTransform.computeModelToCameraMatrix( viewport(), tileViewport(), mat );
        mModelviewPair.setTransformRef(mat);
    }

    /**
     * @return Current bounds for entire space.
     */
    public synchronized LongRect viewport() {
        return mOverrideViewport != null ? mOverrideViewport :
               mTile != null ? mTile.renderSpaceBounds() :
               mViewport;
    }
    
    /**
     * @return Current viewport.
     */
    public synchronized LongRect tileViewport() {
        return mOverrideTileViewport != null ? mOverrideTileViewport :
               mTile != null ? mTile.tileBounds() :
               mViewport;
    }
        

    public LongRect overrideViewport() {
        return mOverrideViewport;
    }
    
    
    public void overrideViewport( LongRect viewport ) {
        if( viewport == mOverrideViewport )
            return;
        
        mOverrideViewport = viewport;
        updateProjectionTransform();
    }
    
    
    public LongRect overrideTileViewport() {
        return mOverrideTileViewport;
    }
    
    
    public void overrideTileViewport( LongRect tileViewport ) {
        if( tileViewport == mOverrideTileViewport )
            return;
        
        mOverrideTileViewport = tileViewport;
        updateProjectionTransform();
    }

    
    
    public void init(GLAutoDrawable gld) {}
    
    
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        mViewport = LongRect.fromBounds( x, y, w, h );
        
        if( mUpdateOnReshape ) {
            updateProjectionTransform();
        }
    }
    
    
    public void dispose( GLAutoDrawable gld ) {}
    
    
    public void pushDraw( GL gl ) {
        if( mUpdateOnDraw )
            updateModelviewTransform();
        
        gl.glGetDoublev( GL_MODELVIEW_MATRIX, mRevert[0], 0 );
        gl.glGetDoublev( GL_PROJECTION_MATRIX, mRevert[1], 0 );
        
        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadMatrixd( mProjectionPair.getTransformRef(), 0 );
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadMatrixd( mModelviewPair.getTransformRef(), 0 );
    }
    
    
    public void popDraw( GL gl ) {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glLoadMatrixd( mRevert[1], 0 );
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glLoadMatrixd( mRevert[0], 0 );
    }
    
    
    
    /**
     * @deprecated Use <code>camera()</code>
     */
    public SpatialObject getCameraObject() {
        return mTransform.getCameraObject();
    }    

    /**
     * @deprecated Use <code>transform()</code>.
     */
    public CameraTransform getCameraTransform() {
        return mTransform;
    }
    
    /**
     * @deprecated Use <code>updateModelviewOnDraw()</code>
     */
    public boolean getUpdateModelviewOnDraw() {
        return mUpdateOnDraw;
    }

    /**
     * @deprecated Use <code>updateModelviewOnDraw()</code>
     */
    public void setUpdateModelviewOnDraw( boolean updateOnPush ) {
        mUpdateOnDraw = updateOnPush;
    }
    
    /**
     * @deprecated Use <code>updateProjectionOnReshape()</code>
     */
    public boolean getUpdateProjectionOnReshape() {
        return mUpdateOnReshape;
    }

    /**
     * @deprecated Use <code>updateProjectionOnReshape()</code>
     */
    public void setUpdateProjectionOnReshape(boolean updateOnReshape) {
        mUpdateOnReshape = updateOnReshape;
    }

    /**
     * @deprecated Use <code>projectionTransform()</code>
     */
    public TransformPair getProjectionTransform() {
        return mProjectionPair;
    }
    
    /**
     * @deprecated Use <code>modelviewTransform()</code>
     */
    public TransformPair getModelviewTransform() {
        return mModelviewPair;
    }
    
    
}
