package bits.draw3d.pick;

/**
 * @author decamp
 */
class RayIntersection {
    final double[] mPoint = new double[3];  //Point of collision.
    double mRayDist;                        // Ray distance parameter.
    int mTargetSide;                        // Side of target, if oriented. 
}
