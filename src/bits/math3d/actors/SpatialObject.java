package bits.math3d.actors;

import bits.math3d.*;

/**
 * Represents an object with basic spatial properties.
 * 
 * @author decamp
 */
public class SpatialObject implements DepthSortable {

    
    private static final int ROTATIONS_PER_NORMALIZATION = 100;

    /**
     * Object position as 3-vector.
     */
    public final double[] mPos = {0,0,0};
    
    /**
     * Object rotation as 4x4-matrix. 
     */
    public final double[] mRot = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
    
    /**
     * Local scaling 3-vector.
     */
    public final double[] mScale = {1,1,1};
    
    /**
     * 3-vector that may be used to hold postion in 
     * normalized device coordinates for sorting.
     */
    public final double[] mNormPos = {0,0,0};
    
    
    protected final double[][] mWork = new double[2][16];
    int mRotationCount = 0;
    

    
    /**
     * Translates object by specified amount.
     * @param tx
     * @param ty
     * @param tz
     */
    public void translate( double tx, double ty, double tz ) {
        mPos[0] += tx;
        mPos[1] += ty;
        mPos[2] += tz;
    }
    
    /**
     * Rotates the object about the specified axis.  
     * Rotation is applied AFTER existing rotation.
     * 
     * @param rads Angle of rotation in radians.
     * @param rx x-component of axis
     * @param ry y-component of axis
     * @param rz z-component of axis
     */
    public void rotate( double rads, double rx, double ry, double rz ) {
        if( Tol.approxZero( rads, Math.PI ) ) {
            return;
        }
        Matrices.computeRotationMatrix( rads, rx, ry, rz, mWork[0] );
        Matrices.multMatMat( mRot, mWork[0], mWork[1] );
        System.arraycopy( mWork[1], 0, mRot, 0, 16 );
        if( ++mRotationCount > ROTATIONS_PER_NORMALIZATION ) {
            normalizeRotation();
        }
    }
    
    /**
     * Rotates the object about the specified axis.  
     * Rotation is applied BEFORE existing rotation.
     * 
     * @param rads Angle of rotation in radians.
     * @param rx x-component of axis
     * @param ry y-component of axis
     * @param rz z-component of axis
     */
    public void preRotate( double rads, double rx, double ry, double rz ) {
        if( Math.abs( rads ) < Tol.ABS_ERR ) {
            return;
        }
        Matrices.computeRotationMatrix( rads, rx, ry, rz, mWork[0] );
        Matrices.multMatMat( mWork[0], mRot, mWork[1] );
        System.arraycopy( mWork[1], 0, mRot, 0, 16 );
        if( ++mRotationCount > ROTATIONS_PER_NORMALIZATION ) {
            normalizeRotation();
        }
    }
    
    /**
     * Scales the object size by the specified amonut.
     * 
     * @param sx
     * @param sy
     * @param sz
     */
    public void scale( double sx, double sy, double sz ) {
        mScale[0] *= sx;
        mScale[1] *= sy;
        mScale[2] *= sz;
    }
    
    /**
     * Computes the transform for this object: <br/>
     * translation * rotation * scaling. <br/>
     * This method is computes the transform directly and efficiently,
     * using only 9 scalar multiplications.
     *  
     * @param out4x4 To hold transform.
     */
    public void computeTransform( double[] out4x4 ) {
        out4x4[ 0] = mScale[0] * mRot[ 0];
        out4x4[ 1] = mScale[0] * mRot[ 1];
        out4x4[ 2] = mScale[0] * mRot[ 2];
        out4x4[ 3] = 0;
        
        out4x4[ 4] = mScale[1] * mRot[ 4];
        out4x4[ 5] = mScale[1] * mRot[ 5];
        out4x4[ 6] = mScale[1] * mRot[ 6];
        out4x4[ 7] = 0;
        
        out4x4[ 8] = mScale[2] * mRot[ 8];
        out4x4[ 9] = mScale[2] * mRot[ 9];
        out4x4[10] = mScale[2] * mRot[10];
        out4x4[11] = 0;
        
        out4x4[12] = mPos[0];
        out4x4[13] = mPos[1];
        out4x4[14] = mPos[2];
        out4x4[15] = 1.0;
    }
    
    /**
     * Normalize the current rotation matrix, ensuring
     * transformation contains orthogonal axes and 
     * contains no translation or scaling.
     * <p>
     * This method is called automatically after every hundred rotations.
     */
    public void normalizeRotation() {
        mRotationCount = 0;
        Matrices.normalizeRotationMatrix( mRot );
    }
    
    
    
    public double[] normPosRef() {
        return mNormPos;
    }

    
    public double normDepth() {
        return mNormPos[2];
    }
    
    
    public void updateNormPos( double[] modelToNormMat ) {
        Matrices.multMatVec( modelToNormMat, mPos, mNormPos );
    }
    
}
