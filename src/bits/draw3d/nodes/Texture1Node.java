package bits.draw3d.nodes;

import javax.media.opengl.GL;
import java.nio.ByteBuffer;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL.GL_LINEAR;
import static javax.media.opengl.GL.GL_TEXTURE_MAG_FILTER;


/**
 * @author decamp
 */
public final class Texture1Node extends AbstractTextureNode {


    private ByteBuffer mBuf = null;


    public Texture1Node() {
        super( GL_TEXTURE_1D, GL_TEXTURE_BINDING_1D );
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
    public void format( int intFormat, int format, int dataType ) {}

    @Override
    public void size( int w, int h ) {}

    @Override
    public void dispose( GL gl ) {
        super.dispose( gl );
        mBuf = null;
    }

    @Override
    protected void doAlloc( GL gl ) {
        gl.glTexImage1D( GL_TEXTURE_1D, 
                         0, //level
                         internalFormat(), 
                         width(),
                         0, //border
                         format(),
                         dataType(),
                         mBuf );
    }



    @Deprecated public static Texture1Node newInstance() {
        return new Texture1Node();
    }


    @Deprecated public static Texture1Node newInstance( ByteBuffer buf, int intFormat, int format, int dataType, int width ) {
        Texture1Node ret = new Texture1Node();
        ret.buffer( buf, intFormat, format, dataType, width );
        return ret;
    }


}
