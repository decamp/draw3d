/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

import java.util.Random;


/**
 * @author Philip DeCamp
 */
class RandomFeature {

    final float[] mVector;
    
    
    public RandomFeature( int dimCount ) {
        this( dimCount, new Random( System.currentTimeMillis() ) );
    }
    
    public RandomFeature( int dimCount, Random rand ) {
        mVector = new float[dimCount];
        for( int i = 0; i < dimCount; i++ ) {
            mVector[i] = rand.nextFloat();
        }
    }


    public float[] vectorRef() {
        return mVector;
    }

    public double distance( RandomFeature f ) {
        float[] vec = f.vectorRef();
        if( vec.length != mVector.length ) {
            throw new IllegalArgumentException();
        }

        double dist = 0f;
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
