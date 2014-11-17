package bits.draw3d.shader;

import bits.draw3d.*;

import javax.media.opengl.GL2ES2;
import java.util.*;


/**
 * @author Philip DeCamp
 */
public class Program implements DrawUnit {

    protected final List<Shader> mShaders = new ArrayList<Shader>( 3 );
    protected int mId = 0;


    public Program() {}



    public int id() {
        return mId;
    }


    public void addShader( Shader shader ) {
        mShaders.add( shader );
    }


    public void init( DrawEnv d ) {
        GL2ES2 gl = d.mGl;
        mId = gl.glCreateProgram();
        for( Shader s: mShaders ) {
            s.init( gl );
            gl.glAttachShader( mId, s.id() );
        }
        gl.glLinkProgram( mId );
        DrawUtil.checkErr( gl );
    }


    public void dispose( DrawEnv d ) {
        if( mId != 0 ) {
            d.mGl.glDeleteProgram( mId );
            mId = 0;
        }
    }


    public void bind( DrawEnv d ) {
        d.mGl.glUseProgram( mId );
    }


    public void unbind( DrawEnv d ) {
        d.mGl.glUseProgram( 0 );
    }

}
