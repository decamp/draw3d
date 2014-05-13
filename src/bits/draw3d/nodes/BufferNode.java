package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import bits.draw3d.context.RenderTile;


/**
 * @author decamp
 */
public class BufferNode extends DrawNodeAdapter {
    
    
    public static BufferNode createVertexNode( int usage ) {
        return new BufferNode( GL_ARRAY_BUFFER, 
                               GL_ARRAY_BUFFER_BINDING, 
                               usage );
    }
    

    public static BufferNode createElementNode( int usage ) {
        return new BufferNode( GL_ELEMENT_ARRAY_BUFFER, 
                               GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                               usage );
    }
    
    
    
    private final int   mType;
    private final int   mGetType;

    private final int[] mRevert = { 0 };
    private final int[] mId     = { 0 };

    private boolean     mNeedInit    = true;
    private ByteBuffer  mCopyBytes   = null;
    private int         mAllocBytes  = -1;
    private int         mUsage       = GL_DYNAMIC_DRAW;
    private int         mCapacity    = 0;
    
    
    private BufferNode( int type, int getType, int usage ) {
        mType    = type;
        mGetType = getType;
        mUsage   = usage;
    }

    
    
    public void pushDraw( GL gl ) {
        gl.glGetIntegerv( mGetType, mRevert, 0 );
        
        if( !mNeedInit ) {
            gl.glBindBuffer( mType, mId[0] );
            return;
        }
        
        mNeedInit = false;
        
        if( mId[0] == 0 ) {
            gl.glGenBuffers( 1, mId, 0 );
        }
        
        gl.glBindBuffer( mType, mId[0] );
        
        if( mCopyBytes != null ) {
            gl.glBufferData( mType, mCopyBytes.remaining(), mCopyBytes, mUsage );
            mCopyBytes = null;
        }
        if( mAllocBytes >= 0 ) {
            gl.glBufferData( mType, mAllocBytes, null, mUsage );
            mAllocBytes = -1;
        }
    }


    public void popDraw( GL gl ) {
        gl.glBindBuffer( mType, mRevert[0] );
    }


    @Override
    public void dispose( GLAutoDrawable gld ) {
        if( mId[0] != 0 ) {
            gld.getGL().glDeleteBuffers( 1, mId, 0 );
            mId[0] = 0;
        }

        mCopyBytes  = null;
        mAllocBytes = -1;
        mNeedInit   = true;
    }
    
    
    public int usage() {
        return mUsage;
    }
    
    /**
     * Sets usage to be used on next buffer allocation. This is,
     * determines usage type to be used on next call to <code>alloc()</code>
     * or <code>buffer()</code>. 
     * <p>
     * Usage types follow the pattern: <code>GL_[STREAM|STATIC|DYNAMIC]_[DRAW_READ_COP]</code>.
     * 
     * @param usage
     */
    public void usage( int usage ) {
        mUsage = usage;
    }
    
    
    public int capacity() {
        return mCapacity;
    }
    
    /**
     * Changes Buffer Object capacity. Causes previous
     * data to be erased. Buffer Object may not be updated
     * until subsequent call to <code>pushDraw(gl)</code>.
     * 
     * @param capacity
     */
    public void alloc( int capacity ) {
        if( capacity < 0 ) {
            return;
        }
        
        mAllocBytes = capacity;
        mCopyBytes  = null;
        mCapacity   = capacity;
        mNeedInit   = true;
    }
    
    /**
     * Fills ByteBuffer with provided data.
     * BufferObject may not be updated until subsequent call to <code>pushDraw(gl)</code>.
     * 
     * @param buf Directly-allocated ByteBuffer containing buffer data.
     */
    public void buffer( ByteBuffer buf ) {
        mAllocBytes = -1;
        mCopyBytes  = buf.duplicate();
        mCapacity   = buf.remaining();
        mNeedInit   = true;
    }

    /**
     * MUST be called while pushed. Retrieves ByteBuffer
     * that can be used to access data in Buffer Object.
     * <p>
     * <code>unmap(gl)</code> MUST be called after user
     * done with ByteBuffer. 
     * 
     * @param gl
     * @param access  Specificies access to buffer. MUST be GL_READ_ONLY, GL_WRITE_ONLY, or GL_READ_WRITE.
     * @return ByteBuffer view of Buffer Object.
     */
    public ByteBuffer map( GL gl, int access ) {
        return gl.glMapBuffer( mType, access );
    }

