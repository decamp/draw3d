package bits.math3d.camera;

import bits.math3d.Matrices;

public class FovFunc implements ProjectionFunc {

    private float mFov  = (float)Math.PI / 2.0f;
    private float mNear = 1f;
    private float mFar  = 1001f;
    
    
    public float fov() {
        return mFov;
    }

    public ProjectionFunc fov( float fov ) {
        mFov = fov;
        return this;
    }
    
    @Override
    public float nearPlane() {
        return mNear;
    }

    @Override
    public ProjectionFunc nearPlane( float nearPlane ) {
        mNear = nearPlane;
        return this;
    }

    @Override
    public float farPlane() {
        return mFar;
    }

    @Override
    public ProjectionFunc farPlane( float farPlane ) {
        mFar = farPlane;
        return this;
    }

    @Override
    public void computeProjectionMat( int[] viewport, int[] tileViewport, double[] outMat ) {
        // Scale far/near based on size of camera target.
        double near   = mNear;
        double far    = mFar;
        
        double aspect = (double)( viewport[2] - viewport[0] ) / ( viewport[3] - viewport[1] );
        double ymax   = near * Math.tan( mFov * 0.5f );
        double ymin   = -ymax;
        double xmax   = aspect * ymax;
        double xmin   = aspect * ymin;
        
        double left, right, bottom, top;
        
        //Is off-axis projection required?
        if( tileViewport == null ) {
            left   = xmin;
            right  = xmax;
            bottom = ymin;
            top    = ymax;
        } else {
            left   = ( xmax - xmin ) * ( tileViewport[0] - viewport[0] ) / ( viewport[2] - viewport[0] ) + xmin;
            right  = ( xmax - xmin ) * ( tileViewport[2] - viewport[0] ) / ( viewport[2] - viewport[0] ) + xmin;
            bottom = ( ymax - ymin ) * ( tileViewport[1] - viewport[1] ) / ( viewport[3] - viewport[1] ) + ymin;
            top    = ( ymax - ymin ) * ( tileViewport[3] - viewport[1] ) / ( viewport[3] - viewport[1] ) + ymin;
        }
        
        Matrices.computeFrustumMatrix( left, right, bottom, top, near, far, outMat );
    }
    
}
