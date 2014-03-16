package bits.math3d.actors;

import bits.math3d.*;

/**
 * @author decamp
 */
public class PovLerper {
    
    private final double[][] mWork = new double[3][4];
    
    public PovLerper() {}
    
    
    public void lerp( Pov a, Pov b, double p, Pov out ) {
        Vectors.lerp( a.posRef(), b.posRef(), p, out.posRef() );
        Matrices.slerpRotations( a.rotRef(), b.rotRef(), p, mWork[0], mWork[1], mWork[2], out.rotRef() );
    }
    
}
