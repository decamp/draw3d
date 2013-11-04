package bits.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class BlendFuncNode extends DrawNodeAdapter {

    
    public static BlendFuncNode newSrcOver() {
        return new BlendFuncNode( true, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
    }
    
    
    private boolean mEnable;
    private int mSrcRgb;
    private int mDstRgb;
    private int mSrcAlpha;
    private int mDstAlpha;
    
    private boolean mRevertEnable = true;
    private final int[] mRevert   = { GL_ONE, GL_ZERO, GL_ONE, GL_ZERO };
    
    
    public BlendFuncNode() {} 
    
    public BlendFuncNode( boolean enable, int srcFactor, int dstFactor ) {
        mEnable   = enable;
        mSrcRgb   = srcFactor;
        mDstRgb   = dstFactor;
        mSrcAlpha = srcFactor;
        mDstAlpha = dstFactor;
    }
   
    public BlendFuncNode( boolean enable, int srcRgb, int dstRgb, int srcAlpha, int dstAlpha ) {
        mEnable   = enable;
        mSrcRgb   = srcRgb;
        mDstRgb   = dstRgb;
        mSrcAlpha = srcAlpha;
        mDstAlpha = dstAlpha;
    }
    
    
    
    public boolean enable() {
        return mEnable;
    }
    
    public BlendFuncNode enable( boolean enable ) {
        mEnable = enable;
        return this;
    }
    
    
    public int srcFactor() {
        return mSrcRgb;
    }
    
    public BlendFuncNode srcFactor( int srcFactor ) {
        mSrcRgb   = srcFactor;
        mSrcAlpha = srcFactor;
        return this;
    }
    
    public int dstFactor() {
        return mDstRgb;
    }
    
    public BlendFuncNode dstFactor( int dstFactor ) {
        mDstRgb   = dstFactor;
        mDstAlpha = dstFactor;
        return this;
    }
    
    
    public int srcRgb() {
        return mSrcRgb;
    }
    
    public BlendFuncNode srcRgb( int srcRgb ) {
        mSrcRgb = srcRgb;
        return this;
    }
    
    public int dstRgb() {
        return mDstRgb;
    }
    
    public BlendFuncNode dstRgb( int dstRgb ) {
        mDstRgb = dstRgb;
        return this;
    }

    
    public int srcAlpha() {
        return mSrcAlpha;
    }
    
    public BlendFuncNode srcAlpha( int srcAlpha ) {
        mSrcAlpha = srcAlpha;
        return this;
    }
    
    public int dstAlpha() {
        return mDstAlpha;
    }
    
    public BlendFuncNode dstAlpha( int dstAlpha ) {
        mDstAlpha = dstAlpha;
        return this;
    }
    
    
    
    @Override
    public void pushDraw(GL gl) {
        mRevertEnable = gl.glIsEnabled( GL_BLEND );
        gl.glGetIntegerv( GL_BLEND_SRC_RGB  , mRevert, 0 );
        gl.glGetIntegerv( GL_BLEND_DST_RGB  , mRevert, 1 );
        gl.glGetIntegerv( GL_BLEND_SRC_ALPHA, mRevert, 2 );
        gl.glGetIntegerv( GL_BLEND_DST_ALPHA, mRevert, 3 );
        
        if( mEnable ) {
            gl.glEnable( GL_BLEND );
        } else {
            gl.glDisable( GL_BLEND );
        }
        
        gl.glBlendFuncSeparate( mSrcRgb, mDstRgb, mSrcAlpha, mDstAlpha );
    }
    
    @Override
    public void popDraw(GL gl) {
        if( mRevertEnable ) {
            gl.glEnable( GL_BLEND );
        } else {
            gl.glDisable( GL_BLEND );
        }
        
        gl.glBlendFuncSeparate( mRevert[0], mRevert[1], mRevert[2], mRevert[3] );
    }
    
}
