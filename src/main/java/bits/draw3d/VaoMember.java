/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import javax.media.opengl.GL2ES2;


/**
 * Vertex Attribute
 *
 * @author Philip DeCamp
 */
public class VaoMember {

    public DrawUnit mBuffer;
    public int      mLocation;
    public int      mCompNum;
    public int      mType;
    public boolean  mNormalize;

    public int      mStride        = -1;
    public int      mOffset        = -1;


    public VaoMember() {}


    public VaoMember( int location, int compNum, int type, boolean normalize ) {
        this( location, compNum, type, normalize, -1, -1 );
    }


    public VaoMember( int location, int compNum, int type, boolean normalize, int stride, int offset ) {
        mLocation  = location;
        mCompNum   = compNum;
        mType      = type;
        mNormalize = normalize;
        mStride    = stride;
        mOffset    = offset;

    }


    public void enable( GL2ES2 gl ) {
        gl.glVertexAttribPointer( mLocation, mCompNum, mType, mNormalize, mStride, mOffset );
        gl.glEnableVertexAttribArray( mLocation );
    }


    public void disable( GL2ES2 gl ) {
        gl.glVertexAttribPointer( mLocation, mCompNum, mType, mNormalize, mStride, mOffset );
        gl.glEnableVertexAttribArray( mLocation );
    }

}
