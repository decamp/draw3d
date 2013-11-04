package bits.draw3d.render;

/**
 * @author decamp
 */
public class RenderDriver {
    
    private final Renderer mRend;
    private final long mMinFrameMicros;
    
    private Thread mThread = null;
    
    
    public RenderDriver(Renderer rend) {
        this(rend, 0.0);
    }

    
    public RenderDriver(Renderer rend, double maxFps) {
        mRend = rend;
        
        if(maxFps <= 0.0) {
            mMinFrameMicros = 0;
        }else{
            mMinFrameMicros = (long)(1000000.0 / maxFps);
        }
    }
    
    
    
    public synchronized void start() {
        if(mThread != null)
            return;
        
        mThread = new Thread("Render Thread"){
            public void run() {
                runLoop();
            }
        };
        
        mThread.start();
    }
    
    
    public synchronized void stop() {
        if(mThread == null)
            return;
        
        mThread = null;
        notifyAll();
    }
    
    
    
    private void runLoop() {
        mRend.init();
        long prevMicros = Long.MIN_VALUE;
        
        while(true) {
            long t = System.currentTimeMillis() * 1000L;
            
            synchronized(this) {
                if(mThread == null)
                    break;
                
                long waitMillis = (mMinFrameMicros - (t - prevMicros)) / 1000L;
                
                if(waitMillis > 10L && prevMicros != Long.MIN_VALUE) {
                    try{
                        wait(waitMillis);
                    }catch(InterruptedException ex) {}
                    
                    continue;
                }
                
                prevMicros = t;
            }
            
            mRend.draw();
            mRend.finish();
        }
        
        mRend.dispose();
    }
    

}
