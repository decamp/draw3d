//package bits.draw3d.model;
//
//import bits.math3d.geom.Aabb;
//
//import java.util.*;
//
//
///**
// * @author Philip DeCamp
// */
//public class MeshModel {
//
//    public List<Group> mGroups;
//
//
//    public MeshModel() {
//        mGroups = new ArrayList<Group>();
//    }
//
//
//    public MeshModel( List<Group> groups ) {
//        mGroups = (groups == null ? new ArrayList<Group>( 0 ) : groups);
//    }
//
//
//
//    public void add( Group group ) {
//        mGroups.add( group );
//    }
//
//
//    public void add( MeshModel model ) {
//        if( this != model ) {
//            mGroups.addAll( model.mGroups );
//        }
//    }
//
//
//    public void collectTris( Collection<? super Triangle> out ) {
//        for( Group g : mGroups ) {
//            out.addAll( g.mTris );
//        }
//    }
//
//
//    public void collectVerts( Collection<double[]> out ) {
//        for( Group g: mGroups ) {
//            g.collectVerts( out );
//        }
//    }
//
//
//    public double[] computeBounds( double[] box3 ) {
//        if( box3 == null ) {
//            box3 = new double[6];
//        }
//
//        double x0 = Double.POSITIVE_INFINITY;
//        double y0 = Double.POSITIVE_INFINITY;
//        double z0 = Double.POSITIVE_INFINITY;
//        double x1 = Double.NEGATIVE_INFINITY;
//        double y1 = Double.NEGATIVE_INFINITY;
//        double z1 = Double.NEGATIVE_INFINITY;
//        boolean first = true;
//
//        for( Group g : mGroups ) {
//            for( Triangle t : g.mTris ) {
//                for( double[] vv : t.mVerts ) {
//                    if( first ) {
//                        first = false;
//                        x0 = x1 = vv[0];
//                        y0 = y1 = vv[1];
//                        z0 = z1 = vv[2];
//
//                    } else {
//                        if( vv[0] < x0 ) {
//                            x0 = vv[0];
//                        } else if( vv[0] > x1 ) {
//                            x1 = vv[0];
//                        }
//
//                        if( vv[1] < y0 ) {
//                            y0 = vv[1];
//                        } else if( vv[1] > y1 ) {
//                            y1 = vv[1];
//                        }
//
//                        if( vv[2] < z0 ) {
//                            z0 = vv[2];
//                        } else if( vv[2] > z1 ) {
//                            z1 = vv[2];
//                        }
//                    }
//                }
//            }
//        }
//
//        box3[0] = x0;
//        box3[1] = y0;
//        box3[2] = z0;
//        box3[3] = x1;
//        box3[4] = y1;
//        box3[5] = z1;
//
//        return box3;
//    }
//
//
//    public Aabb computeAabb() {
//        double[] box = computeBounds( null );
//        return Aabb.fromEdges( box[0], box[1], box[2], box[3], box[4], box[5] );
//    }
//
//
//
//    /**
//     * @return defensive copy of list of groups.
//     */
//    @Deprecated public List<Group> getGroups() {
//        return new ArrayList<Group>( mGroups );
//    }
//
//    /**
//     * @return the list of groups, without making a defesive copy
//     */
//    @Deprecated public List<Group> getGroupsRef() {
//        return mGroups;
//    }
//
//    /**
//     * Sets list of groups, making a defensive copy.
//     */
//    @Deprecated public void setGroups( Collection<? extends Group> groups ) {
//        mGroups = new ArrayList<Group>( mGroups );
//    }
//
//    /**
//     * Sets list of groups by reference, without making defensive copy.
//     *
//     * @param groups
//     */
//    @Deprecated public void setGroupRef( List<Group> groups ) {
//        mGroups = groups;
//    }
//
//
//    @Deprecated public List<Triangle> getAllTriangles( List<Triangle> out ) {
//        if( out == null ) {
//            out = new ArrayList<Triangle>();
//        }
//
//        for( Group g : mGroups ) {
//            out.addAll( g.getTriangles() );
//        }
//
//        return out;
//    }
//
//}
