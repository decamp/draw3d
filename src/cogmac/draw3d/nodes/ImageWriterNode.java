    package cogmac.draw3d.nodes;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;



import javax.media.opengl.*;
import static javax.media.opengl.GL.*;

import cogmac.draw3d.nodes.DrawNodeAdapter;
import cogmac.draw3d.util.DepthFileEncoder;
import cogmac.langx.ref.*;
import cogmac.png.*;
import cogmac.prototype.OutputFileMaker;


/**
 * Saves various GL buffers to file sequences.  
 * <p>
 * May be configured to use a separate thread for writing or
 * with writes performed synchronously by the rendering
 * thread.  
 * 
 * @author decamp
 */
public class ImageWriterNode extends DrawNodeAdapter {
    
    public static final int LEVEL_NO_COMPRESSION   = NativeZLib.Z_NO_COMPRESSION;
    public static final int LEVEL_BEST_SPEED       = NativeZLib.Z_BEST_SPEED;
    public static final int LEVEL_BEST_COMPRESSION = NativeZLib.Z_BEST_COMPRESSION;
    public static final int LEVEL_DEFAULT          = NativeZLib.Z_DEFAULT_COMPRESSION;
    
    
    
    public static ImageWriterNode newInstance() {
        return newInstance( Runtime.getRuntime().availableProcessors() );
    }
    
    
    public static ImageWriterNode newInstance( int threadCount ) {
        return new ImageWriterNode( threadCount );
    }
    
    
    
    private static final int MAX_QUEUE_SIZE = 2;
    private static final int OVERHEAD       = 1024 * 32;
    
    private static Logger sLog = Logger.getLogger(ImageWriterNode.class.getName());
    
    
    private int mThreadCount;
    private int mMaxQueueSize = MAX_QUEUE_SIZE * mThreadCount;
    private int mPoolSize     = (MAX_QUEUE_SIZE + 1) * mThreadCount;
    
    private final List<FrameReader> mSavers = new ArrayList<FrameReader>();
    private final Exec mExec;
    private final FileWriter mWriter;
    
    private int mWidth  = 10;
    private int mHeight = 10;
    
    private Integer mReadTarget     = null;
    private boolean mDoubleBuffered = true;
    
    
    private ImageWriterNode( int threadCount ) {
        mThreadCount  = Math.max(1, threadCount);
        mMaxQueueSize = MAX_QUEUE_SIZE * mThreadCount;
        mPoolSize     = (MAX_QUEUE_SIZE + 1) * mThreadCount;
        
        mExec         = new Exec(mThreadCount, mMaxQueueSize);
        mWriter       = new FileWriter(mMaxQueueSize);
    }
    
    
    public void readTarget( int readTarget ) {
        mReadTarget = readTarget;
    }
    
    
    

    
    public void addColorWriter( File outDir,
                                int compressionLevel,
                                String nameStart,
                                int minDigits,
                                int numStart )
                                throws IOException 
    {
        if(!outDir.exists() && !outDir.mkdirs())
            throw new IOException("Failed to created directory: " + outDir.getPath());
        
        OutputFileMaker fileMaker = new OutputFileMaker(outDir, nameStart, ".png", minDigits, numStart);
        
        ColorReader saver = new ColorReader( fileMaker,
                                             mExec, 
                                             mWriter, 
                                             compressionLevel,
                                             mThreadCount,
                                             mPoolSize );
        
        if(mWidth >= 0 && mHeight >= 0) {
            saver.setSize(mWidth, mHeight);
        }
        
        mSavers.add(saver);
    }
    
    
    public void addDepthWriter( File outDir,
                                int compressionLevel,
                                String nameStart,
                                int minDigits,
                                int numStart )
                                throws IOException 
    {
        if(!outDir.exists() && !outDir.mkdirs())
            throw new IOException("Failed to create directory: " + outDir.getPath());
        
        OutputFileMaker fileMaker = new OutputFileMaker(outDir, nameStart, ".depth", minDigits, numStart);
        
        DepthReader saver = new DepthReader( fileMaker,
                                             mExec,
                                             mWriter,
                                             compressionLevel,
                                             mThreadCount,
                                             mPoolSize );
        
        if(mWidth >= 0 && mHeight >= 0) {
            saver.setSize(mWidth, mHeight);
        }
        
        mSavers.add(saver);
    }
    

    
    public void init( GLAutoDrawable gld ) {
        mDoubleBuffered = gld.getChosenGLCapabilities().getDoubleBuffered();
    }

    
    @Override
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        mWidth  = w;
        mHeight = h;
        
