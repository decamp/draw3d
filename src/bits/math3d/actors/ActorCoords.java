package bits.math3d.actors;

import bits.math3d.*;

/**
 * @author decamp
 */
public class ActorCoords {
    
    private static final double[] AXIS_MATRIX = { 0,  0, -1,  0, 
                                                 -1,  0,  0,  0, 
                                                  0,  1,  0,  0,
                                                  0,  0,  0,  1 };
    
    private static final double[] INVERSE_AXIS_MATRIX = new double[16];
    
    
    static {
        Matrices.invert( AXIS_MATRIX, INVERSE_AXIS_MATRIX ); 
    }
    

    /**
     * Gets the homographic transform for converting actor coordinates to GL coordinates. 
     * @param out4x4
     */
    public static void actorToGlMatrix(double[] out4x4) {
        System.arraycopy(AXIS_MATRIX, 0, out4x4, 0, 16);
    }
    
    /**
     * Gets the homographic transform for converting actor coordinates to GL coordinates. 
     * @return 4x4 matrix
     */
    public static double[] newActorToGlMatrix() {
        return AXIS_MATRIX.clone();
    }
 
    /**
     * Gets the homographic transform for converting GL coordinates to actor coordinates. 
     * @param out4x4
     */
    public static void glToActorMatrix(double[] out4x4) {
        System.arraycopy(INVERSE_AXIS_MATRIX, 0, out4x4, 0, 16);
    }

    /**
     * Gets the homographic transform for converting GL coordinates to actor coordinates. 
     * @return new 4x4 matrix
     */
    public static double[] newGlToActorMatrix() {
        return INVERSE_AXIS_MATRIX.clone();
    }

    
    public static void forwardAxis( double[] out3x1 ) {
        forwardAxis( out3x1, 0 );
    }
    
    public static void forwardAxis( double[] out3x1, int off ) {
        out3x1[0+off] = 1;
        out3x1[1+off] = 0;
        out3x1[2+off] = 0;
    }
    
    public static void backwardAxis(double[] out3x1) {
        backwardAxis( out3x1, 0 );
    }
    
    public static void backwardAxis( double[] out3x1, int off ) {
        out3x1[0+off] = -1;
        out3x1[1+off] =  0;
        out3x1[2+off] =  0;
    }
    
    public static void leftAxis( double[] out3x1 ) {
        leftAxis( out3x1, 0 );
    }
    
    public static void leftAxis( double[] out3x1, int off ) {
        out3x1[0+off] = 0;
        out3x1[1+off] = 1;
        out3x1[2+off] = 0;
    }
    
    public static void rightAxis( double[] out3x1 ) {
        rightAxis( out3x1, 0 );
    }
    
    public static void rightAxis( double[] out3x1, int off ) {
        out3x1[0+off] = 0;
        out3x1[1+off] = -1;
        out3x1[2+off] = 0;
    }
    
    public static void upAxis( double[] out3x1 ) {
        upAxis( out3x1, 0 );
    }
    
    public static void upAxis( double[] out3x1, int off ) {
        out3x1[0+off] = 0;
        out3x1[1+off] = 0;
        out3x1[2+off] = 1;
    }
    
    public static void downAxis( double[] out3x1 ) {
        downAxis( out3x1, 0 );
    }
    
    public static void downAxis( double[] out3x1, int off ) {
        out3x1[0+off] = 0;
        out3x1[1+off] = 0;
        out3x1[2+off] = -1;
    }
    
    public static double[] newForwardAxis() {
        return new double[]{1, 0, 0};
    }
    
    public static double[] newBackwardAxis() {
        return new double[]{-1, 0, 0};
    }
    
    public static double[] newLeftAxis() {
        return new double[]{0, 1, 0};
    }
    
    public static double[] newRightAxis() {
        return new double[]{0, -1, 0};
    }
    
    public static double[] newUpAxis() {
        return new double[]{0, 0, 1};
    }
    
    public static double[] newDownAxis() {
        return new double[]{0, 0, -1};
    }
    
    
    
    
    /**
     * @deprecated
     * 
     * Has not been tested.
     * 
     * @param position3x1
     * @param forward3x1
     * @param up3x1
     * @param out4x4
     */
    public static void viewToMatrixTest(double[] position3x1, double[] forward3x1, double[] up3x1, double[] out4x4) {
        orientationToMatrixTest(forward3x1, up3x1, out4x4);
        out4x4[12] = -position3x1[0];
        out4x4[13] = -position3x1[1];
        out4x4[14] = -position3x1[2];
    }
    
    /**
     * @deprecated
     * 
     * Has not been tested.
     * @param forward3x1
     * @param up3x1
     * @param out4x4
     */
    public static void orientationToMatrixTest(double[] forward3x1, double[] up3x1, double[] out4x4) {

        double[] forward = forward3x1.clone();
        double[] up      = up3x1.clone();
        double[] left    = new double[3];
        double d = Vectors.hypot(forward);
        
        //Normalize forward-vector.  Choose default if provided vector is zero-length.
        if( d > Tol.SQRT_ABS_ERR ) {
            forward[0] /= d;
            forward[1] /= d;
            forward[2] /= d;
        }else{
            forwardAxis(forward);
        }
        
        //Orthoganalize u-vector.
        d = Vectors.dot(forward, up);
        up[0] -= forward[0] * d;
        up[1] -= forward[1] * d;
        up[2] -= forward[2] * d;
        
        d = Vectors.hypot(up);
        
        //Normalize up-vector.  Choose arbitrary orthogonal vector if zero-length.
        if( d > Tol.SQRT_ABS_ERR ) {
            up[0] /= d;
            up[1] /= d;
            up[2] /= d;
        } else {
            Vectors.chooseOrtho(forward[0], forward[1], forward[2], up);
        }
        
        //Compute left-vector as up-cross-forward.
        Vectors.cross(up, forward, left);
        
        out4x4[ 0] = forward[0];
        out4x4[ 1] = forward[1];
        out4x4[ 2] = forward[2];
        out4x4[ 3] = 0;
        
        out4x4[ 4] = left[0];
        out4x4[ 5] = left[1];
        out4x4[ 6] = left[2];
        out4x4[ 7] = 0;
        
        out4x4[ 8] = up[0];
        out4x4[ 9] = up[1];
        out4x4[10] = up[2];
        out4x4[11] = 0;
        
        out4x4[12] = 0;
        out4x4[13] = 0;
        out4x4[14] = 0;
        out4x4[15] = 1;
    }


    
    
    /**
     * @deprecated Name was ambiguous.  Replaced by <code>actorToGlMatrix()</code>.
     */
    public static void gazeMatrix(double[] out4x4) {
        actorToGlMatrix(out4x4);
    }
    
    /**
     * @deprecated Name was ambiguous.  Replaced by <code>glToActorMatrix()</code>. 
     */
    public static void inverseGazeMatrix(double[] out4x4) {
        glToActorMatrix(out4x4);
    }


}
