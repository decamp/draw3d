/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.lighting;


import bits.draw3d.*;
import bits.math3d.Vec4;

import static javax.media.opengl.GL3.*;

/**
 * Stores uniforms for materials. Update materials then call bind to flush changes and bind UBO.
 *
 *
 * @author Philip DeCamp
 */
public class MaterialUniform implements DrawUnit {

    private Material[] mMaterials = null;

    private final Ubo mUbo;
    private final UboMember mAmbient;
    private final UboMember mDiffuse;
    private final UboMember mSpecular;
    private final UboMember mEmissive;
    private final UboMember mShininess;

    private Vec4[]  mVec   = { null };
    private float[] mFloat = { 0f };


    public MaterialUniform( int materialNum ) {
        this( materialNum, Uniforms.defaultBlockBinding( "MATERIALS" ) );
    }


    public MaterialUniform( int materialNum, int bindingLoc ) {
        mMaterials = new Material[materialNum];
        mUbo = new Ubo();
        mUbo.bindLocation( bindingLoc );
        mAmbient   = mUbo.addUniform( materialNum, GL_FLOAT_VEC4,  "AMBIENT"   );
        mDiffuse   = mUbo.addUniform( materialNum, GL_FLOAT_VEC4,  "DIFFUSE"   );
        mSpecular  = mUbo.addUniform( materialNum, GL_FLOAT_VEC4,  "SPECULAR"  );
        mEmissive  = mUbo.addUniform( materialNum, GL_FLOAT_VEC4,  "EMISSIVE"  );
        mShininess = mUbo.addUniform( materialNum, GL_FLOAT,       "SHININESS" );
        mUbo.allocMembersBuffer();
    }


    /**
     * Updates material buffer. UBO might not be updated until next bind.
     */
    public void set( int ind, Material mat ) {
        mMaterials[ind] = mat;
        Vec4[] v = mVec;
        v[0] = mat.mAmbient;
        mAmbient.set( ind, v, 0, 1 );
        v[0] = mat.mDiffuse;
        mDiffuse.set( ind, v, 0, 1 );
        v[0] = mat.mSpecular;
        mSpecular.set( ind, v, 0, 1 );
        v[0] = mat.mEmissive;
        mEmissive.set( ind, v, 0, 1 );
        mShininess.setComponent( ind, 0, 0, mat.mShininess );
    }


    public void init( DrawEnv d ) {
        mUbo.init( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        mUbo.dispose( d );
    }

    @Override
    public void bind( DrawEnv d ) {
        mUbo.bind( d );
    }


    public void bind( DrawEnv d, int location ) {
        mUbo.bind( d, location );
    }

    @Override
    public void unbind( DrawEnv d ) {
        mUbo.unbind( d );
    }


    public void unbind( DrawEnv d, int location ) {
        mUbo.unbind( d );
    }

}
