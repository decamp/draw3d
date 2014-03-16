package bits.math3d.actors;

import java.io.IOException;
import bits.blob.Blob;
import bits.math3d.*;


/**
 * @author decamp
 */
public class Pov {

    public static Pov fromBlob( Blob blob ) throws IOException {
        Pov pov = new Pov();
        double[] pos = pov.mPos;
        double[] rot = pov.mRot;
        
        for( int i = 0; i < 3; i++ ) {
            Double v = blob.getDouble( "position", i );
            if( v == null ) {
                throw new IOException( "Invalid pov format" );
            }
            pos[i] = v;
        }

        for( int i = 0; i < 16; i++ ) {
            Double v = blob.getDouble( "rotation", i );
            if( v == null ) {
                throw new IOException( "Invalid gaze format" );
            }
            rot[i] = v;
        }
        
        return pov;
    }
    
    
    public static Pov fromSpatial( SpatialObject s ) {
        return new Pov( s.mPos, s.mRot );
    }
    
    
    private final double[] mPos;
    private final double[] mRot;


    public Pov() {
        mPos = new double[3];
        mRot = new double[16];
    }

    public Pov( double[] pos, double[] rot ) {
        mPos = pos.clone();
        mRot = rot.clone();
    }

    public Pov( Pov copy ) {
        this( copy.posRef(), copy.rotRef() );
    }
    

    public double[] posRef() {
        return mPos;
    }

    public double[] rotRef() {
        return mRot;
    }

    public void apply( SpatialObject s ) {
        System.arraycopy( mPos, 0, s.mPos, 0, 3 );
        System.arraycopy( mRot, 0, s.mRot, 0, 16 );
    }

    public void setTo( Pov pov ) {
        System.arraycopy( pov.mPos, 0, mPos, 0, 3 );
        System.arraycopy( pov.mRot, 0, mRot, 0, 16 );
    }



    public Blob toBlob() {
        Blob ret = new Blob();

        for( int i = 0; i < 3; i++ ) {
            ret.put( "position", i, mPos[i] );
        }

        for( int i = 0; i < 16; i++ ) {
            ret.put( "rotation", i, mRot[i] );
        }

        return ret;
    }

    public void assertValid() {
        assert (Vectors.isValid( mPos ));
        assert (Matrices.isValid( mRot ));
    }

}
