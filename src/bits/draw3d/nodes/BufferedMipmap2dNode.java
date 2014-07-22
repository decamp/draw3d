package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import bits.draw3d.util.Images;


/**
 * @author decamp
 * @depracated Use {@link Mipmap2Node}
 */
@Deprecated public class BufferedMipmap2dNode extends Texture2dNode {

    
    private static final GLU GLU_INST = new GLU();
    private ByteBuffer mBuf = null;
    
    
    public BufferedMipmap2dNode() {
        param( GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR );
        param( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
        param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        param( GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );
    }
    
    
    
    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            ByteBuffer buf = Images.imageToBgraBuffer( image, null );
            buffer( buf,
                    GL_RGBA,
                    GL_BGRA,
                    GL_UNSIGNED_BYTE,
                    image.getWidth(),
                    image.getHeight() );
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
        GLU_INST.gluBuild2DMipmaps( GL_TEXTURE_2D,
                                    internalFormat(),
                                    width(),
                                    height(),
                                    format(),
                                    dataType(),
                                    mBuf );
    }



    @Deprecated public static BufferedMipmap2dNode newInstance() {
        return new BufferedMipmap2dNode();
    }


    @Deprecated public static BufferedMipmap2dNode newInstance( BufferedImage image ) {
        BufferedMipmap2dNode ret = new BufferedMipmap2dNode();
        ret.buffer( image );
        return ret;
    }


    @Deprecated public static BufferedMipmap2dNode newInstance( ByteBuffer buf,
                                                                int intFormat,
                                                                int format,
                                                                int dataType,
                                                                int w,
                                                                int h )
    {
        BufferedMipmap2dNode ret = new BufferedMipmap2dNode();
        ret.buffer( buf, intFormat, format, dataType, w, h );
        return ret;
    }


}