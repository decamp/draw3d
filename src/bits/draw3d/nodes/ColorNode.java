package bits.draw3d.nodes;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class ColorNode implements DrawNode {
    
    
    private final float[] mRevert;
    private float mRed;
    private float mGreen;
    private float mBlue;
    private float mAlpha;
    
    
    public ColorNode() {
        this(0,0,0,1,false);
    }
    
    public ColorNode(float r, float g, float b, float a) {
        this(r, g, b, a, false);
    }
    
    public ColorNode(boolean revert) {
        this(0,0,0,1, revert);
    }
    
    public ColorNode(float r, float g, float b, float a, boolean revert) {
        mRed    = r;
        mGreen  = g;
        mBlue   = b;
        mAlpha  = a;
        mRevert = revert ? new float[4] : null;
    }

    
    public void setColor(float r, float g, float b, float a) {
        mRed   = r;
        mGreen = g;
        mBlue  = b;
        mAlpha = a;
    }

    
    public void init(GLAutoDrawable gld) {}

    public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {}

    public void dispose(GLAutoDrawable gld) {}

    public void pushDraw(GL gl) {
        if(mRevert != null) {
            gl.glGetFloatv(GL_CURRENT_COLOR, mRevert, 0);
        }
        
        gl.glColor4f(mRed, mGreen, mBlue, mAlpha);
    }

    public void popDraw(GL gl) {
        if(mRevert != null) {
            gl.glColor4fv(mRevert, 0);
        }
    }
    
}