    /**
     * MUST be called while pushed and before Buffer Object is used
     * for rendering. <code>unmap(gl)</code> commits changes made 
     * to ByteBuffer returned by previous call to <code>map(gl,int)</code>.
     *  
     * @param gl
     * @return 
     */
    public boolean unmap( GL gl ) {
        return gl.glUnmapBuffer( mType );
    }




    
    
    @Deprecated
    public static BufferNode newVertexInstance(ByteBuffer buffer, int usage) {
        BufferNode ret = new BufferNode( GL_ARRAY_BUFFER, 
                                         GL_ARRAY_BUFFER_BINDING, 
                                         usage );
        ret.buffer( buffer );
        return ret;
    }
    

    @Deprecated
    public static BufferNode newVertexInstance(int size, int usage) {
        BufferNode ret = new BufferNode( GL_ARRAY_BUFFER, 
                                         GL_ARRAY_BUFFER_BINDING, 
                                         usage );
        ret.alloc( size );
        return ret;
    }
    
    
    @Deprecated
    public static BufferNode newElementInstance(ByteBuffer buffer, int usage) {
        BufferNode ret = new BufferNode( GL_ELEMENT_ARRAY_BUFFER, 
                                         GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                                         usage );
        ret.buffer( buffer );
        return ret;
    }
    

    @Deprecated
    public static BufferNode newElementInstance( int size, int usage ) {
        BufferNode ret = new BufferNode( GL_ELEMENT_ARRAY_BUFFER, 
                                         GL_ELEMENT_ARRAY_BUFFER_BINDING,  
                                         usage );
        ret.alloc( size );
        return ret;
    }
    
    
    @Deprecated
    public static RenderModule newVertexModule() {
        return newModule( GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, null, -1, GL_STATIC_DRAW );
    }
    
    
    @Deprecated
    public static RenderModule newVertexModule( ByteBuffer buffer, int usage ) {
        return newModule( GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, buffer, 0, usage );
    }
    
    
    @Deprecated
    public static RenderModule newVertexModule( int size, int usage ) {
        return newModule( GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, null, size, usage );
    }
    

    @Deprecated
    public static RenderModule newElementModule() {
        return newModule( GL_ELEMENT_ARRAY_BUFFER, 
                          GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                          null, 
                          -1, 
                          GL_STATIC_DRAW );
    }
    

    @Deprecated
    public static RenderModule newElementModule( ByteBuffer buffer, int usage ) {
        return newModule( GL_ELEMENT_ARRAY_BUFFER, 
                          GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                          buffer, 
                          0, 
                          usage );
    }
    

    @Deprecated
    public static RenderModule newElementModule( int size, int usage ) {
        return newModule( GL_ELEMENT_ARRAY_BUFFER, 
                          GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                          null, 
                          size, 
                          usage );
    }
    
    
    private static RenderModule newModule( final int type, 
                                           final int getType, 
                                           ByteBuffer buffer, 
                                           final int alloc, 
                                           final int usage ) 
    {
        final ByteBuffer data = buffer == null ? null : buffer.duplicate();
        NodeFactory<DrawNode> factory = new NodeFactory<DrawNode>() {
            public DrawNode create( RenderTile tile ) {
                BufferNode ret = new BufferNode( type, getType, usage );
                if( data != null ) {
                    ret.buffer( data );
                } else if( alloc >= 0 ) {
                    ret.alloc( alloc );
                }
                return ret;
            }
        };

        RenderModuleBuilder b = new RenderModuleBuilder();
        b.addFactory( DrawNode.class, factory, false );
        return b.build();
    }


    @Deprecated public static BufferNode newVertexInstance( int usage ) {
        return new BufferNode( GL_ARRAY_BUFFER, 
                               GL_ARRAY_BUFFER_BINDING, 
                               usage );
    }
    

    @Deprecated public static BufferNode newElementInstance( int usage ) {
        return new BufferNode( GL_ELEMENT_ARRAY_BUFFER, 
                               GL_ELEMENT_ARRAY_BUFFER_BINDING, 
                               usage );
    }
    
}
