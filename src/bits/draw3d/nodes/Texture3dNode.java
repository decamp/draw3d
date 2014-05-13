package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;


/**
 * @author decamp
 * @deprecated Use {@link Texture3Node}.
 */
public final class Texture3dNode extends AbstractTextureNode {
    
    
    public Texture3dNode() {
        super( GL_TEXTURE_3D, GL_TEXTURE_BINDING_3D );
        param( GL_TEXTURE_MIN_FILTER, GL_LINEAR );
        param( GL_TEXTURE_MAG_FILTER, GL_LINEAR );
    }

    
    @Override
    protected void doAlloc( GL gl ) {
        gl.glTexImage3D( GL_TEXTURE_3D,
                         0, // Level
                         internalFormat(),
                         width(),
                         height(),
                         depth(),
                         0,
                         format(),
                         dataType(),
                         null );
    }


    @Deprecated public static Texture3dNode newInstance() {
        return new Texture3dNode();
    }


}
