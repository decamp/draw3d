package bits.draw3d.pick;

import java.util.Random;

/** 
 * @author Philip DeCamp  
 */
public class KdPointTreeTest {
    
    
    public static void main(String[] args) {
        runIteratorTest();

        runAccuracyTest(20, 10000, 1000, true);
        runSpeedTest(20, 10000, 1000, true);
        runBalanceTest(20, 10000, 1000, true);
    }

    
    
    public static void runIteratorTest() {
        
        
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>(new RandomFeatureComp());
        Random rand = new Random(System.currentTimeMillis());
        for(int i = 0; i < 10; i++){
            tree.add(new RandomFeature(3, rand));
        }


        KdPointTree<RandomFeature> tree2 = new KdPointTree<RandomFeature>(new RandomFeatureComp());
        for(int i = 0; i < 10; i++){
            tree2.add(new RandomFeature(3, rand));
        }


        for(int i = 0; i < 5; i++){
            RandomFeature f = new RandomFeature(3, rand);
            tree.add(f);
            tree2.add(f);
        }

        System.out.println(tree.hashCode());
        System.out.println(tree2.hashCode());
        System.out.println(tree.equals(tree2));

        tree.retainAll(tree2);

        for(RandomFeature f: tree) {
            System.out.println(f);
        }


        System.out.println(tree.hashCode());
        System.out.println(tree2.hashCode());
        System.out.println(tree.equals(tree2));


        tree2.retainAll(tree);

        System.out.println(tree.hashCode());
        System.out.println(tree2.hashCode());
        System.out.println(tree.equals(tree2));

    }


    public static void runAccuracyTest(final int dimCount, int featureCount, int trialCount, boolean useApproximateSearch) {
        final Random rand = new Random(System.currentTimeMillis());
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>(new RandomFeatureComp());

        RandomFeature[] features = new RandomFeature[featureCount];
        for(int i = 0; i < featureCount; i++){
            features[i] = new RandomFeature(dimCount, rand);
            tree.add(features[i]);
        }

        System.out.println("Balancing...");
        tree.optimize();
        System.out.println("Done...");
        
        PointPickResult<RandomFeature> result = tree.newPointPickResult();
        
        RandomFeature[] trials = new RandomFeature[trialCount];
        for(int i = 0; i < trialCount; i++){
            trials[i] = new RandomFeature(dimCount, rand);
        }

        int correct = 0;


        for(int i = 0; i < trialCount; i++){
            //Exhaustive search.
            RandomFeature matchOne = null;
            double minDist = Float.MAX_VALUE;

            for(RandomFeature f: features) {
                double dist = f.distance(trials[i]);

                if(dist < minDist){
                    minDist = dist;
                    matchOne = f;
                }
            }


            //Tree search.
            if(useApproximateSearch) {
                tree.approximatePick(trials[i], result);
                if(result.hasPick() && result.pickedPoint() == matchOne) {
                    correct++;
                }

            }else{
                tree.pick(trials[i], result);
                
                if(result.hasPick() && result.pickedPoint() == matchOne) {
                    correct++;
                }else{
                    //System.out.println(minDist + "\t" + match.distance());
                    //tree.findNearest(trials[i]);
                }
            }
            //System.out.println(minDist + "\t" + match.distance());
        }

        System.out.println("CORRECT: " + correct + " out of " + trialCount + ".  " + (100f * (double)correct / trialCount) + "%");
    }


    public static void runSpeedTest(final int dimCount, int featureCount, int trialCount, boolean useApproximateSearch) {
        final Random rand = new Random(System.currentTimeMillis());
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>(new RandomFeatureComp());

        RandomFeature[] features = new RandomFeature[featureCount];
        for(int i = 0; i < featureCount; i++){
            features[i] = new RandomFeature(dimCount, rand);
            tree.add(features[i]);
        }

        System.out.println("Balancing...");
        tree.optimize();
        System.out.println("Done...");
        PointPickResult<RandomFeature> result = tree.newPointPickResult();

        RandomFeature[] trials = new RandomFeature[trialCount];
        for(int i = 0; i < trialCount; i++){
            trials[i] = new RandomFeature(dimCount, rand);
        }


        long time = System.currentTimeMillis();

        for(int i = 0; i < trialCount; i++){
            RandomFeature matchOne = null;
            double minDist = Float.MAX_VALUE;

            for(RandomFeature f: features){
                double dist = f.distance(trials[i]);

                if(dist < minDist){
                    minDist = dist;
                    matchOne = f;
                }
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Exhaustive search time: " + (time / 1000f) + " seconds");
        time = System.currentTimeMillis();

        if(useApproximateSearch){
            for(int i = 0; i < trialCount; i++) {
                tree.approximatePick(trials[i], result);
            }
        }else{
            for(int i = 0; i < trialCount; i++){
                tree.pick(trials[i], result);
            }
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Tree search time: " + (time / 1000f) + " seconds");
    }


    public static void runBalanceTest(final int dimCount, int featureCount, int trialCount, boolean useApproximateSearch) {
        final Random rand = new Random(System.currentTimeMillis());
        KdPointTree<RandomFeature> tree = new KdPointTree<RandomFeature>(new RandomFeatureComp());
        PointPickResult<RandomFeature> result = tree.newPointPickResult();
        
        RandomFeature[] features = new RandomFeature[featureCount];
        for(int i = 0; i < featureCount; i++){
            features[i] = new RandomFeature(dimCount, rand);
            tree.add(features[i]);
        }

        RandomFeature[] trials = new RandomFeature[trialCount];
        for(int i = 0; i < trialCount; i++){
            trials[i] = new RandomFeature(dimCount, rand);
        }

        long time = System.currentTimeMillis();

        for(int i = 0; i < trialCount; i++) {
            tree.pick(trials[i], result);
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Unbalanced search time: " + (time / 1000f) + " seconds");


        System.out.println("Balancing...");
        tree.optimize();
        System.out.println("Done...");

        time = System.currentTimeMillis();

        for(int i = 0; i < trialCount; i++) {
            tree.pick(trials[i], result);
        }

        time = System.currentTimeMillis() - time;
        System.out.println("Balanced search time: " + (time / 1000f) + " seconds");
    }


}
