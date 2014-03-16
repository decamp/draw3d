package bits.math3d.camera;

import bits.math3d.*;
import bits.math3d.actors.SpatialObject;
import bits.math3d.geom.Aabb;


/**
 * @author decamp
 * @deprecated
 */
public class FrustumCameraTransform extends AbstractCameraTransform {
    
    private final double[] mActorToCameraMatrix;
    private final TransformStack mStack = new TransformStack();
    
    private double mNearPlane = 2.0;
    private double mFarPlane  = 1000.0;
    private double mFov       = 120.0;
    
    
    public FrustumCameraTransform( double[] actorToCameraMat ) {
        this( actorToCameraMat, null );
    }

    
    public FrustumCameraTransform( double[] actorToCameraMat, SpatialObject camera ) {
        super( camera == null ? new SpatialObject() : camera );
        mActorToCameraMatrix = actorToCameraMat;
    }
    
    
    
    public double nearPlane() {
        return mNearPlane;
    }
    
    
    public double farPlane() {
        return mFarPlane;
    }
    
    
    public void setProjectionDepth( double nearPlane, double farPlane ) {
        mNearPlane = nearPlane;
        mFarPlane = farPlane;
    }
    
    
    public double fov() {
        return mFov;
    }
    
    
    public FrustumCameraTransform fov( double fovDegrees ) {
        mFov = fovDegrees;
        return this;
    }
    
    
    public void fitProjectionDepthToTarget( Aabb target ) {
        if( target == null ) {
            setProjectionDepth( 2.0, 1000.0 );
        } else {
            double far = target.maxDim();
            setProjectionDepth( far / 500.0, far );
        }
    }
    
    
    public synchronized void computeModelToCameraMatrix( LongRect viewport, LongRect subViewport, double[] out ) {
        final SpatialObject cam = getCameraObject();
        final double[] pos      = cam.mPos;
        
        mStack.set( cam.mRot );
        mStack.invert();
        mStack.translate( -pos[0], -pos[1], -pos[2] );
        
        if( mActorToCameraMatrix != null ) {
            mStack.premult( mActorToCameraMatrix );
        }
        
        mStack.get( out );
    }
    
    
    public synchronized void computeCameraToNormDeviceMatrix( LongRect viewport, LongRect subViewport, double[] out ) {
        //Scale far/near based on size of camera target.
        double near   = mNearPlane;
        double far    = mFarPlane;
        
        double aspect = (double)viewport.spanX() / (double)viewport.spanY();
        double ymax   = near * Math.tan(mFov * Math.PI / 360.0);
        double ymin   = -ymax;
        double xmax   = aspect * ymax;
        double xmin   = aspect * ymin;
        
        double left, right, bottom, top;
        
        //Is off-axis projection required?
        if( subViewport == null || viewport.equals( subViewport ) ) {
            left   = xmin;
            right  = xmax;
            bottom = ymin;
            top    = ymax;
        }else{
            left   = ( xmax - xmin ) * ( subViewport.minX() - viewport.minX() ) / viewport.spanX() + xmin;
            right  = ( xmax - xmin ) * ( subViewport.maxX() - viewport.minX() ) / viewport.spanX() + xmin;
            top    = ( ymax - ymin ) * ( subViewport.minY() - viewport.minY() ) / viewport.spanY() + ymin;
            bottom = ( ymax - ymin ) * ( subViewport.maxY() - viewport.minY() ) / viewport.spanY() + ymin;
            
            //Flip vertical coordinates.
            bottom  = ymax + ymin - bottom;
            top     = ymax + ymin - top;
        }
        
        Matrices.computeFrustumMatrix( left, right, bottom, top, near, far, out );
    }
    
    

    /**
     * @deprecated Use nearPlane()
     */
    public double getNearPlane() {
        return mNearPlane;
    }

    /**
     * @deprecated Use farPlane()
     */
    public double getFarPlane() {
        return mFarPlane;
    }
    
    /**
     * @deprecated Use fov()
     */
    public double getFov() {
        return mFov;
    }
    
    /**
     * @deprecated Use fov()
     */
    public void setFov( double fovDegrees ) {
        if(mFov == fovDegrees)
            return;
        
        mFov = fovDegrees;
    }
    
    
}
