package bits.draw3d.util;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import static javax.media.opengl.GL.*;

import com.sun.opengl.util.GLUT;


/**
 * @author decamp
 */
public class DrawUtil {

    public static final GLU  GLU  = new GLU();
    public static final GLUT GLUT = new GLUT();


    public static void pushIdentity( GL gl ) {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPushMatrix();
        gl.glLoadIdentity();
    }


    public static void popStacks( GL gl ) {
        gl.glMatrixMode( GL_PROJECTION );
        gl.glPopMatrix();
        gl.glMatrixMode( GL_MODELVIEW );
        gl.glPopMatrix();
    }


    public static void drawNormQuad( GL gl ) {
        gl.glBegin( GL_QUADS );
        gl.glTexCoord2i( 0, 0 );
        gl.glVertex2i( -1, -1 );
        gl.glTexCoord2i( 1, 0 );
        gl.glVertex2i( 1, -1 );
        gl.glTexCoord2i( 1, 1 );
        gl.glVertex2i( 1, 1 );
        gl.glTexCoord2i( 0, 1 );
        gl.glVertex2i( -1, 1 );
        gl.glEnd();
    }

}
