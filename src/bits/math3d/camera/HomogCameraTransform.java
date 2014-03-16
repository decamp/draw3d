package bits.math3d.camera;

import bits.math3d.*;
import bits.math3d.actors.*;
import bits.math3d.geom.Aabb;


/**
 * Transform represented directly by two matrices.  
 * Generates a ConstantSpatialObject for a CameraObject
 * that cannot be changed.
 * 
 * @author decamp
 * @deprecated
 */
public class HomogCameraTransform extends AbstractCameraTransform {
    
    
    public static HomogCameraTransform fromMatrices( double[] projectionMat, double[] modelviewMat ) {
        SpatialObject camera = new SpatialObject();
        
        double[] proj = new double[16];
        double[] mod  = new double[16];
        double[] rot  = camera.mRot;
        
        System.arraycopy( projectionMat, 0, proj, 0, 16 );
        System.arraycopy( modelviewMat, 0, mod, 0, 16 );
        
        double[] pos   = camera.mPos;
        double[] vec   = {0, 0, 0};
        double[] trans = new double[16];
        
        Matrices.multMatVec( modelviewMat, vec, pos );
        Matrices.computeTranslationMatrix( -pos[0], -pos[1], -pos[2], trans );
        Matrices.multMatMat( trans, mod, rot );
        
        return new HomogCameraTransform( camera, proj, mod );
    }
    
    
    
    private final double[] mRawProjectionMat;
    private final double[] mProjectionMat;
    private final double[] mModelviewMat;
    
    private final double mRawNearPlane;
    private final double mRawFarPlane;
    
    private double mNearPlane;
    private double mFarPlane;
    
    
    HomogCameraTransform( SpatialObject camera, double[] projectionMat, double[] modelviewMat ) {
        super( camera );
        mRawProjectionMat = projectionMat;
        mProjectionMat    = projectionMat.clone();
        mModelviewMat     = modelviewMat;
        
        double[] p = computeDepthPlanes( projectionMat );
        mRawNearPlane = mNearPlane = p[0];
        mRawFarPlane  = mFarPlane  = p[1];
    }
    

    
    public double nearPlane() {
        return mNearPlane;
    }
    
    
    public double farPlane() {
        return mFarPlane;
    }
    
    
    public void setProjectionDepth( double nearPlane, double farPlane ) {
        if(mNearPlane == nearPlane && mFarPlane == farPlane)
            return;

        double scaleParam = (farPlane - nearPlane) / (mRawFarPlane - mRawNearPlane);
        double transParam = (mRawNearPlane - mRawNearPlane * scaleParam) / (mRawFarPlane - mRawNearPlane);
        
        double[] work1 = new double[16];
        double[] work2 = new double[16];
        
        Matrices.computeScaleMatrix(1.0, 1.0, scaleParam, work1);
        Matrices.multMatMat(work1, mRawProjectionMat, work2);
        Matrices.computeTranslationMatrix(0.0, 0.0, transParam, work1);
        Matrices.multMatMat(work1, work2, mProjectionMat);
        
        mNearPlane = nearPlane;
        mFarPlane  = farPlane;
    }
    
    
    public void fitProjectionDepthToTarget( Aabb target ) {
        double[] c1  = { target.centerX(), target.centerY(), target.centerZ() };
        double[] pos = getCameraObject().mPos;
        double far   = Vectors.dist( c1, pos ) + target.maxDim();
        setProjectionDepth( far / 1000.0, far );
    }
    
    
    public void computeModelToCameraMatrix( LongRect viewport, LongRect subViewport, double[] outMat ) {
        System.arraycopy( mModelviewMat, 0, outMat, 0, 16 );
    }
    
    
    public void computeCameraToNormDeviceMatrix( LongRect viewport, LongRect subViewport, double[] outMat ) {
        System.arraycopy( mProjectionMat, 0, outMat, 0, 16 );
    }
    
    
    
    private static double[] computeDepthPlanes( double[] projectionMat ) {
        double[] inv = new double[16];
        Matrices.invert( projectionMat, inv );
        
        double[] va = {0, 0, -1};
        double[] vb = {0, 0, 0};
        Matrices.multMatVec( inv, va, vb );
        double nearPlane = Vectors.hypot( vb );
        
        va[2] = 1.0;
        Matrices.multMatVec( inv, va, vb );
        double farPlane = Vectors.hypot( vb );
        
        return new double[] { nearPlane, farPlane };   
    }
    
}
