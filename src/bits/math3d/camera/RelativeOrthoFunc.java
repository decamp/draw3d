package bits.math3d.camera;

import bits.math3d.Matrices;

public class RelativeOrthoFunc implements ProjectionFunc {

    private float mScaleX    = 1f;
    private float mScaleY    = 1f;
    private float mNearPlane = -1f;
    private float mFarPlane  =  1f;
    
    
    public RelativeOrthoFunc( float sx, float sy ) {
        scale( sx, sy );
    }
    
    
    
    public RelativeOrthoFunc scale( float sx, float sy ) {
        mScaleX = sx;
        mScaleY = sy;
        return this;
    }
    
    
    @Override
    public float nearPlane() {
        return mNearPlane;
    }

    
    @Override
    public RelativeOrthoFunc nearPlane( float nearPlane ) {
        mNearPlane = nearPlane;
        return this;
    }

    
    @Override
    public float farPlane() {
        return mFarPlane;
    }

    
    @Override
    public ProjectionFunc farPlane( float farPlane ) {
        mFarPlane = farPlane;
        return this;
    }

    
    @Override
    public void computeProjectionMat( int[] viewport, int[] tileViewport, double[] outMat ) {
        final float w = viewport[2] - viewport[0];
        final float h = viewport[3] - viewport[1];
        
        float x0 = -0.5f * mScaleX * w;
        float y0 = -0.5f * mScaleY * h;
        float x1 =  0.5f * mScaleX * w;
        float y1 =  0.5f * mScaleY * h;
        
        if( tileViewport != null ) {
            float xx0 = ( x1 - x0 ) * ( tileViewport[0] - viewport[0] ) / w + viewport[0];
            float yy0 = ( y1 - y0 ) * ( tileViewport[1] - viewport[1] ) / h + viewport[1];
            float xx1 = ( x1 - x0 ) * ( tileViewport[2] - viewport[0] ) / w + viewport[0];
            float yy1 = ( y1 - y0 ) * ( tileViewport[3] - viewport[1] ) / h + viewport[1];
             
            x0 = xx0;
            y0 = yy0;
            x1 = xx1;
            y1 = yy1;
        }
        
        Matrices.computeOrthoMatrix( x0, x1, y0, y1, mNearPlane, mFarPlane, outMat );
    }

}
