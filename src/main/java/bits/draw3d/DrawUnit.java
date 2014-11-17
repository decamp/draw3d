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
    public void bind( DrawEnv d );
    public void unbind( DrawEnv d );
}
