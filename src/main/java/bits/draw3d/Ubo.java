/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.*;

import javax.media.opengl.GL2ES3;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static javax.media.opengl.GL3.*;


/**
 * @author Philip DeCamp
 */
public class Ubo implements DrawUnit {

    private final ByteAlignment mLayout;
    private final int[] mId = { 0 };

    private final List<Block> mBlocks       = new ArrayList<Block>();
    private       Block       mCurrentBlock = null;

    private boolean mNeedUpdate  = true;
    private boolean mNeedRealloc = true;
    private ByteBuffer mBuf;
    private int mInternalCap = -1;


    public Ubo() {
        this( ByteAlignment.STD140 );
    }


    public Ubo( ByteAlignment layout ) {
        mLayout = layout;
    }



    public UboBlock addBlockFromProgram( UniformBlock block ) {
        mCurrentBlock = null;
        Block b = new Block( block );

        if( !mBlocks.isEmpty() ) {
            Block prev = mBlocks.get( mBlocks.size() - 1 );
            b.mBufOff = prev.mBufOff + prev.mBufSize;
        }

        for( Uniform uniform: block.mUniforms ) {
            Member member   = new Member( b, uniform );
            member.mBufOff  = b.mBufOff + uniform.mBlockOffset;
        }

        mBlocks.add( b );
        mNeedUpdate = mNeedRealloc = true;
        return b;
    }


    public UboBlock startBlock( int defaultLocation, String optName ) {
        UniformBlock ub = new UniformBlock( mBlocks.size(), defaultLocation, null, 0, null );
        Block b = mCurrentBlock = new Block( ub );

        if( !mBlocks.isEmpty() ) {
            Block prev = mBlocks.get( mBlocks.size() - 1 );
            b.mBufOff = prev.mBufOff + prev.mBufSize;
        }

        mBlocks.add( b );
        mNeedUpdate = mNeedRealloc = true;
        return mCurrentBlock;
    }


    public UboMember addUniform( int arrayLen, int memberType, String optName ) {
        Block block = mCurrentBlock;
        if( block == null ) {
            throw new IllegalStateException( "Cannot add member without block" );
        }

        MemberType type = MemberType.fromGl( memberType );
        Uniform uniform = new Uniform( memberType,
                                      arrayLen,
                                      block.mMembers.size(),
                                      -1,
                                      optName != null ? optName : "",
                                      mLayout.arrayStride( type, 1 ),
                                      mLayout.matrixStride( type ),
                                      mBlocks.size() - 1,
                                      block.mBufSize );

        Member member   = new Member( block, uniform );
        member.mBufOff  = block.mBufOff + uniform.mBlockOffset;
        member.mBufSize = mLayout.arrayStride( type, arrayLen );

        block.mMembers.add( new Member( block, uniform ) );
        block.mBufSize += uniform.mArrayStride;
        mNeedUpdate = mNeedRealloc = true;
        return null;
    }

    /**
     * Must be called before accessing any UboMember values. Called automatically
     * by bind() or alloc() if needed.
     */
    public void alloc() {
        mNeedRealloc = false;
        int totalSize = 0;
        for( Block block: mBlocks ) {
            totalSize += block.mBufSize;
        }

        ByteBuffer old = mBuf;
        mBuf = DrawUtil.alloc( totalSize );

        int off = 0;
        for( Block block: mBlocks ) {
            int prevPos   = block.mBufOff;
            int prevLimit = block.mBufOff + block.mBufSize;

            if( old != null && prevPos >= 0 && prevLimit < old.capacity() ) {
                // Rewrite old blocks into new buffer.
                old.clear().position( prevPos ).limit( prevLimit );
                mBuf.position( off );
                mBuf.put( old );
                mBuf.clear();
            }

            block.mBufOff = off;
            off += block.mBufSize;

            for( Member member: block.mMembers ) {
                member.mBufOff = block.mBufOff + member.mTarget.mBlockOffset;
            }

            block.mDirty = true;
        }

        mNeedUpdate = true;
    }


    public int blockNum() {
        return mBlocks.size();
    }

    /**
     * @param index Note this is the Block index within this Bo, not the index within a shader.
     */
    public Block block( int index ) {
        return mBlocks.get( index );
    }


    public Block block( String name ) {
        if( name == null ) {
            return null;
        }
        for( Block block: mBlocks ) {
            if( name.equals( block.mTarget.mName ) ) {
                return block;
            }
        }
        return null;
    }

    @SuppressWarnings( { "rawtype", "unchecked" } )
    public List<UboBlock> blocksRef() {
        return (List)mBlocks;
    }


