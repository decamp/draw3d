/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.Rect;
import bits.math3d.Mat;
import bits.math3d.Mat4;


public class RelativeOrthoFunc implements ProjectionFunc {

    private float mScaleX    = 1f;
    private float mScaleY    = 1f;
    private float mNearPlane = -1f;
    private float mFarPlane  =  1f;
    
    
    public RelativeOrthoFunc( float sx, float sy ) {
        scale( sx, sy );
    }
    

    public void scale( float sx, float sy ) {
        mScaleX = sx;
        mScaleY = sy;
    }

    @Override
    public float nearPlane() {
        return mNearPlane;
    }

    @Override
    public void nearPlane( float nearPlane ) {
        mNearPlane = nearPlane;
    }

    @Override
    public float farPlane() {
        return mFarPlane;
    }

    @Override
    public void farPlane( float farPlane ) {
        mFarPlane = farPlane;
    }

    @Override
    public void computeProjectionMat( Rect viewport, Rect tileViewport, Mat4 out ) {
        final float w = viewport.width();
        final float h = viewport.height();
        
        float left   = -0.5f * mScaleX * w;
        float right  =  0.5f * mScaleX * w;
        float bottom = -0.5f * mScaleY * h;
        float top    =  0.5f * mScaleY * h;
        
        if( tileViewport != null ) {
            float xx0 = ( right - left ) * ( tileViewport.x0 - viewport.x0 ) / w + viewport.x0;
            float xx1 = ( right - left ) * ( tileViewport.x1 - viewport.x0 ) / w + viewport.x0;
            float yy0 = ( top - bottom ) * ( tileViewport.y0 - viewport.y0 ) / h + viewport.y0;
            float yy1 = ( top - bottom ) * ( tileViewport.y1 - viewport.y0 ) / h + viewport.y0;
            left   = xx0;
            bottom = yy0;
            right  = xx1;
            top    = yy1;
        }

        Mat.getOrtho( left, right, bottom, top, mNearPlane, mFarPlane, out );
    }

}
