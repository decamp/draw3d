package bits.math3d.camera;

import bits.math3d.*;
import bits.math3d.actors.SpatialObject;
import bits.math3d.geom.Aabb;


/**
 * @author decamp
 * @deprecated
 */
public class OrthoCameraTransform extends AbstractCameraTransform {
    
    private final double[] mActorToCameraMatrix;
    private final TransformStack mStack = new TransformStack();
    
    private double mNearPlane = 0.0;
    private double mFarPlane  = 1000.0;
    private double mViewSize  = 1.0;
    
    
    public OrthoCameraTransform( double[] actorToCameraMatRef ) {
        this( actorToCameraMatRef, null );
    }
    
    
    public OrthoCameraTransform( double[] actorToCameraMatRef, SpatialObject camera ) {
        super( camera );
        mActorToCameraMatrix = actorToCameraMatRef;
    }
    
    
    
    public double nearPlane() {
        return mNearPlane;
    }

    
    public double farPlane() {
        return mFarPlane;
    }

    
    public void setViewSize( double viewSize ) {
        mViewSize = viewSize;
    }
    
    
    public void fitViewSizeToTarget( Aabb target ) {
        mViewSize = target.maxDim() * 1.1;
    }
    
    
    public void setProjectionDepth( double nearPlane, double farPlane ) {
        if(mNearPlane == nearPlane && mFarPlane == farPlane)
            return;
        
        mNearPlane = nearPlane;
        mFarPlane  = farPlane;
    }
    
    
    public void fitProjectionDepthToTarget( Aabb target ) {
        if( target == null ) {
            setProjectionDepth( 0.0, 1000.0 );
        } else {
            double far = target.maxDim();
            setProjectionDepth( 0.0, far * 2.0 );
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

    
    public synchronized void computeCameraToNormDeviceMatrix(LongRect viewport, LongRect subViewport, double[] out) {
        final double w = viewport.spanX();
        final double h = viewport.spanY();
        double scale = mViewSize / Math.min(w, h);
        
        double xmin = -0.5 * scale * w;
        double xmax =  0.5 * scale * w;
        double ymin = -0.5 * scale * h;
        double ymax =  0.5 * scale * h;
        
        double left, right, bottom, top;
        
        if(subViewport == null || viewport.equals(subViewport)) {
            left   = xmin;
            right  = xmax;
            bottom = ymin;
            top    = ymax;
        }else{
            left   = (xmax - xmin) * (subViewport.minX() - viewport.minX()) / viewport.spanX() + xmin;
            right  = (xmax - xmin) * (subViewport.maxX() - viewport.minX()) / viewport.spanX() + xmin;
            top    = (ymax - ymin) * (subViewport.minY() - viewport.minY()) / viewport.spanY() + ymin;
            bottom = (ymax - ymin) * (subViewport.maxY() - viewport.minY()) / viewport.spanY() + ymin;
            
            //Flip vertical coordinates.
            bottom  = ymax + ymin - bottom;
            top     = ymax + ymin - top;
        }
        
        Matrices.computeOrthoMatrix( left, right, bottom, top, mNearPlane, mFarPlane, out );
    }
    
}
