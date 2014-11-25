/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import bits.draw3d.model.*;
import bits.draw3d.lighting.Material;
import bits.math3d.Vec;
import bits.math3d.Vec3;
import bits.util.Files;
import bits.util.Streams;


/**
 * @author Philip DeCamp
 */
@SuppressWarnings( "unused" )
public class MilkShape {

    private static final Logger sLog = Logger.getLogger( MilkShape.class.getCanonicalName() );


    public static TriModel read( URL inputUrl ) throws IOException {
        ByteBuffer buf = Streams.readBytes( inputUrl );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        byte[] temp = new byte[256];

        // Check magic number and version.
        if( !readName( buf, 0, 10, temp ).equals( "MS3D000000" ) ) {
            throw new IOException( "Not a MilkShape file." );
        }

        final int version = buf.getInt();
        if( version != 4 ) {
            throw new IOException( "Support is only available for MilkShape v4 files." );
        }

        Vec3[]    posArr = readVertPositions( buf );
        DrawTri[] triArr = readTris( buf, posArr );
        List<TriGroup> groupList;
        int[] groupMaterialIndex;
        {
            final int groupNum = (buf.getShort() & 0xFFFF);
            groupList = new ArrayList<TriGroup>( groupNum );
            groupMaterialIndex = new int[groupNum];

            for( int i = 0; i < groupNum; i++ ) {
                // Read name of material.
                String name = readName( buf, 1, 32, temp );

                // Read triangles.
                int numTri = (buf.getShort() & 0xFFFF);
                List<DrawTri> list = new ArrayList<DrawTri>( numTri );
                for( int j = 0; j < numTri; j++ ) {
                    list.add( triArr[ buf.getShort() & 0xFFFF ] );
                }

                // Read material index.
                groupMaterialIndex[i] = (buf.get() & 0xFF);
                TriGroup group = new TriGroup( name, null, list );
                groupList.add( group );
            }
        }

        // Read materials.
        DrawMaterial[] materialArr = readMaterials( buf, inputUrl );

        // Assign materials to groups.
        for( int i = 0; i < groupList.size(); i++ ) {
            int idx = groupMaterialIndex[i];
            if( idx < materialArr.length ) {
                DrawMaterial tm = materialArr[idx];
                if( tm != null ) {
                    TriGroup g = groupList.get( i );
                    g.mMaterial = tm;
                }
            }
        }

        return new TriModel( "", groupList );
    }


    public static void write( TriModel model, File outFile ) throws IOException {
        FileChannel out = new FileOutputStream( outFile ).getChannel();

        ByteBuffer buf = ByteBuffer.allocate( 1024 * 8 );
        buf.order( ByteOrder.LITTLE_ENDIAN );

        final List<DrawVert> verts          = Models.listUniqueVerts( model );
        final List<DrawTri>  tris           = Models.listUniqueTris( model );
        final List<DrawMaterial> materials = Models.listUniqueMaterials( model );

        writeHeader( buf, out );
        writeVerts( verts, buf, out );
        writeTris( model, verts, buf, out );
        writeGroups( model, materials, tris, buf, out );
        writeMaterials( model, materials, buf, out, outFile );
        writeJoints( model, buf, out );

        out.close();
    }



    private static Vec3[] readVertPositions( ByteBuffer buf ) {
        final int posNum = (buf.getShort() & 0xFFFF);
        Vec3[] posArr = new Vec3[posNum];
        for( int i = 0; i < posNum; i++ ) {
            buf.get();
            posArr[i] = new Vec3( buf.getFloat(), buf.getFloat(), buf.getFloat() );
            buf.position( buf.position() + 2 );
        }
        return posArr;
    }


    private static DrawTri[] readTris( ByteBuffer buf, Vec3[] posArr ) {
        final int triNum = ( buf.getShort() & 0xFFFF );
        DrawTri[] triArr = new DrawTri[triNum];
        for( int triInd = 0; triInd < triNum; triInd++ ) {
            buf.position( buf.position() + 2 );
            DrawTri tri = new DrawTri();
            DrawVert[] v = tri.mVerts;

            for( int i = 0; i < 3; i++ ) {
                Vec3 pos = posArr[ buf.getShort() & 0xFFFF];
                v[i] = new DrawVert( pos.x, pos.y, pos.z );
            }

            for( int i = 0; i < 3; i++ ) {
                v[i].mNorm = new Vec3( buf.getFloat(), buf.getFloat(), buf.getFloat() );
                v[i].mTex  = new float[2];
            }

            for( int i = 0; i < 3; i++ ) {
                v[i].mTex[0] = buf.getFloat();
            }
            for( int i = 0; i < 3; i++ ) {
                v[i].mTex[1] = buf.getFloat();
            }

            triArr[triInd] = tri;
            buf.position( buf.position() + 2 );
        }

        return triArr;
    }


