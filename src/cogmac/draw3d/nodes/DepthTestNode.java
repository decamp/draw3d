package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class DepthTestNode extends DrawNodeAdapter {
    
    private boolean mEnable = false;
    private int mFunc       = GL_LESS;
    
    private final int[] mRevert = {0,GL_LESS};
    
    
    public DepthTestNode() {
        this(false, GL_LESS);
    }
    
    public DepthTestNode(boolean enable) {
        this(enable, GL_LESS);
    }
    
    public DepthTestNode(boolean enable, int func) {
        mEnable = enable;
        mFunc   = func;
    }
    
    
    public boolean enable() {
        return mEnable;
    }
    
    public DepthTestNode enable(boolean enable) {
        mEnable = enable;
        return this;
    }
    
    public int function() {
        return mFunc;
    }
    
    public DepthTestNode function(int func) {
        mFunc = func;
        return this;
    }
    
    
    @Override
    public void pushDraw(GL gl) {
        gl.glGetIntegerv(GL_DEPTH_TEST, mRevert, 0);
        gl.glGetIntegerv(GL_DEPTH_FUNC, mRevert, 1);
        
        if(mEnable) {
            gl.glEnable(GL_DEPTH_TEST);
        }else{
            gl.glDisable(GL_DEPTH_TEST);
        }
        
        gl.glDepthFunc(mFunc);
    }
    
    @Override
    public void popDraw(GL gl) {
        if(mRevert[0] == 0) {
            gl.glDisable(GL_DEPTH_TEST);
        }else{
            gl.glEnable(GL_DEPTH_TEST);
        }
        
        gl.glDepthFunc(mRevert[1]);
    }
    
}
