/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

/**
 * @author decamp
 */
public final class Side {
    
    public static final int NONE  = 0;
    public static final int FRONT = 1 << 0;
    public static final int BACK  = 1 << 1;
    public static final int BOTH  = FRONT | BACK;
    
    
    private Side() {}
        
}
