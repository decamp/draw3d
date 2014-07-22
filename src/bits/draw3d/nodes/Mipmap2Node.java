package bits.draw3d.nodes;

import bits.draw3d.util.DrawUtil;
import bits.draw3d.util.Images;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class Mipmap2Node extends AbstractTextureNode {


    private ByteBuffer mBuf = null;


    public Mipmap2Node() {
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
        DrawUtil.GLU.gluBuild2DMipmaps( GL_TEXTURE_2D,
                                        internalFormat(),
                                        width(),
                                        height(),
                                        format(),
                                        dataType(),
                                        mBuf );
        mBuf = null;
    }



    @Deprecated public static Mipmap2Node newInstance() {
        return new Mipmap2Node();
    }


    @Deprecated public static Mipmap2Node newInstance( BufferedImage image ) {
        Mipmap2Node ret = new Mipmap2Node();
        ret.buffer( image );
        return ret;
    }


    @Deprecated public static Mipmap2Node newInstance( ByteBuffer buf,
                                                                int intFormat,
                                                                int format,
                                                                int dataType,
                                                                int w,
                                                                int h )
    {
        Mipmap2Node ret = new Mipmap2Node();
        ret.buffer( buf, intFormat, format, dataType, w, h );
        return ret;
    }


}