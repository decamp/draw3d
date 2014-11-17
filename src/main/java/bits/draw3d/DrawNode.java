/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * Interface for nodes that perform drawing.
 *
 * @author decamp
 */
public interface DrawNode extends DrawResource {
    public void pushDraw( DrawEnv d );
    public void popDraw( DrawEnv d );
}
