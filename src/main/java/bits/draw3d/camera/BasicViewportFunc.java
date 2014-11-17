/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.Rect;
import bits.math3d.Mat;
import bits.math3d.Mat4;

public class BasicViewportFunc implements ViewportFunc {
    @Override
    public void computeViewportMat( Rect viewport, Rect tileViewport, Mat4 out ) {
        Mat.getViewportDepth( viewport.x0,
                              viewport.y0,
                              viewport.width(),
                              viewport.height(),
                              0,
                              1,
                              out );
    }

}
