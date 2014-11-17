package bits.math3d.geom;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import bits.util.gui.ImagePanel;

import bits.math3d.Vec;
import org.junit.Test;
import static org.junit.Assert.*;



/**
 * @author decamp
 */
@SuppressWarnings( "deprecation" )
public class AabbTest {

    @Test public void testClipCenterOfTriangle() throws Exception {
        double[] clip = { 0.0, 0.0, 0.0, 10.0, 10.0, 10.0 };
        double[] v0 = { 10.0, 0.0, 5.0 };
        double[] v1 = { 10.0, 5.0, 10.0 };
        double[] v2 = { 5.0, 0.0, 10.0 };
        double[][] v = { v0, v1, v2 };

        // Inflate triangle.
        {

            double[] c = { 0, 0, 0 };

            for( int i = 0; i < 3; i++ ) {
                for( int j = 0; j < 3; j++ ) {
                    c[j] += v[i][j] / 3.0;
                }
            }

            double inf = 1.2;

            for( int i = 0; i < 3; i++ ) {
                for( int j = 0; j < 3; j++ ) {
                    double d = v[i][j] - c[j];
                    v[i][j] = c[j] + d * inf;
                }
            }
        }

        Loop loop = new Loop();

        for( int m = 0; m < 8; m++ ) {
            double[][] vv = mirror( v, m, clip );

            for( int i = 0; i < 3; i++ ) {
                assertTrue( Aabbs.clipPlanarToAabb( vv, 0, 3, clip, loop ) );
                assertEquals( 3, loop.mLength );
                // graph(clip, loop);
                rotate( vv, 3 );
            }
        }

        // Thread.sleep(100000L);
    }


    @Test public void testClipTriangleOneVertexOut() {
        double[] clip = { 0.0, 0.0, 0.0, 10.0, 10.0, 10.0 };
        double[] v0 = { 5, 5, 5 };
        double[] v1 = { 15, 5, 5 };
        double[] v2 = { 6, 5, 7 };
        double[][] v = { v0, v1, v2 };

        Loop loop = new Loop();

        for( int m = 0; m < 8; m++ ) {
            double[][] vv = mirror( v, m, clip );

            for( int i = 0; i < 3; i++ ) {
                assertTrue( Aabbs.clipPlanarToAabb( vv, 0, 3, clip, loop ) );
                assertEquals( 4, loop.mLength );
                // graph(clip, loop);
                rotate( vv, 3 );
            }
        }
    }


    @Test public void testClipTriangleTwoVerticesOut() {
        double[] clip = { 0.0, 0.0, 0.0, 10.0, 10.0, 10.0 };
        double[] v0 = { 5, 5, 15 };
        double[] v1 = { 15, 5, 5 };
        double[] v2 = { 6, 5, 7 };
        double[][] v = { v0, v1, v2 };

        Loop loop = new Loop();

        for( int m = 0; m < 8; m++ ) {
            double[][] vv = mirror( v, m, clip );

            for( int i = 0; i < 3; i++ ) {
                assertTrue( Aabbs.clipPlanarToAabb( vv, 0, 3, clip, loop ) );
                assertEquals( 4, loop.mLength );
                // graph(clip, loop);
                rotate( vv, 3 );
            }
        }
    }


    @Test public void testClipTriangleAllIn() throws InterruptedException {
        double[] clip = { 0.0, 0.0, 0.0, 10.0, 10.0, 10.0 };
        double[] v0 = { 5, 5, 5 };
        double[] v1 = { 7, 5, 5 };
        double[] v2 = { 6, 5, 7 };
        double[][] v = { v0, v1, v2 };

        Loop loop = new Loop();

        for( int m = 0; m < 8; m++ ) {
            double[][] vv = mirror( v, m, clip );

            for( int i = 0; i < 3; i++ ) {
                assertTrue( Aabbs.clipPlanarToAabb( vv, 0, 3, clip, loop ) );
                assertEquals( 3, loop.mLength );
                // graph(clip, loop);
                rotate( vv, 3 );
            }
        }
    }
    

