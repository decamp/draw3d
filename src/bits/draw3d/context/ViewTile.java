package bits.draw3d.context;

import java.awt.Window;
import javax.media.opengl.*;


/**
 * @author decamp
 */
public class ViewTile extends AbstractRenderTile {
    
    
    public static ViewTile newInstance( RenderSpace space,
                                        int id,
                                        GLAutoDrawable drawable,
                                        boolean isOffscreen,
                                        boolean isFullscreen )
    {
        return new ViewTile(space, id, drawable, isOffscreen, isFullscreen);
    }
            
    
    
    ViewTile( RenderSpace space, 
              int id,
              GLAutoDrawable drawable,
              boolean isOffscreen,
              boolean isFullscreen )
    {
        super( space, 
               id, 
               null, 
               null, 
               null, 
               drawable,
               isOffscreen,
               isFullscreen );
    }

    
    public void installOnscreen() {}

    public Window windowOnscreen() {
        return null;
    }

    public boolean isVisibleOnscreen() {
        return false;
    }

    public void setVisibleOnscreen(boolean visible) {}

}
