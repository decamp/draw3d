package bits.draw3d.context;

import java.awt.Window;
import javax.media.opengl.GLPbuffer;


/**
 * @author decamp
 */
class OffscreenRenderTile extends AbstractRenderTile {
    
    
    static RenderTile newInstance( RenderSpace renderSpace,
                                   int id,
                                   Bounder renderBounder,
                                   Bounder tileBounder,
                                   GLPbuffer drawable)
    {
        return new OffscreenRenderTile(renderSpace, id, renderBounder, tileBounder, drawable);
    }

    
    
    private OffscreenRenderTile( RenderSpace renderSpace,
                                 int id,
                                 Bounder renderBounder,
                                 Bounder tileBounder,
                                 GLPbuffer drawable )
    {
        super(renderSpace, id, renderBounder, tileBounder, null, drawable, true, false);
    }
    
    

    @Override
    public void dispose() {
        GLPbuffer b = (GLPbuffer)drawable();
        if(b != null)
            b.destroy();

        super.dispose();
    }
    
    
    @Override
    public void installOnscreen() {}

    
    @Override
    public Window windowOnscreen() {
        return null;
    }

    
    @Override
    public boolean isVisibleOnscreen() {
        return false;
    }

    
    @Override
    public void setVisibleOnscreen(boolean visible) {}
                                                   
}
