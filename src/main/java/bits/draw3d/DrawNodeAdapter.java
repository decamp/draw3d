/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

/**
 * @author decamp
 */
public abstract class DrawNodeAdapter implements DrawNode {
    public void init( DrawEnv d ) {}
    public void dispose( DrawEnv d ) {}
    public void pushDraw( DrawEnv d ) {}
    public void popDraw( DrawEnv d ) {}
}
