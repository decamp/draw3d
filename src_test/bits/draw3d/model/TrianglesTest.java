package bits.draw3d.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Philip DeCamp  
 */
public class TrianglesTest {
    
    
    @Test
    public void splitTest1() {
        double[][] p1 = new double[][] {
                {0.0,  0.0,  0.0},
                {1.0,  0.0,  0.0},
                {0.0,  1.0,  0.0}};
        
        double[][] cv = new double[][] {
                {-1.0, -1.0, -1.0},
                {1.0,   1.0, -1.0},
                {-1.0, -1.0, 10.0}};
        
        double[][][] ex = new double[][][] {
            {{0, 0, 0}, {1, 0, 0}, {0.5, 0.5, 0.0}},
            {{0, 0, 0}, {0.5, 0.5, 0}, {0, 1, 0}}
        };

        
        test(p1, cv, ex);
    }
    
    
    @Test
    public void splitTest2() {
        double[][] p1 = new double[][] {
                {0.0,  0.0,  0.0},
                {1.0,  0.0,  0.0},
                {0.0,  1.0,  0.0}};
        
        double[][] cv = new double[][] {
                {-1.0, -1.0, -1.0},
                {1.0,   1.0, -1.0},
                {-1.0, -1.0, 10.0}};

        double[][][] ex = new double[][][] {
                {{1, 0, 0}, {0.505, 0.495, 0.0}, {0.01, 0.0, 0.0}},
                {{0, 1, 0}, {0.01, 0, 0}, {0.505, 0.495, 0}},
                {{0, 0, 0}, {0.01, 0, 0}, {0, 1, 0}}
        };
        
        
        for(int i = 0; i < cv.length; i++) {
            cv[i][0] += 0.01;
        }
        
        System.out.println("\nTest2");
        test(p1, cv, ex);
    }
    
    
    @Test
    public void splitTest3() {
        double[][] p1 = new double[][] {
                {0.0,  0.0,  0.0},
                {1.0,  0.0,  0.0},
                {0.0,  1.0,  0.0}};
        
        double[][] cv = new double[][] {
                {0.1, 0.1, 0.1},
                {0.1, 0.1, -0.1},
                {0.3, 0.1, 0.1}};
        
        double[][][] ex = new double[][][] {
                {{0, 1, 0}, {0, 0.1, 0}, {0.9, 0.1, 0}},
                {{0, 0, 0}, {0.9, 0.1, 0}, {0, 0.1, 0}},
                {{1, 0, 0}, {0.9, 0.1, 0}, {0, 0, 0}}
        };

        
        System.out.println("\nTest3");
        test(p1, cv, ex);
        
    }
    
    
    public static void test(double[][] triArr, double[][] cutArr, double[][][] expectArr) {
        Triangle tri = Triangles.triangleFromVertices(triArr[0], triArr[1], triArr[2], false);
        Triangle cut = Triangles.triangleFromVertices(cutArr[0], cutArr[1], cutArr[2], false); 
        Triangle[] div = Triangles.splitTriangle(tri, cut, 0.0001);

        if(div == null) {
            System.out.println("null");
        }else{
            for(Triangle t: div) {
                System.out.println(t);
            }
        }
        
        assertEquals(expectArr.length, div.length);
        for(int i = 0; i < expectArr.length; i++) {
            for(int j = 0; j < 3; j++) {
                for(int k = 0; k < 3; k++) {
                    assertTrue(Math.abs(div[i].vertex(j)[k] - expectArr[i][j][k]) < 0.0001);
                }
            }
        }
    }

    
}
