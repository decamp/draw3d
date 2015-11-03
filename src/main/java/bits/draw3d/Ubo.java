/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opengl.GL3.*;


/**
 * @author Philip DeCamp
 */
public class Ubo implements DrawUnit {

    private final ByteAlignment mLayout;
    private final int[] mId = { 0 };

    final   List<Member> mMembers     = new ArrayList<Member>();
    private int          mMembersSize = 0;
    private int          mBindLoc     = -1;

    private boolean mDirty    = true; // Set true for any state change.
    private boolean mNeedInit = true; // Set true only if realloc is needed.

    private ByteBuffer mBuf;
    private int mInternalCap = -1;


    public Ubo() {
        this( ByteAlignment.STD140 );
    }


    public Ubo( ByteAlignment layout ) {
        mLayout = layout;
    }



    public int bindLocation() {
        return mBindLoc;
    }


    public void bindLocation( int loc ) {
        mBindLoc = loc;
    }


    public void configureForProgramBlock( UniformBlock block ) {
        mMembers.clear();
        mMembersSize = 0;

        for( Uniform uniform : block.mUniforms ) {
            Member member = new Member( uniform );
            member.mBufOff = uniform.mBlockOffset;
        }

        if( mBindLoc < 0 ) {
            mBindLoc = block.mLocation;
        }

        mDirty = mNeedInit = true;
    }


    public UboMember addUniform( int arrayLen, int memberType, String optName ) {
        MemberType type = MemberType.fromGl( memberType );
        Uniform uniform = new Uniform( memberType,
                                      arrayLen,
                                      mMembers.size(),
                                      -1,
                                      optName != null ? optName : "",
                                      mLayout.arrayStride( type, 1 ),
                                      mLayout.matrixStride( type ),
                                      -1,
                                      mMembersSize );

        Member member   = new Member( uniform );
        member.mBufOff  = uniform.mBlockOffset;
        member.mBufSize = mLayout.arrayStride( type, arrayLen );
        mMembersSize += member.mBufSize;
        mMembers.add( member );
        mDirty = mNeedInit = true;

        return member;
    }


    public int memberNum() {
        return mMembers.size();
    }


    public UboMember member( int idx ) {
        return mMembers.get( idx );
    }


    public UboMember member( String name ) {
        if( name == null ) {
            return null;
        }
        for( Member m: mMembers ) {
            if( name.equals( m.target().mName ) ) {
                return m;
            }
        }
        return null;
    }

    /**
     * Must be called before accessing any UboMember values. Called automatically
     * by bind() if needed.
     */
    public void allocMembersBuffer() {
        if( mBuf != null && mBuf.capacity() == mMembersSize ) {
            return;
        }
        mBuf = DrawUtil.alloc( mMembersSize );
        mDirty = true;
    }