    private static DrawMaterial[] readMaterials( ByteBuffer buf, URL inputUrl ) throws IOException {
        final byte[] temp = new byte[128];
        final int materialNum = (buf.getShort() & 0xFFFF);
        final DrawMaterial[] materialArr = new DrawMaterial[materialNum];

        for( int i = 0; i < materialNum; i++ ) {
            String name = readName( buf, 0, 32, temp );
            DrawMaterial matr = new DrawMaterial( name, null, null, new Material() );

            // Read material parameters.
            Vec.put( buf, matr.mMaterial.mAmbient  );
            Vec.put( buf, matr.mMaterial.mDiffuse  );
            Vec.put( buf, matr.mMaterial.mSpecular );
            Vec.put( buf, matr.mMaterial.mEmissive );
            matr.mMaterial.mShininess = buf.getFloat();
            matr.mMaterial.alpha( buf.getFloat() );

            // Skip byte.
            buf.get();

            // Read path to image.
            buf.get( temp, 0, 128 );
            int pathLength = 0;
            for( ; pathLength < 128 && temp[pathLength] != 0; pathLength++ ) {}
            String rawPath = null;
            try {
                rawPath = new String( temp, 0, pathLength, "UTF-8" );
            } catch( UnsupportedEncodingException e ) {
                throw new RuntimeException( e );
            }
            String path = rawPath.replace( '\\', '/' );
            URL url = null;
            if( path != null && path.length() > 0 ) {
                url = new URL( inputUrl, path );
            }

            if( url != null ) {
                try {
                    matr.mImage = ImageIO.read( url );
                } catch( IIOException ex ) {
                    sLog.info( "Error loading texture at: " + url.toString() + " " + ex.getMessage() );
                }
            }

            materialArr[i] = matr;
            buf.position( buf.position() + 128 );
        }

        return materialArr;
    }



    private static void writeHeader( ByteBuffer buf, FileChannel out ) throws IOException {
        buf.put( (byte)'M' );
        buf.put( (byte)'S' );
        buf.put( (byte)'3' );
        buf.put( (byte)'D' );
        buf.put( (byte)'0' );
        buf.put( (byte)'0' );
        buf.put( (byte)'0' );
        buf.put( (byte)'0' );
        buf.put( (byte)'0' );
        buf.put( (byte)'0' );
        buf.putInt( 4 );

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }


    private static void writeVerts( List<DrawVert> verts,
                                    ByteBuffer buf,
                                    FileChannel out )
                                    throws IOException
    {
        buf.putShort( (short)(verts.size()) );

        for( DrawVert iv : verts ) {
            if( buf.remaining() < 15 ) {
                buf.flip();
                writeBuf( buf, out );
                buf.clear();
            }
            Vec3 v = iv.mPos;
            buf.put( (byte)0 );
            buf.putFloat( v.x );
            buf.putFloat( v.y );
            buf.putFloat( v.z );
            buf.put( (byte)-1 );
            buf.put( (byte)0 );
        }

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }


    private static void writeTris( TriModel model,
                                   List<DrawVert> verts,
                                   ByteBuffer buf,
                                   FileChannel out )
                                       throws IOException
    {
        Map<DrawVert,Integer> vertIndex = Models.index( verts.iterator() );

        int triCount = 0;
        for( int groupInd = 0; groupInd < model.mGroups.size(); groupInd++ ) {
            triCount += model.mGroups.get( groupInd ).mTris.size();
        }
        buf.putShort( (short)triCount );

        for( int groupInd = 0; groupInd < model.mGroups.size(); groupInd++ ) {
            final List<DrawTri> tris = model.mGroups.get( groupInd ).mTris;
            final byte groupByte = (byte)groupInd;

            for( DrawTri t: tris ) {
                if( buf.remaining() < 70 ) {
                    buf.flip();
                    writeBuf( buf, out );
                    buf.clear();
                }

                buf.putShort( (short)0 );
                for( int i = 0; i < 3; i++ ) {
                    Integer label = vertIndex.get( t.mVerts[i] );
                    buf.putShort( label.shortValue() );
                }

                for( int i = 0; i < 3; i++ ) {
                    Vec3 norm = t.mVerts[i].mNorm;
                    if( norm != null ) {
                        Vec.put( norm, buf );
                    } else {
                        buf.putFloat( 0 );
                        buf.putFloat( 0 );
                        buf.putFloat( 0 );
                    }
                }

                for( int i = 0; i <  3; i++ ) {
                    float[] tex = t.mVerts[i].mTex;
                    buf.putFloat( tex == null ? 0f : tex[0] );
                }
                for( int i = 0; i <  3; i++ ) {
                    float[] tex = t.mVerts[i].mTex;
                    buf.putFloat( tex == null ? 0f : tex[1] );
                }

                buf.put( (byte)0 );
                buf.put( groupByte );
            }
        }

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }


