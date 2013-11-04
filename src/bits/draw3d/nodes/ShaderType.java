package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public enum ShaderType {
    
    VERTEX    (GL_VERTEX_SHADER),
    GEOMETRY  (GL_GEOMETRY_SHADER_EXT),
    FRAGMENT  (GL_FRAGMENT_SHADER);
    
    
    private final int mGlslCode;
    
    ShaderType(int glslCode) {
        mGlslCode = glslCode;
    }
    
    
    public int glslCode() {
        return mGlslCode;
    }
    
}
