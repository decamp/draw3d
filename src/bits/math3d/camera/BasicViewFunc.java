package bits.math3d.camera;

import bits.math3d.Matrices;
import bits.math3d.actors.*;

public class BasicViewFunc implements ViewFunc {

    private final double[] mActorToCameraMat;
    private final double[][] mWork = new double[2][16];
    
    
    public BasicViewFunc() {
        this( ActorCoords.newActorToGlMatrix() );
    }
    
    
    public BasicViewFunc( double[] actorToCameraMat ) {
        mActorToCameraMat = actorToCameraMat;
    }
    
    
    
    @Override
    public void computeViewMat( SpatialObject camera, double[] out ) {
        if( mActorToCameraMat == null ) {
            Matrices.invert( camera.mRot, mWork[0] );
        } else {
            Matrices.invert( camera.mRot, mWork[1] );
            Matrices.multMatMat( mActorToCameraMat, mWork[1], mWork[0] );
        }
            
        double[] pos = camera.mPos;
        Matrices.computeTranslationMatrix( -pos[0], -pos[1], -pos[2], mWork[1] );
        Matrices.multMatMat( mWork[0], mWork[1], out );
    }

}
