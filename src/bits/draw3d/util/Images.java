package bits.draw3d.util;

import static javax.media.opengl.GL.*;

import java.awt.*;
import java.awt.image.*;
import java.nio.*;

/**
 * @author decamp
 */
public class Images {

    public static final int RED   = 0;
    public static final int GREEN = 1;
    public static final int BLUE  = 2;
    public static final int ALPHA = 3;
    
    
    /**
     * Returns equivalent OpenGL internalFormat, format and data types for a BufferedImage.
     * (e.g., GL_BGRA and GL_UNSIGNED_BYTE). It will also specify if the ordering of 
     * the DataBuffer component values must be reversed to achieve a GL-compatible format.
     * 
     * @param image Some image 
     * @param out4  Length-4 array to hold output. On return: <br>
     *              out3[0] will hold INPUT FORMAT for image. <br>
     *              out3[1] will hold FORMAT for image. <br>
     *              out3[2] will hold DATA TYPE for image.
     *              out3[3] will equal 1 if component values must be swapped (reverse-ordered), otherwise 0. 
     * @return true if equivalent format and data type were found
     */
    public static boolean glFormatFor( BufferedImage image, int[] out4 ) {
        switch( image.getType() ) {
        case BufferedImage.TYPE_USHORT_GRAY:
            out4[0] = GL_RED;
            out4[1] = GL_RED;
            out4[2] = GL_UNSIGNED_SHORT;
            out4[3] = 0;
            return true;
        
        case BufferedImage.TYPE_BYTE_GRAY:
            out4[0] = GL_RED;
            out4[1] = GL_RED;
            out4[2] = GL_UNSIGNED_BYTE;
            out4[3] = 0;
            return true;
            
        case BufferedImage.TYPE_INT_BGR:
            out4[0] = GL_RGBA;
            out4[1] = GL_BGRA;
            out4[2] = GL_UNSIGNED_BYTE;
            out4[3] = 0;
            return true;
        
        case BufferedImage.TYPE_3BYTE_BGR:
            out4[0] = GL_RGB;
            out4[1] = GL_BGR;
            out4[2] = GL_UNSIGNED_BYTE;
            out4[3] = 0;
            return true;
            
        case BufferedImage.TYPE_INT_RGB:
            out4[0] = GL_RGBA;
            out4[1] = GL_RGBA;
            out4[2] = GL_UNSIGNED_BYTE;
            out4[3] = 0;
            return true;
        
        case BufferedImage.TYPE_USHORT_555_RGB:
            out4[0] = GL_RGB;
            out4[1] = GL_RGB;
            out4[2] = GL_UNSIGNED_SHORT_5_5_5_1;
            out4[3] = 0;
            return true;
            
        case BufferedImage.TYPE_USHORT_565_RGB:
            out4[0] = GL_RGB;
            out4[1] = GL_RGB;
            out4[2] = GL_UNSIGNED_SHORT_5_5_5_1;
            out4[3] = 0;
            return true;
                        
        case BufferedImage.TYPE_4BYTE_ABGR:
        case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            return false;
            
        case BufferedImage.TYPE_INT_ARGB:
        case BufferedImage.TYPE_INT_ARGB_PRE:
            out4[0] = GL_RGBA;
            out4[1] = GL_BGRA;
            out4[2] = GL_UNSIGNED_BYTE;
            out4[3] = 1;
            return true;
            
        case BufferedImage.TYPE_BYTE_BINARY:
        case BufferedImage.TYPE_BYTE_INDEXED:
        case BufferedImage.TYPE_CUSTOM:
        default:
            return false;
        }
    }
    
    /**
     * Converts a BufferedImage to a 32-bit BGRA format and places it into
     * a directly allocated java.nio.ByteBuffer.
     * 
     * @param image    Input image to convert.
     * @param optWork  [Optional] array that may be used if {@code workSpace.length >= image.getWidth() }.
     * @return Directly allocated ByteBuffer containing pixels in BGRA format and sRGB color space.
     */
    public static ByteBuffer imageToBgraBuffer( BufferedImage image, int[] optWork ) {
        int w = image.getWidth();
        int h = image.getHeight();
        int[] row = optWork != null && optWork.length >= w ? optWork : new int[w];

        ByteBuffer ret = ByteBuffer.allocateDirect( ( w * h ) * 4 );
        ret.order( ByteOrder.LITTLE_ENDIAN );
        IntBuffer ib = ret.asIntBuffer();

        for( int i = 0; i < h; i++ ) {
            image.getRGB( 0, i, w, 1, row, 0, w );
            ib.put( row, 0, w );
        }

        return ret;
    }
    
