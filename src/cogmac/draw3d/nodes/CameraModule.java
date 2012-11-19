package cogmac.draw3d.nodes;

import static javax.media.opengl.GL.GL_MODELVIEW;
import static javax.media.opengl.GL.GL_PROJECTION;

import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import cogmac.draw3d.context.RenderTile;
import cogmac.math3d.LongRect;
import cogmac.math3d.Matrices;
import cogmac.math3d.TransformPair;
import cogmac.math3d.actors.SpatialObject;
import cogmac.math3d.camera.CameraTransform;


/**
 * @author decamp
 */
public class CameraModule implements RenderModule {

    
    public static CameraModule newInstance(CameraTransform transform) {
        return new CameraModule(transform);
    }

    
    private final CameraTransform mTransform;
    private final Map<RenderTile,DrawHandler> mNodeMap = new LinkedHashMap<RenderTile,DrawHandler>();
    
    private final TransformPair mBaseProjPair  = new TransformPair();
    private final TransformPair mBaseModelTransform = new TransformPair();
    
    private boolean mUpdateOnReshape = true;
    private boolean mUpdateOnDraw    = true;
    
    
    
    private CameraModule(CameraTransform transform) {
        mTransform = transform;
    }
    
    
    
    public SpatialObject getCameraObject() {
        return mTransform.getCameraObject();
    }    

    
    public CameraTransform getCameraTransform() {
        return mTransform;
    }

    

    /**
     * @return true iff this updates modelview matrix automatically on draw events.
     */
    public boolean getUpdateModelviewOnDraw() {
        return mUpdateOnDraw;
    }

    /**
     * Specify whether the modelview transform should be updated
     * on draw events.  Default is <code>true</code>
     * 
     * @param updateOnDraw
     */
    public void setUpdateModelviewOnDraw(boolean updateOnPush) {
        mUpdateOnDraw = updateOnPush;
    }

    /**
     * @return true iff this updates projection matrix automatically on reshape events.
     */
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
     * Returns a view of the projection transform for the entire render space
     * of the camera.  The returned TransformPair SHOULD NOT be modified in 
     * any way; results are undefined.
     * 
     * @return a view of the projection transform
     */
    public TransformPair getProjectionTransform() {
        return mBaseProjPair;
    }
    
    /**
     * Returns a view of the projection transform for a given RenderTile.
     * The returned TransformPair SHOULD NOT be modified in 
     * any way; results are undefined.
     *
     * @param tile  context for which projection transform is retrieved.
     * @return a view of the projection transform
     */
    public TransformPair getProjectionTransform(RenderTile tile) {
        DrawHandler handler = mNodeMap.get(tile);
        if(handler == null) 
            return null;
        
        return handler.mProjTransform;
    }

    /**
     * Returns a view of the modelview transform for the entire RenderSpace
     * of the camera.  The returned TransformPair SHOULD NOT be modified in 
     * any way; results are undefined.
     * 
     * @return a view of the modelview transform
     */
    public TransformPair getModelviewTransform() {
        return mBaseModelTransform;
    }
    
    /**
     * Returns a view of the modelview transform for the entire RenderSpace
     * of the camera.  The returned TransformPair SHOULD NOT be modified in 
     * any way; results are undefined.
     * 
     * @param tile  context for which modelview transform is retrieved.
     * @return a view of the modelview transform
     */
    public TransformPair getModelviewTransform(RenderTile tile) {
        return mBaseModelTransform;
    }
    
    /**
     * Updates the current projection transform.
     */
    public void updateProjectionTransform(LongRect viewport) {
        double[] mat = mBaseProjPair.getTransformRef();
        mTransform.computeCameraToNormDeviceMatrix(viewport, null, mat);
        mBaseProjPair.setTransformRef(mat);
    }

    /**
     * Updates the modelview transform according to the 
     * underlying CameraTransform and current positioning of
     * the camera object.
     */
    public void updateModelviewTransform(LongRect viewport) {
        double[] mat = mBaseModelTransform.getTransformRef();
        mTransform.computeModelToCameraMatrix(viewport, null, mat);
        mBaseModelTransform.setTransformRef(mat);
        
        for(DrawHandler d: mNodeMap.values()) {
            d.updateTileProjection(viewport);
        }
    }

    
    public Object getCameraNode(RenderTile tile, boolean createIfAbsent) {
        synchronized(this) {
            DrawHandler ret = mNodeMap.get(tile);
            if(ret != null || !createIfAbsent)
                return ret;
            
            ret = new DrawHandler(tile);
            ret.updateTileProjection(tile.renderSpaceBounds());
            mNodeMap.put(tile, ret);
            return ret;
        }
    }
    
    
    public Object getNodes(Class<?> nodeClass, RenderTile tile) {
        if(nodeClass == DrawNode.class) {
            return getCameraNode(tile, true);
        }
        
        return null;
    }

    
    
    
    private final class DrawHandler extends DrawNodeAdapter {
        
        final RenderTile mTile;
        final boolean mFirst;
        
        final TransformPair mProjTransform  = new TransformPair();
        
        
        DrawHandler(RenderTile tile) {
            mTile = tile;
            mFirst = tile.isFirst();
        }
        
        
        
        void updateTileProjection(LongRect viewport) {
            LongRect b = mTile.tileBounds();
            double[] trans = new double[16];
            double[] scale = new double[16];
            double[] comp  = new double[16];
            
            double tx = -(b.centerX() - viewport.centerX()) * 2.0 / viewport.spanX();
            double ty =  (b.centerY() - viewport.centerY()) * 2.0 / viewport.spanY();
            double sx = (double)viewport.spanX() / b.spanX();
            double sy = (double)viewport.spanY() / b.spanY();
            
            Matrices.computeTranslationMatrix(tx, ty, 0.0, trans);
            Matrices.computeScaleMatrix(sx, sy, 1.0, scale);
            Matrices.multMatMat(scale, trans, comp);
            
            double[] out = mProjTransform.getTransformRef();
            Matrices.multMatMat(comp, mBaseProjPair.getTransformRef(), out);
            mProjTransform.setTransformRef(out);
        }
        
        
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {
            if(mUpdateOnReshape) {
                updateProjectionTransform(mTile.renderSpaceBounds());
            }
        }
        
        
        public void pushDraw(GL gl) {
            if(mFirst && mUpdateOnDraw) {
                updateModelviewTransform(mTile.renderSpaceBounds());
            }
            
            gl.glMatrixMode(GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glMultMatrixd(mProjTransform.getTransformRef(), 0);
            
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glMultMatrixd(mBaseModelTransform.getTransformRef(), 0);
        }
        
        
        public void popDraw(GL gl) {
            gl.glMatrixMode(GL_PROJECTION);
            gl.glPopMatrix();
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glPopMatrix();
        }
       
    }        
  
    
    
}


