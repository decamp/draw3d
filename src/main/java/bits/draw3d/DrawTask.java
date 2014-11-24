/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.draw3d.DrawEnv;


/**
 * @author Philip DeCamp
 */
public interface DrawTask {
    public void run( DrawEnv g );
}
