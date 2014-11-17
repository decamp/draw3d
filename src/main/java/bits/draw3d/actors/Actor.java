/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.actors;

import bits.draw3d.TransObject;
import bits.math3d.*;


/**
 * Represents an object with basic spatial properties.
 * 
 * @author decamp
 */
public class Actor extends Trans3 implements DepthSortable, TransObject {

    private static final int ROTATIONS_PER_NORMALIZATION = 100;

    /**
     * Local scaling 3-vector.
     */
    public final Vec3 mScale = new Vec3( 1, 1, 1 );

    /**
     * 3-vector that may be used to hold postion in 
     * normalized device coordinates for sorting.
     */
    public final Vec3 mNormPos = new Vec3();


    protected final Mat4 mWorkMat  = new Mat4();
    int mRotationCount = 0;


    /**
     * Translates object.
     */
    public void translate( float tx, float ty, float tz ) {
        Vec.addTo( tx, ty, tz, mPos );
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
    public void rotate( float rads, float rx, float ry, float rz ) {
        if( Tol.approxZero( rads, Ang.PI ) ) {
            return;
        }
        Mat.rotate( mRot, rads, rx, ry, rz, mRot );
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
    public void preRotate( float rads, float rx, float ry, float rz ) {
        if( Tol.approxZero( rads, Ang.PI ) ) {
            return;
        }
        Mat.preRotate( rads, rx, ry, rz, mRot, mRot );
        if( ++mRotationCount > ROTATIONS_PER_NORMALIZATION ) {
            normalizeRotation();
        }
    }
    
    /**
     * Scales the object size by the specified amount.
     */
    public void scale( double sx, double sy, double sz ) {
        mScale.x *= sx;
        mScale.y *= sy;
        mScale.z *= sz;
    }
    
    /**
     * Computes full transform for this object: <br/>
     * translation * rotation * scaling. <br/>
     * This method is computes the transform directly and efficiently,
     * using only 9 scalar multiplications.
     *  
     * @param out Receives output 4x4 transform.
     */
    public void computeTransform( Mat4 out ) {
        Mat3 rot = mRot;
        Vec3 pos = mPos;

        out.m00 = mScale.x * rot.m00;
        out.m10 = mScale.x * rot.m10;
        out.m20 = mScale.x * rot.m20;
        out.m30 = 0;
        
        out.m01 = mScale.y * rot.m01;
        out.m11 = mScale.y * rot.m11;
        out.m21 = mScale.y * rot.m21;
        out.m31 = 0;

        out.m02 = mScale.z * rot.m02;
        out.m12 = mScale.z * rot.m12;
        out.m22 = mScale.z * rot.m22;
        out.m32 = 0;
        
        out.m03 = pos.x;
        out.m13 = pos.y;
        out.m23 = pos.z;
        out.m33 = 1;
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
        Mat.normalizeRotationMatrix( mRot );
    }
    

    public Vec3 normPosRef() {
        return mNormPos;
    }

    
    public float normDepth() {
        return mNormPos.z;
    }
    
    
    public void updateNormPos( Mat4 projViewMat ) {
        Mat.mult( projViewMat, mPos, mNormPos );
    }


    public Vec3 pos() {
        return mPos;
    }


    public Mat3 rot() {
        return mRot;
    }

}
