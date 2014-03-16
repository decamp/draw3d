package bits.math3d.camera;

import bits.math3d.LongRect;
import bits.math3d.actors.SpatialObject;



/**
 * Computes the transformations associated with a given camera object.
 * 
 * @author decamp
 * @deprecated
 */
public interface CameraTransform {

    /**
     * @return the spatial object that determines this CameraTransform's position and orientation.
     */
    public SpatialObject getCameraObject();
    
    
    public double nearPlane();
    
    
    public double farPlane();
    
    /**
     * Computes transform from model-axis-coordinates to camera-axis-coordinates. 
     * 
     * @param viewport      Viewport bounds of the entire render space being covered by this camera.
     * @param subViewport   Viewport Bounds of the portion of the render space currently being rendered.  
     *                      If <code>null</code>, <code>viewport</code> will be as <code>subViewport</code>.
     * @param outMat        Holds modelview matrix after method returns.
     */
    public void computeModelToCameraMatrix(LongRect viewport, LongRect subViewport, double[] outMat);
    
    /**
     * Computes transform from camera-axis-coordinates to normalized-device-coordinates (-1 to 1). 
     * 
     * @param viewport      Viewport bounds of the entire render space being covered by this camera.
     * @param subViewport   Viewport Bounds of the portion of the render space currently being rendered.  
     *                      If <code>null</code>, <code>viewport</code> will be used as <code>subViewport</code>.
     * @param outMat        Holds projection matrix after method returns.
     */
    public void computeCameraToNormDeviceMatrix(LongRect viewport, LongRect subViewport, double[] outMat);
    
    /**
     * Computes transform from normalized-device-coordinates (-1 to 1) to 
     * awt-component-coordinates: (0,0) is the top-left of component, 
     * (width_pixels, height_pixels) is the bottom-right.
     * 
     * @param viewport      Viewport bounds of the entire render space being covered by this camera.
     * @param subViewport   Viewport Bounds of the portion of the render space currently being rendered.  
     *                      If <code>null</code>, <code>viewport</code> will be used as <code>subViewport</code>.
     * @param outMat        Holds viewport matrix after method returns.
     */
    public void computeNormDeviceToAwtMatrix(LongRect viewport, LongRect subViewport, double[] outMat);
    
    /**
     * Computes transform from model-axis-coordinates to normalized-device-coordinates (-1 to 1).
     * This is equivalent to compositing the modelview ond projection matrices.
     * 
     * @param viewport      Viewport bounds of the entire render space being covered by this camera.
     * @param subViewport   Viewport Bounds of the portion of the render space currently being rendered.  
     *                      If <code>null</code>, <code>viewport</code> will be used as <code>subViewport</code>.
     * @param outMat        Holds transform matrix after method returns.
     */
    public void computeModelToNormDeviceMatrix(LongRect viewport, LongRect subViewport, double[] outMat);
    
    /**
     * Computes transform from model-axis-coordinates to

     * awt-component-coordinates: (0,0) is the top-left of the component,
     * (width_pixels, height_pixels) is tho bottom-right of the component. 
     *  
     * @param viewport      Viewport bounds of the entire render space being covered by this camera.
     * @param subViewport   Viewport Bounds of the portion of the render space currently being rendered.  
     *                      If <code>null</code>, <code>viewport</code> will be used as <code>subViewport</code>.
     * @param outMat        Holds transform matrix after method returns.
     */
    public void computeModelToAwtMatrix(LongRect viewport, LongRect subViewport, double[] outMat);
    
}
