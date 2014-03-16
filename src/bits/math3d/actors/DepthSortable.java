package bits.math3d.actors;

import java.util.Comparator;

/**
 * @author decamp
 */
public interface DepthSortable {
    
    /**
     * @return direct reference to length-3 array that holds
     *         position of object in normalized-device-coordinates.
     */
    public double[] normPosRef();
    
    /**
     * Updates the normalized-device-coordinates of the object.
     * 
     * @param modelToNormMat 4x4 matrix that defines transformation from
     *                       model-coordinates to normalized-device-coordinates.
     */
    public void updateNormPos( double[] modelToNormMat );
    
    /**
     * @return The depth of the object in normalized device coordinates.
     */
    public double normDepth();

    
    public static final Comparator<DepthSortable> BACK_TO_FRONT_ORDER = new Comparator<DepthSortable>() {
        public int compare( DepthSortable a, DepthSortable b ) {
            double aa = a.normDepth();
            double bb = b.normDepth();
            return aa > bb ? -1 :
                   aa < bb ?  1 : 0;
        }
    };

    
    public static final Comparator<DepthSortable> FRONT_TO_BACK_ORDER = new Comparator<DepthSortable>() {
        public int compare( DepthSortable a, DepthSortable b ) {
            double aa = a.normDepth();
            double bb = b.normDepth();
            return aa < bb ? -1 :
                   aa > bb ?  1 : 0;
        }
    };
    
}
