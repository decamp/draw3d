package bits.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 * @deprecated Use {@link Texture2Node}.
 */
@Deprecated public class Texture2dNode extends AbstractTextureNode {
    

    public Texture2dNode() {
        super( GL_TEXTURE_2D, GL_TEXTURE_BINDING_2D );
        param( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        param( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
    }
    
    
    @Override
    protected void doAlloc( GL gl ) {
      gl.glTexImage2D( GL_TEXTURE_2D,
                       0, // Level
                       internalFormat(),
                       width(),
                       height(),
                       0, // Border 
                       format(),
                       dataType(),
                       null );
    }


    @Deprecated public static Texture2dNode newInstance() {
        return new Texture2dNode();
    }

}
