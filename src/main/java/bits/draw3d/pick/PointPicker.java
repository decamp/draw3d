/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

/**
 * MIT Media Lab
 * Cognitive Machines Group
 */

package bits.draw3d.pick;

/** 
 * @author Philip DeCamp  
 */
public interface PointPicker<P> {
    public PointPickResult<P> newPointPickResult();
    public boolean pick(P point, PointPickResult<P> out);
}
