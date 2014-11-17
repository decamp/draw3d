package bits.draw3d.pick;

/**
 * @author decamp
 */
public interface DimComparator<T> {
    
    public int dimCount(T item);
    public double value(T item, int dim);
    
    
    public static final DimComparator<double[]> DOUBLE_ARRAY_INSTANCE = new DimComparator<double[]>() {
        public int dimCount(double[] item) {
            return item.length;
        }
        
        public double value(double[] item, int dim) {
            return item[dim];
        }
        
    };
    
}
