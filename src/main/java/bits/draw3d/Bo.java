/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.nio.ByteBuffer;
import static javax.media.opengl.GL2ES3.*;


/**
 * @author decamp
 */
public class Bo extends DrawUnitAdapter implements DrawNode {


    public static Bo createArrayBuffer( int usage ) {
        return new Bo( GL_ARRAY_BUFFER, usage );
    }


    public static Bo createElementBuffer( int usage ) {
        return new Bo( GL_ELEMENT_ARRAY_BUFFER, usage );
    }


    protected final int mType;
    private int         mUsage;

    // mId[0] == ID, mId[1] == ID OF PREVIOUSLY BOUND BUFFER OBJECT.
    protected final int[] mId        = { 0, 0 };

    private boolean     mNeedInit    = true;
    private ByteBuffer  mCopyBytes   = null;
    private int         mCopyOffset  = -1;
    private int         mAllocBytes  = -1;
    private int         mCapacity    = 0;

    public Bo( int type, int usage ) {
        mType    = type;
        mUsage   = usage;
    }



    /**
     * Changes Buffer Object capacity. Causes previous
     * data to be erased. Buffer Object may not be updated
     * until subsequent call to <code>pushDraw(gl)</code>.
     *
     * @param capacity In bytes
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
        mCopyOffset = -1;
        mCapacity   = buf.remaining();
        mNeedInit   = true;
    }

    /**
     * Fills ByteBuffer with provided data.
     * BufferObject may not be updated until subsequent call to <code>pushDraw(gl)</code>.
     *
     * @param buf Directly-allocated ByteBuffer containing buffer data.
     */
    public void bufferSub( ByteBuffer buf, int offset ) {
        mCopyBytes  = buf.duplicate();
        mCopyOffset = offset >= 0 ? offset : 0;
        mCapacity   = buf.remaining();
        mNeedInit   = true;
    }


    public int capacity() {
        return mCapacity;
    }

    /**
     * @return OpenGL id of underlying VBO, or 0 if not initialized.
     */
    public int id() {
        return mId[0];
    }

    /**
     * MUST be called while pushed. Retrieves ByteBuffer
     * that can be used to access data in Buffer Object.
     * <p>
     * <code>unmap(gl)</code> MUST be called after user
     * done with ByteBuffer.
     *
     * @param access  Specificies access to buffer. MUST be GL_READ_ONLY, GL_WRITE_ONLY, or GL_READ_WRITE.
     * @return ByteBuffer view of Buffer Object.
     */
    public ByteBuffer map( DrawEnv d, int access ) {
        return d.mGl.glMapBuffer( mType, access );
    }

    /**
     * MUST be called while pushed and before Buffer Object is used
     * for rendering. {@code unmap(gl)} commits changes made
     * to ByteBuffer returned by previous call to {@code >map(gl,int)}.
     */
    public boolean unmap( DrawEnv d ) {
        return d.mGl.glUnmapBuffer( mType );
    }


    public int usage() {
        return mUsage;
    }
    
    /**
     * Sets usage to be used on next buffer allocation. This is,
     * determines usage type to be used on next call to <code>alloc()</code>
     * or {@code buffer()}.
     *
     * @param usage One of GL_[STREAMING_STATIC_DYNAMIC]_[DRAW_READ_COPY].
     */
    public void usage( int usage ) {
        mUsage = usage;
    }

    @Override
    public void init( DrawEnv d ) {
        doInit( d );
        unbind( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        if( mId[0] != 0 ) {
            d.mGl.glDeleteBuffers( 1, mId, 0 );
            mId[0] = 0;
        }
        mCopyBytes  = null;
        mAllocBytes = -1;
        mNeedInit   = true;
    }

    @Override
    public void bind( DrawEnv d ) {
        if( !mNeedInit ) {
            d.mGl.glBindBuffer( mType, mId[0] );
        } else {
            doInit( d );
        }
    }

    @Override
    public void unbind( DrawEnv d ) {
        d.mGl.glBindBuffer( mType, 0 );
    }

    @Override
    public void pushDraw( DrawEnv d ) {
        int getType = mType == GL_ARRAY_BUFFER ? GL_ARRAY_BUFFER_BINDING : GL_ELEMENT_ARRAY_BUFFER_BINDING;
        d.mGl.glGetIntegerv( getType, mId, 1 );
        bind( d );
    }

    @Override
    public void popDraw( DrawEnv d ) {
        d.mGl.glBindBuffer( mType, mId[1] );
    }


    private void doInit( DrawEnv d ) {
        mNeedInit = false;
        if( mId[0] == 0 ) {
            d.mGl.glGenBuffers( 1, mId, 0 );
        }

        d.mGl.glBindBuffer( mType, mId[0] );

        if( mAllocBytes >= 0 ) {
            d.mGl.glBufferData( mType, mAllocBytes, null, mUsage );
            mCapacity = mAllocBytes;
            mAllocBytes = -1;
        }
        if( mCopyBytes != null ) {
            int len = mCopyBytes.remaining();
            if( mCopyOffset < 0 ) {
                d.mGl.glBufferData( mType, len, mCopyBytes, mUsage );
                mCapacity = len;
            } else {
                d.mGl.glBufferSubData( mType, mCopyOffset, len, mCopyBytes );
            }
            mCopyBytes = null;
        }
    }

}
