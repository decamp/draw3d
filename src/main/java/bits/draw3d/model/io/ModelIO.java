package bits.draw3d.model.io;

import java.io.*;
import java.net.URL;
import java.nio.*;

import bits.blob.Blob;
import bits.draw3d.geom.ModelMaterial;
import bits.draw3d.geom.TriModel;
import bits.draw3d.tex.Material;
import bits.draw3d.model.io.*;
import bits.math3d.Vec4;


/**
 * @author Philip DeCamp
 */
public final class ModelIO {


    public static TriModel read( File inputFile ) throws IOException {
        return read( inputFile.toURI().toURL() );
    }


    public static TriModel read( URL url ) throws IOException {
        if( url.getFile().toLowerCase().endsWith( ".ms3d" ) ) {
            return MilkShape.read( url );
        }

        if( url.getFile().toLowerCase().endsWith( ".obj" ) ) {
            return ObjParser.read( url );
        }

        throw new IOException( "Model format not recognized." );
    }


    public static ByteBuffer bufferStream( InputStream in ) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream( 1024 * 8 );
        byte[] temp = new byte[1024 * 4];

        while( true ) {
            int n = in.read( temp );
            if( n <= 0 ) {
                break;
            }

            out.write( temp, 0, n );
        }

        in.close();
        ByteBuffer buf = ByteBuffer.wrap( out.toByteArray() );
        buf.position( 0 ).limit( buf.capacity() );

        return buf;
    }


    public static ModelMaterial readMaterialBlob( String name, Blob blob ) {
        Vec4 ambient;
        Vec4 diffuse;
        Vec4 specular;
        Vec4 emissive;
        float shininess;

        if( blob.containsKey( "ambient" ) ) {
            ambient = readColor( blob.slice( "ambient" ) );
        } else {
            ambient = new Vec4( 0, 0, 0, 1 );
        }

        if( blob.containsKey( "diffuse" ) ) {
            diffuse = readColor( blob.slice( "diffuse" ) );
        } else {
            diffuse = new Vec4( 0, 0, 0, 1 );
        }

        if( blob.containsKey( "specular" ) ) {
            specular = readColor( blob.slice( "specular" ) );
        } else {
            specular = new Vec4( 0, 0, 0, 1 );
        }

        if( blob.containsKey( "emissive" ) ) {
            emissive = readColor( blob.slice( "emissive" ) );
        } else {
            emissive = new Vec4( 0, 0, 0, 1 );
        }

        shininess = blob.tryGetFloat( 0f, "shininess" );
        diffuse.z *= blob.tryGetFloat( 1f, "alpha" );

        Material matr = new Material( ambient, diffuse, specular, emissive, shininess );
        ModelMaterial ret = new ModelMaterial( name, null, null, matr );

        return ret;
    }


    private static float[] readFloats( Blob blob ) {
        int size = blob.size();
        float[] ret = new float[size];
        for( int i = 0; i < size; i++ ) {
            ret[i] = blob.getFloat( i );
        }
        return ret;
    }


    private static Vec4 readColor( Blob blob ) {
        Vec4 ret = new Vec4();
        int size = blob.size();
        switch( size ) {
        default:
        case 4:
            ret.w = blob.getFloat( 3 );
        case 3:
            ret.z = blob.getFloat( 2 );
        case 2:
            ret.y = blob.getFloat( 1 );
        case 1:
            ret.x = blob.getFloat( 0 );
        }
        return ret;
    }


    private ModelIO() { throw new IllegalAccessError(); }

}
