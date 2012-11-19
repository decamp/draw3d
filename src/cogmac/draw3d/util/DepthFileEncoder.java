package cogmac.draw3d.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.zip.*;

import cogmac.png.NativeZLib;

/**
 * @author decamp
 */
public class DepthFileEncoder {
    
    public static final int LEVEL_NO_COMPRESSION   = NativeZLib.Z_NO_COMPRESSION;
    public static final int LEVEL_BEST_SPEED       = NativeZLib.Z_BEST_SPEED;
    public static final int LEVEL_BEST_COMPRESSION = NativeZLib.Z_BEST_COMPRESSION;
    public static final int LEVEL_DEFAULT          = NativeZLib.Z_DEFAULT_COMPRESSION;
    
    
    private ByteBuffer mOut = null;
    
    private NativeZLib mZ       = new NativeZLib();
    private boolean mDeflating  = false;
    private int mComprLevel     = LEVEL_DEFAULT;
    
    
    private int mWidth          = 0;
    private int mHeight         = 0;
    private int mScanBytes      = 0;
    private ByteBuffer mLineBuf = null;
    
    private int mLineCount = 0;
    private int mLinePlace = 0;
    
    
    
    public DepthFileEncoder() {}
    
    
    
    public void open( ByteBuffer out,
                      int width,
                      int height )
                      throws IOException 
    {
        open(out, width, height, LEVEL_DEFAULT);
    }
    
    
    public void open( ByteBuffer out,
                      int width,
                      int height,
                      int comprLevel )
                      throws IOException 
    {
        close();
        mZ.clear();
        mOut = out;
        mComprLevel = comprLevel;
        
        mOut.putInt(0x12344321);
        mOut.putInt(width);
        mOut.putInt(height);
        mOut.putInt(0);
        mOut.putInt(32);
        
        mWidth  = width;
        mHeight = height;
        
        mScanBytes = 4 * width * height;
        mLineCount = 0;
        mLinePlace = 0;
        
        if(mLineBuf == null || mLineBuf.capacity() < mScanBytes) {
            mLineBuf = ByteBuffer.allocateDirect(mScanBytes);
        }
    }
    
        
    public void writeData( ByteBuffer buf ) throws IOException {
        int left = buf.remaining();
        
        while(left > 0) {
            int n = mScanBytes - mLinePlace;
            
            if(left >= n) {
                buf.limit(buf.position() + n);
                mLineBuf.put(buf);
                left -= n;
                flushScan(true);
                mLinePlace = 0;
                
            }else{
                buf.limit(buf.position() + left);
                mLineBuf.put(buf);
                mLinePlace += left;
                break;
            }
        }
    }
    
    
    public void close() throws IOException {
        if(mOut == null)
            return;
        
        flushScan(true);
        flushDeflate();
        mOut = null;
    }
    
    
    
    
    private void flushDeflate() throws IOException {
        if(!mDeflating)
            return;
        
        mDeflating = false;
        while(mZ.flush(mOut) == NativeZLib.Z_OK) {}
    }
    
    
    private void flushScan(boolean finished) throws IOException {
        mLineBuf.flip();
        
        while(mLineBuf.remaining() > 0) {
            mDeflating = true;
            mZ.deflate(mLineBuf, mOut, false, mComprLevel);
        }
        
        mLineBuf.clear();
    }
    
    
    
    public static float[] readDepthFile(File file, int[] outSize, float[] outArr) throws IOException {
        GZIPInputStream gin = new GZIPInputStream(new FileInputStream(file));
        DataInputStream in = new DataInputStream(new BufferedInputStream(gin));
        
        int id     = in.readInt();
        int w      = in.readInt();
        int h      = in.readInt();
        int format = in.readInt();
        int bits   = in.readInt();
        
        outSize[0] = w;
        outSize[1] = h;
        
        if(outArr == null || outArr.length < w * h)
            outArr = new float[w * h];
        
        for(int i = 0; i < w * h; i++) {
            outArr[i] = in.readFloat();
        }
        
        return outArr;
    }
    
}
