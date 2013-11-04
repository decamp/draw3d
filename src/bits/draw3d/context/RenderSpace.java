package bits.draw3d.context;

import java.util.*;


/**
 * Represents space for rendering as a series of one or more RenderTiles.
 * 
 * @author decamp
 */
public class RenderSpace {

    
    private final List<RenderTile> mTileList = new ArrayList<RenderTile>();
    
    
    RenderSpace() {}
    
    
    
    public synchronized RenderTile firstTile() {
        if(mTileList.isEmpty())
            return null;
        
        return mTileList.get(0);
    }
    
    
    public synchronized RenderTile lastTile() {
        if(mTileList.isEmpty())
            return null;
        
        return mTileList.get(mTileList.size() - 1);
    }
    
    
    public synchronized List<RenderTile> tiles() {
        return new ArrayList<RenderTile>(mTileList);
    }
    
    
    
    public void installOnscreen() {
        for(RenderTile t: tiles()) {
            t.installOnscreen();
        }
    }
    
    
    public boolean isVisibleOnscreen() {
        boolean ret = false;
        
        for(RenderTile t: tiles()) {
            if(t.isOffscreen())
                continue;
            
            ret = t.isVisibleOnscreen();
            if(!ret)
                return false;
        }
        
        return ret;
    }
    
    
    public void setVisibleOnscreen(boolean visible) {
        for(RenderTile t: tiles()) {
            t.setVisibleOnscreen(visible);
        }
    }
    
    
    
    synchronized void addTile(RenderTile tile) {
        mTileList.add(tile);
    }
    
}
