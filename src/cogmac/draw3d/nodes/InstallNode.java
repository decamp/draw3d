package cogmac.draw3d.nodes;

import cogmac.draw3d.context.RenderTile;


/**
 * @author decamp
 */
public interface InstallNode {
    public void install( RenderTile tile );
    public void uninstall( RenderTile tile );
}
