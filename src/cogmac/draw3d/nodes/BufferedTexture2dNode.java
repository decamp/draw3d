package cogmac.draw3d.nodes;

import java.awt.image.BufferedImage;
import java.nio.*;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public final class BufferedTexture2dNode extends Texture2dNode {
    

    public static BufferedTexture2dNode newInstance() {
        return new BufferedTexture2dNode();
    }
    
    
    public static BufferedTexture2dNode newInstance( BufferedImage image ) {
        BufferedTexture2dNode ret = newInstance();
        ret.buffer( image );
        return ret;
    }
    
    
    public static BufferedTexture2dNode newInstance( ByteBuffer buf,
                                                     int intFormat, 
                                                     int format,
                                                     int dataType,
                                                     int w,
                                                     int h )
    {
        BufferedTexture2dNode ret = newInstance();
        ret.buffer( buf, intFormat, format, dataType, w, h );
        return ret;
    }

    
    private ByteBuffer mBuf = null;
    
    
    
    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            buffer( toDirectBuffer( image ),
                    GL_RGBA,
                    GL_RGBA,
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
        gl.glTexImage2D( GL_TEXTURE_2D,
                         0, //level
                         internalFormat(),
                         width(),
                         height(),
                         0, // border
                         format(),
                         dataType(),
                         mBuf );
    }
    
    
    
    static ByteBuffer toDirectBuffer( BufferedImage image ) {
        int w = image.getWidth();
        int h = image.getHeight();
        
        ByteBuffer buf   = ByteBuffer.allocateDirect( w * h * 4 );
        buf.order( ByteOrder.BIG_ENDIAN );
        IntBuffer intBuf = buf.asIntBuffer();
        int[] row = new int[w];
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, row, 0, w );
            
            for( int x = 0; x < w; x++ ) {
                row[x] = ( row[x] << 8 ) | ( row[x] >>> 24 ); 
            }
            
            intBuf.put( row );
        }
        
        return buf;
    }

}
