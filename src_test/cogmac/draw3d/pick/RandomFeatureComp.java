package cogmac.draw3d.pick;

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
