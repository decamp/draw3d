/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.math3d.Vec;
import bits.math3d.Vec4;


/**
 * @author Philip DeCamp
 */
public class FogParams {
    public       boolean mOn      = false;
    public final Vec4    mColor   = new Vec4( 0, 0, 0, 0 );
    public       float   mStart   = 0f;
    public       float   mDensity = 0f;

    public FogParams() {}

    public FogParams( FogParams copy ) {
        mOn = copy.mOn;
        Vec.put( copy.mColor, mColor );
        mStart = copy.mStart;
        mDensity = copy.mDensity;
    }
}
