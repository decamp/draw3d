package bits.draw3d.pick;

import java.io.*;
import java.util.*;

import org.junit.*;

import bits.draw3d.model.*;
import bits.draw3d.pick.*;
import bits.math3d.geom.*;
import bits.prototype.Timer;

import static org.junit.Assert.*;


/**
 * @author decamp
 */
@SuppressWarnings("all")
public class KdTreeTest {

    private static final String MODEL_PATH = "resources/debhouse/debhouse.obj";
    

    @Test
    public void validatePickPointsTest() throws IOException {
        List<Triangle> tris = newGeometry();
        Aabb bounds = Triangles.computeBounds(tris);
        
        System.out.println("Triangle Count: " + tris.size());
        
        List<RayPicker> pickerList = newTreeInstances(tris);
        List<RayPickResult> resultList = new ArrayList<RayPickResult>();
        
        for(RayPicker gp: pickerList)
            resultList.add(gp.newRayPickResult());
        
        
        bounds = bounds.inflate(1.5, 1.5, 1.5);
        double[] boundSpan = new double[]{bounds.spanX(), bounds.spanY(), bounds.spanZ()};
        double[] boundMin  = new double[]{bounds.minX(),  bounds.minY(),  bounds.minZ() };
        
        Random rand = new Random();
        
        for(int i = 0; i < 10000; i++) {
            //System.out.println(i);
            
            double[] pos = new double[3];
            double[] dir = new double[3];
            
            for(int j = 0; j < 3; j++) {
                pos[j] = rand.nextDouble() * boundSpan[j] + boundMin[j];
                dir[j] = rand.nextDouble() * boundSpan[j] + boundMin[j] - pos[j];
            }
        
            pickerList.get(0).pick(pos, dir, Side.BOTH, resultList.get(0));
            
            for(int j = 1; j < pickerList.size(); j++) {
                pickerList.get(j).pick(pos, dir, Side.BOTH, resultList.get(j));
                
                RayPickResult r0 = resultList.get(0);
                RayPickResult r1 = resultList.get(j);
                
                assertTrue(r0.hasPick() == r1.hasPick());
                
                if(r0.hasPick()) {
                    assertNear(r0.pickedPointRef(), r1.pickedPointRef(), 0.01);
                }
            } 
        }
    }


    @Test
    public void pickSpeedTest() throws IOException {
        List<Triangle> tris = newGeometry();
        Aabb bounds = Triangles.computeBounds(tris);
        
        bounds = bounds.inflate(1.5, 1.5, 1.5);
        double[] boundSpan = new double[]{bounds.spanX(), bounds.spanY(), bounds.spanZ()};
        double[] boundMin  = new double[]{bounds.minX(),  bounds.minY(),  bounds.minZ() };

        List<RayPicker> pickerList = newTreeInstances(tris);
        
        for(RayPicker picker: pickerList) {
            RayPickResult result = picker.newRayPickResult();
            
            Random rand = new Random(0);
        
            final int PICK_COUNT = 100000;
            System.out.println("PICK TIME for " + picker.getClass().getName() + " for " + PICK_COUNT + " picks");
            Timer.start();

            for(int i = 0; i < PICK_COUNT ; i++) {

                double[] pos = new double[3];
                double[] dir = new double[3];

                for(int j = 0; j < 3; j++) {
                    pos[j] = rand.nextDouble() * boundSpan[j] + boundMin[j];
                    dir[j] = rand.nextDouble() * boundSpan[j] + boundMin[j] - pos[j];
                }

                picker.pick(pos, dir, Side.BOTH, result);
            }

            Timer.printSeconds("Seconds: ");
        }
    }


    @Test
    public void buildSpeedTest() throws IOException {
        List<Triangle> tris = newGeometry();
        Aabb bounds = Triangles.computeBounds(tris);
        final int BUILD_COUNT = 10;
        
        System.out.println("BUILD TIME for " + BruteForcePicker.class.getName() + " for " + BUILD_COUNT + " builds on " + tris.size() + " triangles:");
        Timer.start();
        
        for(int i = 0; i < BUILD_COUNT; i++) {
            BruteForcePicker.build(tris);
        }
        
        Timer.printSeconds("Seconds: ");
        
        
        System.out.println("BUILD TIME for " + MedianVolumeKdTree.class.getName() + " for " + BUILD_COUNT + " builds on " + tris.size() + " triangles:");
        Timer.start();
        
        for(int i = 0; i < BUILD_COUNT; i++) {
            MedianVolumeKdTree.build(tris);
        }
        
        Timer.printSeconds("Seconds: ");
        
        
        System.out.println("BUILD TIME for " + NaiveSahKdTree.class.getName() + " for " + BUILD_COUNT + " builds on " + tris.size() + " triangles:");
        Timer.start();
        
        for(int i = 0; i < BUILD_COUNT; i++) {
            NaiveSahKdTree.build(tris);
        }
        
        Timer.printSeconds("Seconds: ");
        
        
        System.out.println("BUILD TIME for " + FasterSahKdTree.class.getName() + " for " + BUILD_COUNT + " builds on " + tris.size() + " triangles:");
        Timer.start();
        
        for(int i = 0; i < BUILD_COUNT; i++) {
            FasterSahKdTree.build(tris);
        }
        
        Timer.printSeconds("Seconds: ");
        
        
        System.out.println("BUILD TIME for " + KdTriangleTree.class.getName() + " for " + BUILD_COUNT + " builds on " + tris.size() + " triangles:");
        Timer.start();
        
        for(int i = 0; i < BUILD_COUNT; i++) {
            KdTriangleTree.build(tris);
        }
        
        Timer.printSeconds("Seconds: ");
    }
    
    
    @Ignore
    @Test
    public void arrayVersusListSortTest() {
        final int count = 1000000;

        for(int t = 0; t < 10; t++) {
            Random rand = new Random(0);
            Set<Integer> set = new HashSet<Integer>();
            List<Integer> list = new ArrayList<Integer>(count);
            Integer[] arr = new Integer[count];
            int size = 0;

            for(int i = 0; i < 1000000; i++) {
                Integer n = rand.nextInt();
                if(set.add(n)) {
                    list.add(n);
                    arr[size++] = n;
                }
            }

            set.clear();

            Timer.start();
            Collections.sort(list);
            Timer.printSeconds("Time to sort ArrayList: ");
            
            Timer.start();
            Arrays.sort(arr, 0, size);
            Timer.printSeconds("Time to sort Array: ");
        }
    }    

    
    
    private static List<Triangle> newGeometry() throws IOException {
        MeshModel model = ModelIO.read(new File(MODEL_PATH));
        return model.getAllTriangles(null);
    }
    
    
    private static List<RayPicker> newTreeInstances(List<Triangle> tris) throws IOException {
        List<RayPicker> ret = new ArrayList<RayPicker>();
        
        ret.add(BruteForcePicker.build(tris));
        ret.add(MedianVolumeKdTree.build(tris));
        ret.add(NaiveSahKdTree.build(tris));
        ret.add(FasterSahKdTree.build(tris));
        ret.add(KdTriangleTree.build(tris));
        
        return ret;
    }
    
    
    private static void assertNear(double[] x, double[] y, double tol) {
        for(int i = 0; i < x.length; i++) {
            assertTrue(Math.abs(x[i] - y[i]) < tol);
        }
    }
    
}

