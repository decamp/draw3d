package bits.draw3d.model.io;

/* LinearRGBConverter.java -- conversion to a linear RGB color space
   Copyright (C) 2004 Free Software Foundation

   This file is part of GNU Classpath.

   GNU Classpath is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   GNU Classpath is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with GNU Classpath; see the file COPYING.  If not, write to the
   Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
   02111-1307 USA.

   Linking this library statically or dynamically with other modules is
   making a combined work based on this library.  Thus, the terms and
   conditions of the GNU General Public License cover the whole
   combination.

   As a special exception, the copyright holders of this library give you
   permission to link this library with independent modules to produce an
   executable, regardless of the license terms of these independent
   modules, and to copy and distribute the resulting executable under
   terms of your choice, provided that you also meet, for each linked
   independent module, the terms and conditions of the license of that
   module.  An independent module is a module which is not derived from
   or based on this library.  If you modify this library, you may extend
   this exception to your version of the library, but you are not
   obligated to do so.  If you do not wish to do so, delete this
   exception statement from your version. */


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

    /**
     * linear RGB --> sRGB
     * Use the inverse gamma curve
     */
    public static void linearToSrgb( float[] in, float[] out ) {
        for( int i = 0; i < 3; i++ ) {
            float n = in[i];
            if( n < 0 ) {
                n = 0f;
            }
            if( n > 1 ) {
                n = 1f;
            }
            if( n <= 0.00304f ) {
                out[i] = in[0] * 12.92f;
            } else {
                out[i] = 1.055f * ((float)Math.exp( (1 / 2.4) * Math.log( n ) )) - 0.055f;
            }
        }

        if( in.length > 3 && out.length > 3 ) {
            out[3] = in[3];
        }
    }

    /**
     * sRGB --> linear RGB
     * Use the gamma curve (gamma=2.4 in sRGB)
     */
    public static void srgbToLinear( float[] in, float[] out ) {
        // Convert non-linear RGB coordinates to linear ones,
        //  numbers from the w3 spec.
        for( int i = 0; i < 3; i++ ) {
            float n = in[i];
            if( n < 0 ) {
                n = 0f;
            }
            if( n > 1 ) {
                n = 1f;
            }
            if( n <= 0.03928f ) {
                out[i] = (float)(n / 12.92);
            } else {
                out[i] = (float)(Math.exp( 2.4 * Math.log( (n + 0.055) / 1.055 ) ));
            }
        }

        if( in.length > 3 && out.length > 3 ) {
            out[3] = in[3];
        }
    }

}

