/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import bits.math3d.*;

import java.nio.ByteOrder;


/**
 * @author Philip DeCamp
 */
public class TypeConverter {

    private static final boolean SWAP_ORDER = ByteOrder.nativeOrder() != ByteOrder.BIG_ENDIAN;

    private static final float FLOAT_TO_BYTE  = 255f / 1f;
    private static final float FLOAT_TO_SHORT = Short.MAX_VALUE / 1f;


    public static int toUbytes( float x, float y, float z ) {
        if( SWAP_ORDER ) {
            return ( (int)(x * FLOAT_TO_BYTE)       & 0x000000FF ) |
                   ( (int)(z * FLOAT_TO_BYTE) << 8  & 0x0000FF00 ) |
                   ( (int)(y * FLOAT_TO_BYTE) << 16 & 0x00FF0000 ) |
                   0xFF000000;
        } else {
            return ( (int)(x * FLOAT_TO_BYTE) << 24              ) |
                   ( (int)(y * FLOAT_TO_BYTE) << 16 & 0x00FF0000 ) |
                   ( (int)(z * FLOAT_TO_BYTE) << 8  & 0x0000FF00 ) |
                   0x000000FF;
        }
    }


    public static int toUbytes( Vec4 v ) {
        if( SWAP_ORDER ) {
            return ( (int)( v.x * FLOAT_TO_BYTE )       & 0x000000FF ) |
                   ( (int)( v.y * FLOAT_TO_BYTE ) <<  8 & 0x0000FF00 ) |
                   ( (int)( v.z * FLOAT_TO_BYTE ) << 16 & 0x00FF0000 ) |
                   ( (int)( v.w * FLOAT_TO_BYTE ) << 24              );
        } else {
            return ( (int)( v.x * FLOAT_TO_BYTE ) << 24              ) |
                   ( (int)( v.y * FLOAT_TO_BYTE ) << 16 & 0x00FF0000 ) |
                   ( (int)( v.z * FLOAT_TO_BYTE ) <<  8 & 0x0000FF00 ) |
                   ( (int)( v.w * FLOAT_TO_BYTE )       & 0x000000FF );
        }
    }


    public static int toShorts( float x, float y ) {
        return ( (int)( FLOAT_TO_SHORT * x ) << 16              ) |
               ( (int)( FLOAT_TO_SHORT * y )       & 0x0000FFFF );
    }

}
