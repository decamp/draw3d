package bits.draw3d.nodes;

import javax.media.opengl.GL;

import static javax.media.opengl.GL.*;


/**
 * @author Philip DeCamp
 */
public class OffsetNode extends DrawNodeAdapter {


    public static OffsetNode createFillOffset() {
        return createFillOffset( false, 0f, 0f );
    }

    public static OffsetNode createFillOffset( boolean enable, float factor, float units ) {
        return new OffsetNode( GL_POLYGON_OFFSET_FILL, enable, factor, units );

    }

    public static OffsetNode createLineOffset() {
        return createLineOffset( false, 0f, 0f );
    }

    public static OffsetNode createLineOffset( boolean enable, float factor, float units ) {
        return new OffsetNode( GL_POLYGON_OFFSET_LINE, enable, factor, units );

    }

    public static OffsetNode createPointOffset() {
        return createPointOffset( false, 0f, 0f );
    }

    public static OffsetNode createPointOffset( boolean enable, float factor, float units ) {
        return new OffsetNode( GL_POLYGON_OFFSET_POINT, enable, factor, units );

    }


    private final int mTarget;

    private boolean mEnable;
    private float   mFactor;
    private float   mUnits;

    private       boolean mBoolRevert  = false;
    private final float[] mFloatRevert = { 0, 0 };


    private OffsetNode( int target, boolean enable, float factor, float units ) {
        mTarget = target;
        mEnable = enable;
        mFactor = factor;
        mUnits = units;
    }


    public int target() {
        return mTarget;
    }

    public boolean enable() {
        return mEnable;
    }

    public OffsetNode enable( boolean enable ) {
        mEnable = enable;
        return this;
    }

    public float factor() {
        return mFactor;
    }

    public OffsetNode factor( float factor ) {
        mFactor = factor;
        return this;
    }

    public float units() {
        return mUnits;
    }

    public OffsetNode units( float units ) {
        mUnits = units;
        return this;
    }


    public void pushDraw( GL gl ) {
        mBoolRevert = gl.glIsEnabled( mTarget );
        gl.glGetFloatv( GL_POLYGON_OFFSET_FACTOR, mFloatRevert, 0 );
        gl.glGetFloatv( GL_POLYGON_OFFSET_UNITS, mFloatRevert, 1 );

        if( mEnable ) {
            gl.glEnable( mTarget );
        } else {
            gl.glDisable( mTarget );
        }

        gl.glPolygonOffset( mFactor, mUnits );
    }

    public void popDraw( GL gl ) {
        if( mBoolRevert ) {
            gl.glEnable( mTarget );
        } else {
            gl.glDisable( mTarget );
        }

        gl.glPolygonOffset( mFloatRevert[0], mFloatRevert[1] );
    }

}
