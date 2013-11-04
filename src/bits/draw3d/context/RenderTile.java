package bits.draw3d.context;

import java.awt.Window;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;

import bits.math3d.LongRect;


/**
 * For multi-part, multi-screen rendering, the different 
 * rendering contexts and viewports and represented by 
 * RenderTile objects.  Each RenderTile corresponds to
 * one GLDrawable, and contains additional information 
 * about the overall render space and the portion of the
 * render space represented by the tile.
 * <p>
 * For convenience, RenderTiles can also manage screen resources,
 * which must be requested by calling <tt>installOnScreen()</tt>.
 * For onscreen RenderTiles, this causes <tt>java.awt.Frame</tt>
 * objects or other appropriate GUI elements to be instantiated,
 * and installs the GL Drawables.  
 * <p>
 * RenderTiles should be thread-safe.
 *  
 * @author decamp
 */
public interface RenderTile {
    
    
    /**
     * @return RenderSpace to which this tile belongs. 
     */
    public RenderSpace renderSpace();

    /**
     * @return the drawable used to draw this RenderTile.
     */
    public GLAutoDrawable drawable();
    
    /**
     * Equivalent of calling drawable().getContext().
     * @return GLContext for this tile.
     */
    public GLContext context();
    
    /**
     * @return id of this tile
     */
    public int id();
    
    /**
     * @return true iff this Tile is ordered first in its RenderSpace.
     */
    public boolean isFirst();
    
    /**
     * @return true iff this Tile is ordered last in its RenderSpace.
     */
    public boolean isLast();
    
    /**
     * @return true iff this Tile represents an offscreen drawable space.
     */
    public boolean isOffscreen();

    /**
     * @return true iff this Tile represents a fullscreen drawable space.
     */
    public boolean isFullscreen();
    
    /**
     * Indicates whether this Tile represents a full, independent render space,
     * or only part of one.  If the RenderTile is a complete space, then
     * <code>renderSpaceBounds().equals(tileBounds())</code>.  Note that it is
     * possible to have multiple complete tiles in a given RenderSpace.
     * 
     * @return true iff this Tile represents a full, independent render space.
     */
    public boolean isCompleteSpace();
    
    
    /**
     * @return the bounds of the full rendering space, in pixels.
     */
    public LongRect renderSpaceBounds();
    
    /**
     * @return the bounds of this RenderTile, in pixels
     */
    public LongRect tileBounds();
    
    /**
     * While <code>tileBounds()</code> returns the actual, current bounds
     * of the tile, <code>targetTileBounds()</code> returns the preferred 
     * or initial bounds. Result may be <code>null</code>.
     */
    public LongRect targetTileBounds();
    
    /**
     * @return the onscreen bounds of this Tile's drawable.  May be <code>null</code>. 
     */
    public LongRect screenBounds();
    
    /**
     * While <code>screenBounds()</code> returns the actual, current bounds
     * of the tile, <code>targetScreenBounds()</code> returns the preferred 
     * bounds. Result may be <code>null</code>.
     */
    public LongRect targetScreenBounds();
    

    
    /**
     * Releases all drawable and screen resources. 
     */
    public void dispose();

    
    
    /**
     * Instantiates screen resources for Tile and installs drawable.
     */
    public void installOnscreen();
    
    /**
     * Returns the java.awt.Frame object containing this tile, if exists.
     * This method should return NULL if the tile is not installed.
     * 
     * @return frame containing this tile  
     */
    public Window windowOnscreen();
    
    /**
     * @return true iff tile is visible onscreen.
     */
    public boolean isVisibleOnscreen();
    
    /**
     * Sets the visibility of the tile onscreen.  
     * No effect if not installed.
     * 
     * @param visible Sets whether tile is visible onscreen.  No effect if not installed.
     */
    public void setVisibleOnscreen(boolean visible);
    
}
