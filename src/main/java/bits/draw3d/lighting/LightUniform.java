/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.lighting;

import bits.draw3d.*;
import bits.math3d.*;

import static javax.media.opengl.GL2ES2.*;


/**
 * Stores uniforms for standard lights. Updates lights then call bind to flush changes and bind UBO.
 *
 * @author Philip DeCamp
 */
public class LightUniform implements DrawUnit {

    private String mOptName;
    private Light[] mLights = null;

    private final Ubo       mUbo;
    private final UboMember mAmbient;
    private final UboMember mDiffuse;
    private final UboMember mSpecular;
    private final UboMember mPos;
    private final UboMember mDir;
    private final UboMember mAttenuation;
    private final UboMember mShape;

    private Vec4[] mArr4 = { null };
    private Vec3[] mArr3 = { null };


    public LightUniform( int lightNum ) {
        this( lightNum, Uniforms.defaultBlockBinding( "LIGHTS" ) );
    }


    public LightUniform( int lightNum, int bindingLoc ) {
        mLights = new Light[lightNum];
        mUbo = new Ubo();
        mUbo.bindLocation( bindingLoc );
        mAmbient     = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "AMBIENT" );
        mDiffuse     = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "DIFFUSE" );
        mSpecular    = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "SPECULAR" );
        mPos         = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "POS" );
        mDir         = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "DIR" );
        mAttenuation = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "ATTENUATION" );
        mShape       = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "SHAPE" );
        mUbo.allocMembersBuffer();
    }


    /**
     * Updates material writeBuffer. UBO might not be updated until next bind.
     */
    public void set( int ind, Light light ) {
        mLights[ind] = light;
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
        writeBuffer( d );
        mUbo.bind( d );
    }


    public void bind( DrawEnv d, int location ) {
        writeBuffer( d );
        mUbo.bind( d, location );
    }

    @Override
    public void unbind( DrawEnv d ) {
        unbind( d );
    }


    public void unbind( DrawEnv d, int location ) {
        mUbo.unbind( d );
    }


    private void writeBuffer( DrawEnv d ) {
        final Vec4[] a4 = mArr4;
        final Vec3[] a3 = mArr3;
        final Vec4 v4 = d.mWorkVec4;
        final Vec3 v3 = d.mWorkVec3;

        // Get view and normal matrices.
        Mat4 view = d.mView.get();
        Mat4 work = d.mWorkMat4;
        Mat3 norm = d.mWorkMat3;
        Mat.invert( view, work );
        Mat.put( work, norm );
        Mat.transpose( norm, norm );

        for( int ind = 0; ind < mLights.length; ind++ ) {
            Light light = mLights[ind];
            if( light == null ) {
                continue;
            }

            a4[0] = light.mAmbient;
            mAmbient.set( ind, a4, 0, 1 );
            a4[0] = light.mDiffuse;
            mDiffuse.set( ind, a4, 0, 1 );
            a4[0] = light.mSpecular;
            mSpecular.set( ind, a4, 0, 1 );

            Mat.mult( view, (Vec3)light.mPos, v4 );
            a4[0] = v4;
            mPos.set( ind, a4, 0, 1 );

            Mat.mult( norm, light.mDir, v3 );
            Vec.normalize( v3 );
            a3[0] = v3;
            mDir.set( ind, a3, 0, 1 );

            a3[0] = light.mAttenuation;
            mAttenuation.set( ind, a3, 0, 1 );

            Vec4 shape = light.mShape;
            v4.x = (float)( shape.x > Math.PI ? -2.0 : Math.cos( shape.x ) );
            v4.y = shape.y;
            v4.z = shape.z;
            v4.w = shape.w;
            a4[0] = v4;
            mShape.set( ind, a4, 0, 1 );
        }
    }

}


