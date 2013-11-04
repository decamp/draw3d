package bits.draw3d.context;

import bits.math3d.LongRect;

/**
 * @author decamp
 */
public class ConstantBounder implements Bounder {
    
    private LongRect mBounds;
    
    
    public ConstantBounder( LongRect bounds ) {
        mBounds = bounds;
    }
    
    
    public LongRect getBounds() {
        return mBounds;
    }

}
