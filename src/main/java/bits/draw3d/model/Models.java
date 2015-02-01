/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model;

import bits.math3d.*;
import java.util.*;


/**
 * @author Philip DeCamp
 */
public class Models {


    public static Iterator<DrawVert> vertIterator( Collection<? extends DrawTri> tris ) {
        return new TriVertIter( tris.iterator() );
    }


    public static Iterator<DrawVert> vertIterator( TriModel model ) {
        return new ModelVertIter( model.mGroups.iterator() );

    }


    public static Iterator<DrawTri> triIterator( TriModel model ) {
        return new ModelTriIter( model.mGroups.iterator() );
    }


    public static Iterator<DrawMaterial> materialIterator( TriModel model ) {
        return new ModelMaterialIter( model.mGroups );
    }


    public static DrawVert deepCopy( DrawVert copy ) {
        if( copy == null ) {
            return null;
        }
        return new DrawVert ( copy.mPos   == null ? null : new Vec3( copy.mPos ),
                              copy.mTex   == null ? null : copy.mTex.clone(),
                              copy.mNorm  == null ? null : new Vec3( copy.mNorm ),
                              copy.mColor == null ? null : new Vec4( copy.mColor ) );
    }


    public static DrawTri deepCopy( DrawTri copy ) {
        if( copy == null ) {
            return null;
        }
        return new DrawTri( deepCopy( copy.mVerts[0] ),
                            deepCopy( copy.mVerts[1] ),
                            deepCopy( copy.mVerts[2] ) );
    }


    public static void reverseOrientation( DrawTri t ) {
        DrawVert b = t.mVerts[1];
        t.mVerts[1] = t.mVerts[2];
        t.mVerts[2] = b;
    }


    public static void generateNorms( DrawTri t ) {
        DrawVert[] v = t.mVerts;
        if( v[0].mNorm == null ) v[0].mNorm = new Vec3();
        Vec.cross( v[0].mPos, v[1].mPos, v[2].mPos, v[0].mNorm );
        if( v[1].mNorm == null ) {
            v[1].mNorm = new Vec3( v[0].mNorm );
        } else {
            Vec.put( v[0].mNorm, v[1].mNorm );
        }
        if( v[2].mNorm == null ) {
            v[2].mNorm = new Vec3( v[0].mNorm );
        } else {
            Vec.put( v[0].mNorm, v[2].mNorm );
        }
    }


    public static void getTris( TriModel model, Collection<? super DrawTri> out ) {
        for( TriGroup g: model.mGroups ) {
            out.addAll( g.mTris );
        }
    }


    public static void getVerts( Collection<DrawTri> tris, Collection<? super DrawVert> out ) {
        for( DrawTri t: tris ) {
            out.add( t.mVerts[0] );
            out.add( t.mVerts[1] );
            out.add( t.mVerts[2] );
        }
    }


    public static void getVerts( TriModel model, Collection<? super DrawVert> out ) {
        for( TriGroup g: model.mGroups ) {
            getVerts( g.mTris, out );
        }
    }


    public static void getMaterials( TriModel model, Collection<? super DrawMaterial> out ) {
        for( TriGroup g: model.mGroups ) {
            out.add( g.mMaterial );
        }
    }


    public static List<DrawVert> listUniqueVerts( TriModel model ) {
        Set<DrawVert> set = new HashSet<DrawVert>();
        getVerts( model, set );
        return new ArrayList<DrawVert>( set );
    }


    public static List<DrawTri> listUniqueTris( TriModel model ) {
        Set<DrawTri> set = new HashSet<DrawTri>();
        getTris( model, set );
        return new ArrayList<DrawTri>( set );
    }


    public static List<DrawMaterial> listUniqueMaterials( TriModel model ) {
        Set<DrawMaterial> set = new HashSet<DrawMaterial>();
        getMaterials( model, set );
        return new ArrayList<DrawMaterial>( set );
    }


    public static void computeBounds( DrawTri tri, Box3 box ) {
        Vec3 v = tri.mVerts[0].mPos;
        box.x0 = box.x1 = v.x;
        box.y0 = box.y1 = v.y;
        box.z0 = box.z1 = v.z;
        addToBounds( tri.mVerts[1].mPos, box );
        addToBounds( tri.mVerts[2].mPos, box );
    }


    public static boolean computeBounds( TriModel model, Box3 box ) {
        return computeBounds( vertIterator( model ), box );
    }


