package cogmac.draw3d.model;

import java.util.*;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;

import cogmac.math3d.Tolerance;
import cogmac.math3d.Vectors;
import cogmac.math3d.geom.*;


/** 
 * @author Philip DeCamp  
 */
public class Triangles {

    
    /**
     * Computes the perpendicular distance between triangle (p0,p1,p2) and point (x).
     * This value may be positive or negative, depending on orientation of triangle.
     * 
     * @param p0 Point on triangle
     * @param p1 Point on triangle
     * @param p2 Point on triangle
     * @param x Any point
     * @return distance between point <i>x</i> and triangle <i>(p0,p1,p2)</i>.
     */
    public static double computePerpDistance(double[] p0, double[] p1, double[] p2, double[] x) {
        
        double a = p0[1] * (p1[2] - p2[2]) + 
                   p1[1] * (p2[2] - p0[2]) + 
                   p2[1] * (p0[2] - p1[2]);

        double b = p0[2] * (p1[0] - p2[0]) + 
                   p1[2] * (p2[0] - p0[0]) + 
                   p2[2] * (p0[0] - p1[0]);

        double c = p0[0] * (p1[1] - p2[1]) + 
                   p1[0] * (p2[1] - p0[1]) + 
                   p2[0] * (p0[1] - p1[1]);

        double d = -(p0[0] * (p1[1] * p2[2] - p2[1] * p1[2]) + 
                     p1[0] * (p2[1] * p0[2] - p0[1] * p2[2]) + 
                     p2[0] * (p0[1] * p1[2] - p1[1] * p0[2]));

        return a * x[0] + b * x[1] + c * x[2] + d;
    }
    

    /**
     * Computes normal of triangle <i>(p0, p1, p2)</i>, without normalizing length.
     * 
     * @param p0
     * @param p1
     * @param p2
     * @param out
     */
    public static void computeNorm(double[] p0, double[] p1, double[] p2, double[] out) {
        out[0] = (((p1[1] - p0[1]) * (p2[2] - p0[2])) - ((p2[1] - p0[1]) * (p1[2] - p0[2])));
        out[1] = (((p1[2] - p0[2]) * (p2[0] - p0[0])) - ((p2[2] - p0[2]) * (p1[0] - p0[0])));
        out[2] = (((p1[0] - p0[0]) * (p2[1] - p0[1])) - ((p2[0] - p0[0]) * (p1[1] - p0[1])));
    }
    
    
    /**
     * Computes unit-length normal of triangle <i>(p0, p1, p2)</i>.
     * 
     * @param p0
     * @param p1
     * @param p2
     * @param out
     */
    public static void computeUnitNorm(double[] p0, double[] p1, double[] p2, double[] out) {
        computeNorm( p0, p1, p2, out );
        double d = Vectors.hypot( out );
        
        if( d > Tolerance.ABS_ERR ) {
            out[0] /= d;
            out[1] /= d;
            out[2] /= d;
        }
        
    }

    
    /**
     * Computes angle between p0p1 and p0p2, in radians. 
     * @param p0 vertex at angle
     * @param p1 vertex at end of edge1
     * @param p2 vertex at end of edge2
     * @return radians
     */
    public static double computeAngle(double[] p0, double[] p1, double[] p2) {
        double x1 = p1[0]-p0[0];
        double y1 = p1[1]-p0[1];
        double z1 = p1[2]-p0[2];
        double x2 = p2[0]-p0[0];
        double y2 = p2[1]-p0[1];
        double z2 = p2[2]-p0[2];
        double d1 = Math.sqrt(x1*x1 + y1*y1 + z1*z1);
        double d2 = Math.sqrt(x2*x2 + y2*y2 + z2*z2);
        
        return Math.acos((x1*x2 + y1*y2 + z1*z2) / d1 / d2);
    }
    

