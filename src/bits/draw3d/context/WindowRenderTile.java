package bits.draw3d.context;

import java.awt.*;
import javax.media.opengl.GLCanvas;

import bits.math3d.LongRect;


/**
 * @author decamp
 */
class WindowRenderTile extends AbstractRenderTile {


    static RenderTile create( RenderSpace renderSpace,
                              int id,
                              Bounder renderBounder,
                              Bounder targetTileBounder,
                              Bounder targetScreenBounder,
                              GLCanvas drawable,
                              String frameTitle,
                              boolean resizable )
    {
        return new WindowRenderTile( renderSpace, 
                                     id, 
                                     renderBounder, 
                                     targetTileBounder,
                                     targetScreenBounder,
                                     drawable, 
                                     frameTitle, 
                                     resizable );
    }
    
    
    
    private final String mFrameTitle;
    private final boolean mResizable;
    private Frame mFrame = null;
    
    private boolean mAutoResize   = true;
    
    
    private WindowRenderTile( RenderSpace renderSpace,
                              int id,
                              Bounder renderBounder,
                              Bounder targetTileBounder,
                              Bounder targetScreenBounder,
                              GLCanvas drawable,
                              String frameTitle,
                              boolean resizable )
    {
        super( renderSpace, 
               id, 
               renderBounder, 
               targetTileBounder, 
               targetScreenBounder, 
               drawable, 
               false, 
               false );
        
        mFrameTitle = frameTitle;
        mResizable = resizable;
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

        mFrame = new Frame(mFrameTitle);
        mFrame.removeAll();
        mFrame.setResizable(mResizable);
        
        final java.awt.Component comp = ((GLCanvas)drawable());
        
        mFrame.add( comp );
        mFrame.setLayout( new Layout( comp ) );
        
        updateFrameSize();
    }

    
    @Override
    public Window windowOnscreen() {
        return mFrame;
    }

    
    @Override
    public boolean isVisibleOnscreen() {
        return mFrame != null && mFrame.isVisible();
    }

    
    @Override
    public void setVisibleOnscreen(boolean visible) {
        if(mFrame != null) {
            mFrame.setVisible(visible);
            updateFrameSize();
        }
    }

    
    
    private void updateFrameSize() {
        if(mFrame == null || !mAutoResize)
            return;
        
        Insets insets   = mFrame.getInsets();
        
        LongRect screenTarget = targetScreenBounds();
        LongRect tileTarget   = targetTileBounds();
        LongRect target       = screenTarget != null ? screenTarget : tileTarget;
        
        if(target == null) {
            final int w = 1024;
            final int h = 1024 * 3 / 4;
            mFrame.setSize(w + insets.left + insets.right, 
                           h + insets.top + insets.bottom);
            mFrame.setLocationRelativeTo(null);
            mAutoResize = false;
            
        }else{
            int w = (int)target.spanX() + insets.left + insets.right;
            int h = (int)target.spanY() + insets.top  + insets.bottom;
            
            if(screenTarget != null) {
                int x = (int)Math.max(0, screenTarget.minX() - insets.left);
                int y = (int)Math.max(0, screenTarget.minY() - insets.top);
                mFrame.setBounds(x, y, w, h);
                
            }else{
                mFrame.setSize(w, h);
                mFrame.setLocationRelativeTo(null);
            }
            
            //Turn of auto resizing if the user has ability to resize window.
            if(mFrame.isVisible() && !mResizable) {
                mAutoResize = false;
            }
        }
    }

    
    private final class Layout implements LayoutManager {

        private final java.awt.Component mComp;

        Layout( java.awt.Component comp ) {
            mComp = comp;
        }


        public void layoutContainer( Container cont ) {
            Insets st = cont.getInsets();
            mComp.setBounds( st.left, st.top, cont.getWidth() - st.left - st.right, cont.getHeight() - st.top - st.bottom );
        }

        public Dimension minimumLayoutSize( Container cont ) {
            return new Dimension( 1, 1 );
        }

        public Dimension preferredLayoutSize( Container cont ) {
            return new Dimension( Integer.MAX_VALUE, Integer.MAX_VALUE );
        }

        public void addLayoutComponent( String name, Component comp ) {}

        public void removeLayoutComponent( Component comp ) {}
    }


}
