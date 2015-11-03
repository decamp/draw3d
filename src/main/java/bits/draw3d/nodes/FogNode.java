/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNodeAdapter;

import com.jogamp.opengl.GL2;

import static com.jogamp.opengl.GL2.*;


/**
 * @author decamp
 */
@Deprecated public class FogNode extends DrawNodeAdapter {

    private final int[]   mStateInt     = { 1, GL_EXP };
    private final float[] mStateFloat = { 1f, 0f, 1f, 0f, 0f, 0f, 0f };
    private final int[]   mRevertInt;
    private final float[] mRevertFloat;
    
    
    public FogNode() {
        this( true );
    }
    
    
    public FogNode( boolean pushState ) {
        if( pushState ) {
            mRevertInt   = new int[2];
            mRevertFloat = new float[7];
        } else {
            mRevertInt   = null;
            mRevertFloat = null;
        }
    }


    
    public boolean enable() {
        return mStateInt[0] != 0;
    }
    
    
    public FogNode enable( boolean enable ) {
        mStateInt[0] = enable ? 1 : 0;
        return this;
    }
    
    
    public int mode() {
        return mStateInt[1];
    }
    
    
    public FogNode mode( int mode ) {
        mStateInt[1] = mode;
        return this;
    }
    
    
    public float density() {
        return mStateFloat[0];
    }
    
    
    public FogNode density( float density ) {
        mStateFloat[0] = density;
        return this;
    }
    
    
    public float start() {
        return mStateFloat[1];
    }
    
    
    public FogNode start( float start ) {
        mStateFloat[1] = start;
        return this;
    }
    
    
    public float end() {
        return mStateFloat[2];
    }
    
    
    public FogNode end( float end ) {
        mStateFloat[2] = end;
        return this;
    }
    
    
    public void getColor( float[] out4x1 ) {
        out4x1[0] = mStateFloat[3];
        out4x1[1] = mStateFloat[4];
        out4x1[2] = mStateFloat[5];
        out4x1[3] = mStateFloat[6];
    }
    
    
    public void setColor( float r, float g, float b, float a ) {
        mStateFloat[3] = r;
        mStateFloat[4] = g;
        mStateFloat[5] = b;
        mStateFloat[6] = a;
    }
    
    
    public void color( float r, float g, float b, float a ) {
        mStateFloat[3] = r;
        mStateFloat[4] = g;
        mStateFloat[5] = b;
        mStateFloat[6] = a;
    }

    
    
    public void pushDraw( DrawEnv gl ) {
        if( mRevertInt != null ) {
            read( (GL2)gl.mGl, mRevertInt, mRevertFloat );
        }
        write( (GL2)gl.mGl, mStateInt, mStateFloat );
    }
    
    
    public void popDraw( DrawEnv gl ) {
        if( mRevertInt != null ) {
            write( (GL2)gl.mGl, mRevertInt, mRevertFloat );
        } else {
            gl.mGl.glDisable( GL_FOG );
        }
    }
    
    
    
    
    private static void read( GL2 gl, int[] ints, float[] floats ) {
        gl.glGetIntegerv( GL_FOG,         ints,   0 );
        gl.glGetIntegerv( GL_FOG_MODE,    ints,   1 );
        gl.glGetFloatv(   GL_FOG_DENSITY, floats, 0 );
        gl.glGetFloatv(   GL_FOG_START,   floats, 1 );
        gl.glGetFloatv(   GL_FOG_END,     floats, 2 );
        gl.glGetFloatv(   GL_FOG_COLOR,   floats, 3 );
    }
    

    private static void write( GL2 gl, int[] ints, float[] floats ) {
        if( ints[0] == 0 ) {
            gl.glDisable( GL_FOG );
        } else {
            gl.glEnable( GL_FOG );
        }
        
        gl.glFogiv( GL_FOG_MODE,    ints,   1 );
        gl.glFogfv( GL_FOG_DENSITY, floats, 0 );
        gl.glFogfv( GL_FOG_START,   floats, 1 );
        gl.glFogfv( GL_FOG_END,     floats, 2 );
        gl.glFogfv( GL_FOG_COLOR,   floats, 3 );
    }


    @Deprecated public static FogNode newInstance() {
        return new FogNode();
    }


}
