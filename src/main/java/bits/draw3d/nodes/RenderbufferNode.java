/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.draw3d.DrawEnv;
import bits.draw3d.Texture;
import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public class RenderbufferNode implements Texture {
    
    private final int[] mId = { 0 };
    
    private int mIntFormat  = GL_RGBA;
    private int mWidth  = -1;
    private int mHeight = -1;
    private boolean mResizeOnReshape = false;
    
    private boolean mNeedInit  = true;
    private boolean mNeedAlloc = true;
    

    public RenderbufferNode() {}

    
    public int target() {
        return GL_RENDERBUFFER;
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
    
    public void init( DrawEnv d  ) {
        bind( d );
        unbind( d );
    }
    
    public void dispose( DrawEnv d ) {
        if( mId[0] != 0 ) {
            d.mGl.glDeleteRenderbuffers( 1, mId, 0 );
            mId[0] = 0;
        }
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    public void bind( DrawEnv d ) {
        if( mNeedInit ) {
            doInit( d );
        }
        d.mGl.glBindRenderbuffer( GL_RENDERBUFFER, mId[0] );
    }

    public void bind( DrawEnv g, int unit ) {
        g.mGl.glActiveTexture( GL_TEXTURE0 + unit );
        bind( g );
    }

    public void unbind( DrawEnv d ) {
        d.mGl.glBindRenderbuffer( GL_RENDERBUFFER, 0 );
    }

    public void unbind( DrawEnv g, int unit ) {
        g.mGl.glActiveTexture( GL_TEXTURE0 + unit );
        unbind( g );
    }

    public void reshape( DrawEnv e ) {
        if( resizeOnReshape() ) {
            size( e.mViewport.mW, e.mViewport.mH );
        }
    }

    
    protected void fireInit() {
        mNeedInit = true;
    }
    
    protected void fireAlloc() {
        mNeedInit  = true;
        mNeedAlloc = true;
    }
    
    private void doInit( DrawEnv d ) {
        if( !mNeedInit ) {
            return;
        }
        mNeedInit = false;
        
        if( mId[0] == 0 ) {
            d.mGl.glGenTextures( 1, mId, 0 );
            if( mId[0] == 0 ) {
                throw new RuntimeException("Failed to allocate texture.");
            }
        }
        
        d.mGl.glBindRenderbuffer( GL_RENDERBUFFER, mId[0] );
        if( mNeedAlloc ) {
            mNeedAlloc = false;
            if( hasSize() ) {
                d.mGl.glRenderbufferStorage( GL_RENDERBUFFER, mIntFormat, mWidth, mHeight );
                d.checkErr();
            }
        }
    }


    @Deprecated public static RenderbufferNode newInstance() {
        return new RenderbufferNode();
    }

}
