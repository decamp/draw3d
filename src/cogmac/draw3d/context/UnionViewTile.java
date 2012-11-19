package cogmac.draw3d.context;

import java.awt.Window;

import javax.media.opengl.*;

import cogmac.math3d.LongRect;

/**
 * @author decamp
 */
class UnionViewTile implements RenderTile {

    private final RenderSpace mSpace;
    private final RenderTile mParent;
    private final int mId;
    
    
    UnionViewTile(RenderSpace space, RenderTile parent, int id) {
        mSpace  = space;
        mParent = parent;
        mId     = id;
    }
    
    
    public RenderSpace renderSpace() {
        return mSpace;
    }
    
    public GLAutoDrawable drawable() {
        return mParent.drawable();
    }
    
    public GLContext context() {
        return mParent.context();
    }
    
    public int id() {
        return mId;
    }
    
    public boolean isFirst() {
        return true;
    }
    
    public boolean isLast() {
        return true;
    }
    
    public boolean isOffscreen() {
        return true;
    }
    
    public boolean isFullscreen() {
        return false;
    }
    
    public boolean isCompleteSpace() {
        return true;
    }

    public LongRect renderSpaceBounds() {
        return mParent.renderSpaceBounds();
    }
    
    public LongRect tileBounds() {
        return mParent.renderSpaceBounds();
    }
    
    public LongRect targetTileBounds() {
        return mParent.renderSpaceBounds();
    }
    
    public LongRect screenBounds() {
        return null;
    }
    
    public LongRect targetScreenBounds() {
        return null;
    }
    
    public void dispose() {}
    
    public void installOnscreen() {}
    
    public Window windowOnscreen() {
        return null;
    }
    
    public boolean isVisibleOnscreen() {
        return false;
    }
    
    public void setVisibleOnscreen(boolean visible) {}
        
}
