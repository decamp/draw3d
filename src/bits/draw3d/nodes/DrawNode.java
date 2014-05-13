package bits.draw3d.nodes;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * Interface for nodes that perform drawing.
 *
 * @author decamp
 */
public interface DrawNode {
    public void init( GLAutoDrawable gld );
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h );
    public void dispose( GLAutoDrawable gld );

    public void pushDraw( GL gl );
    public void popDraw( GL gl );
}
