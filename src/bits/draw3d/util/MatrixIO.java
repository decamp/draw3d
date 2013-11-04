package bits.draw3d.util;

import java.io.IOException;
import bits.blob.Blob;
import bits.math3d.Matrices;


/**
 * @author decamp
 */
public class MatrixIO {

    
    public static double[] parseTransformStack( Blob blob ) throws IOException {
        int size = blob.size();
        double[] ret = new double[16];
        Matrices.setToIdentity(ret);        
        
        for(int i = 0; i < size; i++) {
            double[] mat = parseTransform(blob.slice(i));
            double[] comp = new double[16];
            Matrices.multMatMat(ret, mat, comp);
            ret = comp;
        }
        
        return ret;
    }
    
    
    public static double[] parseTransform( Blob blob ) throws IOException {
        int size = blob.size();
        if(size < 1)
            return null;
        
        String type = blob.getString(0);
        if(type == null)
            throw new IOException("Invalid matrix format");

        if(type.equalsIgnoreCase("matrix")) {
            double[] mat = new double[16];
            readDoubles(blob, 1, mat, 0, 16);
            return mat;
        }
        

        if(type.equalsIgnoreCase("translate")) {
            double[] tran = new double[3];
            readDoubles(blob, 1, tran, 0, 3);
            double[] mat = new double[16];
            Matrices.computeTranslationMatrix(tran[0], tran[1], tran[2], mat);
            return mat;
        }
        
        if(type.equalsIgnoreCase("scale")) {
            double[] scale = new double[3];
            readDoubles(blob, 1, scale, 0, 3);
            double[] mat = new double[16];
            Matrices.computeScaleMatrix(scale[0], scale[1], scale[2], mat);
            return mat;
        }
        
        if(type.equalsIgnoreCase("rotateDegrees")) {
            double[] rot = new double[4];
            readDoubles(blob, 1, rot, 0, 4);
            double[] mat = new double[16];
            Matrices.computeRotationMatrix(rot[0] * Math.PI / 180.0, rot[1], rot[2], rot[3], mat);
            return mat;
        }
        
        if(type.equalsIgnoreCase("rotateRadians")) {
            double[] rot = new double[4];
            readDoubles(blob, 1, rot, 0, 4);
            double[] mat = new double[16];
            Matrices.computeRotationMatrix(rot[0], rot[1], rot[2], rot[3], mat);
            return mat;
        }
        
        throw new IOException("Invalid matrix format");
    }
    
    
    private static void readDoubles( Blob blob, int inOff, double[] out, int outOff, int len ) throws IOException {
        for( int i = 0; i < len; i++ ) {
            Double d = blob.getDouble( inOff + i );
            if( d == null ) {
                throw new IOException( "Invalid matrix format" );
            }
            out[outOff+i] = d.doubleValue();
        }
    }
    
}
