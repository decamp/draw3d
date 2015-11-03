/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.Vec4;
import com.jogamp.opengl.util.Animator;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import static com.jogamp.opengl.GL3.*;


/**
 * @author Philip DeCamp
 */
public class TestRenderTri {


    public static void main( String[] args ) throws Exception {
        test1();
    }


    static void test1() throws Exception {
        GLProfile prof = GLProfile.get( GLProfile.GL3 );
        GLCapabilities caps = new GLCapabilities( prof );
        GLCanvas canvas = new GLCanvas( caps );

        JFrame frame = new JFrame();
        Dimension dim = new Dimension( 1024, 768 );
        frame.getContentPane().setPreferredSize( dim );
        frame.pack();
        frame.add( canvas );
        canvas.addGLEventListener( new Handler() );

        frame.setVisible( true );
    }


    private static class Handler implements GLEventListener {

        DrawEnv d = new DrawEnv();
        AutoloadProgram mProg;
        Vao mVao;

        @Override
        public void init( GLAutoDrawable gld ) {
            System.out.println( "init" );
            d.init( gld, null );

            mProg = new AutoloadProgram();
            mProg.addShader( d.mShaderMan.loadResource( GL_VERTEX_SHADER, "../test/java/bits/draw3d/TestRenderTri.vert" ) );
            mProg.addShader( d.mShaderMan.loadResource( GL_FRAGMENT_SHADER, "../test/java/bits/draw3d/TestRenderTri.frag" ) );
            mProg.init( d );
            mProg.bind( d );
            System.out.println( "###" );

            for( Uniform uni: mProg.uniformsRef() ) {
                System.out.println( uni.mName + "\t" +
                                    MemberType.fromGl( uni.mMemberType ) +
                                    " x " + uni.mArrayLength );
            }

            List<Uniform> uniforms = Shaders.listUniforms( d.mGl, mProg.id() );
            List<UniformBlock> blocks = Shaders.listUniformBlocks( d.mGl, mProg.id(), uniforms );

            for( UniformBlock res: blocks ) {
                System.out.println( res.mName + "\t" + res.mLocation + "\t" + res.mDataSize );
                for( Uniform uni: res.mUniforms ) {
                    System.out.println( "    " + uni.mName + "\t" +
                                        MemberType.fromGl( uni.mMemberType ) +
                                        " x " + uni.mArrayLength );
                }
            }

            System.out.println( "###" );

            d.checkErr();
            int loc = d.mGl.glGetUniformLocation( mProg.id(), "Fog.color" );
            System.out.println( loc );

            mVao = new Vao( Bo.createArrayBuffer( GL_STATIC_DRAW ), null );
            mVao.addAttribute( 0, 3, GL_FLOAT, false, 12 + 16,  0 );
            mVao.addAttribute( 1, 4, GL_FLOAT, false, 12 + 16, 12 );
            ByteBuffer bb = DrawUtil.alloc( 3 * 12 + 3 * 16 );
            float[] data = { 0.5f, 0, 0, 1, 0, 0, 1,
                             0, 0.5f, 0, 0, 1, 0, 1,
                             0, 0, 0, 0, 0, 1, 1 };
            bb.asFloatBuffer().put( data );
            mVao.vbo().buffer( bb );

            new Animator( gld ).start();
        }

        @Override
        public void dispose( GLAutoDrawable gld) {}

        Random rand = new Random();

        @Override
        public void display( GLAutoDrawable gld ) {
            //System.out.println( "DRAW" );

            d.init( gld, null );
            GL2ES2 gl = d.mGl;
            gl.glClearColor( 0.2f, 0.18f, 0.18f, 0f );
            gl.glClear( GL_COLOR_BUFFER_BIT );

            d.mView.identity();
            d.mView.translate( 0f, 0f, -1f );
            d.mProj.setOrtho( -1f, 1f, -1f, 1f, 0f, 2f );

            Vec4 fog = new Vec4( rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1f );
            d.mFog.apply( fog, 0.6f, 0f );
            d.checkErr();

            mProg.bind( d );
            mVao.bind( d );
            gl.glDrawArrays( GL_TRIANGLES, 0, 3 );

//            DrawStream s = d.drawStream();
//            s.config( true, false, false );
//            s.beginTris();
//            s.color( 1, 0, 0 );
//            s.vert( 0.5f, 0, 0 );
//            s.color( 0, 1, 0 );
//            s.vert( 0, 0.5f, 0 );
//            s.color( 0, 0, 1 );
//            s.vert( 0, 0, 0 );
//            s.end();


        }

        @Override
        public void reshape( GLAutoDrawable drawable, int i, int i2, int i3, int i4 ) {}
    }

}
