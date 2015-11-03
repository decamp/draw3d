/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.lighting;

import bits.draw3d.*;
import bits.math3d.*;
import bits.math3d.func.SinCosTable;

import static com.jogamp.opengl.GL2ES2.*;


/**
 * Stores uniforms for standard lights. Updates lights then call bind to flush changes and bind UBO.
 *
 * @author Philip DeCamp
 */
public class LightUniform implements DrawUnit {

    private final Vec3 mGlobalAmbient = new Vec3();
    private Light[] mLights = null;

    private final Ubo       mUbo;
    private final UboMember mAmbient;
    private final UboMember mColor;
    private final UboMember mPos;
    private final UboMember mDir;
    private final UboMember mFalloff;
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
        mAmbient  = mUbo.addUniform( 1, GL_FLOAT_VEC3, "AMBIENT" );
        mColor    = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "COLOR" );
        mPos      = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "POS" );
        mDir      = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "DIR" );
        mFalloff  = mUbo.addUniform( mLights.length, GL_FLOAT_VEC3, "FALLOFF" );
        mShape    = mUbo.addUniform( mLights.length, GL_FLOAT_VEC4, "SHAPE" );
        mUbo.allocMembersBuffer();
    }


    /**
     * Sets global ambient light.
     */
    public void ambient( Vec3 color ) {
        Vec.put( color, mGlobalAmbient );
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
        mUbo.unbind( d );
    }


    public void unbind( DrawEnv d, int location ) {
        mUbo.unbind( d );
    }


    private void writeBuffer( DrawEnv d ) {
        final Vec4[] arr4 = mArr4;
        final Vec3[] arr3 = mArr3;
        final Vec4 vec4 = d.mWorkVec4;
        final Vec3 vec3 = d.mWorkVec3;

        // Get view and normal matrices.
        Mat4 view = d.mView.get();
        Mat4 work = d.mWorkMat4;
        Mat3 norm = d.mWorkMat3;
        Mat.invert( view, work );
        Mat.put( work, norm );
        Mat.transpose( norm, norm );

        mAmbient.set( mGlobalAmbient );

        for( int ind = 0; ind < mLights.length; ind++ ) {
            Light light = mLights[ind];
            if( light == null ) {
                continue;
            }

            arr3[0] = light.mColor;
            mColor.set( ind, arr3, 0, 1 );
            Mat.mult( view, light.mPos, vec3 );
            arr3[0] = vec3;
            mPos.set( ind, arr3, 0, 1 );
            Mat.mult( norm, light.mDir, vec3 );
            Vec.normalize( vec3 );
            arr3[0] = vec3;
            mDir.set( ind, arr3, 0, 1 );
            arr3[0] = light.mFalloff;
            mFalloff.set( ind, arr3, 0, 1 );

            if( light.mPositional ) {
                vec4.x = 1f;
                vec4.y = (float)SinCosTable.cos( light.mSpotAngle );
                vec4.z = light.mSpotExp;
            } else {
                vec4.x = 0f;
                vec4.y = -1f;
                vec4.z = 0f;
            }
            arr4[0] = vec4;
            mShape.set( ind, arr4, 0, 1 );
        }
    }
}


