/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import bits.draw3d.*;
import bits.util.Resources;

import java.io.IOException;

import static javax.media.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static javax.media.opengl.GL2ES2.GL_VERTEX_SHADER;


/**
 * @author decamp
 */
public class BlitShader implements DrawUnit {

    private static final String PACKAGE     = BlitShader.class.getPackage().getName().replace( ".", "/" ) + "/";
    private static final String SOURCE_VERT = PACKAGE + "Blit.vert";
    private static final String SOURCE_FRAG = PACKAGE + "BlitDepth.frag";

    private final Program mProgram;

    private boolean mInitialized = false;
    private DrawEnv mPushed      = null;

    private int mColorTex      = 0;
    private int mDepthTex      = 1;
    private int mColorTexParam = 0;
    private int mDepthTexParam = 0;


    public BlitShader() {
        mProgram = new Program();

        try {
            String vertText = Resources.readString( SOURCE_VERT );
            String fragText = Resources.readString( SOURCE_FRAG );
            mProgram.addShader( new Shader( GL_VERTEX_SHADER, vertText ) );
            mProgram.addShader( new Shader( GL_FRAGMENT_SHADER, fragText ) );
        } catch( IOException ex ) {
            Error e = new UnsatisfiedLinkError( "Shader failed." );
            e.initCause( ex );
            throw e;
        }
    }

    
    
    public void texUnits( int colorTex, int depthTex ) {
        mColorTex = colorTex;
        mDepthTex = depthTex;
        
        DrawEnv gl = mPushed;
        if( gl != null ) {
            updateParameters( gl );
        }
    }


    @Override
    public void init( DrawEnv d ) {
        if( !mInitialized ) {
            doInit( d );
        }
    }

    @Override
    public void bind( DrawEnv d ) {
        if( !mInitialized ) {
            doInit( d );
        }

        mProgram.bind( d );
        mPushed = d;
        updateParameters( d );
    }

    @Override
    public void unbind( DrawEnv d ) {
        mProgram.unbind( d );
        mPushed = null;
    }

    @Override
    public void dispose( DrawEnv d ) {
        if( !mInitialized ) {
            return;
        }
        mInitialized = false;
        mColorTexParam   = 0;
        mDepthTexParam   = 0;
    }


    private void updateParameters( DrawEnv d ) {
        d.mGl.glUniform1i( mColorTexParam, mColorTex );
        d.mGl.glUniform1i( mDepthTexParam, mDepthTex );
    }


    private void doInit( DrawEnv d ) {
        d.checkErr();
        mInitialized = true;
        mProgram.init( d );
        mColorTexParam = d.mGl.glGetUniformLocation( mProgram.id(), "COLOR_UNIT" );
        mDepthTexParam = d.mGl.glGetUniformLocation( mProgram.id(), "DEPTH_UNIT" );
    }

}
