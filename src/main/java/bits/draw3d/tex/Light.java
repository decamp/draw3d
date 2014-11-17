/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.tex;

import bits.draw3d.actors.Actor;
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
    public Vec3 mPos;
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
     * {@code cutoff}: the cosine of the maximum angle of the light ( 1 = no spread, 0 = 0.5*PI ) <br>
     * {@code spreadExp}: determines spread of light where light strength is computed as
     * {@code cos(ang)^spreadExp }.
     * 0 = uniform, 1 = cosine func.
     */
    public Vec4 mShape;


    public Light() {
        this( TYPE_AMBIENT, null, null, null, null, null, null, null );
    }


    public Light( int type,
                  Vec4 ambient,
                  Vec4 diffuse,
                  Vec4 specular,
                  Vec3 pos,
                  Vec3 dir,
                  Vec3 attenuation,
                  Vec4 shape )
    {
        mType        = type;
        mAmbient     = ambient     == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( ambient );
        mDiffuse     = diffuse     == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( diffuse );
        mSpecular    = specular    == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( specular );
        mPos         = pos         == null ? new Vec3()             : new Vec3( mPos );
        mDir         = dir         == null ? new Vec3()             : new Vec3( mDir );
        mAttenuation = attenuation == null ? new Vec3( 1, 0, 0 )    : new Vec3( attenuation );
        mShape       = shape       == null ? new Vec4()             : new Vec4( mShape );
    }

}