    @Override
    public void init( DrawEnv d ) {
        if( !mNeedInit ) {
            return;
        }
        doInit( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        if( mId[0] != 0 ) {
            d.mGl.glDeleteBuffers( 1, mId, 0 );
            mId[0] = 0;
        }
        mMembersSize = 0;
        mMembers.clear();
        mNeedInit = true;
        mDirty = true;
    }


    public void bindWithoutLocation( DrawEnv d ) {
        if( !mDirty ) {
            d.mGl.glBindBuffer( GL_UNIFORM_BUFFER, mId[0] );
            return;
        }

        if( mNeedInit ) {
            doInit( d );
        } else {
            d.mGl.glBindBuffer( GL_UNIFORM_BUFFER, mId[0] );
        }
        rebuffer( d );
    }

    @Override
    public void bind( DrawEnv d ) {
        bindWithoutLocation( d );
        if( mBindLoc >= 0 ) {
            d.mGl.glBindBufferBase( GL_UNIFORM_BUFFER, mBindLoc, mId[0] );
        }
    }


    public void bind( DrawEnv d, int location ) {
        bindWithoutLocation( d );
        d.mGl.glBindBufferBase( GL_UNIFORM_BUFFER, location, mId[0] );
    }

    @Override
    public void unbind( DrawEnv g ) {
        g.mGl.glBindBuffer( GL_UNIFORM_BUFFER, 0 );
    }

    /**
     * Called automatically if by any {@code bind()} method if any members
     * have been set.
     *
     * <p>Must be bound before calling.
     */
    public void rebuffer( DrawEnv d ) {
        mDirty = false;
        int cap = mBuf.capacity();
        if( mInternalCap == cap ) {
            d.mGl.glBufferSubData( GL_UNIFORM_BUFFER, 0, cap, mBuf );
        } else {
            d.mGl.glBufferData( GL_UNIFORM_BUFFER, cap, mBuf, GL_DYNAMIC_DRAW );
            mInternalCap = cap;
        }
    }


    private void doInit( DrawEnv d ) {
        mNeedInit = false;
        if( mId[0] == 0 ) {
            d.mGl.glGenBuffers( 1, mId, 0 );
        }
        d.mGl.glBindBuffer( GL_UNIFORM_BUFFER, mId[0] );
        allocMembersBuffer();
        d.checkErr();
    }


    private class Member implements UboMember {

        private final Uniform mTarget;

        private int mBufOff  = -1;
        private int mBufSize = -1;


        Member( Uniform target ) {
            mTarget = target;
        }


        public Uniform target() {
            return mTarget;
        }


        public int getInt() {
            return mBuf.getInt( mBufOff );
        }

        public float getFloat() {
            return mBuf.getFloat( mBufOff );
        }

        public void get( Vec2 vec ) {
            final int toff = mBufOff;
            vec.x = mBuf.getFloat( toff );
            vec.y = mBuf.getFloat( toff + 4 );
        }

        public void get( Vec3 vec ) {
            final int toff = mBufOff;
            vec.x = mBuf.getFloat( toff );
            vec.y = mBuf.getFloat( toff + 4 );
            vec.z = mBuf.getFloat( toff + 8 );
        }

        public void get( Vec4 vec ) {
            final int toff = mBufOff;
            vec.x = mBuf.getFloat( toff );
            vec.y = mBuf.getFloat( toff + 4 );
            vec.z = mBuf.getFloat( toff + 8 );
            vec.w = mBuf.getFloat( toff + 12 );
        }

        public void get( Mat3 mat ) {
            int off = mBufOff;
            mat.m00 = mBuf.getFloat( off + 0 );
            mat.m10 = mBuf.getFloat( off + 4 );
            mat.m20 = mBuf.getFloat( off + 8 );
            off += mTarget.mMatrixStride;
            mat.m01 = mBuf.getFloat( off + 0 );
            mat.m11 = mBuf.getFloat( off + 4 );
            mat.m21 = mBuf.getFloat( off + 8 );
            off += mTarget.mMatrixStride;
            mat.m02 = mBuf.getFloat( off + 0 );
            mat.m12 = mBuf.getFloat( off + 4 );
            mat.m22 = mBuf.getFloat( off + 8 );
        }

        public void get( Mat4 mat ) {
            int toff = mBufOff;
            final int matStride = mTarget.mMatrixStride;
            mat.m00 = mBuf.getFloat( toff + 0 );
            mat.m10 = mBuf.getFloat( toff + 4 );
            mat.m20 = mBuf.getFloat( toff + 8 );
            mat.m30 = mBuf.getFloat( toff + 12 );
            toff += matStride;
            mat.m01 = mBuf.getFloat( toff + 0 );
            mat.m11 = mBuf.getFloat( toff + 4 );
            mat.m21 = mBuf.getFloat( toff + 8 );
            mat.m31 = mBuf.getFloat( toff + 12 );
            toff += matStride;
            mat.m02 = mBuf.getFloat( toff + 0 );
            mat.m12 = mBuf.getFloat( toff + 4 );
            mat.m22 = mBuf.getFloat( toff + 8 );
            mat.m32 = mBuf.getFloat( toff + 12 );
            toff += matStride;
            mat.m03 = mBuf.getFloat( toff + 0 );
            mat.m13 = mBuf.getFloat( toff + 4 );
            mat.m23 = mBuf.getFloat( toff + 8 );
            mat.m33 = mBuf.getFloat( toff + 12 );
        }

        public void get( int firstElem, int[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                vals[i] = mBuf.getInt( pos );
                pos += as;
            }
        }

        public void get( int firstElem, float[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                vals[i] = mBuf.getFloat( pos );
                pos += as;
            }
        }

        public void get( int firstElem, Vec2[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                Vec2 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                pos += as;
            }
        }

        public void get( int firstElem, Vec3[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                Vec3 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                v.z = mBuf.getFloat( pos + 8 );
                pos += as;
            }
        }

        public void get( int firstElem, Vec4[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                Vec4 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                v.z = mBuf.getFloat( pos + 8 );
                v.w = mBuf.getFloat( pos + 12 );
                pos += as;
            }
        }

        public void get( int firstElem, Mat3[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                int p = pos;
                Mat3 mat = vals[i];
                mat.m00 = mBuf.getFloat( p + 0 );
                mat.m10 = mBuf.getFloat( p + 4 );
                mat.m20 = mBuf.getFloat( p + 8 );
                p += mTarget.mMatrixStride;
                mat.m01 = mBuf.getFloat( p + 0 );
                mat.m11 = mBuf.getFloat( p + 4 );
                mat.m21 = mBuf.getFloat( p + 8 );
                p += mTarget.mMatrixStride;
                mat.m02 = mBuf.getFloat( p + 0 );
                mat.m12 = mBuf.getFloat( p + 4 );
                mat.m22 = mBuf.getFloat( p + 8 );
                pos += as;
            }
        }

        public void get( int firstElem, Mat4[] vals, int off, int len ) {
            final int end = off + len;
            final int as = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * as;
            for( int i = off; i < end; i++ ) {
                int p = pos;
                Mat4 mat = vals[i];
                mat.m00 = mBuf.getFloat( p + 0 );
                mat.m10 = mBuf.getFloat( p + 4 );
                mat.m20 = mBuf.getFloat( p + 8 );
                mat.m30 = mBuf.getFloat( p + 12 );
                off += mTarget.mMatrixStride;
                mat.m01 = mBuf.getFloat( p + 0 );
                mat.m11 = mBuf.getFloat( p + 4 );
                mat.m21 = mBuf.getFloat( p + 8 );
                mat.m31 = mBuf.getFloat( p + 12 );
                off += mTarget.mMatrixStride;
                mat.m02 = mBuf.getFloat( p + 0 );
                mat.m12 = mBuf.getFloat( p + 4 );
                mat.m22 = mBuf.getFloat( p + 8 );
                mat.m32 = mBuf.getFloat( p + 12 );
                off += mTarget.mMatrixStride;
                mat.m03 = mBuf.getFloat( p + 0 );
                mat.m13 = mBuf.getFloat( p + 4 );
                mat.m23 = mBuf.getFloat( p + 8 );
                mat.m33 = mBuf.getFloat( p + 12 );
                pos += as;
            }
        }

        public int getComponentInt( int elem, int row, int col ) {
            return mBuf.getInt( mBufOff + elem * mTarget.mArrayStride + col * mTarget.mMatrixStride + 4 * row );
        }

        public float getComponentFloat( int elem, int row, int col ) {
            return mBuf.getFloat( mBufOff + elem * mTarget.mArrayStride + col * mTarget.mMatrixStride + 4 * row );
        }


        public void set( int val ) {
            mBuf.putInt( mTarget.mBlockOffset, val );
            mDirty = true;
        }

        public void set( float val ) {
            mBuf.putFloat( mBufOff, val );
            mDirty = true;
        }

        public void set( Vec2 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mDirty = true;
        }

        public void set( Vec3 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mBuf.putFloat( toff + 8, vec.z );
            mDirty = true;
        }

        public void set( Vec4 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mBuf.putFloat( toff + 8, vec.z );
            mBuf.putFloat( toff + 12, vec.w );
            mDirty = true;
        }

        public void set( Mat3 mat ) {
            int off = mBufOff;
            mBuf.putFloat( off + 0, mat.m00 );
            mBuf.putFloat( off + 4, mat.m10 );
            mBuf.putFloat( off + 8, mat.m20 );
            off += mTarget.mMatrixStride;
            mBuf.putFloat( off + 0, mat.m01 );
            mBuf.putFloat( off + 4, mat.m11 );
            mBuf.putFloat( off + 8, mat.m21 );
            off += mTarget.mMatrixStride;
            mBuf.putFloat( off + 0, mat.m02 );
            mBuf.putFloat( off + 4, mat.m12 );
            mBuf.putFloat( off + 8, mat.m22 );
            mDirty = true;
        }

        public void set( Mat4 mat ) {
            int toff = mBufOff;
            final int matStride = mTarget.mMatrixStride;
            mBuf.putFloat( toff + 0, mat.m00 );
            mBuf.putFloat( toff + 4, mat.m10 );
            mBuf.putFloat( toff + 8, mat.m20 );
            mBuf.putFloat( toff + 12, mat.m30 );
            toff += matStride;
            mBuf.putFloat( toff + 0, mat.m01 );
            mBuf.putFloat( toff + 4, mat.m11 );
            mBuf.putFloat( toff + 8, mat.m21 );
            mBuf.putFloat( toff + 12, mat.m31 );
            toff += matStride;
            mBuf.putFloat( toff + 0, mat.m02 );
            mBuf.putFloat( toff + 4, mat.m12 );
            mBuf.putFloat( toff + 8, mat.m22 );
            mBuf.putFloat( toff + 12, mat.m32 );
            toff += matStride;
            mBuf.putFloat( toff + 0, mat.m03 );
            mBuf.putFloat( toff + 4, mat.m13 );
            mBuf.putFloat( toff + 8, mat.m23 );
            mBuf.putFloat( toff + 12, mat.m33 );
            mDirty = true;
        }

        public void set( int firstElem, int[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + arrStride * firstElem;
            for( int i = off; i < end; i++ ) {
                mBuf.putInt( pos, vals[i] );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, float[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * arrStride;
            for( int i = off; i < end; i++ ) {
                mBuf.putFloat( pos, vals[i] );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, Vec2[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * arrStride;
            for( int i = off; i < end; i++ ) {
                Vec2 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, Vec3[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * arrStride;
            for( int i = off; i < end; i++ ) {
                Vec3 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                mBuf.putFloat( pos + 8, v.z );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, Vec4[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * arrStride;
            for( int i = off; i < end; i++ ) {
                Vec4 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                mBuf.putFloat( pos + 8, v.z );
                mBuf.putFloat( pos + 12, v.w );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, Mat3[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + firstElem * arrStride;
            for( int i = off; i < end; i++ ) {
                int p = pos;

                Mat3 mat = vals[i];
                mBuf.putFloat( p + 0, mat.m00 );
                mBuf.putFloat( p + 4, mat.m10 );
                mBuf.putFloat( p + 8, mat.m20 );
                p += mTarget.mMatrixStride;
                mBuf.putFloat( p + 0, mat.m01 );
                mBuf.putFloat( p + 4, mat.m11 );
                mBuf.putFloat( p + 8, mat.m21 );
                p += mTarget.mMatrixStride;
                mBuf.putFloat( p + 0, mat.m02 );
                mBuf.putFloat( p + 4, mat.m12 );
                mBuf.putFloat( p + 8, mat.m22 );
                pos += arrStride;
            }
            mDirty = true;
        }

        public void set( int firstElem, Mat4[] vals, int off, int len ) {
            final int end = off + len;
            final int arrStride = mTarget.mArrayStride;
            int pos = mBufOff + arrStride * firstElem;
            for( int i = off; i < end; i++ ) {
                int p = pos;
                Mat4 mat = vals[i];
                mBuf.putFloat( p + 0, mat.m00 );
                mBuf.putFloat( p + 4, mat.m10 );
                mBuf.putFloat( p + 8, mat.m20 );
                mBuf.putFloat( p + 12, mat.m30 );
                off += mTarget.mMatrixStride;
                mBuf.putFloat( p + 0, mat.m01 );
                mBuf.putFloat( p + 4, mat.m11 );
                mBuf.putFloat( p + 8, mat.m21 );
                mBuf.putFloat( p + 12, mat.m31 );
                off += mTarget.mMatrixStride;
                mBuf.putFloat( p + 0, mat.m02 );
                mBuf.putFloat( p + 4, mat.m12 );
                mBuf.putFloat( p + 8, mat.m22 );
                mBuf.putFloat( p + 12, mat.m32 );
                off += mTarget.mMatrixStride;
                mBuf.putFloat( p + 0, mat.m03 );
                mBuf.putFloat( p + 4, mat.m13 );
                mBuf.putFloat( p + 8, mat.m23 );
                mBuf.putFloat( p + 12, mat.m33 );

                pos += arrStride;
            }
            mDirty = true;
        }

        public void setComponent( int elem, int row, int col, int val ) {
            mBuf.putInt( mBufOff + elem * mTarget.mArrayStride + col * mTarget.mMatrixStride + 4 * row, val );
        }

        public void setComponent( int elem, int row, int col, float val ) {
            mBuf.putFloat( mBufOff + elem * mTarget.mArrayStride + col * mTarget.mMatrixStride + 4 * row, val );
        }

    }

}
