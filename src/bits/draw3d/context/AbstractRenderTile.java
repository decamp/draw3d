package bits.draw3d.context;

import java.awt.Component;
import java.awt.Window;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;

import bits.math3d.LongRect;




/**
 * @author decamp
 */
abstract class AbstractRenderTile implements RenderTile {
    
    private final RenderSpace mParent;
    private final int mId;
    private final boolean mIsOffscreen;
    private final boolean mIsFullscreen;
    
    private final Bounder mTargetTileBounder;
    private final Bounder mTargetScreenBounder;
    private final Bounder mRenderBounder;
    
    private GLAutoDrawable mDrawable;
    
        
    AbstractRenderTile( RenderSpace parent,
                        int id,
                        Bounder renderBounder,
                        Bounder targetTileBounder,
                        Bounder targetScreenBounder,
                        GLAutoDrawable drawable,
                        boolean isOffscreen,
                        boolean isFullscreen )
    {
        mParent                = parent;
        mId                    = id;
        mIsOffscreen           = isOffscreen;
        mIsFullscreen          = isFullscreen;
        mRenderBounder         = renderBounder;
        mTargetTileBounder     = targetTileBounder;
        mTargetScreenBounder   = targetScreenBounder;
        mDrawable              = drawable;
        mDrawable.setAutoSwapBufferMode(false);
    }
    
    
    
    public RenderSpace renderSpace() {
        return mParent;
    }

    
    public GLAutoDrawable drawable() {
        return mDrawable;
    }

    
    public GLContext context() {
        return mDrawable.getContext();
    }

    
    
    public int id() {
        return mId;
    }
    
    
    public boolean isFirst() {
        return mParent.firstTile() == this;
    }
    
    
    public boolean isLast() {
        return mParent.lastTile() == this;
    }

    
    public boolean isOffscreen() {
        return mIsOffscreen;
    }

    
    public boolean isFullscreen() {
        return mIsFullscreen;
    }

    
    public boolean isCompleteSpace() {
        return mRenderBounder == null;
    }
    
    
    public LongRect tileBounds() {
        long x = 0;
        long y = 0;
        long w = mDrawable.getWidth();
        long h = mDrawable.getHeight();
        
        if(mTargetTileBounder != null) {
            LongRect target = mTargetTileBounder.getBounds();
            
            if(target != null) {
                x = target.minX();
                y = target.minY();
            }
        }
        
        return LongRect.fromBounds(x, y, w, h);
    }

    
    public LongRect targetTileBounds() {
        if(mTargetTileBounder != null)
            return mTargetTileBounder.getBounds();
        
        return null;
    }

    
    public LongRect renderSpaceBounds() {
        if(mRenderBounder != null)
            return mRenderBounder.getBounds();
        
        return tileBounds();
    }
    
    
    public LongRect screenBounds() {
        if(mIsOffscreen || !(mDrawable instanceof Component))
            return null;
        
        java.awt.Point p = ((Component)mDrawable).getLocationOnScreen();
        long x = p.x;
        long y = p.y;
        long w = mDrawable.getWidth();
        long h = mDrawable.getHeight();
        
        return LongRect.fromBounds(x, y, w, h);
    }
    
    
    public LongRect targetScreenBounds() {
        if(mTargetScreenBounder != null)
            return mTargetScreenBounder.getBounds();
        
        return null;
    }

    
    public void dispose() {
        mDrawable = null;
    }
    
    
    
    public abstract void installOnscreen();
    
    
    public abstract Window windowOnscreen();
    
    
    public abstract boolean isVisibleOnscreen();
    
    
    public abstract void setVisibleOnscreen(boolean visible);

}
