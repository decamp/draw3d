/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import javax.media.opengl.GL2ES3;
import java.util.*;

import static javax.media.opengl.GL2ES3.*;


/**
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


    public Bo vbo() {
        return mVbo;
    }


    public void vbo( Bo vbo ) {
        mVbo = vbo;
        mNeedInit = true;
    }


    public Bo ibo() {
        return mIbo;
    }


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


    public void addAttribute( int location, int compNum, int compType, boolean normalize ) {
        addAttribute( new VaoMember( location, compNum, compType, normalize ) );
    }


    public void addAttribute( int location, int compNum, int compType, boolean normalize, int stride, int offset ) {
        addAttribute( new VaoMember( location, compNum, compType, normalize, stride, offset ) );

    }


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
            for( VaoMember va: mAttribs ) {
                va.enable( gl );
            }
        } else {
            gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
        }
        if( mIbo != null ) {
            mIbo.bind( d );
        } else {
            gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
        }
        d.checkErr();
    }

}
