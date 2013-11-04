package bits.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public class TextureUnitNode extends DrawNodeAdapter {
    
    
    public static TextureUnitNode forUnitOffset(int offset) {
        return forUnit(offset + GL_TEXTURE0);
    }
    
    
    public static TextureUnitNode forUnit(int unit) {
        return new TextureUnitNode(unit);
    }
    
    
    private final int mUnit;
    private final int[] mRevert = {0};
    
    
    private TextureUnitNode(int unit) {
        mUnit = unit;
    }

    
    
    @Override
    public void pushDraw(GL gl) {
        gl.glGetIntegerv(GL_ACTIVE_TEXTURE, mRevert, 0);
        gl.glActiveTexture(mUnit);
    }
    
    
    @Override
    public void popDraw(GL gl) {
        gl.glActiveTexture(mRevert[0]);
    }
    

}
