package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class LightNode extends DrawNodeAdapter {

    private static int sInstanceCount = 0; 
    
    
    public static LightNode newInstance() {
        return newInstance(null);
    }
    
    
    public static LightNode newInstance(LightParams params) {
        int id;
        
        synchronized(LightNode.class) {
            id = GL_LIGHT0 + sInstanceCount++ % 8;
        }
        
        return newInstance(id, params);
    }

    
    public static LightNode newInstance(int id, LightParams params) {
        if(params == null)
            params = new LightParams();
        
        return new LightNode(id, params);
    }

    
    
    private final int mLightId;
    
    private final LightParams mRevertParams = new LightParams();
    private final LightParams mParams;
    private final byte[] mRevertOn = {0};
    
    
    private LightNode(int lightId, LightParams params) {
        mLightId = lightId;
        mParams = params;
    }
    
    
    
    public int lightId() {
        return mLightId;
    }   
    
    
    public LightParams lightParams() {
        return mParams;
    }
    
    
    
    @Override
    public void pushDraw(GL gl) {
        gl.glGetBooleanv(mLightId, mRevertOn, 0);

        if(mRevertOn[0] != 0) {
            mRevertParams.read(gl, mLightId);
        }

        if(mParams == null) {
            gl.glDisable(mLightId);
        }else{
            gl.glEnable(mLightId);
            mParams.write(gl, mLightId);
        }
    }

    
    @Override
    public void popDraw(GL gl) {
        if(mRevertOn[0] == 0) {
            gl.glDisable(mLightId);
        }else{
            gl.glEnable(mLightId);
            mRevertParams.write(gl, mLightId);
        }
    }

    
}