    @Test public void testAllOut() {
        double[] clip = { 0.0, 0.0, 0.0, 10.0, 10.0, 10.0 };
        double[] v0 = { 10.0, 0.0, 5.0 };
        double[] v1 = { 10.0, 5.0, 10.0 };
        double[] v2 = { 5.0, 0.0, 10.0 };
        double[][] v = { v0, v1, v2 };

        // Translate triangle.
        {

            double[] c = { 0, 0, 0 };

            for( int i = 0; i < 3; i++ ) {
                for( int j = 0; j < 3; j++ ) {
                    c[j] += v[i][j] / 3.0;
                }
            }

            double dist = Vec.len3( c ) * 1.2;
            c[0] = (10.0 - c[0]) * dist;
            c[1] = (0.0 - c[1]) * dist;
            c[2] = (10.0 - c[2]) * dist;

            for( int i = 0; i < 3; i++ ) {
                for( int j = 0; j < 3; j++ ) {
                    v[i][j] += c[j];
                }
            }
        }

        Loop loop = new Loop();

        for( int m = 0; m < 8; m++ ) {
            double[][] vv = mirror( v, m, clip );

            for( int i = 0; i < 3; i++ ) {
                assertFalse( Aabbs.clipPlanarToAabb( vv, 0, 3, clip, loop ) );
                assertEquals( 0, loop.mLength );
                rotate( vv, 3 );
            }
        }
    }



    private static void rotate( double[][] v, int len ) {
        if( len == 0 ) {
            return;
        }

        double[] temp = v[0];
        for( int i = 1; i < len; i++ ) {
            v[i - 1] = v[i];
        }

        v[len - 1] = temp;
    }


    private static double[][] mirror( double[][] v, int axes, double[] clip ) {
        double[][] ret = new double[v.length][];

        for( int i = 0; i < v.length; i++ ) {
            ret[i] = v[i].clone();
        }

        for( int axis = 0; axis < 3; axis++ ) {
            if( (axes & (1 << axis)) == 0 ) {
                continue;
            }

            double cent = (clip[axis] + clip[axis + 3]) * 0.5;

            for( int i = 0; i < v.length; i++ ) {
                ret[i][axis] = 2.0 * cent - ret[i][axis];
            }
        }

        return ret;
    }


    @SuppressWarnings( "unused" )
    private static void graph( double[] clip, Loop loop ) {
        final int w = 1024;
        final int h = 1024;

        BufferedImage im = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)im.getGraphics();

        g.setColor( Color.WHITE );
        g.fillRect( 0, 0, 1024, 1024 );
        g.setColor( Color.BLACK );

        double min = clip[0] - 10.0;
        double max = clip[3] + 10.0;
        double span = max - min;
        
        // AffineTransform aff = AffineTransform.getTranslateInstance(-clip[0],
        // -clip[3]);
        // aff.scale(w / (clip[3] - clip[0] + 20.0), h / (clip[5] - clip[2] +
        // 20.0));

        AffineTransform aff = AffineTransform.getScaleInstance( w / (clip[3] - clip[0] + 20.0),
                                                                h / (clip[5] - clip[2] + 20.0) );
        aff.translate( -clip[0] + 10.0, -clip[3] + 20.0 );

        g.setTransform( aff );
        g.setStroke( new BasicStroke( (float)(1f * (clip[3] - clip[0]) / w) ) );

        {
            g.draw( new Rectangle2D.Double( clip[0], clip[2], clip[3] - clip[0], clip[5] - clip[2] ) );
        }

        g.setColor( Color.RED );

        for( int i = 0; i < loop.mLength; i++ ) {
            double[] a = loop.mVerts[i];
            double[] b = loop.mVerts[(i + 1) % loop.mLength];

            g.draw( new Line2D.Double( a[0], a[2], b[0], b[2] ) );
        }

        ImagePanel.showImage( im );

    }

}
