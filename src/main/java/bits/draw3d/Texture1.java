/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.nio.ByteBuffer;
import static com.jogamp.opengl.GL2.*;

/**
 * @author decamp
 */
public final class Texture1 extends AbstractTexture {


    private ByteBuffer mBuf = null;


    public Texture1() {
        super( GL_TEXTURE_1D );
        param( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
    }


    public synchronized void buffer( ByteBuffer buf, 
                                     int intFormat, 
                                     int format, 
                                     int dataType, 
                                     int width )
    {
        if( buf == null ) {
            if( mBuf == null ) {
                return;
            }
            
            super.format( -1, -1, -1 );
            super.size( -1, -1 );
            mBuf = null;
        } else {
            super.format( intFormat, format, dataType );
            super.size( width, 1 );
            mBuf = buf;
        }
        
        fireAlloc();
    }

    @Override
    public void dispose( DrawEnv g  ) {
        super.dispose( g );
        mBuf = null;
    }

    @Override
    protected void doAlloc( DrawEnv g ) {
        g.mGl.glTexImage1D( GL_TEXTURE_1D,
                            0, //level
                            internalFormat(),
                            width(),
                            0, //border
                            format(),
                            dataType(),
                            mBuf );
        mBuf = null;
    }

}
