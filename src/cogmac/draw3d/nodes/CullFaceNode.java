package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public class CullFaceNode extends DrawNodeAdapter {
    
    
    private boolean mEnable;
    private int mFace;
    
    private boolean mRevEnable;
    private final int[] mRevFace = {GL_BACK};
    
    
    public CullFaceNode() {
        this(false, GL_BACK);
    }
    
    public CullFaceNode(boolean enable, int face) {
        mEnable = enable;
        mFace   = face;
    }
    
    
    
    public boolean enable() {
        return mEnable;
    }
    
    public CullFaceNode enable(boolean enable) {
        mEnable = enable;
        return this;
    }
    
    public int face() {
        return mFace;
    }
    
    public CullFaceNode face(int face) {
        mFace = face;
        return this;
    }
    
    
    @Override
    public void pushDraw(GL gl) {
        mRevEnable = gl.glIsEnabled(GL_CULL_FACE);
        gl.glGetIntegerv(GL_CULL_FACE_MODE, mRevFace, 0);
        
        if(mEnable) {
            gl.glEnable(GL_CULL_FACE);
        }else{
            gl.glDisable(GL_CULL_FACE);
        }
        
        gl.glCullFace(mFace);
    }
    
    @Override
    public void popDraw(GL gl) {
        if(mRevEnable) {
            gl.glEnable(GL_CULL_FACE);
        }else{
            gl.glDisable(GL_CULL_FACE);
        }
        
        gl.glCullFace(mRevFace[0]);
    }
    
    
    
}
