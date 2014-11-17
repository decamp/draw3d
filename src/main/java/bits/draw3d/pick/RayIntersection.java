package bits.draw3d.pick;

import bits.math3d.Vec3;


/**
 * @author decamp
 */
class RayIntersection {
    final Vec3 mPoint = new Vec3(); //Point of collision.
    float mRayDist;                 // Ray distance parameter.
    int   mTargetSide;              // Side of target, if oriented.
}
