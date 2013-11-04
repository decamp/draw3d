package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;

/**
 * @author decamp
 */
public class Texture1dNode extends AbstractTextureNode {
    
    
    public static Texture1dNode newInstance() {
        return new Texture1dNode();
    }
    
    
    Texture1dNode() {
        super( GL_TEXTURE_1D, GL_TEXTURE_BINDING_1D );
        param( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        param( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
    }

    
    @Override
    protected void doAlloc( GL gl ) {
      gl.glTexImage1D( GL_TEXTURE_1D,
                       0, // Level
                       internalFormat(),
                       width(),
                       0, // Border 
                       format(),
                       dataType(),
                       null );
    }

}
