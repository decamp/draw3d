/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.util;

import java.io.*;
import java.util.*;

import bits.blob.*;
import bits.math3d.geom.*;


/**
 * @author Philip DeCamp
 */
public class VolumeIO {


    public static Map<Object, Volume> readVolumeMap( File file ) throws IOException {
        return readVolumeMap( Blob.readYaml( file ) );
    }


    public static Map<Object, Volume> readVolumeMap( Blob blob ) throws IOException {
        Map<Object, Volume> ret = new HashMap<Object, Volume>();
        Iterator<Map.Entry<?, ?>> iter = blob.entrySet().iterator();

        while( iter.hasNext() ) {
            Map.Entry<?, ?> e = iter.next();
            ret.put( e.getKey(), readVolume( new Blob( e.getValue() ) ) );
        }

        return ret;
    }


    public static Blob writeVolumeMap( Map<Object, ? extends Volume> map ) throws IOException {
        Blob b = new Blob( new LinkedHashMap<Object, Object>() );

        for( Map.Entry<Object, ? extends Volume> e : map.entrySet() ) {
            b.put( e.getKey(), writeVolume( e.getValue() ).get() );
        }

        return b;
    }


    public static Volume readVolume( Blob blob ) throws IOException {
        String t = blob.getString( "class" );

        if( t == null ) {
            throw new IOException( "Invalid input.  No \"type\"" );
        }

        try {
            if( t.equals( Aabb.class.getName() ) ) {
                return readAabb( blob );
            } else if( t.equals( ExtrudedLineLoop.class.getName() ) ) {
                return readExtrudedLineLoop( blob );
            } else if( t.equals( Sphere.class.getName() ) ) {
                return readSphere( blob );
            }

        } catch( Exception ex ) {
            throw new IOException( "Failed to parse manifold of type: " + t );
        }

        throw new IOException( "Unknown type: " + t );
    }


    public static Blob writeVolume( Volume man ) throws IOException {
        if( man == null ) {
            throw new NullPointerException();
        }
        if( man instanceof Aabb ) {
            return writeAabb( (Aabb)man );
        }
        if( man instanceof ExtrudedLineLoop ) {
            return writeExtrudedLineLoop( (ExtrudedLineLoop)man );
        }
        if( man instanceof Sphere ) {
            return writeSphere( (Sphere)man );
        }

        throw new IOException( "Unknown manifold of type: " + man.getClass() );
    }



    private static Aabb readAabb( Blob b ) {
        return Aabb.fromEdges( b.getFloat( "v0", 0 ),
                               b.getFloat( "v0", 1 ),
                               b.getFloat( "v0", 2 ),
                               b.getFloat( "v1", 0 ),
                               b.getFloat( "v1", 1 ),
                               b.getFloat( "v1", 2 ) );
    }


    private static Blob writeAabb( Aabb bounds ) {
        Blob b = new Blob( new LinkedHashMap<Object, Object>() );
        b.put( "class", Aabb.class.getName() );
        b.put( "v0", 0, bounds.minX() );
        b.put( "v0", 1, bounds.minY() );
        b.put( "v0", 2, bounds.minZ() );
        b.put( "v1", 0, bounds.maxX() );
        b.put( "v1", 1, bounds.maxY() );
        b.put( "v1", 2, bounds.maxZ() );
        return b;
    }


    private static ExtrudedLineLoop readExtrudedLineLoop( Blob b ) {
        float minZ = b.getFloat( "minZ" );
        float maxZ = b.getFloat( "maxZ" );

        int pointCount = b.size( "x" );

        float[] x = new float[pointCount];
        float[] y = new float[pointCount];

        for( int i = 0; i < pointCount; i++ ) {
            x[i] = b.getFloat( "x", i );
            y[i] = b.getFloat( "y", i );
        }

        return new ExtrudedLineLoop( pointCount, x, y, minZ, maxZ );
    }


    private static Blob writeExtrudedLineLoop( ExtrudedLineLoop loop ) {
        Blob b = new Blob();
        b.put( "class", ExtrudedLineLoop.class.getName() );

        final int count = loop.pointCount();

        b.put( "minZ", loop.minZ() );
        b.put( "maxZ", loop.maxZ() );

        float[] x = loop.xRef();
        float[] y = loop.yRef();

        for( int i = 0; i < count; i++ ) {
            b.put( "x", i, x[i] );
            b.put( "y", i, y[i] );
        }

        return b;
    }


    private static Sphere readSphere( Blob b ) {
        float x = b.getFloat( "x" );
        float y = b.getFloat( "y" );
        float z = b.getFloat( "z" );
        float r = b.getFloat( "r" );

        return new Sphere( x, y, z, r );
    }


    private static Blob writeSphere( Sphere sphere ) {
        Blob b = new Blob();
        b.put( "class", Sphere.class.getName() );

        b.put( "x", sphere.x() );
        b.put( "y", sphere.y() );
        b.put( "z", sphere.z() );
        b.put( "r", sphere.rad() );

        return b;
    }

}