    /**
     * Computes center point of triangle.
     */
    public static void computeCenter(double[] p0, double[] p1, double[] p2, double[] out) {
        out[0] = (p0[0] + p1[0] + p2[0]) / 3.0;
        out[1] = (p0[1] + p1[1] + p2[1]) / 3.0;
        out[2] = (p0[2] + p1[2] + p2[2]) / 3.0;
    }
    
    
    /**
     * Compute intersection point between triangle and line segment
     * @param v0 vertex 0 of triangle
     * @param v1 vertex 1 of triangle
     * @param v2 vertex 2 of triangle
     * @param e0 endpoint 0 of line segment.
     * @param e1 endpoint 1 of line segment
     * @param lineTolerance line endpoints are extended proportionally when computing intersection.   
     * @param out place to store point of intersection
     * @return  0 iff line segment doesn't intersect triangle, <br/>
     *          1 iff line segment passes into the front of triangle, <br/>
     *         -1 iff line segment passes into the back of triangle
     */
    public static int computeTriangleLineIntersection( double[] v0, 
                                                       double[] v1, 
                                                       double[] v2, 
                                                       double[] e0, 
                                                       double[] e1,
                                                       double lineTolerance,
                                                       double[] out) 
    {
        double[] norm = new double[3];
        computeNorm(v0, v1, v2, norm);
        double dx = e1[0] - e0[0];
        double dy = e1[1] - e0[1];
        double dz = e1[2] - e0[2];
        
        double dot = norm[0]*dx + norm[1]*dy + norm[2]*dz;
        
        if(Math.abs(dot) < Tolerance.ABS_ERR)
            return 0;
        
        double t = (norm[0]*(e0[0]-v0[0]) + norm[1]*(e0[1]-v0[1]) + norm[2]*(e0[2]-v0[2])) / -dot;
        
        if(t < lineTolerance || t > (1.0 - lineTolerance))
            return 0;
        
        out[0] = e0[0] + dx * t;
        out[1] = e0[1] + dy * t;
        out[2] = e0[2] + dz * t;
        
        //Compute if point lies on correct side of each triangle edge.
        double[] norm2 = new double[3];
        
        computeNorm(v0, v1, out, norm2);
        if(norm[0]*norm2[0] + norm[1]*norm2[1] + norm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        computeNorm(v1, v2, out, norm2);
        if(norm[0]*norm2[0] + norm[1]*norm2[1] + norm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        computeNorm(v2, v0, out, norm2);
        if(norm[0]*norm2[0] + norm[1]*norm2[1] + norm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        return (dot < 0.0 ? 1 : -1);
    }


    /**
     * Compute intersection point between triangle and line segment
     * @param v0 vertex 0 of triangle
     * @param v1 vertex 1 of triangle
     * @param v2 vertex 2 of triangle
     * @param e0 endpoint 0 of line segment.
     * @param e1 endpoint 1 of line segment
     * @param lineTolerance line endpoints are extended proportionally when computing intersection.   
     * @param out place to store point of intersection
     * @param outNorm place to stort normal of triangle.
     * @return  0 iff line segment doesn't intersect triangle, <br/>
     *          1 iff line segment passes into the front of triangle, <br/>
     *         -1 iff line segment passes into the back of triangle
     */
    public static int computeTriangleLineIntersection( double[] v0, 
                                                       double[] v1, 
                                                       double[] v2, 
                                                       double[] e0, 
                                                       double[] e1,
                                                       double lineTolerance,
                                                       double[] out,
                                                       double[] outNorm) 
    {
        computeNorm(v0, v1, v2, outNorm);
        double dx = e1[0] - e0[0];
        double dy = e1[1] - e0[1];
        double dz = e1[2] - e0[2];
        
        double dot = outNorm[0]*dx + outNorm[1]*dy + outNorm[2]*dz;
        
        if( Math.abs(dot) < Tolerance.ABS_ERR )
            return 0;
        
        double t = (outNorm[0]*(e0[0]-v0[0]) + outNorm[1]*(e0[1]-v0[1]) + outNorm[2]*(e0[2]-v0[2])) / -dot;
        
        if(t < lineTolerance || t > (1.0 - lineTolerance))
            return 0;
        
        out[0] = e0[0] + dx * t;
        out[1] = e0[1] + dy * t;
        out[2] = e0[2] + dz * t;
        
        //Compute if point lies on correct side of each triangle edge.
        double[] norm2 = new double[3];
        
        computeNorm(v0, v1, out, norm2);
        if(outNorm[0]*norm2[0] + outNorm[1]*norm2[1] + outNorm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        computeNorm(v1, v2, out, norm2);
        if(outNorm[0]*norm2[0] + outNorm[1]*norm2[1] + outNorm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        computeNorm(v2, v0, out, norm2);
        if(outNorm[0]*norm2[0] + outNorm[1]*norm2[1] + outNorm[2]*norm2[2] < Tolerance.ABS_ERR)
            return 0;
        
        return (dot < 0.0 ? 1 : -1);
    }
    
    
    /**
     * Compute intersection point between plane (defined by triangle) and line segment
     * @param v0 vertex 0 on triangle
     * @param v1 vertex 1 of triangle
     * @param v2 vertex 2 of triangle
     * @param e0 endpoint 0 of line segment.
     * @param e1 endpoint 1 of line segment
     * @param lineTolerance line endpoints are extended proportionally when computing intersection.   
     * @param out place to store point of intersection
     * @return  0 iff line segment doesn't intersect triangle, <br/>
     *          1 iff line segment passes into the front of plane, <br/>
     *         -1 iff line segment passes into the back of plane
     */
    public static int computePlaneLineIntersection( double[] v0, 
                                                    double[] v1, 
                                                    double[] v2, 
                                                    double[] e0, 
                                                    double[] e1,
                                                    double lineTolerance,
                                                    double[] out) 
    {
        double[] norm = new double[3];
        computeNorm(v0, v1, v2, norm);
        double dx = e1[0] - e0[0];
        double dy = e1[1] - e0[1];
        double dz = e1[2] - e0[2];
        
        double dot = norm[0]*dx + norm[1]*dy + norm[2]*dz;
        
        if( Math.abs( dot ) < Tolerance.ABS_ERR )
            return 0;
        
        double t = (norm[0]*(e0[0]-v0[0]) + norm[1]*(e0[1]-v0[1]) + norm[2]*(e0[2]-v0[2])) / -dot;
        
        if(t < lineTolerance || t > (1.0 - lineTolerance))
            return 0;
        
        out[0] = e0[0] + dx * t;
        out[1] = e0[1] + dy * t;
        out[2] = e0[2] + dz * t;
        
        return (dot < 0.0 ? 1 : -1);
    }
    
    
    /**
     * Computes the bounds of a list of triangles.
     * @param tris List of triangles.
     * @return smallest possible cuboid that contains all triangles entirely.
     */
    public static Aabb computeBounds(Iterable<? extends Triangle> tris) {
        double x0 = Double.POSITIVE_INFINITY;
        double y0 = Double.POSITIVE_INFINITY;
        double z0 = Double.POSITIVE_INFINITY;
        double x1 = Double.NEGATIVE_INFINITY;
        double y1 = Double.NEGATIVE_INFINITY;
        double z1 = Double.NEGATIVE_INFINITY;
        boolean first = true;
        
        for(Triangle t: tris) {
            for(double[] vv: t.vertexRef()) {
                if(first) {
                    first = false;
                    x0 = x1 = vv[0];
                    y0 = y1 = vv[1];
                    z0 = z1 = vv[2];

                }else{
                    if(vv[0] < x0) {
                        x0 = vv[0];
                    }else if(vv[0] > x1) {
                        x1 = vv[0];
                    }

                    if(vv[1] < y0) {
                        y0 = vv[1];
                    }else if(vv[1] > y1) {
                        y1 = vv[1];
                    }

                    if(vv[2] < z0) {
                        z0 = vv[2];
                    }else if(vv[2] > z1) {
                        z1 = vv[2];
                    }
                }
            }
        }
        
        return Aabb.fromEdges(x0, y0, z0, x1, y1, z1);
    }
    
    
    /**
     * Computes the axis-aligned bounding-box of a single triangle.
     * 
     * @param v0 Vertex 0
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     * @param outAabb length-6 array in which to store aabb.
     */
    public static void computeAabb(double[] v0, double[] v1, double[] v2, double[] outAabb) {
        outAabb[0] = v0[0] < v1[0] ? (v0[0] < v2[0] ? v0[0] : v2[0]) : (v1[0] < v2[0] ? v1[0] : v2[0]);
        outAabb[1] = v0[1] < v1[1] ? (v0[1] < v2[1] ? v0[1] : v2[1]) : (v1[1] < v2[1] ? v1[1] : v2[1]);
        outAabb[2] = v0[2] < v1[2] ? (v0[2] < v2[2] ? v0[2] : v2[2]) : (v1[2] < v2[2] ? v1[2] : v2[2]);
        
        outAabb[3] = v0[0] > v1[0] ? (v0[0] > v2[0] ? v0[0] : v2[0]) : (v1[0] > v2[0] ? v1[0] : v2[0]);
        outAabb[4] = v0[1] > v1[1] ? (v0[1] > v2[1] ? v0[1] : v2[1]) : (v1[1] > v2[1] ? v1[1] : v2[1]);
        outAabb[5] = v0[2] > v1[2] ? (v0[2] > v2[2] ? v0[2] : v2[2]) : (v1[2] > v2[2] ? v1[2] : v2[2]);
    }
    

    /**
     * Computes the axis-aligned bounding-box of a single triangle clipped by
     * the specified bounding-box. 
     * 
     * @param v0 Vertex 0
     * @param v1 Vertex 1
     * @param v2 Vertex 2
     * @param clipAabb Clipping bounds
     * @param outAabb length-6 array to hold output bounds.  Undefined if triangle doesn't intersect clip volume.
     * @return true iff triangle intersects clip volume.
     */
    public static boolean computeClippedAabb( double[] v0, 
                                              double[] v1, 
                                              double[] v2, 
                                              double[] clipAabb, 
                                              double[] outAabb) 
    {
        double[][] verts = {v0, v1, v2};
        
        //Per-edge params indicating nearest and farthest location of edge that is unclipped. 
        double[] clipStart = {0, 0, 0};
        double[] clipStop = {1, 1, 1};
        
        for(int edge = 0; edge < 3; edge++) {
            double[] va = verts[edge];
            double[] vb = verts[(edge+1)%3];
            
            for(int axis = 0; axis < 3; axis++) {
                double a = va[axis];
                double b = vb[axis];
                double c;
                
                //Check min clip
                c = clipAabb[axis];

                if(a < c) {
                    //Vertex is clipped.

                    if(b <= c) {
                        //Next vertex also clipped.  Set start param to one.
                        clipStart[edge] = 1;

                    }else{
                        //Determine clip start.
                        c = (c - a) / (b - a);
                        if(c > clipStart[edge]) {
                            clipStart[edge] = c;
                        }
                    }
                }else if(b < c) {
                    //Next vertex is clipped.
                    //Determine clip stop.
                    c = (c - a) / (b - a);
                    if(c < clipStop[edge]) {
                        clipStop[edge] = c;
                    }
                }
                
                //Check max clip.
                c = clipAabb[axis+3];

                if(a > c) {
                    //Vertex is clipped.
                    
                    if(b >= c) {
                        //Next vertex also clipped.  Set start param to one.
                        clipStart[edge] = 1;
                        
                    }else{
                        //Determine clip start.
                        c = (c - a) / (b - a);
                        if(c > clipStart[edge]) {
                            clipStart[edge] = c;
                        }
                    }
                }else if(b > c) {
                    //next vertex is clipped.
                    //Determine clip stop.
                    c = (c - a) / (b - a);
                    if(c < clipStop[edge]) {
                        clipStop[edge] = c;
                    }
                }
            }
        }
                
        outAabb[0] = outAabb[1] = outAabb[2] = POSITIVE_INFINITY;
        outAabb[3] = outAabb[4] = outAabb[5] = NEGATIVE_INFINITY;
        
        //Stretch out aabb to encompas clipped edges.
        for(int edge = 0; edge < 3; edge++) {
            final double c0 = clipStart[0];
            final double c1 = clipStop[0];

            //Skip if edge is completely clipped.
            if(c0 < c1) {
                
                for(int axis = 0; axis < 3; axis++) {
                    double a = verts[ edge     ][axis];
                    double b = verts[(edge+1)%3][axis];

                    if(c0 < c1) {
                        double v;

                        v = (c0 == 0 ? a : (b - a) * c0);

                        if(v < outAabb[axis])
                            outAabb[axis] = v;

                        if(v > outAabb[axis+3])
                            outAabb[axis+3] = v;

                        v = (c1 == 1 ? b : (b - a) * c1);

                        if(v < outAabb[axis])
                            outAabb[axis] = v;

                        if(v > outAabb[axis+3])
                            outAabb[axis+3] = v;
                    }

                }
            }
        }
        
        return outAabb[0] <= outAabb[3] && outAabb[1] <= outAabb[4] && outAabb[2] <= outAabb[5];
    }
    

    /**
     * Constructs triangle from three vertices.
     * 
     * @param v1
     * @param v2
     * @param v3
     * @return
     */
    public static Triangle triangleFromVertices(double[] v1, double[] v2, double[] v3, boolean computeNorm) {
        double[][] norm = null;
        
        if(computeNorm) {
            norm = new double[3][];
            norm[0] = new double[3];
            Vectors.cross(v1, v2, v3, norm[0]);
            Vectors.normalize(norm[0], 1.0);
            norm[1] = norm[0].clone();
            norm[2] = norm[0].clone();
        }
        
        return new Triangle(new double[][]{v1, v2, v3}, norm, null);
    }
    
    
    /**
     * Splits a triangle into two smaller triangles using a cutting edge that
     * passes through one vertex and one edge.
     * 
     * @param t Triangle to split
     * @param i0 index of vertex & edge through which the cutting plane passes
     * @param p distance ratio between (i0+1)%3 and (i0+2)%3 where cutting pane passes.  0 &lt p &lt 1.
     * @return array of two triangles
     */
    public static Triangle[] splitTriangle1(Triangle t, int i0, double p) {
        final int i1 = (i0+1)%3;
        final int i2 = (i0+2)%3;
        
        double[][] verts0 = new double[3][];
        double[][] verts1 = new double[3][];
        double[][] norms0 = null;
        double[][] norms1 = null;
        double[][] texs0 = null;
        double[][] texs1 = null;
        double[][] colors0 = null;
        double[][] colors1 = null;
        
        {
            double[] v0 = t.vertex(i0);
            double[] v1 = t.vertex(i1);
            double[] v2 = t.vertex(i2);
            
            verts0[0] = v0;
            verts0[1] = v1;
            verts0[2] = interp(v1, v2, p);
            verts1[0] = v0;
            verts1[1] = verts0[2];
            verts1[2] = v2;
        }
        
        double[][] r = t.normalRef();
        
        if(r != null) {
            double[] n0 = r[i0];
            double[] n1 = r[i1];
            double[] n2 = r[i2];
            
            norms0 = new double[3][];
            norms1 = new double[3][];
            norms0[0] = n0.clone();
            norms0[1] = n1.clone();
            norms0[2] = interp(n1, n2, p);
            norms1[0] = n0.clone();
            norms1[1] = norms0[2].clone();
            norms1[2] = n2.clone();
        }
        
        r = t.texRef();
        
        if(r != null) {
            double[] n0 = r[i0];
            double[] n1 = r[i1];
            double[] n2 = r[i2];
            
            texs0 = new double[3][];
            texs1 = new double[3][];
            texs0[0] = n0.clone();
            texs0[1] = n1.clone();
            texs0[2] = interp(n1, n2, p);
            texs1[0] = n0.clone();
            texs1[1] = texs0[2].clone();
            texs1[2] = n2.clone();
        }
        
        r = t.colorRef();
        
        if(r != null) {
            double[] n0 = r[i0];
            double[] n1 = r[i1];
            double[] n2 = r[i2];
            
            colors0 = new double[3][];
            colors1 = new double[3][];
            colors0[0] = n0.clone();
            colors0[1] = n1.clone();
            colors0[2] = interp(n1, n2, p);
            colors1[0] = n0.clone();
            colors1[1] = colors0[2].clone();
            colors1[2] = n2.clone();
        }
        
        return new Triangle[] {
                new Triangle(verts0, norms0, texs0, colors0),
                new Triangle(verts1, norms1, texs1, colors1)};
    }


    /**
     * Splits a triangle into three smaller triangles using a cutting plane
     * that passes through two edges.
     * 
     * @param t Triangle to split
     * @param i0 index of vertex connected to the two edges to be cut.
     * @param p1 distance ratio between vertex i0 and (i0+1)%3 where cutting pane passes.  0 &lt p1 %lt 1.
     * @param p2 distance ratio between vertex i0 and (i0+2)%3 where cutting pane passes.  0 &lt p2 %lt 1.
     * @return array of three triangles.
     * 
     */
    public static Triangle[] splitTriangle2(Triangle t, int i0, double p1, double p2) {
        
        final int i1 = (i0 + 1) % 3;
        final int i2 = (i0 + 2) % 3;
        
        double[][] verts0  = new double[3][];
        double[][] verts1  = new double[3][];
        double[][] verts2  = new double[3][];
        double[][] norms0  = null;
        double[][] norms1  = null;
        double[][] norms2  = null;
        double[][] texs0   = null;
        double[][] texs1   = null;
        double[][] texs2   = null;
        double[][] colors0 = null;
        double[][] colors1 = null;
        double[][] colors2 = null;
        
        {
            double[] v0 = t.vertex(i0);
            double[] v1 = t.vertex(i1);
            double[] v2 = t.vertex(i2);
            
            verts0[0] = v0;
            verts0[1] = interp(v0, v1, p1);
            verts0[2] = interp(v0, v2, p2);
            verts1[0] = v1;
            verts1[1] = verts0[2];
            verts1[2] = verts0[1];
            verts2[0] = v2;
            verts2[1] = verts0[2];
            verts2[2] = v1;
        }
        
        double[][] r = t.normalRef();
        
        if(r != null) {
            double[] v0 = r[i0];
            double[] v1 = r[i1];
            double[] v2 = r[i2];
            
            norms0 = new double[3][];
            norms1 = new double[3][];
            norms2 = new double[3][];            
    
            norms0[0] = v0.clone();
            norms0[1] = interp(v0, v1, p1);
            norms0[2] = interp(v0, v2, p2);
            norms1[0] = v1.clone();
            norms1[1] = norms0[2].clone();
            norms1[2] = norms0[1].clone();
            norms2[0] = v2.clone();
            norms2[1] = norms0[2].clone();
            norms2[2] = v1.clone();
        }
        
        r = t.texRef();
        
        if(r != null) {
            double[] v0 = r[i0];
            double[] v1 = r[i1];
            double[] v2 = r[i2];
            
            texs0 = new double[3][];
            texs1 = new double[3][];
            texs2 = new double[3][];            
    
            texs0[0] = v0.clone();
            texs0[1] = interp(v0, v1, p1);
            texs0[2] = interp(v0, v2, p2);
            texs1[0] = v1.clone();
            texs1[1] = texs0[2].clone();
            texs1[2] = texs0[1].clone();
            texs2[0] = v2.clone();
            texs2[1] = texs0[2].clone();
            texs2[2] = v1.clone();
        }
        
        
        r = t.colorRef();
        
        if(r != null) {
            double[] v0 = r[i0];
            double[] v1 = r[i1];
            double[] v2 = r[i2];
            
            colors0 = new double[3][];
            colors1 = new double[3][];
            colors2 = new double[3][];            
    
            colors0[0] = v0.clone();
            colors0[1] = interp(v0, v1, p1);
            colors0[2] = interp(v0, v2, p2);
            colors1[0] = v1.clone();
            colors1[1] = colors0[2].clone();
            colors1[2] = colors0[1].clone();
            colors2[0] = v2.clone();
            colors2[1] = colors0[2].clone();
            colors2[2] = v1.clone();
        }
        
        return new Triangle[] {
                new Triangle(verts0, norms0, texs0, colors0),
                new Triangle(verts1, norms1, texs1, colors1),
                new Triangle(verts2, norms2, texs2, colors2)
        };
        
    }


    /**
     * Still has issues if the edge of one triangle intersects the through the center vertex then hits the opposite edge exactly.
     * 
     * @param tri Triangle to subdivide.
     * @param cut Cutting triangle that defines subdivision plane and bounds.
     * @param tolerance Distance by which <b>tri</b> must extend past <b>cut</b> to cause division.
     * @return null if no intersection, otherwise subdivided triangle.
     */
    public static Triangle[] splitTriangle(Triangle tri, Triangle cut, double tolerance) {
        
        double[] cutPoint1 = new double[3];
        double[][] tv = tri.vertexRef();
        double[][] cv = cut.vertexRef();
        int edge1 = Integer.MIN_VALUE;
        
        
        for(int i = 0; i < 3; i++) {
            int c = computeTriangleLineIntersection( cv[0], 
                                                     cv[1], 
                                                     cv[2],
                                                     tv[(i+1)%3],
                                                     tv[(i+2)%3],
                                                     tolerance,
                                                     cutPoint1);
            
            if(c != 0) {
                edge1 = i;
                break;
            }
            
            c = computeTriangleLineIntersection( tv[0], 
                                                 tv[1], 
                                                 tv[2],
                                                 cv[(i+1)%3],
                                                 cv[(i+2)%3],
                                                 tolerance,
                                                 cutPoint1);
            
            if(c != 0) {
                edge1 = -1;
                break;
            }
            
        }
        
        if(edge1 == Integer.MIN_VALUE)
            return null;
        
        //Find edges and cut points.
        int edge2 = -1;
        
        if(edge1 == -1) {
            for(int i = 0; i < 3; i++) {
                int c = computePlaneLineIntersection( cv[0], 
                                                      cv[1], 
                                                      cv[2], 
                                                      tv[(i+1)%3], 
                                                      tv[(i+2)%3], 
                                                      tolerance, 
                                                      cutPoint1);
                if(c != 0) {
                    edge1 = i;
                    break;
                }
            }
            
            if(edge1 == -1)
                return null;
        }
        
        double[] cutPoint2 = new double[3];
        
        for(int i = edge1 + 1; i < 3; i++) {
            int c = computePlaneLineIntersection( cv[0],
                                                  cv[1],
                                                  cv[2],
                                                  tv[(i+1)%3],
                                                  tv[(i+2)%3],
                                                  tolerance,
                                                  cutPoint2);

            if(c != 0) {
                edge2 = i;
                break;
            }
        }
        
        
        if(edge2 < 0) {
            //Only found one intersecting edge.
            final int vi0 = edge1;
            final int vi1 = (vi0+1)%3;
            final int vi2 = (vi0+2)%3;
            final double r = distanceRatio(tv[vi1], tv[vi2], cutPoint1);
            return splitTriangle1(tri, vi0, r);
            
        }else{
            //Find point that connects both edges being cut.
            final int vi0 = 3 - edge1 - edge2;
            final int vi1 = (vi0+1)%3;
            final int vi2 = (vi0+2)%3;
            
            //Check if we need to swap cutpoints.
            if((vi0+2) % 3 == edge1) {
                double[] temp = cutPoint2;
                cutPoint2 = cutPoint1;
                cutPoint1 = temp;
            }
            
            double r1 = distanceRatio(tv[vi0], tv[vi1], cutPoint2);
            double r2 = distanceRatio(tv[vi0], tv[vi2], cutPoint1);

            return splitTriangle2(tri, vi0, r1, r2);
        }
    }
    

    public static List<Triangle> splitLongEdges(List<Triangle> tris, double maxLength, int depth) {
        List<Triangle> ret = new ArrayList<Triangle>(tris.size());
        double[] len = new double[3];
        
        for(Triangle t: tris) {
    
            double maxEdge = Double.NEGATIVE_INFINITY;
            int e0 = 0, e1 = 0, e2 = 0;
            
            for(int i = 0; i < 3; i++) {
                len[i] = Vectors.dist(t.vertex(i), t.vertex((i + 1) % 3));
                if(len[i] > maxEdge) {
                    maxEdge = len[i];
                    e0 = i;
                }
            }
            
            if(maxEdge < maxLength) {
                ret.add(t);
                continue;
            }    
                        
            
            if(len[(e0+1)%3] > len[(e0+2)%3]) {
                e1 = (e0+1) % 3;
                e2 = (e0+2) % 3;
            }else{
                e1 = (e0+2) % 3;
                e2 = (e0+1) % 3;
            }
            
            //Determine wether to split down center of longest edge to opposite vertex,
            //or to split down center of longest edge to center of other long edge.
            
            //Basically, I'm just splitting the edges into two groups, and trying 
            //to keep the lengths in each group similar.  
    
            if(e1 - e0 > e2 - e1) {
                Triangle[] chips = splitTriangle1(t, e0, 0.5);
                ret.addAll(splitLongEdges(Arrays.asList(chips), maxLength, depth + 1));
                
            }else{
                Triangle[] chips = splitTriangle2(t, e2, 0.5, 0.5);
                ret.addAll(splitLongEdges(Arrays.asList(chips), maxLength, depth + 1));
            }
        }
        
        return ret;
    }
    

    
    public static Triangle scale(Triangle tri, double s) {
        return scale(tri, s, s, s);
    }


    public static Triangle scale(Triangle tri, double sx, double sy, double sz) {
        Triangle copy = tri.safeCopy();
        
        double[][] v = copy.vertexRef();
        v[0][0] *= sx; v[0][1] *= sy; v[0][2] *= sz;
        v[1][0] *= sx; v[1][1] *= sy; v[1][2] *= sz;
        v[2][0] *= sx; v[2][1] *= sy; v[2][2] *= sz;
        
        return copy;
    }


    static double distanceRatio(double[] x0, double[] x1, double[] c) {
        double d0 = Math.sqrt((x0[0]-c[0])*(x0[0]-c[0]) + (x0[1]-c[1])*(x0[1]-c[1]) + (x0[2]-c[2])*(x0[2]-c[2]));
        double d1 = Math.sqrt((x1[0]-c[0])*(x1[0]-c[0]) + (x1[1]-c[1])*(x1[1]-c[1]) + (x1[2]-c[2])*(x1[2]-c[2]));
        return d0 / (d0 + d1); 
    }
    

    /**
     * Perform linear interpolation between two vectors.
     * 
     * @param a Vector a
     * @param b Vertor b
     * @param p Distance ratio 
     * @return a * (1 - p) + b * p
     */
    static double[] interp(double[] a, double[] b, double p) {
        double[] ret = new double[a.length];
        
        for(int i = 0; i < a.length; i++) {
            ret[i] = a[i] * (1.0 - p) + b[i] * p;
        }
        
        return ret;
    }

}
