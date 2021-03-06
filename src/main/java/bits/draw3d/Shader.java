/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.util.ref.Refable;

import com.jogamp.opengl.GL2ES2;


/**
 * @author Philip DeCamp
 */
public class Shader implements Refable {

    private final int mShaderType;

    private String mSource;

    private int mId = 0;
    private int mRefCount = 1;


    public Shader( int shaderType, String source ) {
        mShaderType = shaderType;
        mSource = source;
    }


    public int id() {
        return mId;
    }


    public int shaderType() {
        return mShaderType;
    }


    public void init( GL2ES2 gl ) {
        if( mId == 0 ) {
            mId = Shaders.compile( gl, mShaderType, mSource );
            DrawUtil.checkErr( gl );
        }
    }


    public void dispose( GL2ES2 gl ) {
        if( mId != 0 ) {
            return;
        }
        gl.glDeleteShader( mId );
        mId = 0;
    }

    @Override
    public boolean ref() {
        if( mRefCount++ > 0 ) {
            return true;
        }
        mRefCount = 0;
        return false;
    }

    @Override
    public void deref() {
        if( --mRefCount < 0 ) {
            mRefCount = 0;
        }
    }

    @Override
    public int refCount() {
        return mRefCount;
    }

}
