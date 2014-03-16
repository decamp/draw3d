package bits.draw3d.pick;

import bits.draw3d.model.*;
import bits.math3d.Vectors;


/**
 * @author decamp
 */
class TriangleIntersector {

    private final double[] mNorm = new double[3];
    private final double[] mTemp = new double[3];
    
    public boolean intersect(double[] rayPoint, double[] rayDir, Triangle target, RayIntersection out) {
        double[] norm = mNorm;
        
        double[] v0 = target.vertexRef()[0];
        double[] v1 = target.vertexRef()[1];
        double[] v2 = target.vertexRef()[2];

        //Compute triangle normal.
        Vectors.cross( v0, v1, v2, norm );
        
        //Compute side of triangle that ray will hit.
        double dot = Vectors.dot(norm, rayDir);
        
        if(dot < 0.0) {
            out.mTargetSide = Side.FRONT;
        }else if(dot > 0.0) {
            out.mTargetSide = Side.BACK;
        }else{
            return false;
        }
        
        //Compute intersection point between ray and plane of triangle.
        double t = (norm[0] * (rayPoint[0] - v0[0]) + 
                    norm[1] * (rayPoint[1] - v0[1]) +
                    norm[2] * (rayPoint[2] - v0[2])) / -dot;

        if( t <= 0.0 ) {
            return false;
        }
        
        double[] outPoint = out.mPoint;
        outPoint[0] = rayPoint[0] + rayDir[0] * t;
        outPoint[1] = rayPoint[1] + rayDir[1] * t;
        outPoint[2] = rayPoint[2] + rayDir[2] * t;
        
        
        //Compute if point lies on correct side of each triangle edge.
        double[] temp = mTemp;
        
        Vectors.cross( v0, v1, outPoint, temp );
        if( Vectors.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        Vectors.cross( v1, v2, outPoint, temp );
        if( Vectors.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        Vectors.cross( v2, v0, outPoint, temp );
        if( Vectors.dot( norm, temp ) < 0.0 ) {
            return false;
        }

        out.mRayDist = t;
        return true;
    }

}
