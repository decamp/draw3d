package bits.draw3d.nodes;

import bits.draw3d.util.Images;

import javax.media.opengl.GL;
import java.awt.image.BufferedImage;
import java.nio.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;


/**
 * @author decamp
  */
public final class Texture2Node extends AbstractTextureNode {


    private ByteBuffer mBuf = null;


    public Texture2Node() {
        super( GL_TEXTURE_2D, GL_TEXTURE_BINDING_2D );
    }


    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            int[] format = new int[4];
            ByteBuffer buf = Images.imageToByteBuffer( image, null, format );
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
    public void dispose( GL gl ) {
        super.dispose( gl );
        mBuf = null;
    }

    @Override
    protected synchronized void doAlloc( GL gl ) {
        gl.glTexImage2D( GL_TEXTURE_2D,
                         0, //level
                         internalFormat(),
                         width(),
                         height(),
                         0, // border
                         format(),
                         dataType(),
                         mBuf );
        mBuf = null;
    }

}
