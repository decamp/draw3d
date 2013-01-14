package cogmac.draw3d.nodes;

import javax.media.opengl.*;

import cogmac.draw3d.context.RenderTile;
import cogmac.math3d.*;
import cogmac.math3d.actors.SpatialObject;
import cogmac.math3d.camera.CameraTransform;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class CameraNode implements DrawNode {

    
    public static CameraNode newInstance(CameraTransform transform) {
        return new CameraNode(transform, null);
    }

    
    public static CameraNode newInstance(CameraTransform transform, RenderTile tile) {
        return new CameraNode(transform, tile);
    }
    
    
    
    private final CameraTransform mTransform;
    private final RenderTile mTile;
    
    private final TransformPair mProjectionPair = new TransformPair();
    private final TransformPair mModelviewPair  = new TransformPair();
    
    private LongRect mBounds = LongRect.fromEdges(0, 0, 1, 1);
    

    private boolean mUpdateOnReshape = true;
    private boolean mUpdateOnDraw    = true;
    
    
    private CameraNode( CameraTransform transform, RenderTile tile ) {
        mTransform = transform;
        mTile      = tile;
    }
    
    
    
    public SpatialObject getCameraObject() {
        return mTransform.getCameraObject();
    }    

    
    public CameraTransform getCameraTransform() {
        return mTransform;
    }
    
    
    public boolean getUpdateModelviewOnDraw() {
        return mUpdateOnDraw;
    }

    /**
     * Specify whether the modelview transform should be updated
     * on draw events.  Default is <code>true</code>
     * 
     * @param updateOnDraw
     */
    public void setUpdateModelviewOnDraw( boolean updateOnPush ) {
        mUpdateOnDraw = updateOnPush;
    }

    
    public boolean getUpdateProjectionOnReshape() {
        return mUpdateOnReshape;
    }

    /**
     * Specify whether the projection transform should be updated on 
     * reshape events.  Default is <code>true</code>
     * 
     * @param updateOnReshape
     */
    public void setUpdateProjectionOnReshape(boolean updateOnReshape) {
        mUpdateOnReshape = updateOnReshape;
    }

    /**
     * Returns a view of the projection transform.  The returned
     * TransformPair SHOULD NOT be modified in any way; results
     * are undefined.
     * 
     * @return a view of the projection transform
     */
    public TransformPair getProjectionTransform() {
        return mProjectionPair;
    }
    
    /**
     * Returns a view of the modelview transform.  The returned
     * TransformPair SHOULD NOT be modified in any way; results
     * are undefined.
     * 
     * @return a view of the modelview transform
     */
    public TransformPair getModelviewTransform() {
        return mModelviewPair;
    }
    
    /**
     * Updates the current projection transform according to
     * the underlying CameraTranfsorm and current position of
     * the camera object.
     */
    public void updateProjectionTransform() {
        double[] mat = mProjectionPair.getTransformRef();
        
        if( mTile == null ) {
            mTransform.computeCameraToNormDeviceMatrix( mBounds, null, mat );
        }else{
            mTransform.computeCameraToNormDeviceMatrix( mTile.renderSpaceBounds(), mTile.tileBounds(), mat );
        }
        
        mProjectionPair.setTransformRef( mat );
    }

    /**
     * Updates the modelview transform according to the 
     * underlying CameraTransform and current positioning of
     * the camera object.
     */
    public void updateModelviewTransform() {
        double[] mat = mModelviewPair.getTransformRef();
        
        if(mTile == null) {
            mTransform.computeModelToCameraMatrix(mBounds, null, mat);
        }else{
            mTransform.computeModelToCameraMatrix(mTile.renderSpaceBounds(), mTile.tileBounds(), mat);
        }
        
        mModelviewPair.setTransformRef(mat);
    }

    
    public LongRect viewport() {
        return mTile == null ? mBounds : mTile.renderSpaceBounds();
    }

    
    public LongRect screenBounds() {
        return mBounds;
    }
    
    
    
    public void init(GLAutoDrawable gld) {}
    
    
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        mBounds = LongRect.fromBounds( x, y, w, h );
        
        if( mUpdateOnReshape ) {
            updateProjectionTransform();
        }
    }
    
    
    public void dispose( GLAutoDrawable gld ) {}
    
    
    public void pushDraw( GL gl ) {
        if( mUpdateOnDraw )
            updateModelviewTransform();
        
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMultMatrixd( mProjectionPair.getTransformRef(), 0 );
        
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMultMatrixd( mModelviewPair.getTransformRef(), 0 );
    }
    
    
    public void popDraw( GL gl ) {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPopMatrix();
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPopMatrix();
    }
    
    
}
