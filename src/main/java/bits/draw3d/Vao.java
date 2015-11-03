/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import com.jogamp.opengl.GL2ES3;
import java.util.*;

/**
 * Vertex Attribute Object (VAO) handle for Java. VAOs hold state associated with a
 * VBO and IBO. Most importantly, a VAO holds references to the VBO and IBO themselves,
 * and when the VAO is bound, it will bind the VBO to GL_ARRAY_BUFFER and the IBO
 * id to GL_ELEMENT_ARRAY_BUFFER. The VAO also binds the vertex layout attributes of the
 * VBO, represented as a list of VaoMember objects.
 * <p>
 * WARNING: The big gotcha with the VAO is that you must bind the VAO, then bind the VBO,
 * then configure the layout. This means that if you are using the VAO, you MUST specify
 * the VBO and IBO that will be used with the VAO before initializing or binding the VAO.
 * This also means that initialization of the VAO will cause the VBO and IBO to be initialized
 * perhaps before you think. However, Vao DOES NOT dispose or otherwise "own" associated
 * VBOs or IBO.
 * <p>
 * Vao will only work with ONE VBO at a time. If you need to your VAO to reference multiple VBOs,
 * you'll need to call GL directly.
 *
 * @author Philip DeCamp
 */
public class Vao implements DrawUnit {

    private final int[] mId = { 0 };
    private final List<VaoMember> mAttribs = new ArrayList<VaoMember>( 5 );

    private Bo mVbo = null;
    private Bo mIbo = null;

    private boolean mNeedInit = true;


    public Vao() {}


    public Vao( Bo vbo, Bo ibo ) {
        mVbo = vbo;
        mIbo = ibo;
    }


    /**
     * @return VBO being used with this VAO, or {@code null} if none.
     */
    public Bo vbo() {
        return mVbo;
    }

    /**
     * Sets the VBO to be used with this VAO. Changes will not
     * take effect until calling {@link #bind} or {@link #init}.
     *
     * @param vbo The VBO to use with this VAO, or {@code null} if none.
     */
    public void vbo( Bo vbo ) {
        mVbo = vbo;
        mNeedInit = true;
    }

    /**
     * @return IBO being used with this VAO, or {@code null} if none.
     */
    public Bo ibo() {
        return mIbo;
    }

    /**
     * Sets the IBO to be used with this VAO. Changes will not
     * take effect until calling {@link #bind} or {@link #init}.
     *
     * @param ibo The IBO to use with this VAO, or {@code null} if none.
     */
    public void ibo( Bo ibo ) {
        mIbo = ibo;
        mNeedInit = true;
    }

    /**
     * @return The stride value of the first VaoMember object, or 0 if none.
     */
    public int firstStride() {
        return mAttribs.isEmpty() ? 0 : mAttribs.get( 0 ).mStride;
    }

    /**
     * Adds VAO attribute. Changes will take effect upon next call to {@link #init} or {@link #bind}.
     * Stride of attribute will inferred upon a call to {@link #packFormat()}.
     *
     * @param location   Shader attribute location.
     * @param compNum    Number of components.
     * @param compType   Component type. EG, {@code GL_FLOAT}, {@code GL_UNSIGNED_BYTE}.
     * @param normalize  Enable unit normalization.
     */
    public void addAttribute( int location, int compNum, int compType, boolean normalize ) {
        addAttribute( new VaoMember( location, compNum, compType, normalize ) );
    }

    /**
     * Adds VAO attribute. Changes will take effect upon next call to {@link #init} or {@link #bind}.
     *
     * @param location   Shader attribute location.
     * @param compNum    Number of components.
     * @param compType   Component type. EG, {@code GL_FLOAT}, {@code GL_UNSIGNED_BYTE}.
     * @param normalize  Enable unit normalization.
     *                   IE, if {@code compType == GL_UNSIGNED_BYTE}, divide components by 255.
     * @param stride     Bytes between vertices in buffer.
     * @param offset     Buffer offset for this attribute in bytes.
     */
    public void addAttribute( int location, int compNum, int compType, boolean normalize, int stride, int offset ) {
        addAttribute( new VaoMember( location, compNum, compType, normalize, stride, offset ) );

    }

    /**
     * Adds VAO attribute. Changes will take effect upon next call to {@link #init} or {@link #bind}.
     *
     * @param attrib Vertex attribute.
     */
    public void addAttribute( VaoMember attrib ) {
        mAttribs.add( attrib );
        mNeedInit = true;
    }


    public void removeAttribute( VaoMember attrib ) {
        mAttribs.remove( attrib );
        mNeedInit = true;
    }

    /**
     * Automatically sets the offset and stride values of vert attributes for a packed vertex format.
     */
    public void packFormat() {
        packFormat( ByteAlignment.BYTE4 );
    }

    /**
     * Automatically sets the offset and stride values of vert attributes.
     *
     * @param alignment  Minimum byte alignment of object.
     */
    public void packFormat( ByteAlignment alignment ) {
        int off = 0;
        for( VaoMember v: mAttribs ) {
            v.mOffset = off;
            off += alignment.size( ComponentType.fromGl( v.mType ), v.mCompNum );
        }

        // Alignment for next vertex.
        for( VaoMember v: mAttribs ) {
            v.mStride = off;
        }

        mNeedInit = true;
    }


    public List<VaoMember> attributesRef() {
        return mAttribs;
    }


    @Override
    public void init( DrawEnv d ) {
        doInit( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        d.mGl.glDeleteVertexArrays( 1, mId, 0 );
        mId[0] = 0;
    }

    /**
     * Binds VAO, initializing VAO if necessary. Note that this does not
     * call bind on associated VBO or IBO unless initializing.
     */
    @Override
    public void bind( DrawEnv d ) {
        if( mNeedInit ) {
            doInit( d );
        } else {
            d.mGl.glBindVertexArray( mId[0] );
        }
    }

    @Override
    public void unbind( DrawEnv g ) {
        g.mGl.glBindVertexArray( 0 );
    }


    private void doInit( DrawEnv d ) {
        mNeedInit = false;
        GL2ES3 gl = d.mGl;

        if( mId[0] == 0 ) {
            gl.glGenVertexArrays( 1, mId, 0 );
        }

        gl.glBindVertexArray( mId[0] );

        if( mVbo != null ) {
            mVbo.bind( d );
        }

        for( VaoMember va: mAttribs ) {
            va.enable( gl );
        }

        if( mIbo != null ) {
            mIbo.bind( d );
        }

        d.checkErr();
    }

}
