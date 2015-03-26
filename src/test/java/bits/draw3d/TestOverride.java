/*
 * Copyright (c) 2015. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.draw3d.model.DrawVert;
import bits.draw3d.shaders.BasicShaderConfig;
import bits.draw3d.shaders.BasicShaders;
import bits.math3d.Vec4;
import com.jogamp.opengl.util.Animator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.util.*;

import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2ES2.*;


/**
 * @author Philip DeCamp
 */
public class TestOverride {

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

        Random  rand   = new Random();
        DrawEnv d      = new DrawEnv();
        Object  custom = null;

        @Override
        public void init( GLAutoDrawable gld ) {
            d.init( gld, null );
            System.out.println( "###" );
            Program prog = createProgram( d );

            for( Uniform uni: ((AutoloadProgram)prog).uniformsRef() ) {
                System.out.println( uni.mName + "\t" +
                                    MemberType.fromGl( uni.mMemberType ) +
                                    " x " + uni.mArrayLength );
            }

            java.util.List<Uniform> uniforms = Shaders.listUniforms( d.mGl, prog.id() );
            java.util.List<UniformBlock> blocks = Shaders.listUniformBlocks( d.mGl, prog.id(), uniforms );

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
            int loc = d.mGl.glGetUniformLocation( prog.id(), "Fog.color" );
            System.out.println( loc );

            custom = new Object();
            d.drawStream().createCustomConfig( custom, prog, createWriter() );
            float[] data = { 0.5f, 0, 0, 1, 0, 0, 1,
                             0, 0.5f, 0, 0, 1, 0, 1,
                             0, 0, 0, 0, 0, 1, 1 };

            new Animator( gld ).start();
        }

        @Override
        public void dispose( GLAutoDrawable gld) {}

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

            DrawStream s = d.drawStream();
            s.configCustom( custom );
            s.beginTris();
            s.color( 1, 0, 0, 1 );
            s.vert( 0.5f, 0, 0 );
            s.color( 0, 1, 0, 1 );
            s.vert( 0, 0.5f, 0 );
            s.color( 0, 0, 1, 1 );
            s.vert( 0, 0, 0 );
            s.end();
        }

        @Override
        public void reshape( GLAutoDrawable drawable, int i, int i2, int i3, int i4 ) {}
    }


    private static Program createProgram( DrawEnv d ) {
        Program prog = new AutoloadProgram();
        prog.addShader( d.mShaderMan.loadResource( GL_VERTEX_SHADER, "../test/java/bits/draw3d/TestRenderTri.vert" ) );
        prog.addShader( d.mShaderMan.loadResource( GL_FRAGMENT_SHADER, "../test/java/bits/draw3d/TestRenderTri.frag" ) );
        prog.init( d );
        prog.bind( d );
        return prog;
    }


    public static BoWriter<DrawVert> createWriter() {
        BasicShaderConfig config = new BasicShaderConfig();
        config.color( true );
        config.normals( false );
        config.texComponentNum( 0 );
        return BasicShaders.createVertWriter( config );
    }

}