    public static boolean computeBounds( Iterator<? extends DrawVert> iter, Box3 box ) {
        if( !iter.hasNext() ) {
            box.x0 = box.y0 = box.z0 = Float.NaN;
            box.x1 = box.y1 = box.z1 = Float.NaN;
            return false;
        }
        Vec3 p = iter.next().mPos;
        box.x0 = box.x1 = p.x;
        box.y0 = box.y1 = p.y;
        box.z0 = box.z1 = p.z;
        while( iter.hasNext() ) {
            addToBounds( iter.next().mPos, box );
        }

        return true;
    }


    public static void addToBounds( Vec3 v, Box3 box ) {
        float t = v.x;
        if( t < box.x0 ) {
            box.x0 = t;
        } else if( t > box.x1 ) {
            box.x1 = t;
        }
        t = v.y;
        if( t < box.y0 ) {
            box.y0 = t;
        } else if( t > box.y1 ) {
            box.y1 = t;
        }
        t = v.z;
        if( t < box.z0 ) {
            box.z0 = t;
        } else if( t > box.z1 ) {
            box.z1 = t;
        }
    }


    public static <T> Map<T,Integer> index( Iterable<T> iter ) {
        return index( iter.iterator() );
    }


    public static <T> Map<T,Integer> index( Iterator<T> iter ) {
        Map<T,Integer> out = new HashMap<T,Integer>();
        Integer index = 0;
        while( iter.hasNext() ) {
            T t = iter.next();
            Integer prev = out.put( t, index );
            if( prev == null ) {
                index++;
            } else {
                out.put( t, prev );
            }
        }
        return out;
    }


    static class TriVertIter implements Iterator<DrawVert> {

        private final Iterator<? extends DrawTri> mTriIter;
        private DrawTri mTri;
        private int     mNext;

        TriVertIter( Iterator<? extends DrawTri> tris ) {
            mTriIter = tris;
            if( !tris.hasNext() ) {
                mTri = null;
                mNext = -1;
            } else {
                mTri = tris.next();
                mNext = 0;
            }
        }

        @Override
        public boolean hasNext() {
            return mTri != null;
        }

        @Override
        public DrawVert next() {
            DrawVert ret = mTri.mVerts[mNext++];
            if( mNext == 3 ) {
                if( mTriIter.hasNext() ) {
                    mTri  = mTriIter.next();
                    mNext = 0;
                } else {
                    mTri  = null;
                }
            }
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }


    static class ModelVertIter implements Iterator<DrawVert> {

        private final Iterator<? extends TriGroup> mGroupIter;
        private Iterator<DrawVert> mVertIter;

        ModelVertIter( Iterator<? extends TriGroup> groups ) {
            mGroupIter = groups;
            mVertIter  = null;
            queue();
        }

        @Override
        public boolean hasNext() {
            return mVertIter != null;
        }

        @Override
        public DrawVert next() {
            DrawVert ret = mVertIter.next();
            if( !mVertIter.hasNext() ) {
                queue();
            }
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }


        private void queue() {
            while( mGroupIter.hasNext() ) {
                TriGroup group = mGroupIter.next();
                mVertIter = new TriVertIter( group.mTris.iterator() );
                if( mVertIter.hasNext() ) {
                    return;
                }
            }
            mVertIter = null;
        }

    }


    static class ModelTriIter implements Iterator<DrawTri> {

        private final Iterator<? extends TriGroup> mGroupIter;
        private Iterator<DrawTri> mTriIter;

        ModelTriIter( Iterator<? extends TriGroup> groups ) {
            mGroupIter = groups;
            mTriIter  = null;
            queue();
        }

        @Override
        public boolean hasNext() {
            return mTriIter != null;
        }

        @Override
        public DrawTri next() {
            DrawTri ret = mTriIter.next();
            if( !mTriIter.hasNext() ) {
                queue();
            }
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }


        private void queue() {
            while( mGroupIter.hasNext() ) {
                TriGroup group = mGroupIter.next();
                mTriIter = group.mTris.iterator();
                if( mTriIter.hasNext() ) {
                    return;
                }
            }
            mTriIter = null;
        }

    }


    static class ModelMaterialIter implements Iterator<DrawMaterial> {

        private final Iterator<TriGroup> mIter;
        private DrawMaterial mNext = null;

        public ModelMaterialIter( List<TriGroup> list ) {
            mIter = list.iterator();
            queue();
        }


        @Override
        public boolean hasNext() {
            return mNext != null;
        }

        @Override
        public DrawMaterial next() {
            DrawMaterial ret = mNext;
            queue();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void queue() {
            mNext = null;
            while( mNext == null && mIter.hasNext() ) {
                mNext = mIter.next().mMaterial;
            }
        }

    }

}
