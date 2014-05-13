package bits.draw3d.nodes;

import bits.draw3d.context.RenderTile;
import bits.draw3d.util.Images;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.*;


/**
 * @author decamp
 */
public class Texture2Module implements RenderModule {

    private ByteBuffer mBuf = null;
    private int mIntFormat  = 0;
    private int mFormat     = 0;
    private int mDataType   = 0;
    private int mWidth      = -1;
    private int mHeight     = -1;

    private final Map<Integer,Integer> mParams = new HashMap<Integer,Integer>();
    private final List<Texture2Node>   mNodes  = new ArrayList<Texture2Node>();


    public Texture2Module() {}


    public void buffer( BufferedImage image ) {
        if( image == null ) {
            buffer( null, 0, 0, 0, -1, -1 );
        } else {
            int[] format = new int[4];
            ByteBuffer buf = Images.imageToByteBuffer( image, format );
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
        mBuf       = buf;
        mIntFormat = intFormat;
        mFormat    = format;
        mDataType  = dataType;
        mWidth     = w;
        mHeight    = h;
        
        for( Texture2Node n : mNodes ) {
            n.buffer( buf, intFormat, format, dataType, w, h );
        }
    }

    
    public synchronized void param( int key, int value ) {
        Integer k = key;
        Integer v = value;
         mParams.put( k, v );
        for( Texture2Node node : mNodes ) {
            node.param( k, v );
        }
    }
    
    
    public synchronized Object getNodes( Class<?> nodeClass, RenderTile tile ) {
        if( nodeClass != DrawNode.class ) {
            return null;
        }
        
        Texture2Node ret = new Texture2Node();
        if( mBuf != null ) {
            ret.buffer( mBuf, mIntFormat, mFormat, mDataType, mWidth, mHeight );
        }
        for( Map.Entry<Integer,Integer> e: mParams.entrySet() ) {
            ret.param( e.getKey(), e.getValue() );
        }
        
        mNodes.add( ret );
        return ret;
    }




    @Deprecated public static Texture2Module newInstance() {
        return new Texture2Module();
    }


    @Deprecated public static Texture2Module newInstance( BufferedImage im ) {
        Texture2Module ret = new Texture2Module();
        ret.buffer( im );
        return ret;
    }


    @Deprecated public static Texture2Module newInstance( ByteBuffer buf, int intFormat, int format, int dataType, int w, int h ) {
        Texture2Module ret = new Texture2Module();
        ret.buffer( buf, intFormat, format, dataType, w, h );
        return ret;
    }

}
