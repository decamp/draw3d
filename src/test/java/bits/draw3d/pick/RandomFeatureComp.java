/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

import bits.draw3d.pick.DimComparator;

/**
 * @author decamp
 */
public class RandomFeatureComp implements DimComparator<RandomFeature> {
    
    public int dimCount(RandomFeature f) {
        return f.vectorRef().length;
    }
    
    public double value(RandomFeature f, int dim) {
        return f.vectorRef()[dim];
    }

}
