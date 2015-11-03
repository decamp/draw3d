/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shader;
import bits.draw3d.*;

import java.awt.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.*;
import java.util.List;
import static com.jogamp.opengl.GL3.*;


/**
 * @author Philip DeCamp
 */
public class TestUniformBlock {

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

        @Override
        public void init( GLAutoDrawable drawable ) {
            System.out.println( "init" );
            d.init( drawable, null );

            Program prog = new Program();
            prog.addShader( d.mShaderMan.loadResource( GL_VERTEX_SHADER, "../test/java/bits/draw3d/shader/Edge.vert" ) );
            prog.addShader( d.mShaderMan.loadResource( GL_GEOMETRY_SHADER, "../test/java/bits/draw3d/shader/Edge.geom" ) );
            prog.addShader( d.mShaderMan.loadResource( GL_FRAGMENT_SHADER, "../test/java/bits/draw3d/shader/Edge.frag" ) );
            prog.init( d );
            prog.bind( d );
            System.out.println( "###" );

            List<Uniform> uniforms = Shaders.listUniforms( d.mGl, prog.id() );
            List<UniformBlock> blocks = Shaders.listUniformBlocks( d.mGl, prog.id(), uniforms );

            for( UniformBlock res: blocks ) {
                System.out.println( res.mName + "\t" + res.mLocation + "\t" + res.mDataSize );
                for( Uniform uni: res.mUniforms ) {
                    System.out.println( "   " + uni.mBlockOffset + "\t" + uni.mName  );
                }
            }

            System.out.println( "###" );

            d.checkErr();
            int loc = d.mGl.glGetUniformLocation( prog.id(), "Fog.color" );
            System.out.println( loc );
        }

        @Override
        public void dispose( GLAutoDrawable drawable ) {}

        @Override
        public void display( GLAutoDrawable drawable ) {}

        @Override
        public void reshape( GLAutoDrawable drawable, int i, int i2, int i3, int i4 ) {}
    }

}
