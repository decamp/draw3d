/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

public interface Animator {
    public void target( float fps );
    public void start();
    public void stop();
    public boolean isRunning();
}
