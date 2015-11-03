/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import static com.jogamp.opengl.GL3.*;

import bits.draw3d.text.FontManager;
import bits.math3d.*;


/**
 * @author decamp
 */
public class DrawEnv {

    public GL3 mGl;

    public GLAutoDrawable mGld;
    /**
     * Holds the size of the entire target render space,
     * including those areas that might be rendered in a
     * different context or process.
     */
    public final Rect mContextViewport = new Rect();

    public final MatStack mView     = new MatStack();
    public final MatStack mProj     = new MatStack();
    public final MatStack mColorMat = new MatStack();
    public final MatStack mTexMat   = new MatStack();

    public final DrawSetting.Program       mProgram       = new DrawSetting.Program( this );
    public final DrawSetting.Buffer        mArrayBuf      = new DrawSetting.Buffer( this, GL_ARRAY_BUFFER );
    public final DrawSetting.Buffer        mElementBuf    = new DrawSetting.Buffer( this, GL_ELEMENT_ARRAY_BUFFER );
    public final DrawSetting.Buffer        mUniformBuf    = new DrawSetting.Buffer( this, GL_UNIFORM_BUFFER );


    public final DrawSetting.Blend         mBlend         = new DrawSetting.Blend( this );
    public final DrawSetting.BlendColor    mBlendColor    = new DrawSetting.BlendColor( this );
    public final DrawSetting.ColorMask     mColorMask     = new DrawSetting.ColorMask( this );
    public final DrawSetting.CullFace      mCullFace      = new DrawSetting.CullFace( this );
    public final DrawSetting.DepthMask     mDepthMask     = new DrawSetting.DepthMask( this );
    public final DrawSetting.DepthTest     mDepthTest     = new DrawSetting.DepthTest( this );
    public final DrawSetting.Fog           mFog           = new DrawSetting.Fog( this );
    public final DrawSetting.LineWidth     mLineWidth     = new DrawSetting.LineWidth( this );
    public final DrawSetting.PolygonOffset mPolygonOffset = new DrawSetting.PolygonOffset( this );
    public final DrawSetting.ScissorTest   mScissorTest   = new DrawSetting.ScissorTest( this );
    public final DrawSetting.StencilTest   mStencilTest   = new DrawSetting.StencilTest( this );
    public final DrawSetting.StencilOp     mStencilOp     = new DrawSetting.StencilOp( this );
    public final DrawSetting.Viewport      mViewport      = new DrawSetting.Viewport( this );

    public final ShaderManager mShaderMan = new ShaderManager();
    public final FontManager   mFontMan   = new FontManager();

    public final Vec2        mWorkVec2   = new Vec2();
    public final Vec3        mWorkVec3   = new Vec3();
    public final Vec4        mWorkVec4   = new Vec4();
    public final Mat3        mWorkMat3   = new Mat3();
    public final Mat4        mWorkMat4   = new Mat4();
    public final ByteBuffer  mWorkBytes  = DrawUtil.alloc( 16 * 4 );
    public final FloatBuffer mWorkFloats = DrawUtil.allocFloats( 16 );
    public final Rect        mWorkRect   = new Rect();

    private final DrawStream mStream = new DrawStream();


    public DrawEnv() {}


    public void checkErr() {
        DrawUtil.checkErr( mGl );
    }


    public DrawStream drawStream() {
        return mStream;
    }


    public FontManager fontManager() {
        return mFontMan;
    }


    public ShaderManager shaderManager() {
        return mShaderMan;
    }


    /**
     * Should be called every frame.

     * @param gld                   Sets the GLContext.
     * @param optContextViewport    Sets the viewport of the whole rendering if doing tiled rendering.
     */
    public void init( GLAutoDrawable gld, Rect optContextViewport ) {
        mGld = gld;
        mGl = (GL3)gld.getGL();
        if( optContextViewport != null ) {
            mContextViewport.set( optContextViewport );
        } else {
            mContextViewport.x0 = 0;
            mContextViewport.y0 = 0;
            mContextViewport.x1 = gld.getSurfaceWidth();
            mContextViewport.y1 = gld.getSurfaceHeight();
        }
        mStream.init( this );
    }


    public void dispose( GLAutoDrawable gld ) {}

}