        for(FrameReader s: mSavers) {
            s.setSize(w, h);
        }
    }

    
    @Override
    public void pushDraw(GL gl) {
        for(FrameReader s: mSavers) {
            s.readFrame(gl);
        }
    }
    
    
    
    
    
    private static interface FrameReader {
        public void setSize(int w, int h);
        public void readFrame(GL gl);
    }
    
    
    private static interface FrameEncoder {
        public ObjectPool<ByteBuffer> pool();
        public ByteBuffer encode(ByteBuffer buffer) throws IOException;
    }
    

    
    
    private static final class Exec implements Executor, Runnable {

        private final int mQueueSize;
        private List<Thread> mThreads;
        private Queue<Runnable> mQ = new LinkedList<Runnable>();
        private boolean mClosed = false;
        
        
        public Exec(int threadCount, int queueSize) {
            mQueueSize = queueSize;
            
            mThreads = new ArrayList<Thread>(threadCount);
            
            for(int i = 0; i < threadCount; i++) {
                Thread t = new Thread(this, "ExecThread");
                mThreads.add(t);
                t.start();
            }
        }
        
        
        
        public synchronized void execute(Runnable run) {
            while(mQ.size() >= mQueueSize) {
                try {
                    wait();
                }catch(InterruptedException ex) {}
            }
            
            mQ.offer(run);
            notifyAll();
        }
        
        
        public void run() {
            while(true) {
                Runnable r = null;
                
                synchronized(this) {
                    if(mClosed)
                        return;
                 
                    r = mQ.poll();
                    
                    if(r == null) {
                        try{
                            wait();
                        }catch(InterruptedException ex) {}
                        
                        continue;
                    }
                    
                    notifyAll();
                }
                
                try {
                    r.run();
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
    

    private static final class FileWriter extends Thread {
        
        private final Queue<WriteTask> mQ = new LinkedList<WriteTask>();
        private final int mMaxQueueSize;
        
        
        FileWriter(int maxQueueSize) {
            super("ImageWriterThread");
            mMaxQueueSize = maxQueueSize;
            start();
        }
        

        
        public synchronized void waitForQueue() {
            try {
                while(mQ.size() >= mMaxQueueSize) {
                    wait();
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        
        
        public synchronized void offer(WriteTask buf) {
            waitForQueue();
            mQ.offer(buf);
            notifyAll();
        }
        
        
        @Override
        public void run() {
            while(true) {
                WriteTask task = null;
                
                synchronized(this) {
                    task = mQ.poll();
                    
                    if(task == null) {
                        try{
                            wait();
                        }catch(InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        
                        continue;
                    }
                    
                    notifyAll();
                }
                
                try {
                    FileChannel chan = new FileOutputStream(task.mFile).getChannel();
                    while(task.mOut.remaining() > 0) {
                        chan.write(task.mOut);
                    }
                    chan.close();
                    
                    task.deref();
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }
    
    
    private final class ColorReader implements FrameReader {
        
        private final OutputFileMaker mFileMaker;
        private final Executor mExec;
        private final FileWriter mWriter;
        private final List<ColorEncoder> mEncoders;
        private final ObjectPool<ByteBuffer> mPool;
        
        private int mWidth        = -1;
        private int mHeight       = -1;
        
        int mCount = 0;
        
        
        ColorReader( OutputFileMaker fileMaker, 
                     Executor exec, 
                     FileWriter writer,
                     int compLevel,
                     int encodeThreads,
                     int poolSize )
        {
            mFileMaker = fileMaker;
            mExec      = exec;
            mEncoders  = new ArrayList<ColorEncoder>(encodeThreads);
            mPool      = new HardPool<ByteBuffer>(poolSize);
            mWriter    = writer;
            
            encodeThreads = Math.max(1, encodeThreads);
            
            for(int i = 0; i < encodeThreads; i++) {
                mEncoders.add(new ColorEncoder(mPool, compLevel));
            }
        }
        
        
        
        public void setSize(int w, int h) {
            mWidth  = w;
            mHeight = h;
        }
        
        
        public void readFrame(GL gl) {
            final int w = mWidth;
            final int h = mHeight;
            
            int cap = w * h * 4 + 8;
            ByteBuffer buf = mPool.poll();
            
            if(buf == null || buf.capacity() < cap) {
                //System.out.println("## Alloc");
                buf = ByteBuffer.allocateDirect(cap + OVERHEAD);
            }else{
                buf.clear();
            }
            
            buf.order(ByteOrder.nativeOrder());
            buf.putInt(w);
            buf.putInt(h);
            
            if( mReadTarget != null ) {
                gl.glReadBuffer( mReadTarget );
            } else {
                gl.glReadBuffer( mDoubleBuffered ? GL_BACK : GL_FRONT );
            }
            
            gl.glReadPixels(0, 0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            buf.position(0).limit(cap);
            
            FrameEncoder enc = mEncoders.get(mCount++ % mEncoders.size());
            WriteTask task = new WriteTask( mPool, 
                                           buf, 
                                           mFileMaker.getNextFile(),
                                           enc,
                                           mWriter );
            
            //task.run();
            mExec.execute(task);
        }
    
    }
    
    
    private static final class ColorEncoder implements FrameEncoder {
        
        private final PngBufferWriter mComp = new PngBufferWriter();
        private final ObjectPool<ByteBuffer> mPool;
        private final int mCompLevel;
        
        
        ColorEncoder(ObjectPool<ByteBuffer> pool, int compLevel) {
            mPool      = pool;
            mCompLevel = compLevel;
        }
        
        
        
        public ObjectPool<ByteBuffer> pool() {
            return mPool;
        }
        
        
        public synchronized ByteBuffer encode(ByteBuffer buffer) throws IOException {
            final int w = buffer.getInt();
            final int h = buffer.getInt();
            
            int p0 = buffer.position();
            int cap = w * h * 4;
            
            ByteBuffer ret = mPool.poll();
            
            if(ret == null || ret.capacity() < cap) {
                //System.out.println("## Alloc 2");
                ret = ByteBuffer.allocateDirect(cap + OVERHEAD);
            }else{
                ret.clear();
            }

            ret.order(ByteOrder.BIG_ENDIAN);
            mComp.open(ret, w, h, PngBufferWriter.COLOR_TYPE_RGBA, 8, mCompLevel);
            
            for(int y = 0; y < h; y++) {
                int m0 = p0 + (h - y - 1) * w * 4;
                int m1 = m0 + w * 4;
                buffer.position(0).limit(m1).position(m0);
                mComp.writeData(buffer);
            }
            
            mComp.close();

            ret.flip();
            return ret;
        }
        
    }
    
    
    private final class DepthReader implements FrameReader {
        
        private final FileWriter mWriter;
        private final Executor mExec;
        private final OutputFileMaker mFileMaker;
        private final List<DepthEncoder> mEncoders;
        private final ObjectPool<ByteBuffer> mPool;
        
        private int mWidth  = 0;
        private int mHeight = 0;
        int mCount = 0;
        
        
        DepthReader( OutputFileMaker fileMaker,
                     Executor exec,
                     FileWriter writer,
                     int compLevel,
                     int encodeThreads,
                     int poolSize )
        {
            mFileMaker = fileMaker;
            mExec      = exec;
            mEncoders  = new ArrayList<DepthEncoder>(encodeThreads);
            mWriter    = writer;
            mPool      = new HardPool<ByteBuffer>(poolSize);
            
            encodeThreads = Math.max(1, encodeThreads);
            
            for(int i = 0; i < encodeThreads; i++) {
                mEncoders.add(new DepthEncoder(mPool, compLevel));
            }
        }
   
        
        
        public void setSize(int w, int h) {
            mWidth  = w;
            mHeight = h;
        }
        
        
        public void readFrame(GL gl) {
            final int w = mWidth;
            final int h = mHeight;
            final int cap = w * h * 4 + 8;
            
            ByteBuffer buf = mPool.poll();
            
            if(buf == null || buf.capacity() < cap) {
                buf = ByteBuffer.allocateDirect(cap + OVERHEAD);
            }else{
                buf.clear();
            }
            
            buf.order(ByteOrder.nativeOrder());
            buf.putInt(w);
            buf.putInt(h);
            
            if( mReadTarget != null ) {
                gl.glReadBuffer( mReadTarget );
            } else {
                gl.glReadBuffer( mDoubleBuffered ? GL_BACK : GL_FRONT );
            }
            
            gl.glReadPixels(0, 0, w, h, GL_DEPTH_COMPONENT, GL_FLOAT, buf);
            
            buf.position(0).limit(cap);
            FrameEncoder enc = mEncoders.get(mCount++ % mEncoders.size());
            
            WriteTask task = new WriteTask( mPool, 
                                           buf, 
                                           mFileMaker.getNextFile(),
                                           enc,
                                           mWriter );
            
            //task.run();
            mExec.execute(task);
        }
        
    }
    
    
    private static final class DepthEncoder implements FrameEncoder {
        
        
        private final DepthFileEncoder mEnc = new DepthFileEncoder();
        private final ObjectPool<ByteBuffer> mPool;
        private final int mCompLevel;        
        
        
        DepthEncoder(ObjectPool<ByteBuffer> pool, int compLevel) {
            mPool      = pool;
            mCompLevel = compLevel;
        }
        
        
        
        public ObjectPool<ByteBuffer> pool() {
            return mPool;
        }
        
        
        public synchronized ByteBuffer encode(ByteBuffer buffer) throws IOException {
            final int w = buffer.getInt();
            final int h = buffer.getInt();
            
            int p0 = buffer.position();
            int cap = w * h * 4;
            
            ByteBuffer ret = mPool.poll();
            
            if(ret == null || ret.capacity() < cap) {
                //System.out.println("## Alloc 2");
                ret = ByteBuffer.allocateDirect(cap + OVERHEAD);
            }else{
                ret.clear();
            }
            
            ret.order(ByteOrder.BIG_ENDIAN);
            mEnc.open(ret, w, h, mCompLevel);
            
            for(int y = 0; y < h; y++) {
                int m0 = p0 + (h - y - 1) * w * 4;
                int m1 = m0 + w * 4;
                buffer.position(0).limit(m1).position(m0);
                mEnc.writeData(buffer);
            }
            
            mEnc.close();
            
            ret.flip();
            return ret;
        }
        
    }
    
    
    private static final class WriteTask implements Runnable {
        
        final ObjectPool<ByteBuffer> mInPool;
        ByteBuffer mIn;
        final File mFile;
        
        final FrameEncoder mEncoder;
        final FileWriter mWriter;
        
        ByteBuffer mOut = null;
        
        
        WriteTask( ObjectPool<ByteBuffer> inPool, 
                   ByteBuffer in, 
                   File file,
                   FrameEncoder encoder,
                   FileWriter writer )
        {         
            mInPool  = inPool;
            mIn      = in;
            mFile    = file;
            
            mEncoder = encoder;
            mWriter  = writer;
        }
        
        
        
        public void run() {
            try {
                mOut = mEncoder.encode(mIn);
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            
            mWriter.offer(this);
        }
        
        
        void deref() {
            if(mIn != null && mInPool != null) {
                mInPool.offer(mIn);
            }
            
            mIn = null;
            
            
            ObjectPool<ByteBuffer> pool = mEncoder.pool();
            
            if(mOut != null && pool != null) {
                pool.offer(mOut);
           }
            
            mOut = null;
        }
        
    }
    
    
    
}
