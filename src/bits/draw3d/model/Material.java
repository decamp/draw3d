package bits.draw3d.model;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;


/** 
 * @author Philip DeCamp  
 */
public class Material {

    private static final int[] ATTRIBUTES = { GL_AMBIENT,
                                              GL_DIFFUSE,
                                              GL_SPECULAR,
                                              GL_EMISSION,
                                              GL_SHININESS };

    
    private String mName = "";
    private final float[][] mValues = new float[5][];
    
    
    public Material() {
        this(null, null, null, null, 0f );
    }

    
    public Material( float[] ambient,
                     float[] diffuse,
                     float[] specular,
                     float[] emissive,
                     float shininess )
    {
        mValues[0] = copyColor( ambient );
        mValues[1] = copyColor(diffuse);
        mValues[2] = copyColor(specular);
        mValues[3] = copyColor(emissive);
        mValues[4] = new float[]{shininess};
    }
    
    
    public float[] ambientRef() {
        return mValues[0];
    }
    
    public float[] diffuseRef() {
        return mValues[1];
    }
    
    public float[] specularRef() {
        return mValues[2];
    }
    
    public float[] emissionRef() {
        return mValues[3];
    }
    
    public float shininess() {
        return mValues[4][0];
    }

    public void shininess( float shininess ) {
        mValues[4][0] = shininess;
    }
    
    public String name() {
        return mName;
    }
    
    public void name( String name ) {
        mName = name;
    }
    
    
    
    public void read(GL gl, int face) {
        for(int i = 0; i < mValues.length; i++) {
            float[] v = mValues[i];
            if(v == null)
                continue;
            
            gl.glGetMaterialfv(face, ATTRIBUTES[i], v, 0);
        }
    }
    
    public void write(GL gl, int face) {
        for(int i = 0; i < mValues.length; i++) {
            
            float[] v = mValues[i];
            if(v == null)
                continue;
            
            gl.glMaterialfv(face, ATTRIBUTES[i], v, 0);
        }
    }

    
    
    
    private static float[] copyColor( float[] c ) {
        if( c == null )
            return new float[] { 0f, 0f, 0f, 1f };
        
        if( c.length < 4 )
            throw new IllegalArgumentException("Color with length < 4");
        
        return new float[]{ c[0], c[1], c[2], c[3] };
    }
    
}
