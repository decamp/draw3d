package bits.draw3d.nodes;

import javax.media.opengl.GL;

import bits.math3d.actors.*;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class ActorNode extends DrawNodeAdapter {

    
    public static ActorNode newInstance( SpatialObject spatial, double[] baseTransform ) {
        return new ActorNode( spatial, baseTransform );
    }
    
    
    private final SpatialObject mSpatial;
    private final double[] mBaseTransform;
    private final double[] mWork = new double[16];
    
    
    private ActorNode( SpatialObject spatial, double[] baseTransform ) {
        mSpatial       = spatial != null ? spatial : new SpatialObject();
        mBaseTransform = baseTransform != null ? baseTransform.clone() : null;
    }    
    
    
    
    public SpatialObject actor() {
        return mSpatial;
    }
    
    
    @Override
    public void pushDraw(GL gl) {
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPushMatrix();
        
        //Set modelview to actor object.
        mSpatial.computeTransform( mWork );
        gl.glMultMatrixd( mWork, 0 );
        
        if( mBaseTransform != null ) {
            gl.glMultMatrixd( mBaseTransform, 0 );
        }
    }
    
    
    @Override
    public void popDraw(GL gl) {
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glPopMatrix();
    }
    
}