    /**
     * Converts a BufferedImage to a directly allocated java.nio.ByteBuffer.
     * This method will first check to see if the image can be ported directly to a
     * GL compatible format. If so, it will dump the data directly without conversion
     * via {@link #dataToByteBuffer}. If not, it will convert the image via
     * {@link #imageToBgraBuffer}.
     *
     * @param optOutFormat  Holds GL enum values for buffe representation. See {@link #glFormatFor}.
     * @return Directly allocated byte buffer holding image.
     */
    public static ByteBuffer imageToByteBuffer( BufferedImage image, int[] optOutFormat ) {
        return imageToByteBuffer( image, null, optOutFormat );
    }

    /**
     * Converts a BufferedImage to a directly allocated java.nio.ByteBuffer.
     * This method will first check to see if the image can be ported directly to a
     * GL compatible format. If so, it will dump the data directly without conversion
     * via {@link #dataToByteBuffer}. If not, it will convert the image via
     * {@link #imageToBgraBuffer}.
     *
     * @param optOutFormat  Holds GL enum values for buffe representation. See {@link #glFormatFor}.
     * @param optWorkspace  Optional array that may be used if <code>workSpace.length &gt;= image.getWidth()</code>.
     * @return Directly allocated byte buffer holding image.
     */
    public static ByteBuffer imageToByteBuffer( BufferedImage image, int[] optWorkspace, int[] optOutFormat ) {

        if( optOutFormat == null || optOutFormat.length < 4 ) {
            optOutFormat = new int[4];
        }
        
        if( !glFormatFor( image, optOutFormat ) ) {
            optOutFormat[0] = GL_RGBA;
            optOutFormat[1] = GL_BGRA;
            optOutFormat[2] = GL_UNSIGNED_BYTE;
            optOutFormat[3] = 0;
            return imageToBgraBuffer( image, optWorkspace );
        }

        ByteOrder order;
        
        if( optOutFormat[2] == GL_UNSIGNED_BYTE || optOutFormat[2] == GL_BYTE ) {
            order = optOutFormat[3] == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        } else {
            if( optOutFormat[3] == 0 ) {
                order = ByteOrder.nativeOrder();
            } else {
                optOutFormat[0] = GL_RGBA;
                optOutFormat[1] = GL_BGRA;
                optOutFormat[2] = GL_UNSIGNED_BYTE;
                optOutFormat[3] = 0;
                return imageToBgraBuffer( image, optWorkspace );
            }
        }
        
        return dataToByteBuffer( image.getData().getDataBuffer(), order );
    }
    
