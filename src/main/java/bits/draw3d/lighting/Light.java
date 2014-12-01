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

    public static Light createDirectionalLight( Vec4 color, Vec3 dir ) {
        return new Light( color, null, dir, null, false, 10f, 0f );
    }

    public static Light createPointLight( Vec3 color, Vec3 pos, Vec3 falloff ) {
        return new Light( color, pos, null, falloff, true, 10f, 0f );
    }

    public static Light createSpotlight( Vec3 color,
                                         Vec3 pos,
                                         Vec3 dir,
                                         Vec3 falloff,
                                         float spotAngle,
                                         float spotExp )
    {
        return new Light( color, pos, dir, falloff, true, spotAngle, spotExp );
    }


    public Vec3 mColor;
    public Vec3 mPos;
    public Vec3 mDir;

    /**
     * Holds constant, linear, and quadratic attenuation coefficients.
     * The strength of the light is proportional to
     * {@code 1.0 / ( mFalloff * Vec3( 1.0, dist, dist * dist ) ) },
     * where {@code dist} is the distance between the light and a given point.
     */
    public Vec3 mFalloff;

    /**
     * 0 if directional, 1 if positional.
     */
    public boolean mPositional;

    /**
     * The angle of the maximum angle of the light. <br>
     * {@code mSpotAngle <= 0 } means light has no spread. <br>
     * {@code mSpotAngle >= Math.PI } means light spreads in all directions.
     */
    public float mSpotAngle;

    /**
     * Exponent of light strength, computed as {@code cos(ang) ^ mSpotExponen}
     */
    public float mSpotExp;


    public Light() {
        this( null, null, null, null, true, 10f, 0f );
    }


    public Light( Vec3 color,
                  Vec3 pos,
                  Vec3 dir,
                  Vec3 falloff,
                  boolean positional,
                  float spotCutoff,
                  float spotExp )
    {
        mColor      = color   == null ? new Vec3( 0, 0, 0 ) : new Vec3( color );
        mPos        = pos     == null ? new Vec3( 0, 0, 0 ) : new Vec3( pos );
        mDir        = dir     == null ? new Vec3( 0, 0, 0 ) : new Vec3( dir );
        mFalloff    = falloff == null ? new Vec3( 1, 0, 0 ) : new Vec3( falloff );
        mPositional = positional;
        mSpotAngle  = spotCutoff;
        mSpotExp    = spotExp;
    }

}
