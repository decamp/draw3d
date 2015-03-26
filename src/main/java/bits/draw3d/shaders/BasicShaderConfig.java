/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shaders;

import static javax.media.opengl.GL2ES3.*;
import static javax.media.opengl.GL3.*;


/**
 * Specifies available capabilities space of BasicShaders.
 * Canonicalized BasicShaders.BasicShaderConfig objects can be used
 * as hash keys for BoPrograms.
 */
public class BasicShaderConfig {

    private int     mMode       = GL_POINTS;
    private int     mTexCompNum = 0;
    private boolean mNorm       = false;
    private boolean mColor      = false;
    private boolean mFog        = false;
    private float   mLineWidth  = 1f;

    private String mVertShader;
    private String mFragShader;
    private String mGeomShader;

    private int mHash = 0;


    public BasicShaderConfig() {}


    public BasicShaderConfig( BasicShaderConfig copy ) {
        set( copy );
    }



    public void set( BasicShaderConfig copy ) {
        mMode       = copy.mMode;
        mTexCompNum = copy.mTexCompNum;
        mNorm       = copy.mNorm;
        mColor      = copy.mColor;
        mFog        = copy.mFog;
        mLineWidth  = copy.mLineWidth;
        mVertShader = copy.mVertShader;
        mGeomShader = copy.mGeomShader;
        mFragShader = copy.mFragShader;
    }


    public void color( boolean color ) {
        mColor = color;
    }


    public boolean color() {
        return mColor;
    }


    public void normals( boolean norm ) {
        mNorm = norm;
    }


    public boolean normals() {
        return mNorm;
    }


    public void texComponentNum( int texCompNum ) {
        mTexCompNum = texCompNum;
        rehash();
    }


    public int texComponentNum() {
        return mTexCompNum;
    }


    public void fog( boolean fog ) {
        mFog = fog;
    }


    public boolean fog() {
        return mFog;
    }

    /**
     * @param mode Draw mode, as in GL_POINTS, GL_LINES, etc.
     */
    public void geomMode( int mode ) {
        mMode = mode;
    }


    public int geomMode() {
        return mMode;
    }


    public float lineWidth() {
        return mLineWidth;
    }


    public void lineWidth( float v ) {
        mLineWidth = v;
        rehash();
    }


    public void chooseAvailable( BasicShaderConfig out ) {
        if( out != this ) {
            out.set( this );
        }
        out.mTexCompNum = Math.max( 0, Math.min( 4, out.mTexCompNum ) );

        switch( mMode ) {
        case GL_LINES:
        case GL_LINE_LOOP:
        case GL_LINES_ADJACENCY:
            if( mLineWidth != 1f ) {
                out.mColor      = true;
                out.mNorm       = false;
                out.mTexCompNum = 0;
                out.mVertShader = "glsl/bits/draw3d/shaders/ColorGeom.vert";
                out.mGeomShader = "glsl/bits/draw3d/shaders/ColorLinesToQuads.geom";
                out.mFragShader = "glsl/bits/draw3d/shaders/Color.frag";
                break;
            }
            // FALL-THROUGH
        default:
        case GL_POINTS:
        case GL_TRIANGLES:
        case GL_TRIANGLES_ADJACENCY:
        case GL_TRIANGLE_STRIP:
        case GL_TRIANGLE_STRIP_ADJACENCY:
        case GL_TRIANGLE_FAN:
        case GL_QUADS:
        {
            if( mTexCompNum == 0 ) {
                out.mNorm = false;
                if( mFog ) {
                    out.mVertShader = "glsl/bits/draw3d/shaders/ColorFog.vert";
                } else {
                    out.mVertShader = "glsl/bits/draw3d/shaders/Color.vert";
                }
                out.mGeomShader = null;
                out.mFragShader = "glsl/bits/draw3d/shaders/Color.frag";
            } else {
                if( mColor ) {
                    if( mNorm ) {
                        out.mVertShader = "glsl/bits/draw3d/shaders/ColorNormTex.vert";
                        out.mGeomShader = null;
                        out.mFragShader = "glsl/bits/draw3d/shaders/ColorTex.frag";
                    } else {
                        if( mFog ) {
                            out.mVertShader = "glsl/bits/draw3d/shaders/ColorTexFog.vert";
                        } else {
                            out.mVertShader = "glsl/bits/draw3d/shaders/ColorTex.vert";
                        }
                        out.mGeomShader = null;
                        out.mFragShader = "glsl/bits/draw3d/shaders/ColorTex.frag";
                    }
                } else {
                    if( mNorm ) {
                        out.mVertShader = "glsl/bits/draw3d/shaders/NormTex.vert";
                        out.mGeomShader = null;
                        out.mFragShader = "glsl/bits/draw3d/shaders/Tex.frag";
                    } else {
                        out.mVertShader = "glsl/bits/draw3d/shaders/Tex.vert";
                        out.mGeomShader = null;
                        out.mFragShader = "glsl/bits/draw3d/shaders/Tex.frag";
                    }
                }
            }
        }}

        out.rehash();
    }


    String vertShader() {
        return mVertShader;
    }


    String geomShader() {
        return mGeomShader;
    }


    String fragShader() {
        return mFragShader;
    }

    @Override
    public boolean equals( Object obj ) {
        if( !(obj instanceof BasicShaderConfig) ) {
            return false;
        }
        BasicShaderConfig c = (BasicShaderConfig)obj;
        return mVertShader == c.mVertShader &&
               mGeomShader == c.mGeomShader &&
               mFragShader == c.mFragShader &&
               mTexCompNum == c.mTexCompNum;
    }

    @Override
    public int hashCode() {
        return mHash;
    }


    private void rehash() {
        mHash = hash( mVertShader ) ^
                hash( mGeomShader ) ^
                hash( mFragShader ) ^
                mTexCompNum;
    }


    private static int hash( String s ) {
        return s == null ? 0 : s.hashCode();
    }

}
