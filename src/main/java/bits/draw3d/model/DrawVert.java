/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model;

import bits.draw3d.PosObject;
import bits.math3d.*;


/**
 * @author Philip DeCamp
 */
public class DrawVert implements PosObject {

    public Vec3    mPos;
    public float[] mTex;
    public Vec3    mNorm;
    public Vec4    mColor; //RGBA

    public transient int mVboPos = -1;


    public DrawVert() {}


    public DrawVert( float x, float y, float z ) {
        mPos = new Vec3( x, y, z );
    }


    public DrawVert( Vec3 posRef, float[] texRef, Vec3 normRef, Vec4 color ) {
        mPos   = posRef;
        mTex   = texRef;
        mNorm  = normRef;
        mColor = color;
    }



    @Override
    public Vec3 pos() {
        return mPos;
    }

    @Override
    public String toString() {
        return mPos == null ? "<null_vert>" : mPos.toString();
    }

}
