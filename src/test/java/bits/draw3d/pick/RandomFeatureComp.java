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
