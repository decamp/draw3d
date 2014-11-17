package bits.draw3d.pick;

import java.io.*;
import java.util.*;

import bits.draw3d.geom.*;
import bits.math3d.*;
import org.junit.*;
import static org.junit.Assert.*;

import bits.draw3d.model.io.ModelIO;


/**
 * @author decamp
 */
@SuppressWarnings( "all" )
public class KdTreeTest {

    private static final String MODEL_PATH = "../test/resources/debhouse.obj";


    @Test public void validatePickPointsTest() throws IOException {
        List<DrawTri> tris = newGeometry();
        Box3 bounds = new Box3();
        GeomUtil.computeBounds( GeomUtil.vertIterator( tris ), bounds );

        System.out.println( "Triangle Count: " + tris.size() + "\t" + bounds );

        List<RayPicker> pickerList = newTreeInstances( tris );
        List<RayPickResult> resultList = new ArrayList<RayPickResult>();
        for( RayPicker gp : pickerList ) {
            resultList.add( gp.newRayPickResult() );
        }

        Box.inflate( bounds, 1.5f, 1.5f, 1.5f, bounds );
        float[] boundSpan = { bounds.x1 - bounds.x0, bounds.y1 - bounds.y0, bounds.z1 - bounds.z0 };
        float[] boundMin  = { bounds.x0, bounds.y0, bounds.z0 };

        Random rand = new Random( 1 );
        Vec3 pos = new Vec3();
        Vec3 dir = new Vec3();

        for( int i = 0; i < 5000; i++ ) {
            pos.x = rand.nextFloat() * boundSpan[0] + boundMin[0];
            pos.y = rand.nextFloat() * boundSpan[1] + boundMin[1];
            pos.z = rand.nextFloat() * boundSpan[2] + boundMin[2];
            dir.x = rand.nextFloat() * boundSpan[0] + boundMin[0] - pos.x;
            dir.y = rand.nextFloat() * boundSpan[1] + boundMin[1] - pos.y;
            dir.z = rand.nextFloat() * boundSpan[2] + boundMin[2] - pos.z;

            //System.out.println( pos + " ->  " + dir );
            pickerList.get( 0 ).pick( pos, dir, Side.BOTH, resultList.get( 0 ) );

            for( int j = 1; j < pickerList.size(); j++ ) {
                //System.out.println( pos + " ->  " + dir );

                pickerList.get( j ).pick( pos, dir, Side.BOTH, resultList.get( j ) );
                RayPickResult r0 = resultList.get( 0 );
                RayPickResult r1 = resultList.get( j );

                assertTrue( r0.hasPick() == r1.hasPick() );

                if( r0.hasPick() ) {
                    if( !assertNear( r0.pickedPointRef(), r1.pickedPointRef(), 0.01f ) ) {
                        System.out.println( "### FAIL" );
                        System.out.println( r0.pickedTriangle() == r1.pickedTriangle() );
                        System.out.println( r0.pickedPoint() + "\t" + r1.pickedPoint() );
                        System.out.println( r0.pickedSide() + "\t" + r1.pickedSide() );

                        System.out.println( r0.pickedParamDistance() + "\t" + r1.pickedParamDistance() );
                        assertTrue( false );
                    }
                }
            }
        }
    }


