/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.Rect;
import bits.math3d.Mat;
import bits.math3d.Mat4;


public class CenteredOrthoFunc implements ProjectionFunc {

    private float mNearPlane   = -1f;
    private float mFarPlane    =  1f;
    private float mHeight      =  1f;
    private float mAspectScale = 1f;
    
    
    public CenteredOrthoFunc( float height ) {
        height( height );
    }
    
    
    
    public void height( float h ) {
        mHeight = h;
    }
    
    
    public float height() {
        return mHeight;
    }

    
    public void aspectScale( float scale ) {
        mAspectScale = scale;
    }


    public float aspectScale() {
        return mAspectScale;
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
        float aspect  = w * mAspectScale / h;
        
        float x0 = -0.5f * aspect * mHeight;
        float y0 = -0.5f * mHeight;
        float x1 =  0.5f * aspect * mHeight;
        float y1 =  0.5f * mHeight;
        
        if( tileViewport != null ) {
            float xx0 = ( x1 - x0 ) * ( tileViewport.x0 - viewport.x0 ) / w + viewport.x0;
            float yy0 = ( y1 - y0 ) * ( tileViewport.y0 - viewport.y0 ) / h + viewport.y0;
            float xx1 = ( x1 - x0 ) * ( tileViewport.x1 - viewport.x0 ) / w + viewport.x0;
            float yy1 = ( y1 - y0 ) * ( tileViewport.y1 - viewport.y1 ) / h + viewport.y1;
            x0 = xx0;
            y0 = yy0;
            x1 = xx1;
            y1 = yy1;
        }
        
        Mat.getOrtho( x0, x1, y0, y1, mNearPlane, mFarPlane, out );
    }

}
