package bits.draw3d.pick;

import java.util.Random;


/**
 * @author Philip DeCamp
 */
public class RandomFeature {

    private final double[] mVector;
    
    
    public RandomFeature( int dimCount ) {
        this( dimCount, new Random( System.currentTimeMillis() ) );
    }
    
    public RandomFeature( int dimCount, Random rand ) {
        mVector = new double[dimCount];
        for( int i = 0; i < dimCount; i++ ) {
            mVector[i] = rand.nextDouble();
        }
    }


    public double[] vectorRef() {
        return mVector;
    }

    public double distance( RandomFeature f ) {
        double[] vec = f.vectorRef();

        if( vec.length != mVector.length ) {
            throw new IllegalArgumentException();
        }

        float dist = 0f;

        for( int i = 0; i < mVector.length; i++ ) {
            double d = mVector[i] - vec[i];
            dist += d * d;
        }

        return (float)Math.sqrt( dist );
    }
    
    
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder( "[" );
        for( int i = 0; i < mVector.length; i++ ) {
            if( i > 0 ) {
                s.append( ", " );
            }
            s.append( mVector[i] );
        }
        
        s.append( "]" );
        return s.toString();
    }

}
