/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.draw3d.lighting.*;
import bits.draw3d.model.*;
import bits.math3d.*;
import com.jogamp.opengl.util.Animator;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

import static javax.media.opengl.GL3.*;


/**
 * @author Philip DeCamp
 */
public class TestLighting {

    public static void main( String[] args ) throws Exception {
        test1();
    }


    static void test1() throws Exception {
        GLProfile prof = GLProfile.get( GLProfile.GL3 );
        GLCapabilities caps = new GLCapabilities( prof );
        GLCanvas canvas = new GLCanvas( caps );

        JFrame frame = new JFrame();
        Dimension dim = new Dimension( 1024, 1024 );
        frame.getContentPane().setPreferredSize( dim );
        frame.pack();
        frame.add( canvas );
        canvas.addGLEventListener( new Handler() );

        frame.setVisible( true );
    }


    private static class Handler implements GLEventListener {
        DrawEnv d = new DrawEnv();
        AutoloadProgram mProg;
        Vao             mVao;
        LightUniform    mLights;
        MaterialUniform mMaterials;
        List<DrawTri>   mTris;
        int             mTriCount;

        @Override
        public void init( GLAutoDrawable gld ) {
            System.out.println( "init" );
            d.init( gld, null );

            mProg = new AutoloadProgram();
            mProg.addShader( d.mShaderMan.loadResource( GL_VERTEX_SHADER,
                                                        "../test/java/bits/draw3d/TestLighting.vert" ) );
            mProg.addShader( d.mShaderMan.loadResource( GL_FRAGMENT_SHADER,
                                                        "../test/java/bits/draw3d/TestLighting.frag" ) );
            mProg.init( d );
            mProg.bind( d );
            System.out.println( "###" );

            for( Uniform uni : mProg.uniformsRef() ) {
                System.out.println( uni.mName + "\t" + MemberType.fromGl( uni.mMemberType ) + " x " + uni.mArrayLength );
            }

            List<UniformBlock> blocks = mProg.uniformBlocksRef();
            for( UniformBlock res : blocks ) {
                System.out.println( res.mName + "\t" + res.mIndex + "\t" + res.mLocation + "\t" + res.mDataSize );
                for( Uniform uni : res.mUniforms ) {
                    System.out.println( "    " + uni.mName + "\t" + MemberType.fromGl( uni.mMemberType ) + " x " + uni.mArrayLength );
                }
            }

            System.out.println( "###" );
            List<DrawTri> tris = genSphere( 0.5, 16, 32 );
            mVao = bufferTris( tris );
            mTris = tris;
            mTriCount = tris.size();
            d.checkErr();

            mLights    = new LightUniform( 1 );
            mMaterials = new MaterialUniform( 2 );
            mLights.ambient( new Vec3( 0.25f, 0.25f, 0.25f ) );
            mLights.set( 0, Light.createSpotlight( new Vec3( 1, 1, 1 ),
                                                   new Vec3( -1, 1, -1 ),
                                                   new Vec3( 1, -1, 1 ),
                                                   new Vec3( 1, 0, 0.1f ),
                                                   (float)Math.PI * 0.25f,
                                                   4f ) );

            mLights.init( d );
            mMaterials.set( 0, new Material( new Vec4( 0.5f, 0.1f, 0.1f, 1.0f ),
                                             new Vec4( 0.1f, 0.5f, 0.1f, 1.0f ),
                                             new Vec4( 0.1f, 0.1f, 0.5f, 1.0f ),
                                             new Vec4( 0.0f, 0.0f, 0.0f, 1.0f ),
                                             20f ) );
            mMaterials.init( d );
            d.checkErr();
            new Animator( gld ).start();
        }

        @Override
        public void dispose( GLAutoDrawable gld) {}

        Random rand = new Random();