    private static void writeGroups( TriModel model,
                                     List<DrawMaterial> allMaterials,
                                     List<DrawTri> allTris,
                                     ByteBuffer buf,
                                     FileChannel out )
                                    throws IOException
    {
        Map<DrawTri,Integer> triIndex = Models.index( allTris );
        Map<DrawMaterial,Integer> materialIndex = Models.index( allMaterials );

        buf.putShort( (short)model.mGroups.size() );
        for( TriGroup group: model.mGroups ) {
            if( buf.remaining() < 128 ) {
                buf.flip();
                writeBuf( buf, out );
                buf.clear();
            }

            buf.put( (byte)0 );
            writeName( group.mName, 32, buf );

            List<DrawTri> tris = group.mTris;
            buf.putShort( (short)tris.size() );
            for( DrawTri tri : tris ) {
                if( buf.remaining() < 128 ) {
                    buf.flip();
                    writeBuf( buf, out );
                    buf.clear();
                }
                buf.putShort( triIndex.get( tri ).shortValue() );
            }

            Integer materialId = materialIndex.get( group.mMaterial );
            if( materialId == null ) {
                buf.put( (byte)-1 );
            } else {
                buf.put( materialId.byteValue() );
            }
        }

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }


    private static void writeMaterials( TriModel model,
                                        List<DrawMaterial> materials,
                                        ByteBuffer buf,
                                        FileChannel out,
                                        File outFile )
                                        throws IOException
    {
        buf.putShort( (short)materials.size() );

        for( DrawMaterial tm : materials ) {
            if( buf.remaining() < 361 ) {
                buf.flip();
                writeBuf( buf, out );
                buf.clear();
            }

            // Don't quite know what I should do with alpha.
            Material mat = tm.mMaterial;

            writeName( tm.mName, 32, buf );
            Vec.put( mat.mAmbient, buf );
            Vec.put( mat.mDiffuse, buf );
            Vec.put( mat.mSpecular, buf );
            Vec.put( mat.mEmissive, buf );
            buf.putFloat( mat.mShininess );
            float alpha = mat.mDiffuse.w;
            buf.putFloat( alpha );
            buf.put( (byte)0 );

            if( tm.mImage == null ) {
                writeName( null, 128, buf );
                writeName( null, 128, buf );
            } else {
                File dir        = outFile.getParentFile();
                String baseName = Files.baseName( outFile );
                File matFile    = genOutputFileName( dir, baseName, "png", 128 );
                ImageIO.write( tm.mImage, "png", matFile );
                writeName( matFile.getName(), 128, buf );
                writeName( matFile.getName(), 128, buf );
            }
        }

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }


    private static void writeJoints( TriModel model,
                                     ByteBuffer buf,
                                     FileChannel out )
                                     throws IOException
    {

        buf.putFloat( 0 );
        buf.putFloat( 0 );
        buf.putInt( 0 );
        buf.putShort( (short)0 );

        buf.flip();
        writeBuf( buf, out );
        buf.clear();
    }

    private static void writeBuf( ByteBuffer buf, FileChannel channel ) throws IOException {
        while( buf.remaining() > 0 ) {
            if( channel.write( buf ) <= 0 ) {
                throw new IOException( "Write operation failed." );
            }
        }
    }


    private static void writeName( String name, int len, ByteBuffer buf ) {
        int nameLength = 0;
        if( name != null ) {
            nameLength = Math.min( len - 1, name.length() );
            for( int i = 0; i < nameLength; i++ ) {
                buf.put( (byte)name.charAt( i ) );
            }
        }
        for( int i = nameLength; i < len; i++ ) {
            buf.put( (byte)0 );
        }
    }


    private static String readName( ByteBuffer buf, int off, int len, byte[] work ) {
        try {
            buf.get( work, 0, off + len );
            return new String( work, off, len, "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
    }


    private static File genOutputFileName( File outDir, String baseName, String suffix, int maxLen ) {
        StringBuilder s = new StringBuilder();

        for( int attempt = 0;; attempt++ ) {
            String num = attempt == 0 ? "" : String.format( "_%d", attempt );
            //Reduce base name length to max.
            int len = baseName.length() + num.length() + 1 + suffix.length();


            if( len > maxLen ) {
                // First shorten base name.
                int n = Math.min( baseName.length(), len - maxLen );
                baseName = baseName.substring( 0, n );
                len -= n;

                if( len > maxLen ) {
                    // Then shorten extension.
                    n = Math.min( suffix.length(), len - maxLen );
                    suffix = suffix.substring( 0, n );
                }
            }

            s.setLength( 0 );
            s.append( baseName );
            s.append( num );
            s.append( '.' );
            s.append( suffix );
            File file = new File( outDir, s.toString() );

            if( !file.exists() ) {
                return file;
            }
        }
    }


}
