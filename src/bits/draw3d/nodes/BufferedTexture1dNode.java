package bits.draw3d.nodes;

import java.nio.*;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public final class BufferedTexture1dNode extends Texture1dNode {


    public static BufferedTexture1dNode newInstance() {
        return new BufferedTexture1dNode();
    }
    
    
    public static BufferedTexture1dNode newInstance( ByteBuffer buf, int intFormat, int format, int dataType, int width ) {
        BufferedTexture1dNode ret = new BufferedTexture1dNode();
        ret.buffer( buf, intFormat, format, dataType, width );
        return ret;
    }
    
    
    
    private ByteBuffer mBuf  = null;
    
    
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
    
}
