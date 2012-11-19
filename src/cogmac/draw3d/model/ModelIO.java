package cogmac.draw3d.model;

import java.io.*;
import java.net.URL;
import java.nio.*;

import cogmac.blob.Blob;
import cogmac.draw3d.model.io.*;


/** 
 * @author Philip DeCamp  
 */
public final class ModelIO {

    
    public static MeshModel read(File inputFile) throws IOException {
        return read(inputFile.toURI().toURL());
    }

    
    public static MeshModel read(URL url) throws IOException {	
        if(url.getFile().toLowerCase().endsWith(".ms3d")){
            return MilkShape.loadModel(url);
        }
        
        if(url.getFile().toLowerCase().endsWith(".obj")) {
            return ObjParser.read(url);
        }
        
        throw new IOException("Model format not recognized.");
    }

    
    public static ByteBuffer bufferStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024*8);
        byte[] temp = new byte[1024*4];
        
        while(true){
            int n = in.read(temp);
            if(n <= 0)
                break;
            
            out.write(temp, 0, n);
        }
        
        in.close();
        ByteBuffer buf = ByteBuffer.wrap(out.toByteArray());
        buf.position(0).limit(buf.capacity());
        
        return buf;
    }

    
    public static Material readMaterialBlob( String name, Blob blob ) {
        float[] ambient  = null;
        float[] diffuse  = null;
        float[] specular = null;
        float[] emissive = null;
        float shininess;
        float alpha;
        
        if( blob.containsKey( "ambient" ) ) {
            ambient = readFloats( blob.slice( "ambient" ) );
        } else {
            ambient = new float[]{ 0, 0, 0, 1 };
        }

        if( blob.containsKey( "diffuse" ) ) {
            diffuse = readFloats( blob.slice( "diffuse" ) );
        } else {
            diffuse = new float[]{ 0, 0, 0, 1 };
        }

        if( blob.containsKey( "specular" ) ) {
            specular = readFloats( blob.slice( "specular" ) );
        } else {
            specular = new float[]{ 0, 0, 0, 1 };
        }

        if( blob.containsKey( "emissive" ) ) {
            emissive = readFloats( blob.slice( "emissive" ) );
        } else {
            emissive = new float[]{ 0, 0, 0, 1 };
        }

        shininess = blob.tryGetFloat( 0f, "shininess" );
        alpha = blob.tryGetFloat(1f, "alpha");

        if( alpha != 1f ) {
            ambient[3]  *= alpha;
            diffuse[3]  *= alpha;
            specular[3] *= alpha;
            emissive[3] *= alpha;
        } 
        
        Material ret = new Material( ambient, diffuse, specular, emissive, shininess );
        ret.name( name );
        return ret;
        
    }

    
    
    private static float[] readFloats(Blob blob) {
        int size = blob.size();
        float[] ret = new float[size];
        
        for(int i = 0; i < size; i++) {
            ret[i] = blob.getFloat(i); 
        }
        
        return ret;
    }

    
    
    private ModelIO() { throw new IllegalAccessError(); }
    
}
