package bits.math3d.camera;

public interface ViewportFunc {
    public void computeViewportMat( int[] viewport, int[] tileViewport, double[] outMat );
}
