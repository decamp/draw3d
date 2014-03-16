package bits.math3d.camera;

import bits.math3d.actors.SpatialObject;

public interface ViewFunc {
    public void computeViewMat( SpatialObject camera, double[] out );
}
