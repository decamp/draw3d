//package bits.draw3d.model;
//
//import bits.math3d.*;
//
//
///**
// * @author Philip DeCamp
// */
//@Deprecated public class Triangle implements Cloneable {
//
//    public static Triangle fromVerts( Vec3 v0, Vec3 v1, Vec3 v2, boolean computeNorms ) {
//        Triangle ret = new Triangle( new Vec3[]{ v0, v1, v2 }, null, null, null );
//        if( computeNorms ) {
//            ret.mNorms = ret.createNormVecs();
//        }
//        return ret;
//    }
//
//
//    public static Triangle fromVerts( Vec3 v0, Vec3 v1, Vec3 v2,
//                                      Vec2 tex0, Vec2 tex1, Vec2 tex2,
//                                      boolean computeNorms )
//    {
//        Triangle ret = new Triangle( new Vec3[]{ v0, v1, v2 },
//                                     null,
//                                     new Vec2[]{ tex0, tex1, tex2 },
//                                     null );
//        if( computeNorms ) {
//            ret.mNorms = ret.createNormVecs();
//        }
//        return ret;
//    }
//
//
//    public Vec3[] mVerts;
//    public Vec3[] mNorms;
//    public Vec2[] mTexs;
//    public Vec4[] mColors;
//
//
//    public Triangle() {}
//
//
//    public Triangle( Vec3[] verts, Vec3[] normals, Vec2[] texs ) {
//        mVerts = verts;
//        mNorms = normals;
//        mTexs  = texs;
//    }
//
//
//    public Triangle( Vec3[] verts, Vec3[] normals, Vec2[] texs, Vec4[] colors ) {
//        mVerts  = verts;
//        mNorms  = normals;
//        mTexs   = texs;
//        mColors = colors;
//    }
//
//
//    public Triangle( Triangle copy ) {
//        mVerts = new Vec3[]{ new Vec3( copy.mVerts[0] ), new Vec3( copy.mVerts[1] ), new Vec3( copy.mVerts[2] ) };
//
//        if( copy.mNorms != null ) {
//            mNorms = new Vec3[]{ new Vec3( copy.mNorms[0] ), new Vec3( copy.mNorms[1] ), new Vec3( copy.mNorms[2] ) };
//        }
//
//        if( copy.mTexs != null ) {
//            mTexs = new Vec2[]{ new Vec2( copy.mTexs[0] ), new Vec2( copy.mTexs[1] ), new Vec2( copy.mTexs[2] ) };
//        }
//
//        if( copy.mColors != null ) {
//            mColors = new Vec4[]{ new Vec4( copy.mColors[0] ),
//                                  new Vec4( copy.mColors[1] ),
//                                  new Vec4( copy.mColors[2] ),
//                                  new Vec4( copy.mColors[3] ) };
//        }
//    }
//
//
//    public void center( Vec3 out ) {
//        out.x = ( 1.0f / 3.0f ) * ( mVerts[0].x + mVerts[1].x + mVerts[2].x );
//        out.y = ( 1.0f / 3.0f ) * ( mVerts[0].y + mVerts[1].y + mVerts[2].y );
//        out.z = ( 1.0f / 3.0f ) * ( mVerts[0].z + mVerts[1].z + mVerts[2].z );
//    }
//
//
//    public void reverseOrientation() {
//        if( mNorms != null ) {
//            Vec3 ff = mNorms[0];
//            mNorms[0] = mNorms[1];
//            mNorms[1] = ff;
//        }
//
//        if( mTexs != null ) {
//            Vec2 ff = mTexs[0];
//            mTexs[0] = mTexs[1];
//            mTexs[1] = ff;
//        }
//
//        if( mColors != null ) {
//            Vec4 ff = mColors[0];
//            mColors[0] = mColors[1];
//            mColors[1] = ff;
//        }
//
//        Vec3 v = mVerts[0];
//        mVerts[0] = mVerts[1];
//        mVerts[1] = v;
//    }
//
//
//    public Vec3[] createNormVecs() {
//        Vec3 normals  = new Vec3();
//        Vec.cross( mVerts[0], mVerts[1], mVerts[2], normals );
//        Vec.normalize( normals );
//        return new Vec3[]{ normals, new Vec3( normals ), new Vec3( normals ) };
//    }
//
//
//    @Override
//    public String toString() {
//        return String.format( "Triangle<%s, t%s, t$s>", mVerts[0], mVerts[1], mVerts[2] );
//    }
//
//
//    public Triangle safeCopy() {
//        return new Triangle( this );
//    }
//
//
//}
