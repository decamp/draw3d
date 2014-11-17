//package bits.draw3d.geom;
//
//import bits.math3d.*;
//import bits.math3d.geom.PolyLine;
//
//
///**
// * @author Philip DeCamp
// */
//public class TriClipper {
//
//    /**
//     * Provided with an array of COPLANAR vertices and an AABB, this method
//     * will provide the geometric intersection.
//     *
//     * @param vertLoop  Array of length-3 vertices.
//     * @param vertCount Number of vertices
//     * @param clipAabb  Clipping box
//     * @param out       pre-allocated loop object to hold clipped results.
//     * @return true iff intersection is found with non zero surface area.
//     * (This isn't precisely true because I haven't formalized all the boundary conditions)
//     */
//    public static boolean clipPlanar( Vec3[] vertLoop, int vertOff, int vertCount, Box3 clipAabb, PolyLine out ) {
//        out.ensureCapacity( vertCount + 6 );
//        Vec3[] verts = out.mVerts;
//
//        // Copy data into output.
//        System.arraycopy( vertLoop, vertOff, verts, 0, vertCount );
//
//        // Iterate through clip planes.
//        // Iterate through clip planes.
//        for( int axis = 0; axis < 3; axis++ ) {
//            // Iterate through vertices.
//            float min = Box.min( clipAabb, axis );
//            float max = Box.max( clipAabb, axis );
//            int m0 = -1;
//            int m1 = -1;
//            int n0 = -1;
//            int n1 = -1;
//
//            // Find plane crossings.
//            for( int i = 0; i < vertCount; i++ ) {
//                float a = Vec.el( verts[i], axis );
//                float b = Vec.el( verts[(i + 1) % vertCount], axis );
//
//                if( (a < min) != (b < min) ) {
//                    if( m0 == -1 ) {
//                        m0 = i;
//                    } else {
//                        m1 = i;
//                    }
//                }
//
//                if( (a > max) != (b > max) ) {
//                    if( n0 == -1 ) {
//                        n0 = i;
//                    } else {
//                        n1 = i;
//                    }
//                }
//            }
//
//            if( m1 == -1 ) {
//                // No intersection with min plane.
//                if( Vec.el( verts[0], axis ) < min ) {
//                    // No intersection with volume.
//                    out.mSize = 0;
//                    return false;
//                }
//            } else {
//                // Split loop at min plane.
//                if( Vec.el( verts[0], axis ) < min ) {
//                    vertCount = retainLoopSection( verts, vertCount, m0, m1, axis, min );
//                } else {
//                    vertCount = removeLoopSection( verts, vertCount, m0, m1, axis, min );
//                }
//
//                // Recompute max crossings, if necessary.
//                if( n1 != -1 ) {
//                    n0 = -1;
//                    n1 = -1;
//
//                    for( int i = 0; i < vertCount; i++ ) {
//                        float a = Vec.el( verts[i], axis );
//                        float b = Vec.el( verts[(i + 1) % vertCount], axis );
//                        if( (a > max) != (b > max) ) {
//                            if( n0 == -1 ) {
//                                n0 = i;
//                            } else {
//                                n1 = i;
//                            }
//                        }
//                    }
//                }
//            }
//
//            if( n1 == -1 ) {
//                // No intersection with max plane.
//                if( Vec.el( verts[0], axis ) > max ) {
//                    // No intersection with volume.
//                    out.mSize = 0;
//                    return false;
//                }
//            } else {
//                // Split loop at max plane.
//                if( Vec.el( verts[0], axis ) > max ) {
//                    vertCount = retainLoopSection( verts, vertCount, n0, n1, axis, max );
//                } else {
//                    vertCount = removeLoopSection( verts, vertCount, n0, n1, axis, max );
//                }
//            }
//        }
//
//        out.mSize = vertCount;
//        return true;
//    }
//
//
//    private static int removeLoopSection( Vec3[] v, int count, int start, int stop, int axis, float pos ) {
//        // Make sure there's enough room between start and stop vertices if adjacent.
//        if( start + 1 == stop ) {
//            Vec3 temp = v[v.length - 1];
//            System.arraycopy( v, stop - 1, v, stop, v.length - stop );
//
//            v[stop++] = temp;
//            Vec.write( v[stop], temp );
//            count++;
//        }
//
//        // Cache start values.
//        Vec3 va = v[start];
//        Vec3 vb = v[start + 1];
//        float a = Vec.el( va, axis );
//        float b = Vec.el( vb, axis );
//
//        // Check if va is precisely on edge.
//        if( a != pos ) {
//            float r = (pos - a) / (b - a);
//            if( va.x != vb.x ) {
//                vb.x = r * vb.x + (1 - r) * va.x;
//            }
//            if( va.y != vb.y ) {
//                vb.y = r * vb.y + (1 - r) * va.y;
//            }
//            if( va.z != vb.z ) {
//                vb.z = r * vb.z + (1 - r) * va.z;
//            }
//            Vec.el( vb, axis, pos ); // To avoid rounding errors.
//            start++;
//        }
//
//        // Cache stop values.
//        va = v[stop];
//        vb = v[(stop + 1) % count];
//        a = Vec.el( va, axis );
//        b = Vec.el( vb, axis );
//
//        // Check if vb is precisely on edge.
//        if( b != pos ) {
//            float r = (pos - a) / (b - a);
//            if( va.x != vb.x ) {
//                va.x = r * vb.x + (1 - r) * va.x;
//            }
//            if( va.y != vb.y ) {
//                va.y = r * vb.y + (1 - r) * va.y;
//            }
//            if( va.z != vb.z ) {
//                va.z = r * vb.z + (1 - r) * va.z;
//            }
//            Vec.el( va, axis, pos ); // To avoid rounding errors.
//        } else {
//            stop++;
//        }
//
//        // Check if there's a gap to fill.
//        int gap = stop - start - 1;
//
//        if( gap == 0 )
//            return count;
//
//        count -= gap;
//
//        for( int i = start + 1; i < count; i++ ) {
//            double[] temp = v[i];
//            v[i] = v[i + gap];
//            v[i + gap] = temp;
//        }
//
//        return count;
//    }
//
//
//
//}
