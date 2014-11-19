/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import java.io.IOException;

import bits.blob.Blob;
import bits.math3d.*;


/**
 * @author decamp
 */
@Deprecated public class MatrixIO {

    public static Mat4 parseTransformStack( Blob blob ) throws IOException {
        int size = blob.size();
        Mat4 ret = new Mat4();
        Mat.identity( ret );

        for( int i = 0; i < size; i++ ) {
            Mat4 mat = parseTransform( blob.slice( i ) );
            Mat.mult( ret, mat, ret );
        }

        return ret;
    }


    public static Mat4 parseTransform( Blob blob ) throws IOException {
        int size = blob.size();
        if( size < 1 ) {
            return null;
        }

        String type = blob.getString( 0 );
        if( type == null ) {
            throw new IOException( "Invalid matrix format" );
        }

        if( type.equalsIgnoreCase( "matrix" ) ) {
            Mat4 mat = new Mat4();
            float[] arr = new float[16];
            readFloats( blob, 1, arr, 0, 16 );
            Mat.put( arr, mat );
            return mat;
        }

        if( type.equalsIgnoreCase( "translate" ) ) {
            float[] tran = new float[3];
            readFloats( blob, 1, tran, 0, 3 );
            Mat4 mat = new Mat4();
            Mat.getTranslation( tran[0], tran[1], tran[2], mat );
            return mat;
        }

        if( type.equalsIgnoreCase( "scale" ) ) {
            float[] scale = new float[3];
            readFloats( blob, 1, scale, 0, 3 );
            Mat4 mat = new Mat4();
            Mat.getScale( scale[0], scale[1], scale[2], 1f, mat );
            return mat;
        }

        if( type.equalsIgnoreCase( "rotateDegrees" ) ) {
            float[] rot = new float[4];
            readFloats( blob, 1, rot, 0, 4 );
            Mat4 mat = new Mat4();
            Mat.getRotation( (float)( rot[0] * Ang.DEG_TO_RAD ), rot[1], rot[2], rot[3], mat );
            return mat;
        }

        if( type.equalsIgnoreCase( "rotateRadians" ) ) {
            float[] rot = new float[4];
            readFloats( blob, 1, rot, 0, 4 );
            Mat4 mat = new Mat4();
            Mat.getRotation( rot[0], rot[1], rot[2], rot[3], mat );
            return mat;
        }

        throw new IOException( "Invalid matrix format" );
    }


    private static void readFloats( Blob blob, int inOff, float[] out, int outOff, int len ) throws IOException {
        for( int i = 0; i < len; i++ ) {
            Number d = blob.getType( Number.class, inOff + i );
            if( d == null ) {
                throw new IOException( "Invalid matrix format" );
            }
            out[outOff + i] = d.floatValue();
        }
    }

}
