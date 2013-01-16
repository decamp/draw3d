package cogmac.draw3d.nodes;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;


/**
 * Manages setup of GL state.
 * 
 * @author decamp
 */
public class InitNode extends DrawNodeAdapter {


    public static InitNode newInstance() {
        return newInstance(0,0,0,1);
    }
    
    
    public static InitNode newInstance( float r, float g, float b, float a ) {
        return new InitNode( r,g,b,a );
    }

    

    private int mClearBits = GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT;
    private final float[] mClearColor = {0,0,0,1};
    
    
    private InitNode( float r, float g, float b, float a ) {
        setClearColor( r, g, b, a );
    }
    
    
    
    public void setClearBits(int bits) {
        mClearBits = bits;
    }

    
    public void addClearBits(int bit) {
        mClearBits |= bit;
    }
    
    
    public void removeClearBits(int bits) {
        mClearBits &= ~bits;
    }
    
    
    public void setClearColor(float r, float g, float b, float a) {
        mClearColor[0] = r;
        mClearColor[1] = g;
        mClearColor[2] = b;
        mClearColor[3] = a;
    }
    
    
    public void init(GLAutoDrawable gld) {
        GL gl = gld.getGL();
        
        gl.glBlendFunc( GL_SRC_ALPHA_SATURATE, GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable(GL_BLEND);
        
        gl.glDepthFunc(GL_LESS);
        gl.glEnable(GL_DEPTH_TEST);
        gl.glClearDepth(1f);
        
        gl.glDisable(GL_LOGIC_OP);
        gl.glClearStencil(0);
        
        gl.glDisable(GL_LINE_SMOOTH);
        gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        
        gl.glDisable(GL_DITHER);
        gl.glDisable(GL_FOG);
        gl.glDisable(GL_LIGHTING);
        gl.glDisable(GL_STENCIL_TEST);
        gl.glDisable(GL_TEXTURE_1D);
        gl.glDisable(GL_TEXTURE_2D);
        
        gl.glAlphaFunc(GL_NOTEQUAL, 0);
        gl.glEnable(GL_ALPHA_TEST);

        gl.glPixelTransferi(GL_MAP_COLOR, GL_FALSE);
        gl.glPixelTransferi(GL_RED_SCALE, 1);
        gl.glPixelTransferi(GL_RED_BIAS, 0);
        gl.glPixelTransferi(GL_GREEN_SCALE, 1);
        gl.glPixelTransferi(GL_GREEN_BIAS, 0);
        gl.glPixelTransferi(GL_BLUE_SCALE, 1);
        gl.glPixelTransferi(GL_BLUE_BIAS, 0);

        gl.glCullFace(GL_BACK);
        gl.glFrontFace(GL_CCW);
        gl.glEnable(GL_CULL_FACE);

        gl.glDrawBuffer(GL_BACK);
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        if(gld.getChosenGLCapabilities().getDoubleBuffered()) {
            gl.glDrawBuffer(GL_BACK);
        }
    }
    
    
    @Override
    public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {
        GL gl = gld.getGL();
        gl.glViewport(x, y, w, h);
    }
    
    
    @Override
    public void pushDraw(GL gl) {
        gl.glClearColor(mClearColor[0], mClearColor[1], mClearColor[2], mClearColor[3]);
        gl.glClear(mClearBits);
    }
    
}
