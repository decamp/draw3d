/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.draw3d.model.DrawVert;
import bits.draw3d.shaders.BasicShaderConfig;
import bits.draw3d.shaders.BasicShaders;
import bits.math3d.*;
import bits.util.ref.AbstractRefable;

import com.jogamp.opengl.GL3;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.jogamp.opengl.GL2ES2.*;


/**
 * Emulates the old OpenGL immediate mode rendering. Meant largely to help transition old code to new GL versions.
 *
 * @author Philip DeCamp
 */
public class DrawStream {

    private static final int DEFAULT_BUF_SIZE = 64 * 1024;

    private final Bo mVbo = Bo.createArrayBuffer( GL_STREAM_DRAW );
    private final Bo mIbo = Bo.createElementBuffer( GL_STREAM_DRAW );
    private final ByteBuffer mVertBuf;
    private final ByteBuffer mIndBuf;

    private final DrawVert mVert = new DrawVert( new Vec3(),
                                                 new float[4],
                                                 new Vec3(),
                                                 new Vec4( 0, 0, 0, 1 ) );

    private final BasicShaderConfig mConfig         = new BasicShaderConfig();
    private       BasicShaderConfig mChosenConfig   = new BasicShaderConfig();
    private       Object            mOverrideConfig = null;

    private final Map<Object, Writer> mWriters     = new HashMap<Object, Writer>();
    private final IndWriter           mQuadIndexer = new QuadIndWriter();

    private DrawEnv mDraw;

    private Writer    mActiveWriter  = null;
    private IndWriter mActiveIndexer = null;
    private int       mActiveCap     = 0;
    private int       mActivePos     = 0;
    private int       mActiveMode    = 0;


    public DrawStream() {
        this( DEFAULT_BUF_SIZE );
    }


    public DrawStream( int bufSize ) {
        mVertBuf = DrawUtil.alloc( bufSize );
        // 12 is smallest possible vert size.
        // 6/4 is the max ration between indices and verts (for drawing quads)
        // 4 at the end is bytes per index.
        mIndBuf = DrawUtil.alloc( bufSize / 12 * 6 / 4 * 4 );
    }


    public void init( DrawEnv g ) {
        mDraw = g;
        GL3 gl = g.mGl;

        if( mVbo.id() != 0 ) {
            return;
        }

        mVbo.alloc( mVertBuf.capacity() );
        mVbo.init( g );
        mIbo.alloc( mIndBuf.capacity() );
        mIbo.init( g );
        DrawUtil.checkErr( gl );
    }

    /**
     * Configures the rendering program for the draw stream.
     * Some version of {@code config()} or {@code configCustom()}
     * must be called prior to any {@code begin*()}.
     *
     * @param color Enables color information.
     * @param tex   Enables texture information.
     * @param norm  Enables normal information.
     */
    public void config( boolean color, boolean tex, boolean norm ) {
        config( color, tex, norm, false );
    }

    /**
     * Configures the rendering program for the draw stream.
     * Some version of {@code config()} or {@code configCustom()}
     * must be called prior to any {@code begin*()}.
     *
     * @param color Enables color information.
     * @param tex   Enables texture information.
     * @param norm  Enables normal information.
     * @param fog   Enables fog information.
     */
    public void config( boolean color, boolean tex, boolean norm, boolean fog ) {
        mOverrideConfig = null;
        mConfig.texComponentNum( tex ? 4 : 0 );
        mConfig.color( color );
        mConfig.normals( norm );
        mConfig.fog( fog );
    }

    /**
     * Specifies that a custom configuration object will be used for rendering.
     *
     * @param key Unique object that identifies configuration.
     */
    public void configCustom( Object key ) {
        mOverrideConfig = key;
    }

    /**
     * Enables user to provide a custom configuration that can be used with this DrawStream.
     *
     * @param key        Unique object used to identify the configuration.
     * @param prog       Program to use in configuration.
     * @param vertWriter BoWriter to serialize data in configuration.
     */
    public void createCustomConfig( Object key, Program prog, BoWriter<? super DrawVert> vertWriter ) {
        Writer writer = new Writer( prog, vertWriter );
        Writer prev = mWriters.put( key, writer );
        if( prev != null ) {
            prev.deref();
        }
    }

