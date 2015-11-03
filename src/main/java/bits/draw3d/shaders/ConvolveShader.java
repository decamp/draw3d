/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import bits.draw3d.*;
import bits.util.Resources;

import java.io.IOException;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;


/**
 * @author decamp
 */
public class ConvolveShader implements DrawUnit {
    
    private static final boolean HORIZONTAL = false;
    private static final boolean VERTICAL   = true;

    private static final String PACKAGE             = ConvolveShader.class.getPackage().getName().replace( ".", "/" ) + "/";
    private static final String SOURCE_VERT         = PACKAGE + "Blit.vert";
    private static final String SOURCE_X_FRAG       = PACKAGE + "ConvolveX.frag";
    private static final String SOURCE_Y_FRAG       = PACKAGE + "ConvolveY.frag";
    private static final String SOURCE_X_DEPTH_FRAG = PACKAGE + "ConvolveXDepth.frag";
    private static final String SOURCE_Y_DEPTH_FRAG = PACKAGE + "ConvolveYDepth.frag";
    

    public static ConvolveShader createHorizontal( int kernDim, boolean processDepth ) {
        try {
            String vertText = Resources.readString( SOURCE_VERT );
            String path     = processDepth ? SOURCE_X_DEPTH_FRAG : SOURCE_X_FRAG;
            String fragText = Resources.readString( path );
            fragText = fragText.replace( "#define SAMP_DIM 1", "#define SAMP_DIM " + kernDim );
            return new ConvolveShader( vertText, fragText, HORIZONTAL );
        } catch( IOException ex ) {
            Error e = new UnsatisfiedLinkError( "Shader failed." );
            e.initCause( ex );
            throw e;
        }
    }
    
    
    public static ConvolveShader createVertical( int kernDim, boolean processDepth ) {
        try {
            String vertText = Resources.readString( SOURCE_VERT );
            String path     = processDepth ? SOURCE_Y_DEPTH_FRAG : SOURCE_Y_FRAG;
            String fragText = Resources.readString( path );
            fragText = fragText.replace( "#define SAMP_DIM 1", "#define SAMP_DIM " + kernDim );
            return new ConvolveShader( vertText, fragText, VERTICAL );
        } catch( IOException ex ) {
            Error e = new UnsatisfiedLinkError( "Shader failed." );
            e.initCause( ex );
            throw e;
        }
    }


    private final Program mProgram;
    private final boolean mDir;

    private boolean mLoaded          = false;
    private int     mSourceParam     = -1;
    private int     mSourceStepParam = -1;
    private int     mKernelParam     = -1;
    private int     mDepthParam      = -1;

    private int mSourceTex    = 0;
    private int mSourceWidth  = 1;
    private int mSourceHeight = 1;
    private int mKernelTex    = 1;
    private int mDepthTex     = 2;


    private ConvolveShader( String vsText, String fsText, boolean dir ) {
        mProgram = new Program();
        mDir = dir;
        mProgram.addShader( new Shader( GL_VERTEX_SHADER, vsText ) );
        mProgram.addShader( new Shader( GL_FRAGMENT_SHADER, fsText ) );
    }


    public void sourceTexUnit( int tex ) {
        mSourceTex = tex;
    }


    public void sourceDims( int w, int h ) {
        mSourceWidth = w;
        mSourceHeight = h;
    }


    public void kernelTexUnit( int tex ) {
        mKernelTex = tex;
    }


    public void depthTexUnit( int depthTex ) {
        mDepthTex = depthTex;
    }


    public void init( DrawEnv d ) {
        if( !mLoaded ) {
            doInit( d );
        }
    }


    public void bind( DrawEnv d ) {
        if( !mLoaded ) {
            doInit( d );
        }
        mProgram.bind( d );
        updateParameters( d );
    }


    public void unbind( DrawEnv d ) {
        mProgram.unbind( d );
    }


    public void dispose( DrawEnv d ) {
        if( !mLoaded ) {
            return;
        }
        mLoaded = false;
        mSourceParam = 0;
        mSourceStepParam = 0;
        mKernelParam = 0;
    }


    private void updateParameters( DrawEnv d ) {
        d.mGl.glUniform1i( mSourceParam, mSourceTex );
        d.mGl.glUniform1i( mKernelParam, mKernelTex );
        d.mGl.glUniform1i( mDepthParam, mDepthTex );
        if( mDir == HORIZONTAL ) {
            d.mGl.glUniform1f( mSourceStepParam, 1.0f / mSourceWidth );
        } else {
            d.mGl.glUniform1f( mSourceStepParam, 1.0f / mSourceHeight );
        }
    }


    private void doInit( DrawEnv d ) {
        d.checkErr();

        mLoaded = true;
        mProgram.init( d );
        mSourceStepParam = d.mGl.glGetUniformLocation( mProgram.id(), "SOURCE_STEP" );
        mSourceParam = d.mGl.glGetUniformLocation( mProgram.id(), "SOURCE_UNIT" );

        mKernelParam = d.mGl.glGetUniformLocation( mProgram.id(), "KERNEL_UNIT" );
        mDepthParam = d.mGl.glGetUniformLocation( mProgram.id(), "DEPTH_UNIT" );
    }

}
