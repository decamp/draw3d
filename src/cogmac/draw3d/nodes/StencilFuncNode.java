package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


public class StencilFuncNode extends DrawNodeAdapter {

    public boolean mEnable = false;
    public int mFrontFunc  = GL_ALWAYS;
    public int mFrontRef   = 0;
    public int mFrontMask  = 0xFFFFFFFF;
    public int mBackFunc   = GL_ALWAYS;
    public int mBackRef    = 0;
    public int mBackMask   = 0xFFFFFFFF;
    
    private int[] mRevert = { 0, GL_ALWAYS, 0, 0xFFFFFFFF, GL_ALWAYS, 0, 0xFFFFFFFF };
    
    
    public StencilFuncNode() {}
    
    
    public StencilFuncNode( boolean enable ) {
        mEnable = enable;
    }
    
    
    public StencilFuncNode( boolean enable, int func, int ref, int mask ) {
        mEnable = enable;
        set( func, ref, mask );
    }
    
    
    public StencilFuncNode( boolean enable, 
                            int frontFunc,
                            int frontRef,
                            int frontMask,
                            int backFunc,
                            int backRef,
                            int backMask ) 
    {
        mEnable = enable;
        setFront( frontFunc, frontRef, frontMask );
        setBack( backFunc, backRef, backMask );
    }    
    
    
    
    public StencilFuncNode set( int func, int ref, int mask ) {
        mFrontFunc = mBackFunc = func;
        mFrontRef  = mBackRef  = ref;
        mFrontMask = mBackMask = mask;
        return this;
    }
    
    
    public StencilFuncNode setFront( int func, int ref, int mask ) {
        mFrontFunc = func;
        mFrontRef  = ref;
        mFrontMask = mask;
        return this;
    }
    
    
    public StencilFuncNode setBack( int func, int ref, int mask ) {
        mBackFunc = func;
        mBackRef  = ref;
        mBackMask = mask;
        return this;
    }
    

    
    public void apply( GL gl ) {
        if( mEnable ) {
            gl.glEnable( GL_STENCIL_TEST );
            gl.glStencilFuncSeparate( GL_FRONT, mFrontFunc, mFrontRef, mFrontMask );
            gl.glStencilFuncSeparate( GL_BACK, mBackFunc, mBackRef, mBackMask );
        } else {
            gl.glDisable( GL_STENCIL_TEST );
        }
    }
    
    
    @Override
    public void pushDraw( GL gl ) {
        gl.glGetIntegerv( GL_STENCIL_TEST, mRevert, 0 );
        gl.glGetIntegerv( GL_STENCIL_FUNC, mRevert, 1 );
        gl.glGetIntegerv( GL_STENCIL_REF, mRevert, 2 );
        gl.glGetIntegerv( GL_STENCIL_VALUE_MASK, mRevert, 3 );
        gl.glGetIntegerv( GL_STENCIL_BACK_FUNC, mRevert, 4 );
        gl.glGetIntegerv( GL_STENCIL_BACK_REF, mRevert, 5 );
        gl.glGetIntegerv( GL_STENCIL_BACK_VALUE_MASK, mRevert, 6 );
        apply( gl );
    }
    
        
    @Override
    public void popDraw( GL gl ) {
        if( mRevert[0] == 0 ) {
            gl.glDisable( GL_STENCIL_TEST );
        } else {
            gl.glEnable( GL_STENCIL_TEST );
        }
        
        gl.glStencilFuncSeparate( GL_FRONT, mRevert[1], mRevert[2], mRevert[3] );
        gl.glStencilFuncSeparate( GL_BACK,  mRevert[4], mRevert[5], mRevert[6] );
    }
    
}
