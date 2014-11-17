package bits.draw3d.util;

import bits.math3d.*;


/**
 * @author Philip DeCamp
 */
public class TypeConverter {

    private static final float FLOAT_TO_BYTE  = 255f / 1f;
    private static final float BYTE_TO_FLOAT  = 1f / 255f;
    private static final float FLOAT_TO_SHORT = Short.MAX_VALUE / 1f;


    public static int toUbytes( float x, float y, float z ) {
        return ( (int)( x * FLOAT_TO_BYTE ) << 24              ) |
               ( (int)( y * FLOAT_TO_BYTE ) << 16 & 0x00FF0000 ) |
               ( (int)( z * FLOAT_TO_BYTE ) <<  8 & 0x0000FF00 ) |
               0xFF;
    }


    public static int toUbytes( Vec4 v ) {
        return ( (int)( v.x * FLOAT_TO_BYTE ) << 24              ) |
               ( (int)( v.y * FLOAT_TO_BYTE ) << 16 & 0x00FF0000 ) |
               ( (int)( v.z * FLOAT_TO_BYTE ) <<  8 & 0x0000FF00 ) |
               ( (int)( v.w * FLOAT_TO_BYTE )       & 0x000000FF );
    }


    public static int toShorts( float x, float y ) {
        return ( (int)( FLOAT_TO_SHORT * x ) << 16              ) |
               ( (int)( FLOAT_TO_SHORT * y )       & 0x0000FFFF );
    }

}
