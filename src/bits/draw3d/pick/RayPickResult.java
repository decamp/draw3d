package bits.draw3d.pick;

import bits.draw3d.model.Triangle;

/**
 * RayPickResults are used to store the output of a pick request.  PickResults are designed
 * to be used with the class of GeometryPicker that allocates them such that PickResults
 * can be used to store both results and working data, providing better control over
 * thread safety and memory management.  
 * 
 * @author Philip DeCamp
 */
public interface RayPickResult {

    /**
     * @return true iff PickResult has a valid, non-empty pick (aka, collision/intersection) 
     */
    public boolean hasPick();
    
    /**
     * Returns the distance from the start point of the ray to the picked point.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, the return value is undefined.
     * 
     * @return distance from ray origin to picked point.
     */
    public double pickedDistance();
    
    /**
     * Returns the distance from the start point of the ray to the picked point in units
     * of the ray direction provided for the pick.  This parameter is often written as 
     * <tt>t</tt>: the parameterized distance from the ray's origin.  If the ray direction
     * was normalized, then this result will be identical to <tt>pickedDistance()</tt>,
     * rounding errors not withstanding.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, the return value is undefined.
     * 
     * @return ray distance from origin to picked point
     */
    public double pickedParamDistance();
    
    /**
     * Returns a safe copy of the picked point as a length-3 array.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, this method is undefined.
     * 
     * @return safe copy of picked point
     */
    public double[] pickedPoint();
    
    /**
     * Returns direct reference to the picked point as a length-3 array.  
     * Reuse of this PickResult will cause this array to be overwritten.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, this method is undefined.
     * 
     * @return reference to picked point
     */
    public double[] pickedPointRef();
    
    /**
     * Returns the picked triangle.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, this method is undefined.
     * 
     * @return picked triangle
     */
    public Triangle pickedTriangle();
    
    /**
     * @return side of picked object.
     */
    public int pickedSide();
    
    /**
     * Returns data associated with picked object, if any.
     * <p>
     * If <tt>hasPick()</tt> is <tt>false</tt>, this method is undefined.
     * 
     * @return data associated with pick 
     */
    public Object pickedData();
    
}
