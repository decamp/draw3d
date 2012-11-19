package cogmac.draw3d.nodes;

import cogmac.draw3d.context.RenderTile;

/**
 * Interface for classes that provide nodes for a render path.
 * 
 * @author decamp
 */
public interface RenderModule {
    public Object getNodes(Class<?> nodeClass, RenderTile context);
}
