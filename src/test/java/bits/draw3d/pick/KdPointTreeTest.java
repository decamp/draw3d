/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;
import static org.junit.Assert.*;


/**
 * @author Philip DeCamp
 */
public class KdPointTreeTest {

    public static void main( String[] args ) {
        //testIterator();
        //runAccuracyTest( 4, 10000, 1000, true );
        //runSpeedTest( 4, 10000, 1000, true );
        //runBalanceTest( 4, 10000, 1000 );
    }

    @Test
    public void testIterator() {
        final int DIM = 3;

        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>( new RandomFeatureComp( DIM ) );
        Random rand = new Random( 1000 );
        for( int i = 0; i < 10; i++ ) {
            tree.add( new RandomFeature( DIM, rand ) );
        }
        
        KdPointTree<RandomFeature> tree2 = new KdPointTree<RandomFeature>( new RandomFeatureComp( DIM ) );
        for( int i = 0; i < 10; i++ ) {
            tree2.add( new RandomFeature( DIM, rand ) );
        }
        
        for( int i = 0; i < 5; i++ ) {
            RandomFeature f = new RandomFeature( DIM, rand );
            tree.add( f );
            tree2.add( f );
        }

        tree.retainAll( tree2 );
        assertEquals( 5, tree.size() );
        assertEquals( 15, tree2.size() );

        tree2.retainAll( tree );
        assertEquals( 5, tree2.size() );
    }

    @Test
    public void testAccuracy() {
        float t = runAccuracyTest( 5, 3000, 400, false );
        assertTrue( t == 1f );
    }

    @Test
    @Ignore
    public void testSpeed() {
        runSpeedTest( 5, 100000, 1000, false );
    }


    @Test
    @Ignore
    public void testBalance() {
        runBalanceTest( 5, 100000, 1000 );
    }


    public static void runSpeedTest( final int dimCount,
                                     int featureCount,
                                     int trialCount,
                                     boolean useApproximateSearch )
    {
        final Random rand = new Random( 1 );
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>( new RandomFeatureComp( dimCount ) );

        RandomFeature[] features = new RandomFeature[featureCount];
        for( int i = 0; i < featureCount; i++ ) {
            features[i] = new RandomFeature( dimCount, rand );
            tree.add( features[i] );
        }

        System.out.println( "Balancing..." );
        tree.optimize();
        System.out.println( "Done..." );
        PointPickResult<RandomFeature> result = tree.newPointPickResult();

        RandomFeature[] trials = new RandomFeature[trialCount];
        for( int i = 0; i < trialCount; i++ ) {
            trials[i] = new RandomFeature( dimCount, rand );
        }

        long time = System.currentTimeMillis();
        for( int i = 0; i < trialCount; i++ ) {
            double minDist = Float.MAX_VALUE;
            for( RandomFeature f : features ) {
                double dist = f.distance( trials[i] );
                if( dist < minDist ) {
                    minDist = dist;
                }
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println( "Exhaustive search time: " + (time / 1000f) + " seconds" );
        time = System.currentTimeMillis();

        if( useApproximateSearch ) {
            for( int i = 0; i < trialCount; i++ ) {
                tree.approximatePick( trials[i], result );
            }
        } else {
            for( int i = 0; i < trialCount; i++ ) {
                tree.pick( trials[i], result );
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println( "Tree search time: " + (time / 1000f) + " seconds" );
    }


    public static void runBalanceTest( final int dimCount,
                                       int featureCount,
                                       int trialCount )
    {
        final Random rand = new Random( System.currentTimeMillis() );
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>( new RandomFeatureComp( dimCount ) );
        PointPickResult<RandomFeature> result = tree.newPointPickResult();

        RandomFeature[] features = new RandomFeature[featureCount];
        for( int i = 0; i < featureCount; i++ ) {
            features[i] = new RandomFeature( dimCount, rand );
            tree.add( features[i] );
        }

        RandomFeature[] trials = new RandomFeature[trialCount];
        for( int i = 0; i < trialCount; i++ ) {
            trials[i] = new RandomFeature( dimCount, rand );
        }

        long time = System.currentTimeMillis();

        for( int i = 0; i < trialCount; i++ ) {
            tree.pick( trials[i], result );
        }

        time = System.currentTimeMillis() - time;
        System.out.println( "Unbalanced search time: " + (time / 1000f) + " seconds" );

        System.out.println( "Balancing..." );
        tree.optimize();
        System.out.println( "Done..." );

        time = System.currentTimeMillis();

        for( int i = 0; i < trialCount; i++ ) {
            tree.pick( trials[i], result );
        }

        time = System.currentTimeMillis() - time;
        System.out.println( "Balanced search time: " + (time / 1000f) + " seconds" );
    }



    public static float runAccuracyTest( final int dimCount,
                                         int featureCount,
                                         int trialCount,
                                         boolean useApproximateSearch )
    {
        final Random rand = new Random( 0 );
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>( new RandomFeatureComp( dimCount ) );

        RandomFeature[] features = new RandomFeature[featureCount];
        for( int i = 0; i < featureCount; i++ ) {
            features[i] = new RandomFeature( dimCount, rand );
            tree.add( features[i] );
        }

        System.out.println( "Balancing..." );
        tree.optimize();
        System.out.println( "Done..." );

        PointPickResult<RandomFeature> result = tree.newPointPickResult();

        RandomFeature[] trials = new RandomFeature[trialCount];
        for( int i = 0; i < trialCount; i++ ) {
            trials[i] = new RandomFeature( dimCount, rand );
        }

        int correct = 0;


        for( int i = 0; i < trialCount; i++ ) {
            // Exhaustive search.
            RandomFeature matchOne = null;
            double minDist = Float.MAX_VALUE;

            for( RandomFeature f : features ) {
                double dist = f.distance( trials[i] );

                if( dist < minDist ) {
                    minDist = dist;
                    matchOne = f;
                }
            }

            // Tree search.
            if( useApproximateSearch ) {
                tree.approximatePick( trials[i], result );
                if( result.hasPick() && result.pickedPoint() == matchOne ) {
                    correct++;
                }
            } else {
                tree.pick( trials[i], result );
                if( result.hasPick() && result.pickedPoint() == matchOne ) {
                    correct++;
                } else {
                    // System.out.println(minDist + "\t" + match.distance());
                    // tree.findNearest(trials[i]);
                }
            }
            // System.out.println(minDist + "\t" + match.distance());
        }

        return (float)correct / trialCount;
    }

}
