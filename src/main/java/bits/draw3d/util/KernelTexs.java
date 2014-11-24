/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import bits.draw3d.DrawUtil;
import bits.draw3d.Texture1;
import bits.math3d.func.Gaussian1;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static javax.media.opengl.GL3.*;


public final class KernelTexs {

    public static Texture1 createDashedLine( int w, float segments, float dutyCycle ) {
        ByteBuffer buf = ByteBuffer.allocateDirect( w );
        buf.order( ByteOrder.nativeOrder() );
        
        final float blankStart = dutyCycle * 0.5f;
        final float blankEnd   = 1f - dutyCycle * 0.5f;
        
        for( int i = 0; i < w; i++ ) {
            float seg = i / ( w - 1.0f ) * segments;
            float t   = seg % 1.0f;
            if( t <= blankStart || t >= blankEnd ) {
                buf.put( (byte)255 );
            } else {
                buf.put( (byte)0 );
            }
        }
        
        buf.flip();
        Texture1 ret = new Texture1();
        ret.buffer( buf, GL_RED, GL_RED, GL_UNSIGNED_BYTE, w );
        ret.param( GL_TEXTURE_SWIZZLE_R, GL_ONE );
        ret.param( GL_TEXTURE_SWIZZLE_G, GL_ONE );
        ret.param( GL_TEXTURE_SWIZZLE_B, GL_ONE );
        ret.param( GL_TEXTURE_SWIZZLE_A, GL_RED );
        ret.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        return ret;
    }
    
    
    public static Texture1 createGauss1( int w ) {
        return createGauss1( w, 2.0 );
    }
    
    
    public static Texture1 createGauss1( int w, double sigs ) {
        ByteBuffer buf = DrawUtil.alloc( w + 2 );
        int hw = w / 2;

        Gaussian1 g  = Gaussian1.fromSigma( hw / sigs );
        buf.put( (byte)0 );
        double sum   = 0.0;
        
        for( int x = 0; x < w; x++) {
            sum += g.apply( x - hw );
        }

        for( int x = 0; x < w; x++ ) {
            double v = 0xFF * g.apply( x - hw ) / sum;
            buf.put( (byte)( v ) );
        }
        
        buf.put( (byte)0 );
        buf.flip();

        Texture1 node = new Texture1();
        node.buffer( buf, GL_RED, GL_RED, GL_UNSIGNED_BYTE, w + 2 );
        node.param( GL_TEXTURE_SWIZZLE_R, GL_ONE );
        node.param( GL_TEXTURE_SWIZZLE_G, GL_ONE );
        node.param( GL_TEXTURE_SWIZZLE_B, GL_ONE );
        node.param( GL_TEXTURE_SWIZZLE_A, GL_RED );
        node.param( GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
        return node;
    }


    private KernelTexs() {}

}
