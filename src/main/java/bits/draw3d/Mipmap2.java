/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import static com.jogamp.opengl.GL2ES2.GL_TEXTURE_2D;


/**
 * @author decamp
 */
public class Mipmap2 extends AbstractTexture {


    private ByteBuffer mBuf = null;


    public Mipmap2() {
        super( GL_TEXTURE_2D );
    }
    
    
    
    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            int[] format = new int[4];
            ByteBuffer buf = DrawUtil.imageToByteBuffer( image, null, format );
            buffer( buf, format[0], format[1], format[2], image.getWidth(), image.getHeight() );
        }
    }


    public synchronized void buffer( ByteBuffer buf,
                                     int intFormat,
                                     int format,
                                     int dataType,
                                     int w,
                                     int h )
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
            super.size( w, h );
            mBuf = buf.duplicate();
        }
        
        fireAlloc();
    }

    @Override
    public void format( int intFormat, int format, int dataType ) {}

    @Override
    public void size( int w, int h ) {}

    @Override
    public void dispose( DrawEnv g ) {
        super.dispose( g );
        mBuf = null;
    }

    @Override
    protected synchronized void doAlloc( DrawEnv g ) {
        g.mGl.glTexImage2D( GL_TEXTURE_2D,
                            0, //level
                            internalFormat(),
                            width(),
                            height(),
                            0, // border
                            format(),
                            dataType(),
                            mBuf );
        g.mGl.glGenerateMipmap( GL_TEXTURE_2D );
        mBuf = null;
    }

}