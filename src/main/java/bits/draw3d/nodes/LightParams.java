package bits.draw3d.nodes;

import javax.media.opengl.*;
import static javax.media.opengl.GL2.*;


/** 
 * @author Philip DeCamp  
 */
@Deprecated public class LightParams {

    private static final int[] ATTRIBUTES = { GL_AMBIENT,
                                              GL_DIFFUSE,
                                              GL_SPECULAR,
                                              GL_POSITION,
                                              GL_SPOT_DIRECTION,
                                              GL_SPOT_EXPONENT,
                                              GL_SPOT_CUTOFF,
                                              GL_CONSTANT_ATTENUATION,
                                              GL_LINEAR_ATTENUATION,
                                              GL_QUADRATIC_ATTENUATION };
    
    private final float[][] mValues = new float[ATTRIBUTES.length][];
    private float mSpotCutoff; 

    
    public LightParams() {
        this(null, null, null, null, true, null, 0, 180, 1, 0, 0);
    }

    
    public LightParams( float[] ambient4x1,
                        float[] diffuse4x1,
                        float[] specular4x1,
                        float[] position3x1,
                        boolean directional)
    {
        this(ambient4x1, diffuse4x1, specular4x1, position3x1, directional, null, 0, 180, 1, 0, 0);
    }
    
    
    public LightParams( float[] ambient4x1,
                        float[] diffuse4x1,
                        float[] specular4x1,
                        float[] position3x1,
                        boolean directional,
                        float[] spotDirection3x1,
                        float spotExponent,
                        float spotCutoff,
                        float attenuationConstant,
                        float attenuationLinear,
                        float attenuationQuadratic)
    {
        if(ambient4x1 == null) {
            mValues[0] = new float[]{0,0,0,1};
        }else{
            mValues[0] = copy(ambient4x1, 4);
        }
        
        if(diffuse4x1 == null) {
            mValues[1] = new float[]{0,0,0,1};
        }else{
            mValues[1] = copy(diffuse4x1, 4);
        }
        
        if(specular4x1 == null) {
            mValues[2] = new float[]{0,0,0,1};
        }else{
            mValues[2] = copy(specular4x1, 4);
        }
        
        float w = (directional ? 0 : 1);
        
        if(position3x1 == null) {
            mValues[3] = new float[]{0,0,1,w};
        }else{
            if(position3x1.length < 3)
                throw new IllegalArgumentException("position3x1.length < 3");
            
            mValues[3] = new float[]{position3x1[0], position3x1[1], position3x1[2], w};
        }
        
        if(spotDirection3x1 == null) {
            mValues[4] = new float[]{0,0,-1};
        }else{
            mValues[4] = copy(spotDirection3x1, 3);
        }
        
        mSpotCutoff = Math.max(0, Math.min(90, spotCutoff));
        mValues[5] = new float[]{spotExponent};
        mValues[6] = new float[]{spotCutoff};
        mValues[7] = new float[]{attenuationConstant};
        mValues[8] = new float[]{attenuationLinear};
        mValues[9] = new float[]{attenuationQuadratic};
    }
    
    
    
    public void read(GL2 gl, int light) {
        for(int i = 0; i < mValues.length; i++) {
            gl.glGetLightfv(light, ATTRIBUTES[i], mValues[i], 0);
        }
        
        float cutoff = mValues[6][0];
        if(cutoff <= 90) {
            mSpotCutoff = mValues[6][0];
        }
    }
    
    
    public void write(GL2 gl, int light) {
        for(int i = 0; i < mValues.length; i++) {
            gl.glLightfv(light, ATTRIBUTES[i], mValues[i], 0);
        }
    }
    
    
    
    public float[] ambientRef() {
        return mValues[0];
    }
    
    public LightParams ambient(float r, float g, float b, float a) {
        mValues[0][0] = r;
        mValues[0][1] = g;
        mValues[0][2] = b;
        mValues[0][3] = a;
        return this;
    }
   
    public float[] diffuseRef() {
        return mValues[1];
    }
    
    public LightParams diffuse(float r, float g, float b, float a) {
        mValues[1][0] = r;
        mValues[1][1] = g;
        mValues[1][2] = b;
        mValues[1][3] = a;
        return this;        
    }
    
    public float[] specularRef() {
        return mValues[2];
    }
    
    public LightParams specular(float r, float g, float b, float a) {
        mValues[2][0] = r;
        mValues[2][1] = g;
        mValues[2][2] = b;
        mValues[2][3] = a;
        return this;
    }
    
    public float[] positionRef() {
        return mValues[3];
    }

    public boolean isDirectional() {
        return mValues[3][3] == 0;
    }
    
    public LightParams position(float x, float y, float z) {
        mValues[3][0] = x;
        mValues[3][1] = y;
        mValues[3][2] = z;
        mValues[3][3] = 1f;
        return this;
    }  
    
    public LightParams direction(float x, float y, float z) {
        mValues[3][0] = x;
        mValues[3][1] = y;
        mValues[3][2] = z;
        mValues[3][3] = 1f;
        return this;
    }
    
    public float[] spotDirectionRef() {
        return mValues[4];
    }
    
    public boolean isSpotlight() {
        return mValues[6][0] != 180;
    }
    
    public LightParams spotlight(boolean enable) {
        if(enable) {
            if(mSpotCutoff > 90f) {
                mSpotCutoff = 90f;
            }
            
            mValues[6][0] = mSpotCutoff;
        }else{
            mValues[6][0] = 180;
        }
        
        return this;
    }
    
    public LightParams spotDirection(float x, float y, float z) {
        mValues[4][0] = x;
        mValues[4][1] = y;
        mValues[4][2] = z;
        return this;
    }

    public float spotCutoff() {
        return mSpotCutoff;
    }
    
    public LightParams spotCutoff(float cutoff) {
        if(cutoff > 90f) {
            return spotlight(false);
        }
        
        mValues[6][0] = mSpotCutoff = Math.max(0, cutoff);
        return this;
    }
    
    public float spotExponent() {
        return mValues[5][0];
    }
    
    public LightParams spotExponent(float exp) {
        mValues[5][0] = Math.max(0, Math.min(128, exp));
        return this;
    }

    public float constantAttenuation() {
        return mValues[7][0];
    }
    
    public float linearAttenuation() {
        return mValues[8][0];
    }
    
    public float quadraticAttenuation() {
        return mValues[9][0];
    }

    public LightParams attenuation(float constant, float linear, float quadratic) {
        mValues[7][0] = constant;
        mValues[8][0] = linear;
        mValues[9][0] = quadratic;
        return this;
    }
    
        
        
    private static float[] copy(float[] a, int len) {
        if(a.length < len) 
            throw new IllegalArgumentException("Length < " + len);
        
        float[] ret = new float[len];
        System.arraycopy(a, 0, ret, 0, len);
        return ret;
    }
    
}
