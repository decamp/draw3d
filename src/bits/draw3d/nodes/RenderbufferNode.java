package bits.draw3d.nodes;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public class RenderbufferNode implements TextureNode {
    
    
    public static RenderbufferNode newInstance() {
        return new RenderbufferNode();
    }
    
    
    private final int[] mId = {0};
    
    private int mIntFormat  = GL_RGBA;
    private int mWidth  = -1;
    private int mHeight = -1;
    private boolean mResizeOnReshape = false;
    
    private boolean mNeedInit  = true;
    private boolean mNeedAlloc = true;
    
    private final int[] mRevert = {0};
    
    
    private RenderbufferNode() {}
    
    
    
    public int target() {
        return GL_RENDERBUFFER_EXT;
    }
    
    public int id() {
        return mId[0];
    }
    
    public void format( int intFormat, int format, int dataType ) {
        if( mIntFormat != intFormat ) {
            fireAlloc();
            mIntFormat = intFormat;
        }
    }
    
    public int internalFormat() {
        return mIntFormat;
    }
    
    public int format() {
        return -1;
    }
    
    public int dataType() {
        return -1;
    }
    
    public void size( int w, int h ) {
        if( w < 0 || h < 0 ) {
            w = -1;
            h = -1;
        }
        
        if( w == mWidth && h == mHeight ) {
            return;
        }
        
        mWidth  = w;
        mHeight = h;
        fireAlloc();
    }

    public int width() {
        return mWidth;
    }
    
    public int height() {
        return mHeight;
    }
    
    public boolean hasSize() {
        return mWidth >= 0;
    }
    
    public void resizeOnReshape( boolean enable ) {
        mResizeOnReshape = enable;
    }
    
    public boolean resizeOnReshape() {
        return mResizeOnReshape;
    }
    
    public void depth( int depth ) {}
    
    public int depth() {
        return 1;
    }
    
    public Integer param( int key ) {
        return null;
    }
    
    public void param( int key, int value ) {}
    
    public void init( GL gl ) {
        pushDraw( gl );
        popDraw( gl );
    }
    
    public void dispose( GL gl ) {
        if( mId[0] != 0 ) {
            gl.glDeleteRenderbuffersEXT( 1, mId, 0 );
            mId[0] = 0;
        }
        
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    public void bind( GL gl ) {
        if( mNeedInit )
            doInit( gl );   
        
        gl.glBindRenderbufferEXT( GL_RENDERBUFFER_EXT, mId[0] );
    }
    
    public void unbind( GL gl ) {
        gl.glBindRenderbufferEXT( GL_RENDERBUFFER_EXT, 0 );
    }
    
    public void init( GLAutoDrawable gld ) {
        init( gld.getGL() );
    }
    
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        if( resizeOnReshape() ) {
            size( w, h );
        }
    }
    
    public void pushDraw( GL gl ) {
        gl.glGetIntegerv( GL_RENDERBUFFER_BINDING_EXT, mRevert, 0 );
        if( mNeedInit ) {
            doInit( gl );
        }
        gl.glBindRenderbufferEXT( GL_RENDERBUFFER_EXT, mId[0] );
    }

    public void popDraw( GL gl ) {
        gl.glBindRenderbufferEXT( GL_RENDERBUFFER_EXT, mRevert[0] );
    }
    
    public void dispose( GLAutoDrawable gld ) {
        dispose( gld.getGL() );
    }
    
    
    protected void fireInit() {
        mNeedInit = true;
    }
    
    protected void fireAlloc() {
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    private void doInit( GL gl ) {
        if( !mNeedInit )
            return;
        
        mNeedInit = false;
        
        if( mId[0] == 0 ) {
            gl.glGenTextures( 1, mId, 0 );
            if( mId[0] == 0 ) {
                throw new RuntimeException("Failed to allocate texture.");
            }
        }
        
        gl.glBindRenderbufferEXT( GL_RENDERBUFFER_EXT, mId[0] );
        
        
        if( mNeedAlloc ) {
            mNeedAlloc = false;
            
            if( hasSize() ) {
                gl.glRenderbufferStorageEXT( GL_RENDERBUFFER_EXT,
                                             mIntFormat,
                                             mWidth,
                                             mHeight );
                
                if( gl.glGetError() != GL_NO_ERROR ) {
                    throw new RuntimeException( "Failed to allocate texture storage." );
                }
            }
        }
    }

}
