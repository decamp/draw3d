package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


public class StencilOpNode extends DrawNodeAdapter {
    
    
    public int mFrontFail      = GL_KEEP;
    public int mFrontDepthFail = GL_KEEP;
    public int mFrontPass      = GL_KEEP;
    public int mBackFail       = GL_KEEP;
    public int mBackDepthFail  = GL_KEEP;
    public int mBackPass       = GL_KEEP;
    
    private int[] mRevert = { GL_KEEP, GL_KEEP, GL_KEEP, GL_KEEP, GL_KEEP, GL_KEEP }; 
    
    
    public StencilOpNode() {}
    
    
    public StencilOpNode set( int fail, int depthFail, int pass ) {
        mFrontFail = mBackFail = fail;
        mFrontDepthFail = mBackDepthFail = depthFail;
        mFrontPass = mBackPass = pass;
        return this;
    }
    
    
    public StencilOpNode setFront( int fail, int depthFail, int pass ) {
        mFrontFail = fail;
        mFrontDepthFail = depthFail;
        mFrontPass = pass;
        return this;
    }
    
    
    public StencilOpNode setBack( int fail, int depthFail, int pass ) {
        mBackFail = fail;
        mBackDepthFail = depthFail;
        mBackPass = pass;
        return this;
    }
    
    
    public void apply( GL gl ) {
        gl.glStencilOpSeparate( GL_FRONT, mFrontFail, mFrontDepthFail, mFrontPass );
        gl.glStencilOpSeparate( GL_BACK, mBackFail, mBackDepthFail, mBackPass );
    }
    
    
    public void pushDraw( GL gl ) {
        gl.glGetIntegerv( GL_STENCIL_FAIL, mRevert, 0 );
        gl.glGetIntegerv( GL_STENCIL_PASS_DEPTH_FAIL, mRevert, 1 );
        gl.glGetIntegerv( GL_STENCIL_PASS_DEPTH_PASS, mRevert, 2 );
        gl.glGetIntegerv( GL_STENCIL_BACK_FAIL, mRevert, 3 );
        gl.glGetIntegerv( GL_STENCIL_BACK_PASS_DEPTH_FAIL, mRevert, 4 );
        gl.glGetIntegerv( GL_STENCIL_BACK_PASS_DEPTH_PASS, mRevert, 5 );
        apply( gl );
    }
    
    
    @Override
    public void popDraw( GL gl ) {
        gl.glStencilOpSeparate( GL_FRONT, mRevert[0], mRevert[1], mRevert[2] );
        gl.glStencilOpSeparate( GL_BACK,  mRevert[3], mRevert[4], mRevert[5] );
    }
    
}
