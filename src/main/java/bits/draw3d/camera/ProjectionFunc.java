/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.Rect;
import bits.math3d.Mat4;


public interface ProjectionFunc {
    public float nearPlane();
    public void  nearPlane( float nearPlane );
    public float farPlane();
    public void  farPlane( float farPlane );
    public void  computeProjectionMat( Rect viewport, Rect tileViewport, Mat4 out );
}
