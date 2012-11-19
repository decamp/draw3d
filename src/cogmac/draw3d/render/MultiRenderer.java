package cogmac.draw3d.render;

import java.util.*;


/**
 * Aggregates multiple Renderer objects.
 * 
 * @author decamp
 */
public class MultiRenderer implements Renderer {

    
    private final List<Renderer> mRenderers;
    
    
    public MultiRenderer() {
        mRenderers = new ArrayList<Renderer>();
    }
    
    
    public MultiRenderer( Collection<? extends Renderer> renderers ) {
        mRenderers = new ArrayList<Renderer>( renderers );
    }
    

    
    public void add( Renderer rend ) {
        mRenderers.add( rend );
    }

    
    public boolean remove( Renderer rend ) {
        return mRenderers.remove( rend );
    }
    
    
    
    public void init() {
        for(Renderer t: mRenderers) {
            t.init();
        }
    }

    
    public void draw() {
        for(Renderer t: mRenderers) {
            t.draw();
        }
    }
    
    
    public void finish() {
        for(Renderer t: mRenderers) {
            t.finish();
        }
    }
    
    
    public void dispose() {
        for(Renderer t: mRenderers) {
            t.dispose();
        }
    }
    
}
