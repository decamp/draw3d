/*
 * Copyright (c) 2015. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.Vec4;
import com.jogamp.opengl.util.Animator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static javax.media.opengl.GL.GL_COLOR_BUFFER_BIT;


/**
 * @author Philip DeCamp
 */
public class TestBasicFog {

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
        Texture2 tex;

        @Override
        public void init( GLAutoDrawable gld ) {
            d.init( gld, null );
            System.out.println( "###" );

            BufferedImage im = new BufferedImage( 512, 512, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g = (Graphics2D)im.getGraphics();
            g.setBackground( new Color( 255, 255, 128 ) );
            g.clearRect( 0, 0, 512, 512 );
            tex = new Texture2();
            tex.buffer( im );

            new Animator( gld ).start();
        }

        @Override
        public void dispose( GLAutoDrawable gld) {}

        @Override
        public void display( GLAutoDrawable gld ) {
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
            tex.bind( d, 0 );

            DrawStream s = d.drawStream();
            s.config( true, true, false, true );
            s.beginTris();
            s.tex( 0, 0 );
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


}
