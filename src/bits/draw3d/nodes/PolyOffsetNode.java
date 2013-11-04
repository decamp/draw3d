package bits.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class PolyOffsetNode extends DrawNodeAdapter {
    
    
    public static PolyOffsetNode newFillInstance() {
        return newFillInstance(false, 0f, 0f);
    }
    
    public static PolyOffsetNode newFillInstance(boolean enable, float factor, float units) {
        return new PolyOffsetNode(GL_POLYGON_OFFSET_FILL, enable, factor, units);
        
    }
    
    public static PolyOffsetNode newLineInstance() {
        return newLineInstance(false, 0f, 0f);
    }
    
    public static PolyOffsetNode newLineInstance(boolean enable, float factor, float units) {
        return new PolyOffsetNode(GL_POLYGON_OFFSET_LINE, enable, factor, units);
        
    }
    
    public static PolyOffsetNode newPointInstance() {
        return newPointInstance(false, 0f, 0f);
    }
    
    public static PolyOffsetNode newPointInstance(boolean enable, float factor, float units) {
        return new PolyOffsetNode(GL_POLYGON_OFFSET_POINT, enable, factor, units);
        
    }
    
    
    private final int mTarget;
    private boolean mEnable;
    private float mFactor;
    private float mUnits;
    
    private boolean mBoolRevert = false;
    private final float[] mFloatRevert  = {0,0};
    
    
    private PolyOffsetNode(int target, boolean enable, float factor, float units) {
        mTarget = target;
        mEnable = enable;
        mFactor = factor;
        mUnits  = units;
    }

    
    
    public int target() {
        return mTarget;
    }
    
    public boolean enable() {
        return mEnable;
    }
    
    public PolyOffsetNode enable(boolean enable) {
        mEnable = enable;
        return this;
    }
    
    public float factor() {
        return mFactor;
    }
    
    public PolyOffsetNode factor(float factor) {
        mFactor = factor;
        return this;
    }
    
    public float units() {
        return mUnits;
    }
    
    public PolyOffsetNode units(float units) {
        mUnits = units;
        return this;
    }
    
    
    public void pushDraw(GL gl) {
        mBoolRevert = gl.glIsEnabled(mTarget);
        gl.glGetFloatv(GL_POLYGON_OFFSET_FACTOR, mFloatRevert, 0);
        gl.glGetFloatv(GL_POLYGON_OFFSET_UNITS, mFloatRevert, 1);
        
        if(mEnable) {
            gl.glEnable(mTarget);
        }else{
            gl.glDisable(mTarget);
        }
        
        gl.glPolygonOffset(mFactor, mUnits);
    }
    
    public void popDraw(GL gl) {
        if(mBoolRevert) {
            gl.glEnable(mTarget);
        }else{
            gl.glDisable(mTarget);
        }
        
        gl.glPolygonOffset(mFloatRevert[0], mFloatRevert[1]);
    }
    
    
}
