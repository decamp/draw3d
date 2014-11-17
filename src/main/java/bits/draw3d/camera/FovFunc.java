/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.Rect;
import bits.math3d.Mat;
import bits.math3d.Mat4;

public class FovFunc implements ProjectionFunc {

    private float mFov  = (float)Math.PI / 2.0f;
    private float mNear = 1f;
    private float mFar  = 1001f;
    
    
    public float fov() {
        return mFov;
    }


    public void fov( float fov ) {
        mFov = fov;
    }
    
    @Override
    public float nearPlane() {
        return mNear;
    }

    @Override
    public void nearPlane( float nearPlane ) {
        mNear = nearPlane;
    }

    @Override
    public float farPlane() {
        return mFar;
    }

    @Override
    public void farPlane( float farPlane ) {
        mFar = farPlane;
    }

    @Override
    public void computeProjectionMat( Rect viewport, Rect tileViewport, Mat4 out ) {
        // Scale far/near based on size of camera target.
        float near = mNear;
        float far  = mFar;
        
        float aspect = (float)viewport.width() / viewport.height();
        float ymax   = near * (float)Math.tan( mFov * 0.5f );
        float ymin   = -ymax;
        float xmax   = aspect * ymax;
        float xmin   = aspect * ymin;
        
        float left, right, bottom, top;

        //Is off-axis projection required?
        if( tileViewport == null ) {
            left   = xmin;
            right  = xmax;
            bottom = ymin;
            top    = ymax;
        } else {
            left   = ( xmax - xmin ) * ( tileViewport.x0 - viewport.x0 ) / viewport.width() + xmin;
            right  = ( xmax - xmin ) * ( tileViewport.x1 - viewport.x0 ) / viewport.width() + xmin;
            bottom = ( ymax - ymin ) * ( tileViewport.y0 - viewport.y0 ) / viewport.height() + ymin;
            top    = ( ymax - ymin ) * ( tileViewport.y1 - viewport.y0 ) / viewport.height() + ymin;
        }
         Mat.getFrustum( left, right, bottom, top, near, far, out );
    }
    
}
