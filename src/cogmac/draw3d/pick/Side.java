package cogmac.draw3d.pick;

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