    @Override
    public void init( DrawEnv d ) {
        doInit( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        if( mId[0] != 0 ) {
            d.mGl.glDeleteBuffers( 1, mId, 0 );
            mId[0] = 0;
        }
        mBlocks.clear();
        mNeedUpdate = mNeedRealloc = true;
    }

    @Override
    public void bind( DrawEnv d ) {
        if( !mNeedUpdate ) {
            d.mGl.glBindBuffer( GL_UNIFORM_BUFFER, mId[0] );
        } else {
            doInit( d );
        }
    }

    @Override
    public void unbind( DrawEnv g ) {
        g.mGl.glBindBuffer( GL_UNIFORM_BUFFER, 0 );
    }

    /**
     * Rebuffers all blocks at once.
     *
     * <p>Ubo must be bound before calling.
     */
    public void rebuffer( DrawEnv d ) {
        int cap = mBuf.capacity();
        if( mInternalCap == cap ) {
            d.mGl.glBufferSubData( GL_UNIFORM_BUFFER, 0, cap, mBuf );
        } else {
            d.mGl.glBufferData( GL_UNIFORM_BUFFER, cap, mBuf, GL_DYNAMIC_DRAW );
            mInternalCap = cap;
        }

        for( Block b: mBlocks ) {
            b.mDirty = false;
        }
    }


    private void doInit( DrawEnv d ) {
        mNeedUpdate = false;

        GL2ES3 gl = d.mGl;
        if( mId[0] == 0 ) {
            gl.glGenBuffers( 1, mId, 0 );
        }

        gl.glBindBuffer( GL_UNIFORM_BUFFER, mId[0] );
        if( mNeedRealloc ) {
            alloc();
        }

        // Rebuffer everything.
        rebuffer( d );
        d.checkErr();
    }



    private class Block implements UboBlock {

        final UniformBlock mTarget;
        final List<Member> mMembers = new ArrayList<Member>();

        private int mBinding = -1;
        private int mBufOff  =  0;
        private int mBufSize =  0;

        private boolean mDirty = true;


        public Block( UniformBlock target ) {
            mTarget  = target;
            mBinding = target.mLocation;
        }


        @Override
        public UniformBlock target() {
            return mTarget;
        }

        @Override
        public int memberNum() {
            return mMembers.size();
        }

        @Override
        public UboMember member( int idx ) {
            return mMembers.get( idx );
        }

        @Override
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


        @Override
        public void bind( DrawEnv d ) {
            bind( d, mTarget.mLocation >= 0 ? mTarget.mLocation : 0 );
        }

        @Override
        public void bind( DrawEnv d, int location ) {
            d.mGl.glBindBufferRange( GL_UNIFORM_BUFFER, location, mId[0], mBufOff, mBufSize );
            mBinding = location;
            if( mDirty ) {
                d.mGl.glBufferSubData( GL_UNIFORM_BUFFER, mBufOff, mBufSize, mBuf );
                mDirty = false;
            }
        }

        @Override
        public void unbind( DrawEnv d ) {
            mBinding = -1;
        }

        @Override
        public void unbind( DrawEnv d, int location ) {
            mBinding = -1;
        }

    }


    private class Member implements UboMember {

        private final Block   mBlock;
        private final Uniform mTarget;

        private int mBufOff  = -1;
        private int mBufSize = -1;


        Member( Block parent, Uniform target ) {
            mBlock = parent;
            mTarget = target;
        }


        public Uniform target() {
            return mTarget;
        }

        public Block block() {
            return mBlock;
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

        public void get( int[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            final int arrStride = mTarget.mArrayStride;
            for( int i = off; i < end; i++ ) {
                vals[i] = mBuf.getInt( pos );
                pos += arrStride;
            }
        }

        public void get( float[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                vals[i] = mBuf.getFloat( pos );
                pos += mTarget.mArrayStride;
            }
        }

        public void get( Vec2[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec2 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                pos += mTarget.mArrayStride;
            }
        }

        public void get( Vec3[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec3 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                v.z = mBuf.getFloat( pos + 8 );
                pos += mTarget.mArrayStride;
            }
        }

        public void get( Vec4[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec4 v = vals[i];
                v.x = mBuf.getFloat( pos );
                v.y = mBuf.getFloat( pos + 4 );
                v.z = mBuf.getFloat( pos + 8 );
                v.w = mBuf.getFloat( pos + 12 );
                pos += mTarget.mArrayStride;
            }
        }

        public void get( Mat3[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
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
                pos += mTarget.mArrayStride;
            }
        }

        public void get( Mat4[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
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
                pos += mTarget.mArrayStride;
            }
        }


        public void set( int val ) {
            mBuf.putInt( mTarget.mBlockOffset, val );
            mBlock.mDirty = true;
        }

        public void set( float val ) {
            mBuf.putFloat( mBufOff, val );
            mBlock.mDirty = true;
        }

        public void set( Vec2 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mBlock.mDirty = true;
        }

        public void set( Vec3 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mBuf.putFloat( toff + 8, vec.z );
            mBlock.mDirty = true;
        }

        public void set( Vec4 vec ) {
            final int toff = mBufOff;
            mBuf.putFloat( toff, vec.x );
            mBuf.putFloat( toff + 4, vec.y );
            mBuf.putFloat( toff + 8, vec.z );
            mBuf.putFloat( toff + 12, vec.w );
            mBlock.mDirty = true;
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
            mBlock.mDirty = true;
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
            mBlock.mDirty = true;
        }

        public void set( int[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            final int arrStride = mTarget.mArrayStride;
            for( int i = off; i < end; i++ ) {
                mBuf.putInt( pos, vals[i] );
                pos += arrStride;
            }
            mBlock.mDirty = true;
        }

        public void set( float[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                mBuf.putFloat( pos, vals[i] );
                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

        public void set( Vec2[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec2 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

        public void set( Vec3[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec3 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                mBuf.putFloat( pos + 8, v.z );
                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

        public void set( Vec4[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
            for( int i = off; i < end; i++ ) {
                Vec4 v = vals[i];
                mBuf.putFloat( pos, v.x );
                mBuf.putFloat( pos + 4, v.y );
                mBuf.putFloat( pos + 8, v.z );
                mBuf.putFloat( pos + 12, v.w );
                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

        public void set( Mat3[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
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

                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

        public void set( Mat4[] vals, int off, int len ) {
            final int end = off + len;
            int pos = mBufOff;
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

                pos += mTarget.mArrayStride;
            }
            mBlock.mDirty = true;
        }

    }

}
