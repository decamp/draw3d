package bits.draw3d.nodes;

import javax.media.opengl.GL;

import java.nio.ByteBuffer;

import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public final class Texture3Node extends AbstractTextureNode {


    private ByteBuffer mBuf = null;


    public Texture3Node() {
        super( GL_TEXTURE_3D, GL_TEXTURE_BINDING_3D );
    }


    public synchronized void buffer( ByteBuffer buf,
                                     int intFormat,
                                     int format,
                                     int dataType,
                                     int w,
                                     int h,
                                     int depth )
    {
        if( buf == null ) {
            if( mBuf == null ) {
                return;
            }
            super.format( -1, -1, -1 );
            super.size( -1, -1 );
            super.depth( -1 );
            mBuf = null;
        } else {
            super.format( intFormat, format, dataType );
            super.size( w, h );
            super.depth( depth );
            mBuf = buf.duplicate();
        }

        fireAlloc();
    }



    @Override
    protected void doAlloc( GL gl ) {
        gl.glTexImage3D( GL_TEXTURE_3D,
                         0, // Level
                         internalFormat(),
                         width(),
                         height(),
                         depth(),
                         0,
                         format(),
                         dataType(),
                         mBuf );
        mBuf = null;
    }

}
