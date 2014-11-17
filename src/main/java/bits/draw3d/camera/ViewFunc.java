package bits.draw3d.camera;

import bits.draw3d.actors.Actor;
import bits.math3d.Mat4;

public interface ViewFunc {
    public void computeViewMat( Actor camera, Mat4 out );
}
