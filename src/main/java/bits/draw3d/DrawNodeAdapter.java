package bits.draw3d;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNode;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * @author decamp
 */
public abstract class DrawNodeAdapter implements DrawNode {

    public void init( DrawEnv d ) {}

    public void dispose( DrawEnv d ) {}

    public void pushDraw( DrawEnv d ) {}

    public void popDraw( DrawEnv d ) {}

}
