/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

/**
 * Interface for nodes that perform drawing.
 *
 * @author decamp
 */
public interface DrawNode extends DrawResource {
    void pushDraw( DrawEnv d );
    void popDraw( DrawEnv d );
}
