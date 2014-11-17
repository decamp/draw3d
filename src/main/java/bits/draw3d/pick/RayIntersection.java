/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

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