        @Override
        public void display( GLAutoDrawable gld ) {
            d.init( gld, null );
            GL2ES2 gl = d.mGl;
            gl.glClearColor( 0.2f, 0.18f, 0.18f, 0f );
            gl.glClear( GL_COLOR_BUFFER_BIT );

            d.mCullFace.apply( false );
            d.mView.identity();
            d.mView.translate( 0f, 0f, -1f );
            d.mProj.setOrtho( -1f, 1f, -1f, 1f, 0f, 2f );
            mProg.bind( d );

            d.mGl.glUniformBlockBinding( mProg.id(), 0, 6 );
            d.mGl.glUniformBlockBinding( mProg.id(), 0, 7 );

            mVao.bind( d );

            //Vec4 fog = new Vec4( 0.8f, rand.nextFloat(), rand.nextFloat(), 1f );
            //d.mFog.apply( fog, 0.6f, 0f );
            //d.checkErr();

            mLights.bind( d );
            d.checkErr();
            mMaterials.bind( d );
            d.checkErr();

            gl.glDrawElements( GL_TRIANGLES, mTriCount * 3, GL_UNSIGNED_INT, 0 );
            d.checkErr();

        }

        @Override
        public void reshape( GLAutoDrawable drawable, int i, int i2, int i3, int i4 ) {}
    }


    private static Vao bufferTris( List<DrawTri> tris ) {
        ByteBuffer vb = DrawUtil.alloc( tris.size() * 3 * ( 12 + 12 ) );
        ByteBuffer ib = DrawUtil.alloc( tris.size() * 3 * 4 );
        int ind = 0;

        for( DrawTri tri: tris ) {
            for( int i = 0; i < 3; i++ ) {
                DrawVert vert = tri.mVerts[i];
                if( vert.mVboPos < 0 ) {
                    vert.mVboPos = ind++;
                    Vec.put( vert.mPos, vb );
                    Vec.put( vert.mNorm, vb );
                }
                ib.putInt( vert.mVboPos );
            }
        }

        vb.flip();
        ib.flip();

        Bo vbo = Bo.createArrayBuffer( GL_STATIC_DRAW );
        Bo ibo = Bo.createElementBuffer( GL_STATIC_DRAW );
        vbo.buffer( vb );
        ibo.buffer( ib );

        Vao vao = new Vao( vbo, ibo );
        vao.addAttribute( 0, 3, GL_FLOAT, false, 24,  0 );
        vao.addAttribute( 1, 3, GL_FLOAT, false, 24, 12 );

        return vao;
    }


    private static List<DrawTri> genSphere( double rad, int phiDivs, int thetaDivs ) {
        DrawVert[][] verts = genSphereVerts( rad, phiDivs, thetaDivs );
        List<DrawTri> tris = new ArrayList<DrawTri>();

        for( int i = 0; i < verts.length - 1; i++ ) {
            DrawVert[] top    = verts[i];
            DrawVert[] bottom = verts[i+1];

            final int len = Math.max( top.length, bottom.length );

            for( int j = 0; j < len; j++ ) {
                DrawVert v0 = bottom[ j    % bottom.length ];
                DrawVert v1 = bottom[(j+1) % bottom.length ];
                DrawVert v2 = top   [ j    % top.length    ];
                DrawVert v3 = top   [(j+1) % top.length    ];

                if( v0 != v1 ) {
                    tris.add( new DrawTri( v0, v1, v2 ) );
                }

                if( v2 != v3 ) {
                    tris.add( new DrawTri( v1, v3, v2 ) );
                }
            }
        }

        return tris;
    }


    private static DrawVert[][] genSphereVerts( double rad, int phiDivs, int thetaDivs ) {
        DrawVert[][] ret = new DrawVert[phiDivs+1][];

        for( int row = 0; row <= phiDivs; row++ ) {
            double phi = Ang.PI * row / phiDivs;
            int colNum = thetaDivs;
            if( row == 0 || row == phiDivs ) {
                colNum = 1;
            }
            ret[row] = new DrawVert[colNum];

            for( int col = 0; col < colNum; col++ ) {
                double theta = Ang.TWO_PI * col / colNum;
                double x = rad * Math.cos( theta ) * Math.sin( phi );
                double y = rad * Math.sin( theta ) * Math.sin( phi );
                double z = rad * Math.cos( phi );

                DrawVert vert = new DrawVert( (float)x, (float)y, (float)z );
                vert.mNorm = new Vec3( vert.mPos );
                Vec.normalize( vert.mNorm );

                ret[row][col] = vert;
            }
        }

        return ret;
    }

}
