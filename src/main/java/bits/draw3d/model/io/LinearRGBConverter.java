/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model.io;

import bits.math3d.Vec4;


/**
 * LinearRGBConverter - conversion routines for a linear sRGB colorspace
 * sRGB is a standard for RGB colorspaces, adopted by the w3c.
 * <p/>
 * The specification is available at:
 * http://www.w3.org/Graphics/Color/sRGB.html
 *
 * @author Sven de Marothy
 */
public class LinearRGBConverter {

    public static float linearToSrgb( float v ) {
        if( v < 0 ) {
            v = 0f;
        } else if( v > 1 ) {
            v = 1f;
        }
        if( v <= 0.00304f ) {
            return v * 12.92f;
        } else {
            return 1.055f * ( (float)Math.exp( ( 1 / 2.4 ) * Math.log( v ) ) ) - 0.055f;
        }
    }


    public static float srgbToLinear( float v ) {
        if( v < 0 ) {
            v = 0f;
        } else if( v > 1 ) {
            v = 1f;
        }
        if( v <= 0.03928f ) {
            return (float)( v / 12.92 );
        } else {
            return (float)( Math.exp( 2.4 * Math.log( (v + 0.055) / 1.055 ) ));
        }
    }

    /**
     * linear RGB --> sRGB
     * Use the inverse gamma curve
     */
    public static void linearToSrgb( Vec4 in, Vec4 out ) {
        out.x = linearToSrgb( in.x );
        out.y = linearToSrgb( in.y );
        out.z = linearToSrgb( in.z );
        out.w = in.w;
    }

    /**
     * sRGB --> linear RGB
     * Use the gamma curve (gamma=2.4 in sRGB)
     */
    public static void srgbToLinear( Vec4 in, Vec4 out ) {
        out.x = srgbToLinear( in.x );
        out.y = srgbToLinear( in.y );
        out.z = srgbToLinear( in.z );
        out.w = in.w;
    }

}

