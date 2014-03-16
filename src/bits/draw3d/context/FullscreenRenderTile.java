package bits.draw3d.context;

import java.awt.*;

import javax.media.opengl.GLCanvas;
import javax.swing.JFrame;


/**
 * @author decamp
 */
class FullscreenRenderTile extends AbstractRenderTile {

    
    static RenderTile newInstance( RenderSpace renderSpace,
                                   int id,
                                   Bounder renderBounder,
                                   Bounder targetTileBounder,
                                   GLCanvas drawable,
                                   String frameTitle,
                                   GraphicsDevice screen, 
                                   boolean exclusive)
    {
        if(screen == null) {
            screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        }

        return new FullscreenRenderTile(renderSpace, id, renderBounder, targetTileBounder, drawable, frameTitle, screen, exclusive);
    }
    
    
    
    private final String mFrameTitle;
    private final GraphicsDevice mScreenDevice;
    private final boolean mExclusive;
    
    private JFrame mFrame = null;
    
    
    private FullscreenRenderTile( RenderSpace renderSpace,
                                  int id,
                                  Bounder renderBounder,
                                  Bounder targetTileBounder,
                                  GLCanvas drawable,
                                  String frameTitle,
                                  GraphicsDevice screen,
                                  boolean exclusive) 
    {
        super(renderSpace, id, renderBounder, targetTileBounder, null, drawable, false, true);
        mFrameTitle = frameTitle;
        mScreenDevice = screen;
        mExclusive = exclusive;
    }
                                  
    
    
    @Override
    public void dispose() {
        if(mFrame != null) {
            mFrame.dispose();
            mFrame = null;
        }
        
        super.dispose();
    }
    
    
    @Override
    public void installOnscreen() {
        if(mFrame != null)
            return;
        
        mFrame = new JFrame(mFrameTitle, mScreenDevice.getDefaultConfiguration());
        mFrame.setUndecorated(true);
        mFrame.setIgnoreRepaint(true);
        
        mFrame.getContentPane().removeAll();
        mFrame.getContentPane().setLayout(new FillLayout());
        mFrame.add((GLCanvas)drawable());
        
        mFrame.setBounds(mScreenDevice.getDefaultConfiguration().getBounds());
    }

    
    @Override
    public Window windowOnscreen() {
        return mFrame;
    }
    
    
    public boolean isVisibleOnscreen() {
        return mFrame != null && mFrame.isVisible();
    }
    
    
    @Override
    public void setVisibleOnscreen(boolean visible) {
        if(mFrame == null)
            return;
        
        if(mExclusive) {
            if(visible) {
                mScreenDevice.setFullScreenWindow(mFrame);
            }else{
                mScreenDevice.setFullScreenWindow(null);
            }           
        }else{
            mFrame.setVisible(visible);
        }
    }
    
    
    
    private static class FillLayout implements LayoutManager {
        
        public FillLayout() {}
        
        
        public void layoutContainer( Container cont ) {
            final int w = cont.getWidth();
            final int h = cont.getHeight();
            final int count = cont.getComponentCount();
            for( int i = 0; i < count; i++ ) {
                Component c = cont.getComponent( i );
                if( c != null ) {
                    c.setBounds(0, 0, w, h );
                }
            }
        }


        
        @Override
        public void addLayoutComponent( String arg0, Component  c ) {}


        
        @Override
        public Dimension minimumLayoutSize( Container c ) {
            return new Dimension( 1, 1 );
        }


        
        @Override
        public Dimension preferredLayoutSize( Container c ) {
            return new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
        }


        
        @Override
        public void removeLayoutComponent( Component c ) {}

    }

    
}
