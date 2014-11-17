/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

/**
 * @author decamp
 */
public interface PointPickResult<P> {

    public boolean hasPick();
    
    public double pickedDistance();
    
    public P pickedPoint();
    
    public Object pickedData(); 
    
}
