package bits.math3d.camera;

public interface ProjectionFunc {
    public float nearPlane();
    public ProjectionFunc nearPlane( float nearPlane );
    public float farPlane();
    public ProjectionFunc farPlane( float farPlane );
    
    public void computeProjectionMat( int[] viewport, int[] tileViewport, double[] outMat );
}
