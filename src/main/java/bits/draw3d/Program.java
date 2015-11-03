/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import com.jogamp.opengl.GL2ES2;
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
        if( mId == 0 ) {
            init( d );
        }
        d.mGl.glUseProgram( mId );
    }


    public void unbind( DrawEnv d ) {
        d.mGl.glUseProgram( 0 );
    }

}
