package bits.draw3d.nodes;

import javax.media.opengl.GL;

import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class LightNode extends DrawNodeAdapter {

    private static int sInstanceCount = 0;


    private final int mLightId;

    private final LightParams mRevertParams = new LightParams();
    private final LightParams mParams;
    private final byte[] mRevertOn = { 0 };


    public LightNode( int lightId, LightParams optParams ) {
        mLightId = lightId;
        mParams = optParams != null ? optParams : new LightParams();
    }


    public int lightId() {
        return mLightId;
    }


    public LightParams lightParams() {
        return mParams;
    }


    @Override
    public void pushDraw( GL gl ) {
        gl.glGetBooleanv( mLightId, mRevertOn, 0 );

        if( mRevertOn[0] != 0 ) {
            mRevertParams.read( gl, mLightId );
        }

        if( mParams == null ) {
            gl.glDisable( mLightId );
        } else {
            gl.glEnable( mLightId );
            mParams.write( gl, mLightId );
        }
    }


    @Override
    public void popDraw( GL gl ) {
        if( mRevertOn[0] == 0 ) {
            gl.glDisable( mLightId );
        } else {
            gl.glEnable( mLightId );
            mRevertParams.write( gl, mLightId );
        }
    }



    @Deprecated public static LightNode newInstance() {
        return newInstance( null );
    }


    @Deprecated public static LightNode newInstance( LightParams params ) {
        int id;

        synchronized( LightNode.class ) {
            id = GL_LIGHT0 + sInstanceCount++ % 8;
        }

        return newInstance( id, params );
    }


    @Deprecated public static LightNode newInstance( int id, LightParams params ) {
        if( params == null ) {
            params = new LightParams();
        }

        return new LightNode( id, params );
    }


}