    @Ignore @Test public void pickSpeedTest() throws IOException {
        List<DrawTri> tris = newGeometry();
        Box3 bounds = new Box3();
        GeomUtil.computeBounds( GeomUtil.vertIterator( tris ), bounds );

        Box.inflate( bounds, 1.5f, 1.5f, 1.5f, bounds );
        float[] boundSpan = { bounds.x1 - bounds.x0, bounds.y1 - bounds.y0, bounds.z1 - bounds.z0 };
        float[] boundMin  = { bounds.x0, bounds.y0, bounds.z0 };

        List<RayPicker> pickerList = newTreeInstances( tris );

        for( RayPicker picker : pickerList ) {
            RayPickResult result = picker.newRayPickResult();

            Random rand = new Random( 0 );
            Vec3 pos = new Vec3();
            Vec3 dir = new Vec3();

            final int PICK_COUNT = 100000;
            System.out.println( "PICK TIME for " + picker.getClass().getName() + " for " + PICK_COUNT + " picks" );
            long startTime = System.nanoTime();
            
            for( int i = 0; i < PICK_COUNT; i++ ) {
                pos.x = rand.nextFloat() * boundSpan[0] + boundMin[0];
                pos.y = rand.nextFloat() * boundSpan[1] + boundMin[1];
                pos.z = rand.nextFloat() * boundSpan[2] + boundMin[2];
                dir.x = rand.nextFloat() * boundSpan[0] + boundMin[0] - pos.x;
                dir.y = rand.nextFloat() * boundSpan[1] + boundMin[1] - pos.y;
                dir.z = rand.nextFloat() * boundSpan[2] + boundMin[2] - pos.z;

                picker.pick( pos, dir, Side.BOTH, result );
            }

            double secs = ( System.nanoTime() - startTime ) / 1000000000.0;
            System.out.println( "Seconds: " + secs );
        }
    }


    @Ignore @Test public void buildSpeedTest() throws IOException {
        List<DrawTri> tris = newGeometry();
        Box3 bounds = new Box3();
        GeomUtil.computeBounds( GeomUtil.vertIterator( tris ), bounds );

        final int BUILD_COUNT = 10;

        System.out.println( "BUILD TIME for " + BruteForcePicker.class.getName() + " for " + BUILD_COUNT +
                            " builds on " + tris.size() + " triangles:" );
        
        long testStart = System.nanoTime();

        for( int i = 0; i < BUILD_COUNT; i++ ) {
            BruteForcePicker.build( tris );
        }

        double testSecs = ( System.nanoTime() - testStart ) / 1000000000.0;
        System.out.println( "Seconds: " + testSecs );



        System.out.println( "BUILD TIME for " + KdTriangleTree.class.getName() + " for " + BUILD_COUNT + " builds on " +
                            tris.size() + " triangles:" );
        testStart = System.nanoTime();

        for( int i = 0; i < BUILD_COUNT; i++ ) {
            KdTriangleTree.build( tris );
        }

        System.out.println( "Seconds: " + ( System.nanoTime() - testStart ) / 1000000000.0 );
    }

    
    @Ignore @Test public void arrayVersusListSortTest() {
        final int count = 1000000;

        for( int t = 0; t < 10; t++ ) {
            Random rand = new Random( 0 );
            Set<Integer> set = new HashSet<Integer>();
            List<Integer> list = new ArrayList<Integer>( count );
            Integer[] arr = new Integer[count];
            int size = 0;

            for( int i = 0; i < 1000000; i++ ) {
                Integer n = rand.nextInt();
                if( set.add( n ) ) {
                    list.add( n );
                    arr[size++] = n;
                }
            }

            set.clear();

            long testStart = System.nanoTime();
            Collections.sort( list );
            System.out.println( "Seconds: " + ( System.nanoTime() - testStart ) / 1000000000.0 );

            testStart = System.nanoTime();
            Arrays.sort( arr, 0, size );
            System.out.println( "Seconds: " + ( System.nanoTime() - testStart ) / 1000000000.0 );
        }
    }

    

    private static List<DrawTri> newGeometry() throws IOException {
        TriModel model = ModelIO.read( new File( MODEL_PATH ) );
        return GeomUtil.listUniqueTris( model );
    }


    private static List<RayPicker> newTreeInstances( List<DrawTri> tris ) throws IOException {
        List<RayPicker> ret = new ArrayList<RayPicker>();
        ret.add( BruteForcePicker.build( tris ) );
        //ret.add( MedianVolumeKdTree.build( tris ) );
        //ret.add( NaiveSahKdTree.build( tris ) );
        //ret.add( FasterSahKdTree.build( tris ) );
        ret.add( KdTriangleTree.build( tris ) );
        return ret;
    }


    private static boolean assertNear( Vec3 x, Vec3 y, float tol ) {
        if( Vec.dist( x, y ) > tol ) {
            System.out.println( Vec.dist( x, y ) + "\t" + tol );
            System.out.println( Vec.format( x ) + "\t" + Vec.format( y ) );
            return false;
        }
        
        return true;
    }

}
