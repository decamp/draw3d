/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import bits.draw3d.*;
import bits.math3d.Mat;
import bits.math3d.Mat4;

import java.nio.FloatBuffer;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;


/**
 * @author decamp
 */
public class ColorMatrixTexShader implements DrawUnit {

    private static final String PACKAGE     = ColorMatrixTexShader.class.getPackage().getName().replace( ".", "/" ) + "/";
    private static final String SOURCE_VERT = PACKAGE + "Tex.vert";
    private static final String SOURCE_FRAG = PACKAGE + "ColorMatrixTex.frag";

    private final Program mProgram;

    private int  mTex = 0;
    private Mat4 mMat = new Mat4();

    private boolean mInitialized   = false;
    private int     mTexParam      = 0;
    private int     mColorMatParam = 0;


    public ColorMatrixTexShader() {
        Mat.identity( mMat );
        mProgram = new Program();
    }


    public void textureUnit( int unit ) {
        mTex = unit;
    }


    public Mat4 matrix() {
        return mMat;
    }


    public void init( DrawEnv gl ) {
        if( !mInitialized ) {
            doInit( gl );
        }
    }


    public void bind( DrawEnv gl ) {
        if( !mInitialized ) {
            doInit( gl );
        }
        mProgram.bind( gl );
        updateParameters( gl );
    }
    
    
    public void unbind( DrawEnv gl ) {
        mProgram.unbind( gl );
    }

    
    public void dispose( DrawEnv gld ) {
        if( !mInitialized ) {
            return;
        }
        
        mInitialized = false;
        mTexParam    = 0;
        mColorMatParam = 0;
    }

    
    private void updateParameters( DrawEnv d ) {
        d.mGl.glUniform1i( mTexParam,  mTex );
        FloatBuffer work = d.mWorkFloats;
        work.clear();
        Mat.put( mMat, work );
        work.flip();
        d.mGl.glUniformMatrix4fv( mColorMatParam, 1, false, work );
    }
    
    
    private void doInit( DrawEnv d ) {
        d.checkErr();
        mInitialized = true;

        mProgram.addShader( d.mShaderMan.loadResource( GL_VERTEX_SHADER, SOURCE_VERT ) );
        mProgram.addShader( d.mShaderMan.loadResource( GL_FRAGMENT_SHADER, SOURCE_FRAG ) );
        mProgram.init( d );
        mProgram.bind( d );

        mTexParam      = d.mGl.glGetUniformLocation( mProgram.id(), "TEX_UNIT0" );
        mColorMatParam = d.mGl.glGetUniformLocation( mProgram.id(), "COLOR_MAT" );
        
        d.checkErr();
    }
    
}
