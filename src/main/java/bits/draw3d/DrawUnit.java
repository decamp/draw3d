/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;


/**
 * @author Philip DeCamp
 */
public interface DrawUnit extends DrawResource {
    void bind( DrawEnv d );
    void unbind( DrawEnv d );
}
