package bits.draw3d.nodes;

import bits.draw3d.context.RenderTile;

/**
 * @author decamp
 */
public interface NodeFactory<A> {
    public A newInstance(RenderTile tile);
}
