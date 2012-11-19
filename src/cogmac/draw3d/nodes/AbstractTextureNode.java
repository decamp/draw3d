package cogmac.draw3d.nodes;

import java.util.*;
import javax.media.opengl.*;

import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
abstract class AbstractTextureNode implements TextureNode {
    
    
    private final int mTarget;
    private final int mBinding;
    private final int[] mId = {0};
    
    private int mIntFormat = GL_RGBA;
    private int mFormat    = GL_RGBA;
    private int mDataType  = GL_UNSIGNED_BYTE;
    
    private int mWidth  = -1;
    private int mHeight = -1;
    private int mDepth  =  1;
    private boolean mResizeOnReshape = false;
    
    private final Map<Integer,Integer> mParams = new HashMap<Integer,Integer>(4);
    
    private boolean mNeedInit  = true;
    private boolean mNeedAlloc = true;
    
    private final int[] mRevert = {0,0};
    
    
    protected AbstractTextureNode( int target, int binding ) {
        mTarget    = target;
        mBinding   = binding;
    }
    
    
    
    public int target() {
        return mTarget;
    }
    
    public int id() {
        return mId[0];
    }
    
    public void format( int intFormat, int format, int dataType ) {
        if( intFormat == mIntFormat && 
            format    == mFormat    && 
            dataType  == mDataType)
        {
            return;
        }
        
        mIntFormat = intFormat;
        mFormat    = format;
        mDataType  = dataType;
        fireAlloc();
    }
    
    public int internalFormat() {
        return mIntFormat;
    }
    
    public int format() {
        return mFormat;
    }
    
    public int dataType() {
        return mDataType;
    }
    
    public void size( int w, int h ) {
        if( mTarget == GL_TEXTURE_1D ) {
            if( w < 0 ) {
                w = -1;
                h = -1;
            } else {
                h = 1;
            }
        }else{
            if( w < 0 || h < 0 ) {
                w = -1;
                h = -1;
            }
        }
        
        if( w == mWidth && h == mHeight )
            return;
        
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
        return mWidth >= 0 && mDepth >= 0;
    }
    
    public void resizeOnReshape( boolean enable ) {
        mResizeOnReshape = enable;
    }
    
    public boolean resizeOnReshape() {
        return mResizeOnReshape;
    }
    
    public void depth( int depth ) {
        if( mTarget != GL_TEXTURE_3D )
            return;
        
        if( depth < 0 )
            depth = -1; 
        
        if( depth == mDepth )
            return;
        
        mDepth     = depth;
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    public int depth() {
        return mDepth;
    }
    
    public Integer param( int key ) {
        return mParams.get( key );
    }
    
    public void param( int key, int value ) {
        Integer prev = mParams.put( key, value );
        
        if( prev == null || prev.intValue() != value ) {
            fireInit();
        }
    }  
    
    
    public void init( GL gl ) {
        pushDraw(gl);
        popDraw(gl);
    }
    
    public void dispose( GL gl ) {
        if( mId[0] != 0 ) {
            gl.glDeleteTextures( 1, mId, 0 );
            mId[0] = 0;
        }
        
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    public void bind( GL gl ) {
        if( mNeedInit )
            doInit( gl );

        gl.glBindTexture( mTarget, mId[0] );
    }
    
    public void unbind( GL gl ) {
        gl.glBindTexture( mTarget, 0 );
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
        gl.glGetIntegerv( mTarget,  mRevert, 0 );
        gl.glGetIntegerv( mBinding, mRevert, 1 );
        if( mNeedInit ) {
            doInit( gl );
        }
        gl.glBindTexture( mTarget, mId[0] );
        gl.glEnable( mTarget );
    }

    public void popDraw( GL gl ) {
        if( mRevert[0] == 0 ) {
            gl.glDisable( mTarget );
        }
        
        gl.glBindTexture( mTarget, mRevert[1] );
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
    
    protected abstract void doAlloc( GL gl ); 

    
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
        
        gl.glBindTexture( mTarget, mId[0] );
        
        if( !mParams.isEmpty() ) {
            for( Map.Entry<Integer,Integer> e: mParams.entrySet() ) {
                gl.glTexParameteri( mTarget, e.getKey(), e.getValue() );
            }
        }
        
        if( mNeedAlloc ) {
            mNeedAlloc = false;
            
            if( hasSize() ) {
                doAlloc( gl );
            
                if( gl.glGetError() != GL_NO_ERROR ) {
                    throw new RuntimeException( "Failed to allocate texture storage." );
                }
            }
        }
    }

}
