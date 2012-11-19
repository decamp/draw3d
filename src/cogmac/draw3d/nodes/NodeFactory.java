package cogmac.draw3d.nodes;

import cogmac.draw3d.context.RenderTile;

/**
 * @author decamp
 */
public interface NodeFactory<A> {
    public A newInstance(RenderTile tile);
}