    /**
     * Converts a DataBuffer to a directly allocated java.nio.ByteBuffer.
     */
    public static ByteBuffer dataToByteBuffer( DataBuffer in, ByteOrder order ) {
        int type      = in.getDataType();
        int elSize    = DataBuffer.getDataTypeSize( type );
        int count     = in.getSize();
        
        ByteBuffer ret = ByteBuffer.allocateDirect( ( count * elSize + 7 ) / 8 );
        ret.order( order );
        
        switch( type ) {
        case DataBuffer.TYPE_BYTE:
        {
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                ret.put( ( (DataBufferByte)in ).getData( i ) );
            }
            ret.flip();
            break;
        }
            
        case DataBuffer.TYPE_INT:
        {
            IntBuffer b = ret.asIntBuffer();
            
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferInt)in ).getData(i) );
            }
            
            break;
        }
        
        case DataBuffer.TYPE_FLOAT:
        {
            FloatBuffer b = ret.asFloatBuffer();
            
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferFloat)in ).getData(i) );
            }
            
            break;
        }
            
        case DataBuffer.TYPE_DOUBLE:
        {
            DoubleBuffer b = ret.asDoubleBuffer();
            
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferDouble)in ).getData( i ) );
            }
            
            break;
        }
        
        case DataBuffer.TYPE_SHORT:
        {
            ShortBuffer b = ret.asShortBuffer();
            
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ((DataBufferShort)in ).getData( i ) );
            }
            
            break;
        }
        
        case DataBuffer.TYPE_USHORT:
        {
            ShortBuffer b = ret.asShortBuffer();
            
            for( int i = 0; i < in.getNumBanks(); i++ ) {
                b.put( ( (DataBufferUShort)in ).getData( i ) );
            }
            
            break;
        }
        
        default:
            throw new IllegalArgumentException( "Unknown data buffer type: " + type );
        }
        
        return ret;
    }
    
    
    public static float[][] imageToRgbPlanes( BufferedImage image, int[] optWork, float[][] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[3][w*h];
        }
        
        final float[] cr = optOut[0];
        final float[] cg = optOut[1];
        final float[] cb = optOut[2];
        final float scale = 1f / 255f;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int v = optWork[x];
                cr[i] = ( v >> 16 & 0xFF ) * scale;
                cg[i] = ( v >>  8 & 0xFF ) * scale;
                cb[i] = ( v       & 0xFF ) * scale;
            }
        }
        
        return optOut;
    }


    public static float[][] imageToRgbaPlanes( BufferedImage image, int[] optWork, float[][] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[4][w*h];
        }
        
        final float[] cr = optOut[0];
        final float[] cg = optOut[1];
        final float[] cb = optOut[2];
        final float[] ca = optOut[3];
        final float scale = 1f / 255f;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int v = optWork[x];
                cr[i] = ( v >> 16 & 0xFF ) * scale;
                cg[i] = ( v >>  8 & 0xFF ) * scale;
                cb[i] = ( v       & 0xFF ) * scale;
                ca[i] = ( v >> 24 & 0xFF ) * scale;
            }
        }
        
        return optOut;
    }
    
    
    public static float[] imageToPlane( BufferedImage image, int component, int[] optWork, float[] optOut ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        if( optOut == null ) {
            optOut = new float[w*h];
        }
        
        final int shift;
        switch( component ) {
        case RED:
            shift = 16;
            break;
        case GREEN:
            shift = 8;
            break;
        case BLUE:
            shift = 0;
            break;
        default:
            shift = 24;
            break;
        }
        
        final float scale = 1f / 255f;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optOut[ x + y * w ] = ( optWork[x] >> shift & 0xFF ) * scale;
            }
        }
        
        return optOut;
    }
    
    
    public static BufferedImage rgbPlanesToImage( float[][] planes, 
                                                  int w,
                                                  int h, 
                                                  int[] optWork, 
                                                  BufferedImage optOut ) 
    {
        if( optOut == null ) {
            optOut = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        }
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        final float[] pr = planes[0];
        final float[] pg = planes[1];
        final float[] pb = planes[2];
        
        for( int y = 0; y < h; y++ ) {
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int cr = (int)( pr[i] * 255f + 0.5f ) & 0xFF;
                int cg = (int)( pg[i] * 255f + 0.5f ) & 0xFF;
                int cb = (int)( pb[i] * 255f + 0.5f ) & 0xFF;
                optWork[x] = 0xFF000000 | cr << 16 | cg << 8 | cb;
            }
            optOut.setRGB( 0, y, w, 1, optWork, 0, w );
        }
        
        return optOut;
    }
    

    public static BufferedImage rgbaPlanesToImage( float[][] planes, 
                                                   int w,
                                                   int h, 
                                                   int[] optWork, 
                                                   BufferedImage optOut ) 
    {
        if( optOut == null ) {
            optOut = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        }
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        final float[] pr = planes[0];
        final float[] pg = planes[1];
        final float[] pb = planes[2];
        final float[] pa = planes[3];

        for( int y = 0; y < h; y++ ) {
            for( int x = 0; x < w; x++ ) {
                int i = x + y * w;
                int cr = (int)( pr[i] * 255f + 0.5f ) & 0xFF;
                int cg = (int)( pg[i] * 255f + 0.5f ) & 0xFF;
                int cb = (int)( pb[i] * 255f + 0.5f ) & 0xFF;
                int ca = (int)( pa[i] * 255f + 0.5f ) & 0xFF;
                optWork[x] = ca << 24 | cr << 16 | cg << 8 | cb;
            }
            optOut.setRGB( 0, y, w, 1, optWork, 0, w );
        }

        return optOut;
    }



    public static void flipVertical( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < 2 * w ) {
            optWork = new int[2 * w];
        }

        for( int y0 = 0; y0 < h / 2; y0++ ) {
            int y1 = h - y0 - 1;
            image.getRGB( 0, y0, w, 1, optWork, 0, w );
            image.getRGB( 0, y1, w, 1, optWork, w, w );
            image.setRGB( 0, y0, w, 1, optWork, w, w );
            image.setRGB( 0, y1, w, 1, optWork, 0, w );
        }
    }



    public static void invert( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = 0xFF - ( v >>> 24        );
                int r = 0xFF - ( v >>  16 & 0xFF );
                int g = 0xFF - ( v >>   8 & 0xFF );
                int b = 0xFF - ( v        & 0xFF );
                optWork[x] = a | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void invertRgb( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
        
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = v & 0xFF000000;
                int r = 0xFF - ( v >> 16 & 0xFF);
                int g = 0xFF - ( v >>  8 & 0xFF);
                int b = 0xFF - ( v       & 0xFF);
                optWork[x] = a | r << 16 | g << 8 | b;
            }
            
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void invertAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = 0xFF - ( v >>> 24 );
                optWork[x] = v & 0x00FFFFFF | a << 24;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void meanRgbToAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );

            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int r = v >> 16 & 0xFF;
                int g = v >>  8 & 0xFF;
                int b = v       & 0xFF;
                optWork[x] = v & 0x00FFFFFF | ( r + g + b ) / 3 << 24;
            }

            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void alphaToRgb( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = ( optWork[x] >>> 24 ) * 0x01010101;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void multRgb( BufferedImage image, int value, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = v >>> 24;
                int r = ( v >> 16 & 0xFF ) * value / 0xFF;
                int g = ( v >>  8 & 0xFF ) * value / 0xFF;
                int b = ( v       & 0xFF ) * value / 0xFF;
                optWork[x] = a << 24 | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }

    
    public static void multRgbByAlpha( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int a = v >>> 24;
                int r = ( v >> 16 & 0xFF ) * a / 0xFF;
                int g = ( v >>  8 & 0xFF ) * a / 0xFF;
                int b = ( v       & 0xFF ) * a / 0xFF;
                optWork[x] = a << 24 | r << 16 | g << 8 | b;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void fillRgb( BufferedImage image, int rgb, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        rgb &= 0x00FFFFFF;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = optWork[x] & 0xFF000000 | rgb;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    public static void fillAlpha( BufferedImage image, int alpha, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        alpha = ( alpha & 0xFF ) << 24;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                optWork[x] = optWork[x] & 0x00FFFFFF | alpha;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void fillTransparentRgb( BufferedImage image, int rgb, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        rgb &= 0x00FFFFFF;
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                if( ( optWork[x] & 0xFF000000 ) == 0 ) {
                    optWork[x] = optWork[x] & 0xFF000000 | rgb;
                }
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }


    public static void desaturate( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }

        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                int r = v >> 16 & 0xFF;
                int g = v >>  8 & 0xFF;
                int b = v       & 0xFF;
                optWork[x] = v & 0xFF000000 | ( ( r + g + b ) / 3 * 0x00010101 );
            }

            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }

    
    public static void swapRedBlue( BufferedImage image, int[] optWork ) {
        final int w = image.getWidth();
        final int h = image.getHeight();
        if( optWork == null || optWork.length < w ) {
            optWork = new int[w];
        }
        
        for( int y = 0; y < h; y++ ) {
            image.getRGB( 0, y, w, 1, optWork, 0, w );
            for( int x = 0; x < w; x++ ) {
                int v = optWork[x];
                optWork[x] = v & 0xFF00FF00 | v >> 16 & 0xFF | v << 16 & 0x00FF0000;
            }
            image.setRGB( 0, y, w, 1, optWork, 0, w );
        }
    }
    
    
    
    public static BufferedImage toArgb( BufferedImage im ) {
        if( im.getType() == BufferedImage.TYPE_INT_ARGB ) {
            return im;
        }
        
        final int w = im.getWidth();
        final int h = im.getHeight();
        BufferedImage ret = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setComposite( AlphaComposite.Src );
        g.drawImage( im, 0, 0, null );
        return ret;
    }
    
    
    public static BufferedImage toGrayscale( BufferedImage im ) {
        if( im.getType() == BufferedImage.TYPE_BYTE_GRAY ) {
            return im;
        }
        
        final int w = im.getWidth();
        final int h = im.getHeight();
        BufferedImage ret = new BufferedImage( w, h, BufferedImage.TYPE_BYTE_GRAY );
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setComposite( AlphaComposite.Src );
        g.drawImage( im, 0, 0, null );
        return ret;
    }
    
    
    public static BufferedImage resizeBilinear( BufferedImage im, int w, int h ) {
        BufferedImage ret = new BufferedImage(w, h, im.getType());
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.drawImage( im, 0, 0, w, h, null );
        return ret;
    }
    
    
    public static BufferedImage resizeBicubic( BufferedImage im, int w, int h ) {
        BufferedImage ret = new BufferedImage(w, h, im.getType());
        Graphics2D g = (Graphics2D)ret.getGraphics();
        g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
        g.drawImage( im, 0, 0, w, h, null );
        return ret;
    }
    
}
