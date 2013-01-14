package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class BlendFuncNode extends DrawNodeAdapter {

    private boolean mEnable;
    private int mSrcFactor;
    private int mDstFactor;
    
    private boolean mRevertEnable = true;
    private final int[] mRevert   = { GL_ONE, GL_ONE_MINUS_SRC_ALPHA };
    
    
    public BlendFuncNode( boolean enable, int srcFactor, int dstFactor ) {
        mEnable    = enable;
        mSrcFactor = srcFactor;
        mDstFactor = dstFactor;
    }
    
    
    public boolean enable() {
        return mEnable;
    }
    
    public BlendFuncNode enable( boolean enable ) {
        mEnable = enable;
        return this;
    }
    
    public int srcFactor() {
        return mSrcFactor;
    }
    
    public BlendFuncNode srcFactor( int srcFactor ) {
        mSrcFactor = srcFactor;
        return this;
    }
    
    public int dstFactor() {
        return mDstFactor;
    }
    
    public BlendFuncNode dstFactor( int dstFactor ) {
        mDstFactor = dstFactor;
        return this;
    }
    
    
    @Override
    public void pushDraw(GL gl) {
        mRevertEnable = gl.glIsEnabled( GL_BLEND );
        gl.glGetIntegerv( GL_BLEND_SRC, mRevert, 0 );
        gl.glGetIntegerv( GL_BLEND_SRC, mRevert, 1 );
        
        if( mEnable ) {
            gl.glEnable( GL_BLEND );
        } else {
            gl.glDisable( GL_BLEND );
        }
        
        gl.glBlendFunc( mSrcFactor, mDstFactor );
    }
    
    @Override
    public void popDraw(GL gl) {
        if( mRevertEnable ) {
            gl.glEnable( GL_BLEND );
        } else {
            gl.glDisable( GL_BLEND );
        }
        
        gl.glBlendFunc( mRevert[0], mRevert[1] );
    }
    
}
