//package bits.draw3d.model;
//
//import bits.draw3d.tex.Material;
//import bits.draw3d.pick.*;
//import bits.math3d.*;
//import bits.math3d.geom.Aabb;
//import bits.math3d.geom.Volume;
//
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.util.*;
//import java.util.List;
//
//
///**
// * @author Philip DeCamp
// */
//public final class Meshes {
//
//
//
//    public static MeshModel copy( MeshModel model, boolean newMaterials, boolean newTexs ) {
//        MeshModel ret = new MeshModel();
//        for( Group g: model.mGroups ) {
//            ret.mGroups.add( copy( g, newMaterials, newTexs ) );
//        }
//        return ret;
//    }
//
//
//    public static Group copy( Group group, boolean newMaterial, boolean newTex ) {
//        BufferedImage im = group.mTex;
//        if( im != null && newTex ) {
//            BufferedImage c = new BufferedImage( im.getWidth(), im.getHeight(), im.getType() );
//            Graphics2D g = (Graphics2D)c.getGraphics();
//            g.setComposite( AlphaComposite.Src );
//            g.drawImage( im, 0, 0, null );
//            im = c;
//        }
//
//        Material mat = group.mMaterials;
//        if( newMaterial ) {
//            mat = new Material( mat );
//        }
//
//        return new Group( group.mName, im, mat, copy( group.mTris ) );
//    }
//
//
//    public static List<Triangle> copy( Collection<? extends Triangle> tris ) {
//        List<Triangle> ret = new ArrayList<Triangle>( tris.size() );
//        Map<double[],double[]> map = new HashMap<double[],double[]>();
//
//        for( Triangle t: tris ) {
//            Triangle dst = new Triangle();
//            dst.mVerts = copy( t.mVerts, map );
//            dst.mNorms = copy( t.mNorms, map );
//            dst.mTexs = copy( t.mTexs, map );
//            ret.add( dst );
//        }
//
//        return ret;
//    }
//
//
//    public static void transform( Collection<? extends Triangle> coll, double[] mat, boolean transformNorms ) {
//        Set<double[]> points = new HashSet<double[]>();
//        double[] work = new double[3];
//        for( Triangle t: coll ) {
//            double[][] v = t.mVerts;
//            double[][] n = t.mNorms;
//            for( int i = 0; i < 3; i++ ) {
//                if( points.add( v[i] ) ) {
//                    transform( mat, v[i], work );
//                }
//                if( transformNorms && n != null && points.add( n[i] ) ) {
//                    transform( mat, n[i], work );
//                }
//            }
//        }
//    }
//
//
//    public static void transform( MeshModel model, double[] mat, boolean transformNorms ) {
//        Set<double[]> points = new HashSet<double[]>();
//        double[] work = new double[3];
//        for( Group group: model.mGroups ) {
//            for( Triangle t: group.mTris ) {
//                double[][] v = t.mVerts;
//                double[][] n = t.mNorms;
//                for( int i = 0; i < 3; i++ ) {
//                    if( points.add( v[i] ) ) {
//                        transform( mat, v[i], work );
//                    }
//                    if( transformNorms && n != null && points.add( n[i] ) ) {
//                        transform( mat, n[i], work );
//                    }
//                }
//            }
//        }
//    }
//
//
//    public static void reverseOrientation( MeshModel model ) {
//        for( Group g: model.mGroups ) {
//            for( Triangle t: g.mTris ) {
//                t.reverseOrientation();
//            }
//        }
//    }
//
//
//    public static void generateNorms( MeshModel model ) {
//        for( Group g: model.mGroups ) {
//            for( Triangle t: g.mTris ) {
//                if( t.mNorms == null ) {
//                    t.mNorms = t.createNormVecs();
//                }
//            }
//        }
//    }
//
//
//    public static void normalizeNorms( MeshModel model ) {
//        for( Group g: model.mGroups ) {
//            normalizeNorms( g.mTris );
//        }
//    }
//
//
//    public static void normalizeNorms( Collection<Triangle> tris ) {
//        for( Triangle t: tris ) {
//            if( t.mNorms != null ) {
//                Vec.normalize( t.mNorms[0] );
//                Vec.normalize( t.mNorms[1] );
//                Vec.normalize( t.mNorms[2] );
//            }
//        }
//    }
//
//
//    public static void setColor( MeshModel model, double r, double g, double b, double a ) {
//        for( Group gr : model.mGroups ) {
//            setColor( gr.mTris, r, g, b, a );
//        }
//    }
//
//
//    public static void setColor( Collection<? extends Triangle> tris, float r, float g, float b, float a ) {
//        for( Triangle t : tris ) {
//            t.mColors = new Vec4[]{ new Vec4( r, g, b, a ),
//                                    new Vec4( r, g, b, a ),
//                                    new Vec4( r, g, b, a ),
//                                    new Vec4( r, g, b, a ) };
//        }
//    }
//
//
//    public static void assignTexCoords2( List<Triangle> tris, Mat4 mat ) {
//        Vec3 work = new Vec3();
//        for( Triangle t: tris ) {
//            t.mTexs = new Vec2[3];
//            for( int i = 0; i < 3; i++ ) {
//                Mat.mult( mat, t.mVerts[i], work );
//                Vec2 o = t.mTexs[i];
//                o.x = work.x;
//                o.y = work.y;
//            }
//        }
//    }
//
//
//    public static KdPointTree<LabelVert> labelVerts( MeshModel model ) {
//        return labelVerts( model, Tol.SQRT_ABS_ERR );
//    }
//
//
//    public static KdPointTree<LabelVert> labelVerts( MeshModel model, double equalDistance ) {
//        KdPointTree<double[]> tree       = new KdPointTree<double[]>( DimComparator.DOUBLE_ARRAY_INSTANCE );
//        PointPickResult<double[]> result = tree.newPointPickResult();
//
//        for( Group g : model.mGroups ) {
//            for( Triangle t : g.mTris ) {
//                for( int i = 0; i < 3; i++ ) {
//                    if( !tree.pick( t.mVerts[i], result ) || result.pickedDistance() > equalDistance ) {
//                        tree.add( t.mVerts[i] );
//                    }
//                }
//            }
//        }
//
//        KdPointTree<LabelVert> ret = new KdPointTree<LabelVert>( LabelVert.ACCESSOR );
//        int idx = 0;
//
//        for( double[] v : tree ) {
//            ret.add( new LabelVert( idx++, v ) );
//        }
//
//        return ret;
//    }
//
//
//    public static Map<Triangle, Integer> labelTris( MeshModel model ) {
//        Map<Triangle, Integer> map = new HashMap<Triangle, Integer>();
//        int idx = 0;
//        for( Group g : model.mGroups ) {
//            for( Triangle t : g.mTris ) {
//                if( !map.containsKey( t ) ) {
//                    map.write( t, idx++ );
//                }
//            }
//        }
//        return map;
//    }
//
//
//    public static Map<Triangle, Integer> labelTriangleGroups( MeshModel model ) {
//        Map<Triangle, Integer> map = new HashMap<Triangle, Integer>();
//        List<Group> groups = model.mGroups;
//
//        for( int i = 0; i < groups.size(); i++ ) {
//            Integer n = i;
//            for( Triangle t : groups.get( i ).mTris ) {
//                map.write( t, n );
//            }
//        }
//
//        return map;
//    }
//
//
//
//    public static Map<ModelMaterial, Integer> labelMaterials( MeshModel model ) {
//        Map<ModelMaterial, Integer> map = new HashMap<ModelMaterial, Integer>();
//        List<Group> groups = model.mGroups;
//        int idx = 0;
//
//        for( Group g : groups ) {
//            BufferedImage im = g.mTex;
//            Material mat = g.mMaterials;
//            ModelMaterial tm = new ModelMaterial( im, mat );
//
//            if( (im != null || mat != null) && !map.containsKey( tm ) ) {
//                map.write( tm, idx++ );
//            }
//        }
//
//        return map;
//    }
//
//
//
//    public static void correctPolygonOrientation( MeshModel model ) {
//        Aabb bounds     = model.computeAabb();
//        double centerX  = bounds.centerX();
//        double centerY  = bounds.centerY();
//        double centerZ  = bounds.centerZ();
//        double[] center = { centerX, centerY, centerZ };
//
//        for( Group g : model.mGroups ) {
//            Set<Triangle> set = new HashSet<Triangle>();
//            List<Triangle> t = g.mTris;
//
//            if( t.isEmpty() ) {
//                continue;
//            }
//            set.addAll( t );
//
//            while( !set.isEmpty() ) {
//                Set<Triangle> doneSet = new HashSet<Triangle>();
//                Set<Triangle> correctSet = new HashSet<Triangle>();
//                int awayNum = 1;
//
//                Triangle tt = set.iterator().next();
//                set.remove( tt );
//
//                if( Triangles.computePerpDistance( tt.mVerts[0], tt.mVerts[1], tt.mVerts[2], center ) > 0 ) {
//                    tt.reverseOrientation();
//                }
//
//                correctSet.add( tt );
//                awayNum++;
//
//                while( correctSet.size() > 0 ) {
//                    tt = correctSet.iterator().next();
//                    correctSet.remove( tt );
//                    doneSet.add( tt );
//
//                    for( Triangle ttt : set ) {
//
//COMPARE_TRIANGLES:
//                        for( int v1 = 0; v1 < 3; v1++ ) {
//                            for( int v2 = 0; v2 < 3; v2++ ) {
//
//                                if( tt.mVerts[ v1 ] != ttt.mVerts[ v2 ] ) {
//                                    continue;
//                                }
//
//                                if( tt.mVerts[ (v1 + 1) % 3 ] == ttt.mVerts[ (v2 + 1) % 3 ] ) {
//                                    ttt.reverseOrientation();
//                                    correctSet.add( ttt );
//                                    set.remove( ttt );
//
//                                    if( Triangles.computePerpDistance( ttt.mVerts[ 0 ],
//                                                                       ttt.mVerts[ 1 ],
//                                                                       ttt.mVerts[ 2 ],
//                                                                       center ) < 0 )
//                                    {
//                                        awayNum++;
//                                    }
//
//                                    break COMPARE_TRIANGLES;
//                                }
//
//                                if( tt.mVerts[ (v1 + 1) % 3 ] == ttt.mVerts[ ( v2 + 2 ) % 3 ] ) {
//                                    correctSet.add( ttt );
//                                    set.remove( ttt );
//
//                                    if( Triangles.computePerpDistance( ttt.mVerts[ 0 ],
//                                                                       ttt.mVerts[ 1 ],
//                                                                       ttt.mVerts[ 2 ],
//                                                                       center ) < 0 )
//                                    {
//                                        awayNum++;
//                                    }
//
//                                    break COMPARE_TRIANGLES;
//                                }
//                            }
//                        }
//                    }
//                }
//
//                if( awayNum < doneSet.size() / 2 ) {
//                    for( Triangle ttt : doneSet ) {
//                        ttt.reverseOrientation();
//                    }
//                }
//            }
//        }
//    }
//
//
//    public static void clip( MeshModel model, Aabb clip ) {
//        for( Group g : model.mGroups) {
//            g.mTris = clip( g.mTris, clip );
//        }
//    }
//
//
//    public static List<Triangle> clip( List<Triangle> tris, Aabb clipCube ) {
//        List<Triangle> ret = new ArrayList<Triangle>();
//
//        int[] ii = new int[3];
//        int[] oi = new int[3];
//
//        double[][] bounds = new double[][]{ { clipCube.minX(), clipCube.maxX() },
//                                            { clipCube.minY(), clipCube.maxY() },
//                                            { clipCube.minZ(), clipCube.maxZ() } };
//
//        for( Triangle t : tris ) {
//            if( t == null ) {
//                continue;
//            }
//
//            int inCount = 0;
//            int outCount = 0;
//
//            double maxClip = Double.NEGATIVE_INFINITY;
//            int dim = 0;
//            double plane = 0.0;
//            int side = -1;
//
//            //Find clipping pane.
//
//            for( int vi = 0; vi < 3; vi++ ) {
//                double[] x = t.mVerts[ vi ];
//                boolean outed = false;
//
//                for( int d = 0; d < 3; d++ ) {
//                    if( x[d] < bounds[d][0] ) {
//                        double dist = bounds[d][0] - x[d];
//
//                        if( dist > maxClip ) {
//                            maxClip = dist;
//                            dim = d;
//                            plane = bounds[d][0];
//                            side = -1;
//                        }
//
//                        outed = true;
//
//                    } else if( x[d] > bounds[d][1] ) {
//                        double dist = x[d] - bounds[d][1];
//
//                        if( dist > maxClip ) {
//                            maxClip = dist;
//                            dim = d;
//                            plane = bounds[d][1];
//                            side = 1;
//                        }
//
//                        outed = true;
//                    }
//                }
//
//                if( outed ) {
//                    oi[outCount++] = vi;
//                } else {
//                    ii[inCount++] = vi;
//                }
//            }
//
//            switch( inCount ) {
//            case 1: {
//                //One point in, two points out.
//                double p0 = Math.abs( t.mVerts[ ii[0] ][dim] - plane );
//                double p1 = Math.abs( t.mVerts[ (ii[0] + 1) % 3 ][dim] - plane );
//                double p2 = Math.abs( t.mVerts[ (ii[0] + 2) % 3 ][dim] - plane );
//
//                Triangle[] chips = Triangles.splitTriangle2( t, ii[0], p0 / (p0 + p1), p0 / (p0 + p2) );
//
//                //In case of rounding errors, make sure the clip is clean.
//                if( side > 0 ) {
//                    double[] x;
//                    x = chips[0].mVerts[ 1 ];
//                    x[dim] = Math.min( x[dim], plane );
//                    x = chips[0].mVerts[ 2 ];
//                    x[dim] = Math.min( x[dim], plane );
//
//                } else {
//                    double[] x;
//                    x = chips[0].mVerts[ 1 ];
//                    x[dim] = Math.max( x[dim], plane );
//                    x = chips[0].mVerts[ 2 ];
//                    x[dim] = Math.max( x[dim], plane );
//                }
//
//                //The other two chips are culled.
//                chips[1] = null;
//                chips[2] = null;
//
//                //Resegment, in case the triangle passes through more than one bounding plane.
//                ret.addAll( clip( Arrays.asList( chips ), clipCube ) );
//                break;
//            }
//
//            case 2: {
//                //Two points in, one point out.
//                double p0 = Math.abs( t.mVerts[ oi[0] ][dim] - plane );
//                double p1 = Math.abs( t.mVerts[ (oi[0] + 1) % 3 ][dim] - plane );
//                double p2 = Math.abs( t.mVerts[ (oi[0] + 2) % 3 ][dim] - plane );
//
//                Triangle[] chips = Triangles.splitTriangle2( t, oi[0], p0 / (p0 + p1), p0 / (p0 + p2) );
//
//                //In case of rounding errors, make sure the clip is clean.
//
//                if( side > 0 ) {
//                    double[] x;
//                    x = chips[1].mVerts[ 1 ];
//                    x[dim] = Math.min( x[dim], plane );
//                    x = chips[1].mVerts[ 2 ];
//                    x[dim] = Math.min( x[dim], plane );
//                    x = chips[2].mVerts[ 1 ];
//                    x[dim] = Math.min( x[dim], plane );
//                } else {
//                    double[] x;
//                    x = chips[1].mVerts[ 1 ];
//                    x[dim] = Math.max( x[dim], plane );
//                    x = chips[1].mVerts[ 2 ];
//                    x[dim] = Math.max( x[dim], plane );
//                    x = chips[2].mVerts[ 1 ];
//                    x[dim] = Math.max( x[dim], plane );
//                }
//
//                chips[0] = null;
//                ret.addAll( clip( Arrays.asList( chips ), clipCube ) );
//                break;
//            }
//
//            case 3:
//                //All points in.
//                ret.add( t );
//                break;
//            }
//        }
//
//        return ret;
//    }
//
//
//    public static void partition( List<Triangle> tris,
//                                  Volume manifold,
//                                  List<Triangle> inside,
//                                  boolean computeNorms,
//                                  List<Triangle> outside )
//    {
//        List<Triangle> manTris   = Triangulations.triangulate( manifold, false );
//        List<Triangle> splitTris = split( tris, manTris, null );
//        double[] cent = new double[3];
//
//        for( Triangle t : splitTris ) {
//            t.center( cent );
//            if( manifold.contains( cent[0], cent[1], cent[2] ) ) {
//                inside.add( t );
//            } else {
//                outside.add( t );
//            }
//        }
//    }
//
//
//    public static List<Triangle> split( List<Triangle> tris, List<Triangle> cuts, List<Triangle> out ) {
//        if( out == null ) {
//            out = new ArrayList<Triangle>();
//        }
//
//        if( cuts.isEmpty() ) {
//            out.addAll( tris );
//            return out;
//        }
//
//        List<Triangle> listA = new ArrayList<Triangle>();
//        List<Triangle> listB = new ArrayList<Triangle>();
//
//        for( int i = 0; i < cuts.size(); i++ ) {
//            List<Triangle> a;
//            List<Triangle> b;
//
//            if( i % 2 == 0 ) {
//                a = listA;
//                b = listB;
//            } else {
//                a = listB;
//                b = listA;
//            }
//
//            b.clear();
//
//            if( i == 0 ) {
//                a = tris;
//            }
//
//            if( i == cuts.size() - 1 ) {
//                b = out;
//            }
//
//            Triangle cut = cuts.get( i );
//
//            for( Triangle t : a ) {
//                Triangle[] frac = Triangles.splitTriangle( t, cut, 0.00001 );
//
//                if( frac == null ) {
//                    b.add( t );
//                } else {
//                    Collections.addAll( b, frac );
//                }
//            }
//        }
//
//        return out;
//    }
//
//
//    public static void setNaNs( MeshModel m, double v ) {
//        for( Group g: m.mGroups ) {
//            setNaNs( g.mTris, v );
//        }
//    }
//
//
//    public static void setNaNs( List<Triangle> tris, double v ) {
//        for( Triangle t: tris ) {
//            setNaNs( t, v );
//        }
//    }
//
//
//    public static void setNaNs( Triangle t, double v ) {
//        setNaNs( t.mVerts,  v );
//        setNaNs( t.mColors, v );
//        setNaNs( t.mTexs,   v );
//        setNaNs( t.mNorms,  v );
//    }
//
//
//    public static void setNaNs( double[][] arr, double v ) {
//        if( arr == null ) {
//            return;
//        }
//        for( double[] a: arr ) {
//            setNaNs( a, v );
//        }
//    }
//
//
//    public static void setNaNs( double[] arr, double v ) {
//        if( arr == null ) {
//            return;
//        }
//        for( int i = 0; i < arr.length; i++ ) {
//            if( Double.isNaN( arr[i] ) ) {
//                arr[i] = v;
//            }
//        }
//    }
//
//
//    private static void transform( double[] mat, double[] vec, double[] work ) {
//        Matrices.multMatVec( mat, vec, work );
//        vec[0] = work[0];
//        vec[1] = work[1];
//        vec[2] = work[2];
//    }
//
//
//    private static double[][] copy( double[][] arr, Map<double[],double[]> map ) {
//        if( arr == null ) {
//            return null;
//        }
//
//        double[][] ret = new double[arr.length][];
//        for( int i = 0; i < arr.length; i++ ) {
//            double[] v = map.get( arr[i] );
//            if( v == null ) {
//                v = arr[i].clone();
//                map.write( arr[i], v );
//            }
//
//            ret[i] = v;
//        }
//
//        return ret;
//    }
//
//
//
//    private Meshes() {}
//
//}
//
