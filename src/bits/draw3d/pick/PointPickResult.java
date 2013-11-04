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
