package cogmac.draw3d.nodes;

import javax.media.opengl.GL;

import cogmac.draw3d.nodes.DrawNodeAdapter;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class FogNode extends DrawNodeAdapter {
    
    
    public static FogNode newInstance() {
        return new FogNode();
    }
    
    
    private int[] mMode      = {GL_LINEAR};
    private float[] mDensity = {1f};
    private float[] mStart   = {0f};
    private float[] mEnd     = {1f};
    private float[] mColor   = {0,0,0,1};
    
    private FogNode() {}
    
    
    
    public void mode(int mode) {
        mMode[0] = mode;
    }
    
    public void density(float density) {
        mDensity[0] = density;
    }
    
    public void start(float start) {
        mStart[0] = start;
    }
    
    public void end(float end) {
        mEnd[0] = end;
    }
    
    public void color(float r, float g, float b, float a) {
        mColor[0] = r;
        mColor[1] = g;
        mColor[2] = b;
        mColor[3] = a;
    }
    
    
    
    public void pushDraw(GL gl) {
        gl.glEnable(GL_FOG);
        gl.glFogiv(GL_FOG_MODE, mMode, 0);
        gl.glFogfv(GL_FOG_DENSITY, mDensity, 0);
        gl.glFogfv(GL_FOG_START, mStart, 0);
        gl.glFogfv(GL_FOG_END, mEnd, 0);
        gl.glFogfv(GL_FOG_COLOR, mColor, 0);
    }

    
    public void popDraw(GL gl) {
        gl.glDisable(GL_FOG);
    }
    

}
