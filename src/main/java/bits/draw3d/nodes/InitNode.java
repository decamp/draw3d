package bits.draw3d.nodes;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNode;
import bits.math3d.Vec;
import bits.math3d.Vec4;

import javax.media.opengl.GLAutoDrawable;
import static javax.media.opengl.GL.*;


/**
 * @author Philip DeCamp
 */
public final class InitNode implements DrawNode {

    private GLAutoDrawable mGld = null;
    private DrawEnv mEnv = null;

    private final Vec4 mClearColor = new Vec4( 0, 0, 0, 1 );
    private int  mClearBits  = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT;
    private int  mDrawBuffer = GL_BACK;

    private int     mDoubleBuffered = 0; //-1 false, 0 unknown, 1 true
    private boolean mAutoFlush      = true;
    private boolean mAutoSwap       = true;
    private boolean mDoAutoFlush    = false;


    public InitNode() {}


    public boolean autoFlush() {
        return mAutoFlush;
    }


    public void autoFlush( boolean autoFlush ) {
        mAutoFlush = autoFlush;
        updateAutoFlush();
    }


    public boolean autoSwap() {
        return mAutoSwap;
    }


    public void autoSwap( boolean autoSwap ) {
        mAutoSwap = autoSwap;

        if( mGld != null ) {
            if( mDoubleBuffered >= 0 ) {
                mGld.setAutoSwapBufferMode( autoSwap );
            } else {
                mGld.setAutoSwapBufferMode( false );
            }
        }

        updateAutoFlush();
    }


    public int getClearBits() {
        return mClearBits;
    }


    public void setClearBits( int bits ) {
        mClearBits = bits;
    }


    public void addClearBits( int bit ) {
        mClearBits |= bit;
    }


    public void removeClearBits( int bits ) {
        mClearBits &= ~bits;
    }


    public Vec4 getClearColor() {
        return new Vec4( mClearColor );
    }


    public void setClearColor( float r, float g, float b, float a ) {
        Vec.put( r, g, b, a, mClearColor );
    }


    public void init( DrawEnv g ) {
        mGld = g.mGld;
        if( mGld.getChosenGLCapabilities().getDoubleBuffered() ) {
            mDoubleBuffered = 1;
            mGld.setAutoSwapBufferMode( mAutoSwap );
            mDrawBuffer = GL_BACK;
        } else {
            mDoubleBuffered = -1;
            mGld.setAutoSwapBufferMode( false );
            mDrawBuffer = GL_FRONT;
        }
        updateAutoFlush();

        g.mProj.identity();
        g.mView.identity();
        g.mColorMat.identity();
        g.mTexMat.identity();

        g.mBlend.set( true, GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA );
        g.mColorMask.set( true, true, true, true );
        g.mGl.glCullFace( GL_BACK );
        g.mGl.glFrontFace( GL_CCW );
        g.mCullFace.set( false );
        g.mDepthMask.set( true );
        g.mDepthTest.set( true, GL_LESS );
        g.mPolygonOffset.set( false );
        g.mScissorTest.set( false );
        g.mStencilTest.set( false, GL_ALWAYS, 0, 0xFFFFFFFF );
        g.mStencilOp.set( GL_KEEP, GL_KEEP, GL_KEEP );

        g.mGl.glClear( mClearBits );
    }


    public void dispose( DrawEnv g ) {}


    public void pushDraw( DrawEnv g ) {
        g.mGl.glDrawBuffer( mDrawBuffer );
        if( mClearBits != 0 ) {
            g.mGl.glClearColor( mClearColor.x, mClearColor.y, mClearColor.z, mClearColor.w );
            g.mGl.glClear( mClearBits );
        }
    }


    public void popDraw( DrawEnv g ) {
        if( mDoAutoFlush ) {
            g.mGl.glFlush();
        }
    }


    private void updateAutoFlush() {
        mDoAutoFlush = mAutoFlush && !( mAutoFlush && mDoubleBuffered == 1 );
    }

}

