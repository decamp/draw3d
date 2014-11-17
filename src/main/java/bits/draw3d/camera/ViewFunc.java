/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.actors.Actor;
import bits.math3d.Mat4;

public interface ViewFunc {
    public void computeViewMat( Actor camera, Mat4 out );
}
