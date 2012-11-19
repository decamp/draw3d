package cogmac.draw3d.context;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.*;

import javax.media.opengl.*;

import cogmac.blob.Blob;
import cogmac.math3d.LongRect;

/**
 * @author decamp
 */
public class RenderSpaceBuilder {

    
    public static RenderSpace newSpaceFromBlob( GLCapabilities caps, Blob blob ) throws IOException {
        String type = blob.getString( "type" );
        if(type == null)
            throw new IOException( "Could not find key: \"type\"" );
        
        String title = blob.tryGetString( "", "title" );
        LongRect bounds = null;
        if(blob.containsKey("bounds")) {
            bounds = LongRect.fromBounds( blob.getInt("bounds", 0),
                                          blob.getInt("bounds", 1),
                                          blob.getInt("bounds", 2),
                                          blob.getInt("bounds", 3) );
        }
                
        if(type.equalsIgnoreCase("window"))
            return newWindowSpace(caps, bounds, true, title);
        
        if(type.equalsIgnoreCase("fullscreen"))
            return newFullscreenSpace(caps, null, true, title);
        
        if(type.equalsIgnoreCase("multiscreen"))
            return newMultiscreenSpace(caps, title, null);
        
        if(type.equalsIgnoreCase("offscreen"))
            return newOffscreenSpace(caps, bounds, null);
        
        throw new IOException("Unknown \"type\" value: \"" + type + "\"");        
    }
    
    
    
    public static RenderSpace newWindowSpace( GLCapabilities caps, 
                                              LongRect windowBounds, 
                                              boolean resizable, 
                                              String title )
    {  
        RenderSpaceBuilder b = new RenderSpaceBuilder();
        b.addWindowTile(caps, windowBounds, null, resizable, title);
        return b.build();
    }

    
    public static RenderSpace newFullscreenSpace( GLCapabilities caps,
                                                  GraphicsDevice screen,
                                                  boolean exclusive,
                                                  String title )
    {
        RenderSpaceBuilder b = new RenderSpaceBuilder();
        b.addFullscreenTile(caps, screen, null, exclusive, title);
        return b.build();
    }
    

    public static RenderSpace newOffscreenSpace( GLCapabilities caps,
                                                 LongRect bounds,
                                                 GLContext shareContext )
    {
        RenderSpaceBuilder b = new RenderSpaceBuilder();
        b.addOffscreenTile(caps, bounds, shareContext);
        return b.build();
    }

    
    public static RenderSpace newTiledWindowSpace( GLCapabilities caps,
                                                   boolean resizable,
                                                   String title,
                                                   LongRect... bounds )
    {
        return newTiledWindowSpace(caps, resizable, title, false, bounds);
    }
    
    
    public static RenderSpace newTiledWindowSpace( GLCapabilities caps,
                                                   boolean resizable,
                                                   String title,
                                                   boolean shareContext,
                                                   LongRect... bounds )
    {
        LongRect fullRect = null;
        
        for(LongRect b: bounds) {
            if(fullRect == null) {
                fullRect = b;
            }else{
                fullRect = fullRect.union(b);
            }
        }
        
        Bounder fullBounder = new ConstantBounder(fullRect);
        RenderSpaceBuilder rsb = new RenderSpaceBuilder(shareContext);
        
        for(LongRect b: bounds) {
            rsb.addPartialWindowTile(fullBounder, caps, b, b, resizable, title); 
        }
        
        return rsb.build();
    }
    
    
    public static RenderSpace newMultiscreenSpace( GLCapabilities caps, 
                                                   String title,
                                                   GraphicsDevice[] screens )
    {
        return newMultiscreenSpace(caps, title, false, screens);
    }

    
    public static RenderSpace newMultiscreenSpace( GLCapabilities caps,
                                                   String title,
                                                   boolean shareContext,
                                                   GraphicsDevice[] screens )
    {
        if(screens == null) {
            screens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            Arrays.sort(screens, GRAPHICS_DEVICE_COMP);
        }
        
        if(screens.length == 0)
            return new RenderSpace();
        
        if(screens.length == 1)
            return newFullscreenSpace(caps, screens[0], true, title);
        
        LongRect unionRect = null;
        LongRect firstRect = null;
        LongRect[] rects   = new LongRect[screens.length];
        
        {
            Rectangle bounds = screens[0].getDefaultConfiguration().getBounds();
            firstRect = LongRect.fromBounds(bounds.x, bounds.y, bounds.width, bounds.height);
            unionRect = LongRect.fromBounds(0, 0, bounds.width, bounds.height);
            rects[0]  = unionRect;
        }
        
        for(int i = 1; i < screens.length; i++) {
            Rectangle bounds = screens[i].getDefaultConfiguration().getBounds();
            rects[i] = LongRect.fromBounds( bounds.x - firstRect.minX(),
                                            bounds.y - firstRect.minY(),
                                            bounds.width,
                                            bounds.height );
            
            unionRect = unionRect.union(rects[i]);
        }
        
        RenderSpaceBuilder b = new RenderSpaceBuilder(shareContext);
        Bounder spaceBounder = new ConstantBounder(unionRect);
        
        for(int i = 0; i < screens.length; i++) {
            b.addPartialFullscreenTile(spaceBounder, caps, screens[i], rects[i], true, title);   
        }

        return b.build();
    }
    
    
    
    /**
     * Creates a view of a different render space in a single tile
     * without generating a new GLContext.  Useful when rendering 
     * to an offscreen drawable that will later be split into
     * different contexts. 
     * 
     * @param space
     * @return
     */
    public static RenderSpace newUnionSpace(RenderTile tile) {
        RenderSpace ret = new RenderSpace();
        ret.addTile(new UnionViewTile(ret, tile, 0));
        return ret;
    }
    
    
    
