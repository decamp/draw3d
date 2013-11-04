package bits.draw3d.pick;

/**
 * @author decamp
 */
public interface RayPicker {
    
    /**
     * @return a newly allocated <tt>PickResult</tt> object.
     */
    public RayPickResult newRayPickResult();

    /**
     * Given a point and a direction, the <i>pick</i> operation finds the nearest point
     * in some set of geometric objects.  We're mostly just working with Triangles, but
     * we might eventually use other primitives. 
     * 
     * @param rayPoint Origin point of the ray as a length-3 array.
     * @param rayDir Direction of ray as a length-3 array.  Need not be normalized, but zero-length directions produce undefined results.
     * @param sides Which sides to pick. (Side.FRONT, Side.BACK, or Side.BOTH)
     * @param out PickResult allocated with <tt>newRayPickResult()</tt> that will hold results on a successful pick.
     * @return true iff point was picked
     */
    public boolean pick(double[] rayPoint, double[] rayDir, int sides, RayPickResult out);

}