    /**
     * Disposes previously created configuration.
     *
     * @param key Unique object used to identify the configuration.
     */
    public boolean disposeCustomConfig( Object key ) {
        Writer prev = mWriters.remove( key );
        if( prev != null ) {
            prev.deref();
            return true;
        }
        return false;
    }



    public void beginPoints() {
        mConfig.geomMode( GL_POINTS );
        begin( getWriter(), null, GL_POINTS, 1 );
    }


    public void beginLines() {
        mConfig.geomMode( GL_LINES );
        begin( getWriter(), null, GL_LINES, 2 );
    }


    public void beginLineStrip() {
        mConfig.geomMode( GL_LINE_STRIP );
        begin( getWriter(), null, GL_LINE_STRIP, 2 );
    }


    public void beginLineLoop() {
        mConfig.geomMode( GL_LINE_LOOP );
        begin( getWriter(), null, GL_LINE_LOOP, 2 );
    }


    public void beginTris() {
        mConfig.geomMode( GL_TRIANGLES );
        begin( getWriter(), null, GL_TRIANGLES, 3 );
    }


    public void beginTriStrip() {
        mConfig.geomMode( GL_TRIANGLE_STRIP );
        begin( getWriter(), null, GL_TRIANGLE_STRIP, 3 );
    }


    public void beginQuads() {
        mConfig.geomMode( GL_TRIANGLES );
        begin( getWriter(), mQuadIndexer, GL_TRIANGLES, 4 );
    }


    public void beginQuadStrip() {
        mConfig.geomMode( GL_TRIANGLE_STRIP );
        begin( getWriter(), null, GL_TRIANGLE_STRIP, 4 );
    }


    private void begin( Writer writer, IndWriter indexer, int mode, int blockSize ) {
        mDraw.checkErr();

        mActiveWriter  = writer;
        mActiveIndexer = indexer;
        mActiveMode    = mode;
        mVertBuf.clear();

        int bytes     = mVertBuf.capacity();
        int vertBytes = writer.mVertWriter.bytesPerElem();
        mActiveCap = ( bytes / ( vertBytes * blockSize ) ) * blockSize;
        mActivePos = 0;

        writer.mProgram.bind( mDraw );
        writer.mVao.bind( mDraw );
        mVbo.bind( mDraw );

        if( indexer != null ) {
            indexer.reset();
            mIndBuf.clear();
            mIbo.bind( mDraw );
        }

        DrawUtil.checkErr( mDraw.mGl );
    }


    public void end() {
        if( mActiveWriter == null ) {
            return;
        }
        if( mActivePos > 0 ) {
            flush();
        }
        if( mActiveIndexer != null ) {
            mDraw.mGl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
            mActiveIndexer = null;
        }
        mActiveWriter.mVao.unbind( mDraw );
        mActiveWriter.mProgram.unbind( mDraw );
        DrawUtil.checkErr( mDraw.mGl );
    }


    public void colorub( int red, int green, int blue ) {
        colorub( red, green, blue, 0xFF );
    }


    public void colorub( int red, int green, int blue, int alpha ) {
        Vec4 c = mVert.mColor;
        c.x = red   / 255f;
        c.y = green / 255f;
        c.z = blue  / 255f;
        c.w = alpha / 255f;
    }


    public void color( float red, float green, float blue ) {
        color( red, green, blue, 1f );
    }


    public void color( float red, float green, float blue, float alpha ) {
        Vec4 c = mVert.mColor;
        c.x = red;
        c.y = green;
        c.z = blue;
        c.w = alpha;
    }


    public void color( Vec3 v ) {
        color( v.x, v.y, v.z, 1f );
    }


    public void color( Vec4 v ) {
        color( v.x, v.y, v.z, v.w );
    }


    public void norm( int x, int y, int z ) {
        norm( (float)x, (float)y, (float)z );
    }


    public void norm( float x, float y, float z ) {
        Vec3 v = mVert.mNorm;
        v.x = x;
        v.y = y;
        v.z = z;
    }


    public void norm( float[] v ) {
        tex( v[0], v[1], v[2], 1f );
    }


    public void tex( int x, int y ) {
        this.tex( (float)x, (float)y, 0f, 1f );
    }


    public void tex( float x, float y ) {
        tex( x, y, 0f, 1f );
    }


    public void tex( int x, int y, int z ) {
        tex( (float)x, (float)y, (float)z, 1f );
    }


    public void tex( float x, float y, float z ) {
        tex( x, y, z, 1 );
    }


