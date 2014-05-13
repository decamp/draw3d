package bits.draw3d.nodes;

import java.nio.*;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * @author decamp
 */
public class DrawBufferNode extends DrawNodeAdapter {


    private DrawBufferParams mParams;
    private BufferNode mVbo;
    private BufferNode mIbo;


    public DrawBufferNode( DrawBufferParams params,
                           ByteBuffer optVertBuffer,
                           int vertUsage,
                           ByteBuffer optIndexBuffer,
                           int indexUsage )
    {
        mParams = params;

        if( optVertBuffer != null ) {
            mVbo = BufferNode.createVertexNode( vertUsage );
            mVbo.buffer( optVertBuffer );
        } else {
            mVbo = null;
        }

        if( optIndexBuffer != null ) {
            mIbo = BufferNode.createElementNode( indexUsage );
            mIbo.buffer( optIndexBuffer );
        } else {
            mIbo = null;
        }
    }


    public DrawBufferParams getParams() {
        return mParams;
    }


    public void pushDraw( GL gl ) {
        if( mVbo != null ) {
            mVbo.pushDraw( gl );
        }
        if( mIbo != null ) {
            mIbo.pushDraw( gl );
        }
        mParams.push( gl );
        mParams.execute( gl );
    }


    public void popDraw( GL gl ) {
        mParams.pop( gl );
        if( mIbo != null ) {
            mIbo.popDraw( gl );
        }
        if( mVbo != null ) {
            mVbo.popDraw( gl );
        }
    }


    public void dispose( GLAutoDrawable gld ) {
        if( mVbo != null ) {
            mVbo.dispose( gld );
        }
        if( mIbo != null ) {
            mIbo.dispose( gld );
        }

    }



    @Deprecated public static DrawBufferNode newInstance( DrawBufferParams params ) {
        return new DrawBufferNode( params, null, 0, null, 0 );
    }


    @Deprecated public static DrawBufferNode newInstance( DrawBufferParams params,
                                              ByteBuffer vertBuffer,
                                              int vertUsage )
    {
        return new DrawBufferNode( params, vertBuffer, vertUsage, null, 0 );
    }


    @Deprecated public static DrawBufferNode newInstance( DrawBufferParams params,
                                              ByteBuffer vertBuffer,
                                              int vertUsage,
                                              ByteBuffer indexBuffer,
                                              int indexUsage )
    {
        return new DrawBufferNode( params, vertBuffer, vertUsage, indexBuffer, indexUsage );
    }



}

