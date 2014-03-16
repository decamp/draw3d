package bits.math3d.camera;

import bits.math3d.Matrices;

public class BasicViewportFunc implements ViewportFunc {
    @Override
    public void computeViewportMat( int[] viewport, int[] tileViewport, double[] outMat ) {
        Matrices.computeViewportDepthMatrix( viewport[0], viewport[1], viewport[2] - viewport[0], viewport[3] - viewport[1], 0.0, 1.0, outMat );  
    }
}
