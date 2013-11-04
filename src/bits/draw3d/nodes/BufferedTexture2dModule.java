package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import java.util.*;
import java.nio.*;
import java.awt.image.BufferedImage;

import bits.draw3d.context.RenderTile;



/**
 * @author decamp
 */
public class BufferedTexture2dModule implements RenderModule {


    public static BufferedTexture2dModule newInstance() {
        return new BufferedTexture2dModule();
    }

    public static BufferedTexture2dModule newInstance( BufferedImage im ) {
        BufferedTexture2dModule ret = new BufferedTexture2dModule();
        ret.buffer( im );
        return ret;
    }

    public static BufferedTexture2dModule newInstance( ByteBuffer buf, int intFormat, int format, int dataType, int w, int h ) {
        BufferedTexture2dModule ret = new BufferedTexture2dModule();
        ret.buffer( buf, intFormat, format, dataType, w, h );
        return ret;
    }
    
    
    
    private ByteBuffer mBuf = null;
    private int mIntFormat  = 0;
    private int mFormat     = 0;
    private int mDataType   = 0;
    private int mWidth      = -1;
    private int mHeight     = -1;
    
    private final Map<Integer,Integer>        mParams = new HashMap<Integer,Integer>();
    private final List<BufferedTexture2dNode> mNodes  = new ArrayList<BufferedTexture2dNode>();


    private BufferedTexture2dModule() {}


    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            ByteBuffer buf = BufferedTexture2dNode.toDirectBuffer( image );
            buffer( buf, GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE, image.getWidth(), image.getHeight() );
        }
    }

    
    public synchronized void buffer( ByteBuffer buf, 
                                     int intFormat, 
                                     int format, 
                                     int dataType, 
                                     int w, 
                                     int h ) 
    {
        mBuf       = buf;
        mIntFormat = intFormat;
        mFormat    = format;
        mDataType  = dataType;
        mWidth     = w;
        mHeight    = h;
        
        for( BufferedTexture2dNode n : mNodes ) {
            n.buffer( buf, intFormat, format, dataType, w, h );
        }
    }

    
    public synchronized void param( int key, int value ) {
        for( BufferedTexture2dNode node : mNodes ) {
            node.param( key, value );
        }
    }
    
    
    public synchronized Object getNodes( Class<?> nodeClass, RenderTile tile ) {
        if( nodeClass != DrawNode.class )
            return null;
        
        BufferedTexture2dNode ret = BufferedTexture2dNode.newInstance();
        
        if( mBuf != null ) {
            ret.buffer( mBuf, mIntFormat, mFormat, mDataType, mWidth, mHeight );
        }
        
        for( Map.Entry<Integer,Integer> e: mParams.entrySet() ) {
            ret.param( e.getKey(), e.getValue() );
        }
        
        mNodes.add( ret );
        return ret;
    }
    
}
