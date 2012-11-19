package cogmac.draw3d.render;

import javax.media.opengl.*;

import cogmac.draw3d.context.*;
import cogmac.draw3d.nodes.*;
import cogmac.draw3d.scene.SceneGraph;
import cogmac.math3d.LongRect;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class RendererTest {

    
    public static void main(String[] args) {
        testWindow();
    }
    
    
    static void testWindow() {
        RenderSpace sp = RenderSpaceBuilder.newWindowSpace( null, 
                                                            LongRect.fromBounds(100, 100, 600, 600),
                                                            true,
                                                            "Window Test" );
        
        
        SceneGraph g = new SceneGraph();
        InitNode in  = InitNode.newInstance(0,0,0,0);
        g.connectLast(in, new SquareNode());
        
        TileRenderer rend = TileRenderer.newInstance(sp.firstTile(), g);
        
        RenderDriver driver = new RenderDriver(rend, 60.0);
        driver.start();
        
    }

    
    static void testFullscreen() {
        RenderSpace sp = RenderSpaceBuilder.newFullscreenSpace( null,
                                                                null,
                                                                true,
                                                                "Fullscreen Test" );
        
        SceneGraph g = new SceneGraph();
        InitNode in  = InitNode.newInstance(0,0,0,0);
        g.connectLast(in, new SquareNode());
        
        TileRenderer rend = TileRenderer.newInstance(sp.firstTile(), g);
        
        RenderDriver driver = new RenderDriver(rend, 60.0);
        driver.start();        
    }

    
    
    private static final class SquareNode extends DrawNodeAdapter {
        
        @Override
        public void pushDraw(GL gl) {
            gl.glDisable(GL_DEPTH_TEST);
            
            gl.glColor3f(1,0,0);
            gl.glBegin(GL_QUADS);
            gl.glVertex2i(0,0);
            gl.glVertex2i(1,0);
            gl.glVertex2i(1,1);
            gl.glVertex2i(0,1);
            gl.glEnd();
            
            gl.glColor3f(1,1,0);
            gl.glBegin(GL_QUADS);
            gl.glVertex2f(0.2f, 0.2f);
            gl.glVertex2f(0.8f, 0.2f);
            gl.glVertex2f(0.8f, 0.8f);
            gl.glVertex2f(0.2f, 0.8f);
            gl.glEnd();
            
        }
        
    }
    
}
