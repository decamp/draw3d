/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.lighting;

import bits.math3d.*;

/**
 * @author Philip DeCamp
 */
public class Light {

    public static final int TYPE_AMBIENT     = 0;
    public static final int TYPE_POINT       = 1;
    public static final int TYPE_DIRECTIONAL = 2;
    public static final int TYPE_SPOTLIGHT   = 3;

    public int mType;

    public Vec4 mAmbient;
    public Vec4 mDiffuse;
    public Vec4 mSpecular;
    public Vec4 mPos;
    public Vec3 mDir;

    /**
     * Holds constant, linear, and quadratic attenuation coefficients.
     * The strength of the light is proportional to
     * {@code 1.0 / ( mAttenuation * Vec3( 1.0, dist, dist * dist ) ) },
     * where {@code dist} is the distance between the light and a given point.
     */
    public Vec3 mAttenuation;

    /**
     * Holds type-specific parameters that define the distribution of the light.
     *
     * <p>For a spotlight type light, this value holds
     * {@code [ cutoff, spreadExp ]}, where:<br>
     * {@code cutoff}: the angle of the maximum angle of the light ( 0 = no spread, 0.5 * PI = max ) <br>
     * {@code spreadExp}: determines spread of light where light strength is computed as
     * {@code cos(ang)^spreadExp }.
     * 0 = member, 1 = cosine func.
     */
    public Vec4 mShape;


    public Light() {
        this( TYPE_AMBIENT, null, null, null, null, null, null, null );
    }


    public Light( int type,
                  Vec4 ambient,
                  Vec4 diffuse,
                  Vec4 specular,
                  Vec4 pos,
                  Vec3 dir,
                  Vec3 attenuation,
                  Vec4 shape )
    {
        float posw = type == TYPE_DIRECTIONAL ? 0f : 1f;

        mType        = type;
        mAmbient     = ambient     == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( ambient );
        mDiffuse     = diffuse     == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( diffuse );
        mSpecular    = specular    == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( specular );

        if( pos != null ) {
            mPos = new Vec4( pos );
        } else if( type == TYPE_DIRECTIONAL ) {
            mPos = new Vec4( 0, 0, 0, 0 );
        } else {
            mPos = new Vec4( 0, 0, 0, 1 );
        }

        mDir         = dir         == null ? new Vec3()             : new Vec3( dir );
        mAttenuation = attenuation == null ? new Vec3( 1, 0, 0 )    : new Vec3( attenuation );

        if( shape != null ) {
            mShape = new Vec4( shape );
        } else if( type == TYPE_SPOTLIGHT ) {
            mShape = new Vec4( 0.5f * (float)Ang.PI, 1f, 0, 0 );
        } else {
            mShape = new Vec4( 10f, 0, 0, 0 );
        }
    }


    public void applyTypeToParams() {
        if( mType == TYPE_DIRECTIONAL ) {
            mPos.w = 0;
        } else {
            mPos.w = 1;
        }

        if( mType != TYPE_SPOTLIGHT ) {
            mShape.x = 2;
            mShape.y = 0;
        }
    }

}