    public void tex( int x, int y, int z, int w ) {
        tex( (float)x, (float)y, (float)z, (float)w );
    }


    public void tex( float x, float y, float z, float w ) {
        float[] v = mVert.mTex;
        v[0] = x;
        v[1] = y;
        v[2] = z;
        v[3] = w;
    }


    public void tex( Vec2 v ) {
        tex( v.x, v.y, 0f, 1f );
    }


    public void tex( Vec3 v ) {
        tex( v.x, v.y, v.z, 1f );
    }


    public void tex( Vec4 v ) {
        Vec.put( v, mVert.mTex );
    }


    public void vert( int x, int y ) {
        vert( (float)x, (float)y, 0f );
    }


    public void vert( float x, float y ) {
        vert( x, y, 0f );
    }


    public void vert( int x, int y, int z ) {
        vert( (float)x, (float)y, (float)z );
    }


    public void vert( float x, float y, float z ) {
        Vec.put( x, y, z, mVert.mPos );
        mActiveWriter.mVertWriter.write( mVert, mVertBuf );
        if( mActiveIndexer != null ) {
            mActiveIndexer.write( mActivePos, mIndBuf );
        }
        if( ++mActivePos < mActiveCap ) {
            return;
        }
        flush();
    }


    public void vert( Vec3 v ) {
        vert( v.x, v.y, v.z );
    }


    public void pointSize( float f ) {
        mDraw.mGl.glPointSize( f );
    }



    private Writer getWriter() {
        if( mOverrideConfig != null ) {
            Writer ret = mWriters.get( mOverrideConfig );
            if( ret != null ) {
                return ret;
            }
            throw new IllegalStateException( "DrawStream configuration not found." );
        }

        mConfig.lineWidth( mDraw.mLineWidth.mValue );
        mConfig.chooseAvailable( mChosenConfig );
        Writer writer = mWriters.get( mChosenConfig );
        if( writer != null ) {
            return writer;
        }

        BoProgram<DrawVert,?> prog = BasicShaders.createProgram( mChosenConfig, mDraw.mShaderMan );
        writer = new Writer( prog.mProgram, prog.mVertWriter );
        mWriters.put( new BasicShaderConfig( mChosenConfig ), writer );
        return writer;
    }



    private class Writer extends AbstractRefable {
        public Object                     mParent;
        public Program                    mProgram;
        public BoWriter<? super DrawVert> mVertWriter;
        public Vao                        mVao;

        public Writer( Program program, BoWriter<? super DrawVert> writer ) {
            mProgram    = program;
            mVertWriter = writer;
            mVao        = new Vao( mVbo, null );

            program.init( mDraw );
            writer.attributes( mVao );
        }

        @Override
        protected void freeObject() {
            mProgram.dispose( mDraw );
            mVao.dispose( mDraw );
        }
    }


    private void flush() {
        DrawEnv d = mDraw;

        mVertBuf.flip();
        d.mGl.glBufferSubData( GL_ARRAY_BUFFER, 0, mVertBuf.remaining(), mVertBuf );
        mVertBuf.clear();

        if( mActiveIndexer == null ) {
            d.mGl.glDrawArrays( mActiveMode, 0, mActivePos );
        } else {
            mIndBuf.flip();
            d.mGl.glBufferSubData( GL_ELEMENT_ARRAY_BUFFER, 0, mIndBuf.remaining(), mIndBuf );
            d.mGl.glDrawElements( mActiveMode, mActiveIndexer.count(), GL_UNSIGNED_INT, 0 );
            mIndBuf.clear();
        }
        mActivePos = 0;
    }


    public static interface IndWriter {
        void reset();
        void write( int ind, ByteBuffer out );
        int count();
    }


    private static class QuadIndWriter implements IndWriter {
        int mCount = 0;
        int mPos   = 0;
        int[] mV   = { 0, 0, 0, 0 };

        public void reset() {
            mPos = 0;
            mCount = 0;
        }

        public void write( int ind, ByteBuffer out ) {
            mV[mPos++] = ind;
            if( mPos == 4 ) {
                out.putInt( mV[0] );
                out.putInt( mV[1] );
                out.putInt( mV[2] );
                out.putInt( mV[0] );
                out.putInt( mV[2] );
                out.putInt( mV[3] );
                mPos = 0;
                mCount += 6;
            }
        }

        public int count() {
            return mCount;
        }
    }

}
