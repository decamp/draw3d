/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import com.jogamp.opengl.GL;

import bits.draw3d.*;


/**
 * @author decamp
 */
public final class DrawNodes {
    

    public static DrawNode createEnable( final int glConstant ) {
        return new DrawNodeAdapter() {
            boolean mRevert = false;

            @Override
            public void pushDraw( DrawEnv d ) {
                mRevert = d.mGl.glIsEnabled( glConstant );
                d.mGl.glEnable( glConstant );
            }

            @Override
            public void popDraw( DrawEnv d ) {
                if( mRevert ) {
                    d.mGl.glEnable( glConstant );
                } else {
                    d.mGl.glDisable( glConstant );
                }
            }

        };
    }


    public static DrawNode createDisable( final int glConstant ) {
        return new DrawNodeAdapter() {
            boolean mRevert = false;

            public void pushDraw( GL gl ) {
                mRevert = gl.glIsEnabled( glConstant );
                gl.glDisable( glConstant );
            }

            public void popDraw( GL gl ) {
                if( mRevert ) {
                    gl.glEnable( glConstant );
                } else {
                    gl.glDisable( glConstant );
                }
            }
        };

    }


    public static DrawNode createLoadIdentity() {
        return LOAD_IDENTITY;
    }



    private static DrawNode LOAD_IDENTITY = new DrawNodeAdapter() {

        @Override
        public void pushDraw( DrawEnv d ) {
            d.mProj.push();
            d.mProj.identity();
            d.mView.push();
            d.mView.identity();
        }

        @Override
        public void popDraw( DrawEnv d ) {
            d.mProj.pop();
            d.mView.pop();
        }

    };

}
