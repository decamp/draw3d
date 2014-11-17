package bits.draw3d;

import bits.draw3d.bo.*;
import bits.draw3d.geom.DrawVert;
import bits.draw3d.shader.*;
import bits.math3d.*;

import javax.media.opengl.GL3;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static javax.media.opengl.GL2ES2.*;


/**
 * Emulates the old OpenGL immediate mode rendering. Meant largely to help transition old code to new GL versions.
 *
 * @author Philip DeCamp
 */
public class DrawStream {

    private static final int DEFAULT_BUF_SIZE = 64 * 1024;

    private final int[] mVbo = { 0 };
    private final int[] mIbo = { 0 };
    private final ByteBuffer mVertBuf;
    private final ByteBuffer mIndBuf;


    private final DrawVert          mVert         = new DrawVert( new Vec3(),
                                                                  new float[4],
                                                                  new Vec3(),
                                                                  new Vec4( 0, 0, 0, 1 ) );
    private final BasicShaderConfig mConfig       = new BasicShaderConfig();
    private       BasicShaderConfig mChosenConfig = new BasicShaderConfig();

    private final Map<BasicShaderConfig, Writer> mWriters     = new HashMap<BasicShaderConfig, Writer>();
    private final IndWriter                      mQuadIndexer = new QuadIndWriter();

    private DrawEnv mG;

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
        mIndBuf = DrawUtil.alloc( bufSize / 16 * 6 / 4 );
    }


    public void init( DrawEnv g ) {
        mG = g;
        GL3 gl = g.mGl;

        if( mVbo[0] != 0 ) {
            return;
        }

        gl.glGenBuffers( 1, mVbo, 0 );
        gl.glBindBuffer( GL_ARRAY_BUFFER, mVbo[0] );
        gl.glBufferData( GL_ARRAY_BUFFER, mVertBuf.capacity(), null, GL_STREAM_DRAW );
        gl.glBindBuffer( GL_ARRAY_BUFFER, 0 );
        DrawUtil.checkErr( gl );

        gl.glGenBuffers( 1, mIbo, 0 );
        gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, mIbo[0] );
        gl.glBufferData( GL_ELEMENT_ARRAY_BUFFER, mIndBuf.capacity(), null, GL_STREAM_DRAW );
        gl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
        DrawUtil.checkErr( gl );
    }


    public void config( boolean color, boolean tex, boolean norm ) {
        mConfig.texComponentNum( tex ? 4 : 0 );
        mConfig.color( color );
        mConfig.normals( norm );
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
        mG.checkErr();

        mActiveWriter  = writer;
        mActiveIndexer = indexer;
        mActiveMode    = mode;
        mVertBuf.clear();

        int bytes     = mVertBuf.capacity();
        int vertBytes = writer.mVertWriter.bytesPerElem();
        mActiveCap = ( bytes / ( vertBytes * blockSize ) ) * blockSize;
        mActivePos = 0;

        writer.mProgram.bind( mG );
        writer.mVao.bind( mG );

        if( indexer != null ) {
            indexer.reset();
            mIndBuf.clear();
            mG.mGl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, mIbo[0] );
        }

        DrawUtil.checkErr( mG.mGl );
    }


    public void end() {
        if( mActiveWriter == null ) {
            return;
        }
        if( mActivePos > 0 ) {
            flush();
        }
        if( mActiveIndexer != null ) {
            mG.mGl.glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0 );
            mActiveIndexer = null;
        }
        mActiveWriter.mVao.unbind( mG );
        mActiveWriter.mProgram.unbind( mG );
        DrawUtil.checkErr( mG.mGl );
    }


    public void color( int red, int green, int blue ) {
        color( red, green, blue, 0xFF );
    }


    public void color( int red, int green, int blue, int alpha ) {
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
        color( v.x, v.y, v.z, 1f );
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
        Vec.put( x, y, 0f, mVert.mPos );
    }


    public void vert( int x, int y, int z ) {
        vert( (float)x, (float)y, (float)z );
    }


    public void vert( float x, float y, float z ) {
        Vec.put( x, y, z, mVert.mPos );
    }


    public void vert( Vec3 v ) {
        Vec.put( v, mVert.mPos );
    }


    public void pointSize( float f ) {
        mG.mGl.glPointSize( f );
    }



    private Writer getWriter() {
        mConfig.chooseAvailable( mChosenConfig );
        Writer writer = mWriters.get( mChosenConfig );
        if( writer != null ) {
            return writer;
        }

        BoProgram<DrawVert,?> prog = BasicShaders.createProgram( mChosenConfig, mG.mShaderMan );
        writer = new Writer();
        writer.mProgram = prog.mProgram;
        writer.mVertWriter = prog.mVertWriter;
        writer.mVao = new Vao();
        writer.mVertWriter.attributes( writer.mVao );
        writer.mVao.bind( mG );
        mG.mGl.glBindBuffer( GL_ARRAY_BUFFER, mVbo[0] );

        mWriters.put( mChosenConfig, writer );
        mChosenConfig = new BasicShaderConfig();

        return writer;
    }


    private void flush() {
        mVertBuf.clear();
        mG.mGl.glBufferSubData( GL_ARRAY_BUFFER, 0, mVertBuf.remaining(), mVertBuf );
        if( mActiveIndexer == null ) {
            mG.mGl.glDrawArrays( mActiveMode, 0, mActivePos );
        } else {
            mIndBuf.clear();
            mG.mGl.glBufferSubData( GL_ELEMENT_ARRAY_BUFFER, 0, mIndBuf.remaining(), mIndBuf );
            mG.mGl.glDrawElements( mActiveMode, mActiveIndexer.count(), GL_UNSIGNED_INT, 0 );
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


    private static class Writer {
        public Program            mProgram;
        public BoWriter<DrawVert> mVertWriter;
        public Vao                mVao;
    }

}
