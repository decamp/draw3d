package cogmac.draw3d.model.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;

import cogmac.draw3d.model.*;
import cogmac.draw3d.pick.*;



/** 
 * @author Philip DeCamp  
 */
public class MilkShape {
    
    private static final Logger sLog = Logger.getLogger( MilkShape.class.getCanonicalName() );

    
    public static MeshModel loadModel( URL inputURL ) throws IOException {
        ByteBuffer buf = ModelIO.bufferStream( inputURL.openStream() );
        buf.order( ByteOrder.LITTLE_ENDIAN );
        
        byte[] temp = new byte[256];
        buf.get( temp, 0, 10 );
        
        if( !new String( temp, 0, 10, "UTF-8" ).equals( "MS3D000000" ) )
            throw new IOException( "Not a MilkShape file." );

        final int version = buf.getInt();
        if( version != 4 )
            throw new IOException( "Support is only available for MilkShape v4 files." );
        
        
        final int vertexNum = ( buf.getShort() & 0xFFFF );
        //System.out.println("VertexNum: " + vertexNum);
        double[][] vert = new double[vertexNum][];
        
        for(int i = 0; i < vertexNum; i++){
            buf.get();
            vert[i] = new double[]{buf.getFloat(), buf.getFloat(), buf.getFloat()};
            buf.position(buf.position() + 2);
        }
        
        
        final int triangleNum = ((buf.getShort() & 0xFFFF) & 0xFFFF);
        //System.out.println("TriangleNum: " + triangleNum);
        Triangle[] tri = new Triangle[triangleNum];
        
        for(int i = 0; i < triangleNum; i++){
            buf.position(buf.position() + 2);
            
            double[][] v = new double[][]{ vert[(buf.getShort() & 0xFFFF)], 
                                           vert[(buf.getShort() & 0xFFFF)], 
                                           vert[(buf.getShort() & 0xFFFF)]};
            
            double[][] norm = new double[3][3];
            
            for(int j = 0; j < 3; j++){
                for(int k = 0; k < 3; k++){
                    norm[j][k] = buf.getFloat();
                }
            }
            
            double[][] tex = new double[3][2];
            for(int j = 0; j < 3; j++){
                tex[j][0] = buf.getFloat();
            }
            
            for(int j = 0; j < 3; j++){
                tex[j][1] = buf.getFloat() - 1f;
            }
            
            tri[i] = new Triangle(v, norm, tex);
            buf.position(buf.position() + 2);
        }
                
        final int groupNum       = ( buf.getShort() & 0xFFFF );
        List<Group> groupList    = new ArrayList<Group>( groupNum );
        int[] groupMaterialIndex = new int[groupNum];
        //System.out.println("GroupNum: " + groupNum);
        
        for( int i = 0; i < groupNum; i++ ) {
            buf.get( temp, 0, 33 );
            String name = new String( temp, 1, 32 );
            int numTri  = ( buf.getShort() & 0xFFFF );
            
            List<Triangle> t = new ArrayList<Triangle>( numTri );
            for( int j = 0; j < numTri; j++ )
                t.add( tri[buf.getShort() & 0xFFFF] );
            
            groupMaterialIndex[i] = ( buf.get() & 0xFF );
            Group group = new Group( name, null, null, t );
            groupList.add( group );
        }
        
        
        final int materialNum = ( buf.getShort() & 0xFFFF );
        
        ModelMaterial[] material = new ModelMaterial[materialNum];
        //System.out.println("MaterialNum: " + materialNum);
        
        for( int i = 0; i < materialNum; i++ ) {
            buf.get( temp, 0, 32 );
            String name = new String( temp, 0, 32, "utf8" );

            float[][] light = new float[4][4];
            
            for( int j = 0; j < 16; j++ ) {
                light[j / 4][j % 4] = buf.getFloat();
            }
            
            float shiny = buf.getFloat();
            float alpha = buf.getFloat();
            
            if( alpha != 1f ) {
                for( float[] g: light ) {
                    g[3] *= alpha;
                }
            }
            
            buf.get();
            buf.get( temp, 0, 128 );
            int pathLength = 0;
            
            for( ; pathLength < 128 && temp[pathLength] != 0; pathLength++ ) {}
            
            String rawPath = new String( temp, 0, pathLength, "utf8" );
            String path    = rawPath.replace( '\\', '/' );
            URL url        = null;
            
            if(path != null && path.length() > 0){
                url = new URL( inputURL, path );
            }
            
            if( null != url ) {
                try{
                    BufferedImage im = ImageIO.read( url );
                    Material mat     = new Material( light[0], light[1], light[2], light[3], shiny );
                    mat.name( name );
                    material[i] = new ModelMaterial( im, mat );
                }catch( IIOException ex ) {
                    sLog.info( "Error loading texture at: " + url.toString() + " " + ex.getMessage() );
                }
            }
            buf.position( buf.position() + 128 );
        }
        
        for( int i = 0; i < groupList.size(); i++ ) {
            int idx = groupMaterialIndex[i];
            if( idx < material.length ) {
                ModelMaterial tm = material[idx];
                if( tm != null ) {
                    Group g = groupList.get( i );
                    g.setTexture( tm.mTex );
                    g.setMaterial( tm.mMat );
                }
            }
        }
        
        return new MeshModel( groupList );
    }

    
    public static void saveModel( MeshModel model, File outFile ) throws IOException {
        FileChannel out = new FileOutputStream(outFile).getChannel();
        ByteBuffer buf = ByteBuffer.allocate(1024*8);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        
        KdPointTree<IndexedVertex> vertices = Models.indexVertices(model);
        Map<Triangle, Integer> triangles    = Models.indexTriangles(model);
        Map<ModelMaterial, Integer> materials = Models.indexTexMaterials(model);
        
        saveHeader( model, buf, out );
        saveVertices( model, vertices, buf, out );
        saveTriangles( model, vertices, triangles, buf, out );
        saveGroups( model, triangles, materials, buf, out );
        saveMaterials( model, materials, buf, out, outFile.getParentFile() );
        saveJoints( model, buf, out );
        
        out.close();
    }
    
   
    
    
    private static void saveHeader( MeshModel model, 
                                    ByteBuffer buf, 
                                    FileChannel out) 
                                     throws IOException 
    {
        buf.put((byte)'M');
        buf.put((byte)'S');
        buf.put((byte)'3');
        buf.put((byte)'D');
        buf.put((byte)'0');
        buf.put((byte)'0');
        buf.put((byte)'0');
        buf.put((byte)'0');
        buf.put((byte)'0');
        buf.put((byte)'0');
        buf.putInt(4);
        
        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
    

    private static void saveVertices( MeshModel model, 
                                      Collection<IndexedVertex> vertices, 
                                      ByteBuffer buf,
                                      FileChannel out)
                                        throws IOException 
    {
        buf.putShort((short)(vertices.size()));
        SortedSet<IndexedVertex> set = new TreeSet<IndexedVertex>(IndexedVertex.ORDER);
        set.addAll(vertices);
             
        for(IndexedVertex iv: set) {
                 
            if(buf.remaining() < 15) {
                buf.flip();
                writeBuf(buf, out);
                buf.clear();
            }
                 
            double[] v = iv.vertex();
            buf.put((byte)0);
            buf.putFloat((float)v[0]);
            buf.putFloat((float)v[1]);
            buf.putFloat((float)v[2]);
            buf.put((byte)-1);
            buf.put((byte)0);
        }
        
        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
    
    
    private static void saveTriangles( MeshModel model,
                                       KdPointTree<IndexedVertex> vertices,
                                       Map<Triangle, Integer> triangles,
                                       ByteBuffer buf,
                                       FileChannel out )
                                        throws IOException
    {
    
        buf.putShort((short)triangles.size());
        
        PointPickResult<IndexedVertex> result = vertices.newPointPickResult();
        SortedMap<Integer, Triangle> map = new TreeMap<Integer, Triangle>();
        Map<Triangle, Integer> groupMap = Models.indexTriangleGroups(model);
            
        for(Map.Entry<Triangle, Integer> entry: triangles.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }

        for(Triangle t: map.values()) {
                
            if(buf.remaining() < 70) {
                buf.flip();
                writeBuf(buf, out);
                buf.clear();
            }
                
            buf.putShort((short)0);
                
            for(int i = 0; i < 3; i++) {
                IndexedVertex iv = new IndexedVertex(0, t.vertex(i));
                if(!vertices.pick(iv, result))
                    throw new IOException("Failed to index vertices.");
                
                buf.putShort((short)(result.pickedPoint().index()));
            }
                
            double[][] r = t.normalRef();
            
            if(r != null) {
                for(int i = 0; i < 3; i++) {
                    for(int j = 0; j < 3; j++) {
                        buf.putFloat((float)r[i][j]);
                    }
                }
            }else{
                for(int i = 0; i < 9; i++) {
                    buf.putFloat(0);
                }
            }
                
            r = t.texRef();
            
            if(r == null) {
                for(int i = 0; i < 6; i++) {
                    buf.putFloat(0);
                }
                
            }else{
                for(int i = 0; i < 3; i++) {
                    buf.putFloat((float)r[i][0]);
                }

                for(int i = 0; i < 3; i++) {
                    buf.putFloat((float)r[i][1]);
                }
            }

            buf.put((byte)0);
            buf.put(groupMap.get(t).byteValue());
        }
        
        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
    
    
    private static void saveGroups( MeshModel model,
                                    Map<Triangle, Integer> triangles,
                                    Map<ModelMaterial, Integer> materials,
                                    ByteBuffer buf,
                                    FileChannel out )
                                    throws IOException 
    {
        List<Group> groups = model.getGroupsRef();
        buf.putShort((short)groups.size());
        
        for(int i = 0; i < groups.size(); i++) {
            
            if(buf.remaining() < 128) {
                buf.flip();
                writeBuf(buf, out);
                buf.clear();
            }
            
            Group group = groups.get(i);
            buf.put((byte)0);
            putName(group.getName(), 32, buf);

            List<Triangle> tri = group.getTriangles();
            buf.putShort((short)tri.size());
       
            for(int j = 0; j < tri.size(); j++) {
                if(buf.remaining() < 128) {
                    buf.flip();
                    writeBuf(buf, out);
                    buf.clear();
                }
                
                buf.putShort(triangles.get(tri.get(j)).shortValue());
            }
            
            Material mat = group.getMaterial();
            if( mat == null ) {
                buf.put( (byte)-1 );
            }else{
                buf.put( materials.get(mat).byteValue() );
            }
        }
        
        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
    
    
    private static void saveMaterials( MeshModel model,
                                       Map<ModelMaterial, Integer> materials,
                                       ByteBuffer buf,
                                       FileChannel out,
                                       File outDir )
                                       throws IOException 
    {
    
        SortedMap<Integer,ModelMaterial> map = new TreeMap<Integer,ModelMaterial>();
        
        for(Map.Entry<ModelMaterial, Integer> entry: materials.entrySet()) {
            map.put( entry.getValue(), entry.getKey() );
        }
        
        buf.putShort((short)materials.size());
        
        for( ModelMaterial tm: map.values() ) {
            if( buf.remaining() < 361 ) {
                buf.flip();
                writeBuf( buf, out );
                buf.clear();
            }
            
            Material mat = tm.mMat;
            float alpha  = 0f;
            alpha = Math.max( alpha, mat.ambientRef()[3] );
            alpha = Math.max( alpha, mat.diffuseRef()[3] );
            alpha = Math.max( alpha, mat.specularRef()[3] );
            alpha = Math.max( alpha, mat.emissionRef()[3] );
            float alphaDiv = alpha <= 0f ? 1f : alpha;
            
            putName(mat.name(), 32, buf);
            float[] v = mat.ambientRef();
            buf.putFloat( v[0] );
            buf.putFloat( v[1] );
            buf.putFloat( v[2] );
            buf.putFloat( v[3] / alphaDiv );
            
            v = mat.diffuseRef();
            buf.putFloat( v[0] );
            buf.putFloat( v[1] );
            buf.putFloat( v[2] );
            buf.putFloat( v[3] / alphaDiv );
            
            v = mat.specularRef();
            buf.putFloat( v[0] );
            buf.putFloat( v[1] );
            buf.putFloat( v[2] );
            buf.putFloat( v[3] / alphaDiv );
            
            v = mat.emissionRef();
            buf.putFloat( v[0] );
            buf.putFloat( v[1] );
            buf.putFloat( v[2] );
            buf.putFloat( v[3] / alphaDiv );
            
            buf.putFloat( mat.shininess() );
            buf.putFloat( alpha );
            buf.put( (byte)0 );
            
            
            //TODO: Fix implementation of save milkeshape.
            //if(mat instanceof ImageMaterial)
            //    image = ((ImageMaterial)mat).getImage();
            //BufferedImage image = null;
            //if( image == null ) {
            putName( null, 128, buf );
            putName( null, 128, buf );
            //}
        }
        
        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
        
    
    private static void saveJoints( MeshModel model, 
                                    ByteBuffer buf,
                                    FileChannel out )
                                        throws IOException 
    {
        
        buf.putFloat(0);
        buf.putFloat(0);
        buf.putInt(0);
        buf.putShort((short)0);

        buf.flip();
        writeBuf(buf, out);
        buf.clear();
    }
        
       

    
    private static void writeBuf(ByteBuffer buf, FileChannel channel) throws IOException {
        while(buf.remaining() > 0)
            if(channel.write(buf) <= 0)
                throw new IOException("Write operation failed.");
    }

    
    private static void putName(String name, int len, ByteBuffer buf) {
        int nameLength = 0;
        
        if(name != null) {
            nameLength = Math.min(len-1, name.length());
            
            for(int i = 0; i < nameLength; i++) {
                buf.put((byte)name.charAt(i));
            }
        }
        
        for(int i = nameLength; i < len; i++)
            buf.put((byte)0);
    }


    private static File getOutputFile(File outDir, String resourceName, int len) {
        int attempt = 0;
        File file;
        
        do{
            String num;
            
            if(attempt == 0) {
                num = "";
            }else{
                num = String.format("_%d", attempt);
            }
                
            String name = resourceName.substring(resourceName.lastIndexOf("/") + 1);
            int dotIndex = name.lastIndexOf(".");
            String ext;
            
            if(dotIndex < 0) {
                ext = "";
            }else{
                ext = name.substring(dotIndex);
                name = name.substring(0, dotIndex);
            }
            
            //Start by reducing the base name length.
            int n = name.length() + ext.length() + num.length();
            
            if(n >= len) {
                int m = Math.min(name.length(), n - len);
                name = name.substring(0, m);
                n -= m;
            
                //Reduce the extension.
                if(n >= len) {
                    m = Math.min(ext.length(), n - len);
                    ext = ext.substring(0, m);
                    n -= m;
                    
                    if(n >= len)
                        return null;
                }
            }

            file = new File(outDir, name + num + ext);
            attempt++;
            
        }while(file.exists());
        
        return file;
    }
}
