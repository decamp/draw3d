package bits.draw3d.model;

import java.util.ArrayList;
import java.util.List;

import bits.math3d.Vectors;
import bits.math3d.geom.*;


/**
 * @author decamp
 */
public final class Triangulations {


    public static List<Triangle> triangulate( Volume volume, boolean computeNorms ) {
        if( volume instanceof Aabb ) {
            return Triangulations.triangulateAabb( (Aabb)volume, computeNorms );
        }

        if( volume instanceof ExtrudedLineLoop ) {
            return Triangulations.triangulateExtrudedLineLoop( (ExtrudedLineLoop)volume, computeNorms );
        }

        return null;
    }


    public static List<Triangle> triangulateAabb( Aabb aabb, boolean computeNorms ) {

        double[][] edge = new double[][]{ { aabb.minX(), aabb.minY(), aabb.minZ() },
                                          { aabb.maxX(), aabb.maxY(), aabb.maxZ() } };
        double[][] verts = new double[8][];

        for( int i = 0; i < verts.length; i++ ) {
            double x = edge[(i >> 2) & 0x1][0];
            double y = edge[(i >> 1) & 0x1][1];
            double z = edge[(i) & 0x1][2];

            verts[i] = new double[]{ x, y, z };
        }

        List<Triangle> ret = new ArrayList<Triangle>( 12 );
        
        for( int i = 0; i < 3; i++ ) {
            int m0 = (1 << i);
            int m1 = (1 << ((i + 1) % 3));
            int m2 = (1 << ((i + 2) % 3));

            for( int j = 0; j < 2; j++ ) {
                ret.add( Triangles.triangleFromVertices( verts[m0 * j],
                                                         verts[m0 * j | m1],
                                                         verts[m0 * j | m1 | m2],
                                                         computeNorms ) );

                ret.add( Triangles.triangleFromVertices( verts[m0 * j | m1 | m2],
                                                         verts[m0 * j | m2],
                                                         verts[m0 * j | 0],
                                                         computeNorms ) );
            }
        }

        return ret;
    }

    /**
     * This is not a true triangulation, but should work okay if you don't have
     * any weirdo shapes.
     * 
     * @param loop
     * @return
     */
    public static List<Triangle> triangulateExtrudedLineLoop( ExtrudedLineLoop loop, boolean computeNorms ) {
        int n = loop.pointCount();
        double[] x = loop.xRef();
        double[] y = loop.yRef();
        double minZ = loop.minZ();
        double maxZ = loop.maxZ();
        boolean clockWise = loop.isClockwise();

        List<Triangle> ret = new ArrayList<Triangle>( (n - 1) * 4 );
        List<double[]> verts = new ArrayList<double[]>( n );

        for( int i = 0; i < n; i++ ) {
            verts.add( new double[]{ x[i], y[i], minZ } );
        }

        {
            List<double[]> vv = new ArrayList<double[]>( verts );

            // Triangulate bottom surface.
            while( vv.size() >= 3 ) {
                int size = vv.size();
                double minAng = Double.POSITIVE_INFINITY;
                int acutest = 0;

                for( int i = 0; i < size; i++ ) {
                    double[] v0 = vv.get( i );
                    double[] v1 = vv.get( (i + size - 1) % size );
                    double[] v2 = vv.get( (i + 1) % size );

                    double ang = Vectors.ang( v0, v1, v2 );
                    if( Double.isNaN( ang ) ) {
                        ang = Double.NEGATIVE_INFINITY;
                    }

                    if( ang < minAng ) {
                        minAng = ang;
                        acutest = i;
                    }
                }

                double[] v0 = vv.get( acutest );
                double[] v1 = vv.get( (acutest + size - 1) % size );
                double[] v2 = vv.get( (acutest + 1) % size );
                vv.remove( acutest );

                if( clockWise ) {
                    ret.add( Triangles.triangleFromVertices( v0, v2, v1, computeNorms ) );
                } else {
                    ret.add( Triangles.triangleFromVertices( v0, v1, v2, computeNorms ) );
                }
            }
        }


        // Duplicate surface for top.
        for( int i = ret.size() - 1; i >= 0; i-- ) {
            Triangle t = ret.get( i ).safeCopy();
            t.vertex( 0 )[2] = maxZ;
            t.vertex( 1 )[2] = maxZ;
            t.vertex( 2 )[2] = maxZ;
            t.reverseOrientation();
            ret.add( t );
        }

        // Triangulate around edges.
        for( int i = 0; i < n; i++ ) {
            double[] v0, v1;
            double[] v2, v3;

            if( clockWise ) {
                v0 = verts.get( (i + 1) % n );
                v1 = verts.get( i );
            } else {
                v0 = verts.get( i );
                v1 = verts.get( (i + 1) % n );
            }

            v2 = v0.clone();
            v3 = v1.clone();
            v2[2] = maxZ;
            v3[2] = maxZ;

            ret.add( Triangles.triangleFromVertices( v0, v1, v3, computeNorms ) );
            ret.add( Triangles.triangleFromVertices( v3, v2, v0, computeNorms ) );
        }

        return ret;
    }



    private Triangulations() {}

}