    private final boolean mShareContext;
    
    private int mId = 0;
    private RenderSpace mSpace = null;
    
    private GLContext mParentContext = null;
    
    
    public RenderSpaceBuilder() {
        this(false);
    }
    
    
    public RenderSpaceBuilder(boolean shareContext) {
        mShareContext = shareContext;
    }
    
    
    
    public void addWindowTile( GLCapabilities caps,
                               LongRect screenBounds,
                               LongRect tileBounds,
                               boolean resizable,
                               String title )
    {
        addPartialWindowTile(null, caps, screenBounds, tileBounds, resizable, title);
    }
    
    
    public void addPartialWindowTile( Bounder viewportBounder,
                                      GLCapabilities caps,
                                      LongRect screenBounds,
                                      LongRect tileBounds,
                                      boolean resizable,
                                      String title )
    {
        initSpace();
        
        if(caps == null)
            caps = newDefaultCapabilities(false);
        
        GLCanvas canvas = newCanvas(caps, null);
        Bounder targetScreenBounder = screenBounds == null ? null : new ConstantBounder(screenBounds); 
        Bounder targetTileBounder   = tileBounds   == null ? null : new ConstantBounder(tileBounds);
        
        RenderTile tile = WindowRenderTile.newInstance( mSpace, 
                                                        mId++, 
                                                        viewportBounder,
                                                        targetTileBounder, 
                                                        targetScreenBounder,
                                                        canvas,
                                                        title,
                                                        null,
                                                        resizable );
        
        mSpace.addTile(tile);
    }
                                      
    
    public void addFullscreenTile( GLCapabilities caps,
                                   GraphicsDevice screen,
                                   LongRect tileBounds,
                                   boolean exclusive,
                                   String title )
    {
        addPartialFullscreenTile(null, caps, screen, tileBounds, exclusive, title);
    }
    
    
    public void addPartialFullscreenTile( Bounder viewportBounder, 
                                          GLCapabilities caps,
                                          GraphicsDevice screen,
                                          LongRect tileBounds,
                                          boolean exclusive,
                                          String title )
    {
        initSpace();
        
        if(caps == null)
            caps = newDefaultCapabilities(false);
        
        GLCanvas canvas = newCanvas(caps, screen);
        Bounder targetTileBounder = tileBounds == null ? null : new ConstantBounder(tileBounds);
        RenderTile tile = FullscreenRenderTile.newInstance( mSpace, 
                                                            mId++, 
                                                            viewportBounder,
                                                            targetTileBounder, 
                                                            canvas, 
                                                            title,
                                                            screen, 
                                                            exclusive );
        
        mSpace.addTile(tile);
    }
    
    
    public void addOffscreenTile( GLCapabilities caps, 
                                  LongRect bounds, 
                                  GLContext shareContext )
    {
        addPartialOffscreenTile(null, caps, bounds, shareContext);
    }
    
    
    public void addPartialOffscreenTile( Bounder viewportBounder,
                                         GLCapabilities caps,
                                         LongRect bounds, 
                                         GLContext shareContext ) 
    {
        initSpace();
        
        if(caps == null) 
            caps = newDefaultCapabilities(true);
        
        if(bounds == null)
            bounds = LongRect.fromBounds(0, 0, 1024, 768);
        
        Bounder tileBounder = new ConstantBounder(bounds);
        GLPbuffer buf = GLDrawableFactory.getFactory().createGLPbuffer( caps, 
                                                                        null,
                                                                        (int)bounds.spanX(),
                                                                        (int)bounds.spanY(),
                                                                        shareContext );
                                                                        
        
        RenderTile tile = OffscreenRenderTile.newInstance(mSpace, mId++, viewportBounder, tileBounder, buf);
        mSpace.addTile(tile);
    }

    
    
    public RenderSpace build() {
        initSpace();
        
        RenderSpace ret = mSpace;
        mSpace = null;
        mId = 0;
        
        return ret;
    }
    

    
    private void initSpace() {
        if(mSpace == null) {
            mSpace = new RenderSpace();
            mId = 0;
        }
    }
    
    
    private GLCanvas newCanvas(GLCapabilities caps, GraphicsDevice screen) {
        GLCanvas ret = new GLCanvas(caps, null, mParentContext, screen);
        
        if(mShareContext && mParentContext == null) {
            mParentContext = ret.getContext();
        }
        
        return ret;
    }
    
    
    static GLCapabilities newDefaultCapabilities(boolean offscreen) {
        GLCapabilities caps = new GLCapabilities();
        caps.setHardwareAccelerated(true);
        caps.setDepthBits(16);
        caps.setStencilBits(8);
        caps.setRedBits(8);
        caps.setGreenBits(8);
        caps.setBlueBits(8);
        caps.setAlphaBits(8);
        
        if(offscreen) {
            caps.setDoubleBuffered(false);
            caps.setPbufferRenderToTextureRectangle(true);
        }else{
            caps.setDoubleBuffered(true);
        }
        
        return caps;
    }

    
    private static Comparator<GraphicsDevice> GRAPHICS_DEVICE_COMP = new Comparator<GraphicsDevice>() {
        public int compare(GraphicsDevice g1, GraphicsDevice g2) {
            if(g1 == g2)
                return 0;
            
            Rectangle r1 = g1.getDefaultConfiguration().getBounds();
            Rectangle r2 = g2.getDefaultConfiguration().getBounds();
            
            if(r1.y < r2.y)
                return -1;
            
            if(r1.y > r2.y)
                return 1;
            
            if(r1.x < r2.x)
                return -1;
            
            if(r1.x > r2.x)
                return 1;
            
            return 0;
        }
    };
    
}
