package bits.draw3d.pick;


import bits.draw3d.model.DrawTri;
import bits.math3d.Vec;
import bits.math3d.Vec3;


/**
 * @author decamp
 */
class TriangleIntersector {

    private final Vec3 mNorm = new Vec3();
    private final Vec3 mTemp = new Vec3();

    public boolean intersect( float[] rayPoint, float[] rayDir, DrawTri target, RayIntersection out ) {
        Vec3 norm = mNorm;

        Vec3 v0 = target.mVerts[0].mPos;
        Vec3 v1 = target.mVerts[1].mPos;
        Vec3 v2 = target.mVerts[2].mPos;

        //Compute triangle normal.
        Vec.cross( v0, v1, v2, norm );

        //Compute side of triangle that ray will hit.
        float dot = ( norm.x * rayDir[0] + norm.y * rayDir[1] + norm.z * rayDir[2] );

        if(dot < 0.0) {
            out.mTargetSide = Side.FRONT;
        }else if(dot > 0.0) {
            out.mTargetSide = Side.BACK;
        }else{
            return false;
        }

        //Compute intersection point between ray and plane of triangle.
        float t = ( norm.x * (rayPoint[0] - v0.x ) +
                    norm.y * (rayPoint[1] - v0.y ) +
                    norm.z * (rayPoint[2] - v0.z ) ) / -dot;

        if( t <= 0 ) {
            return false;
        }

        Vec3 outPoint = out.mPoint;
        outPoint.x = rayPoint[0] + rayDir[0] * t;
        outPoint.y = rayPoint[1] + rayDir[1] * t;
        outPoint.z = rayPoint[2] + rayDir[2] * t;


        //Compute if point lies on correct side of each triangle edge.
        Vec3 temp = mTemp;

        Vec.cross( v0, v1, outPoint, temp );
        if( Vec.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        Vec.cross( v1, v2, outPoint, temp );
        if( Vec.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        Vec.cross( v2, v0, outPoint, temp );
        if( Vec.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        out.mRayDist = t;
        return true;
    }

}
