package bits.draw3d.util;

import java.util.*;
import java.util.regex.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.*;

import javax.imageio.ImageIO;

import bits.png.PngFileWriter;




/**
 * @author decamp
 */
public class ImageCombiner {
    
    
    public static File combine( File dir, File outFile ) throws IOException {
        Pattern pat = Pattern.compile( "(\\d++)\\.png", Pattern.CASE_INSENSITIVE );
        File[] files = dir.listFiles();
        
        if( files == null || files.length == 0 )
            return null;
        
        List<IndexedFile> indexed = new ArrayList<IndexedFile>();
        
        for( File f: files ) {
            Matcher m = pat.matcher( f.getName() );
            if( !m.matches() )
                continue;
        
            indexed.add( new IndexedFile( Integer.parseInt( m.group(1) ), f ) );
        }
        
        if( indexed.isEmpty() )
            return null;
        
        Collections.sort( indexed );
        List<File> list = new ArrayList<File>();
        
        for( IndexedFile i: indexed ) {
            list.add( i.mFile );
        }
        
        int dim = (int)(Math.sqrt( indexed.size() ) + 0.5 );
        
        if( outFile == null ) {
            String path = "";
        
            if( dir.getParentFile() != null ) {
                path = dir.getParentFile().getName() + "_";
            }
        
            path += dir.getName() + "_combined.png";
            outFile = new File( dir.getParentFile(), path );
        }
        
        combine( list, dim, dim, outFile );
        
        return outFile;
    }
    
    
    public static void combine( List<File> files, int cols, int rows, File out ) throws IOException {
        PngFileWriter writer = new PngFileWriter();
        int[] pix      = null;
        ByteBuffer buf = null;
        
        int ow = 0;
        int oh = 0;
        
        for( int row = 0; row < rows; row++ ) {
            BufferedImage im = ImageIO.read( files.get( row * cols ) );
            oh += im.getHeight();
        }
        
        
        for(int row = 0; row < rows; row++ ) {
            BufferedImage[] ims = new BufferedImage[cols];
            
            for( int col = 0; col < cols; col++ ) {
                ims[col] = ImageIO.read( files.get( row * cols + col ) );
            }
            
            if( row == 0 ) {
                for( int col = 0; col < cols; col++ ) {
                    ow += ims[col].getWidth();
                }
                
                pix = new int[ow];
                writer.open( out, ow, oh, PngFileWriter.COLOR_TYPE_RGBA, 8, PngFileWriter.LEVEL_BEST_COMPRESSION, null );
                buf = ByteBuffer.allocateDirect( ow * 4 );
                buf.order( ByteOrder.BIG_ENDIAN );
            }
            
            int h = ims[0].getHeight();
        
            for( int y = 0; y < h; y++ ) {
                int x = 0;
                
                for( int col = 0; col < cols; col++ ) {
                    int w = ims[col].getWidth();
                    ims[col].getRGB( 0, y, w, 1, pix, x, w );
                    x += w;
                }
                
                buf.clear();
                
                for( int i = 0; i < ow; i++ ) {
                    buf.put( (byte)((pix[i] >> 16) & 0xFF) );
                    buf.put( (byte)((pix[i] >>  8) & 0xFF) );
                    buf.put( (byte)((pix[i]      ) & 0xFF) );
                    buf.put( (byte)(pix[i] >> 24) );
                }
                
                buf.flip();
                writer.writeData( buf );
            }
        }
        
        writer.close();
    }
    
    
    private static final class IndexedFile implements Comparable<IndexedFile> {
        
        final int mIndex;
        final File mFile;
        
        IndexedFile( int index, File file ) {
            mIndex = index;
            mFile  = file;
        }
        
        
        public int compareTo( IndexedFile f ) {
            return mIndex - f.mIndex;
        }
        
    }

}
