package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;
import javax.media.opengl.GL;


/**
 * @author decamp
 */
public class DrawBufferParams {

    
    public static DrawBufferParams newInstance() {
        return new DrawBufferParams();
    }
    
    
    public static DrawBufferParams copy(DrawBufferParams node) {
        return new DrawBufferParams(node);
    }

    
    private boolean mVertEnabled  = false;
    private int mVertSize         = 3;
    private int mVertType         = GL_FLOAT;
    private int mVertStride       = 0;
    private int mVertOffset       = 0;
    
    private boolean mTexEnabled   = false;
    private int mTexSize          = 2;
    private int mTexType          = GL_FLOAT;
    private int mTexStride        = 0;
    private int mTexOffset        = 0;
    
    private boolean mNormEnabled  = false;
    private int mNormType         = GL_FLOAT;
    private int mNormStride       = 0;
    private int mNormOffset       = 0;
    
    private boolean mColorEnabled = false;
    private int mColorSize        = 4;
    private int mColorType        = GL_UNSIGNED_BYTE;
    private int mColorStride      = 0;
    private int mColorOffset      = 0;
    
    private boolean mIndexEnabled = false;
    private int mIndexType        = GL_UNSIGNED_INT;
    private int mIndexStride      = 4;
    
    private boolean mCommandEnabled = false;
    private int mCommandMode        = GL_TRIANGLES;
    private int mCommandOffset      = 0;
    private int mCommandCount       = 0;
    
    
    private DrawBufferParams() {}
    
    
    private DrawBufferParams(DrawBufferParams ref) {
        mVertEnabled    = ref.mVertEnabled;
        mVertSize       = ref.mVertSize;
        mVertType       = ref.mVertType;
        mVertStride     = ref.mVertStride;
        mVertOffset     = ref.mVertOffset;
        mTexEnabled     = ref.mTexEnabled;
        mTexSize        = ref.mTexSize;
        mTexStride      = ref.mTexStride;
        mTexOffset      = ref.mTexOffset;
        mNormEnabled    = ref.mNormEnabled;
        mNormType       = ref.mNormType;
        mNormStride     = ref.mNormStride;
        mNormOffset     = ref.mNormOffset;
        mColorEnabled   = ref.mColorEnabled;
        mColorSize      = ref.mColorSize;
        mColorType      = ref.mColorType;
        mColorStride    = ref.mColorStride;
        mColorOffset    = ref.mColorOffset;
        mIndexEnabled   = ref.mIndexEnabled;
        mIndexType      = ref.mIndexType;
        mCommandEnabled = ref.mCommandEnabled;
        mCommandMode    = ref.mCommandMode;
        mCommandOffset  = ref.mCommandOffset;
        mCommandCount   = ref.mCommandCount;
    }
    

    
    public void enableCommand(int mode, int offset, int count) {
        mCommandEnabled = true;
        mCommandMode    = mode;
        mCommandOffset  = offset;
        mCommandCount   = count;
    }
    
    
    public void disableCommand() {
        mCommandEnabled = false;
    }
    
    
    public void enableVertexPointer(int coordCount, int coordType, int stride, int offset) {
        mVertEnabled = true;
        mVertSize    = coordCount;
        mVertType    = coordType;
        mVertStride  = stride;
        mVertOffset  = offset;
    }

    
    public void disableVertexPointer() {
        mVertEnabled = false;
    }
    
    
    public void enableColorPointer(int coordCount, int coordType, int stride, int offset) {
        mColorEnabled = true;
        mColorSize    = coordCount;
        mColorType    = coordType;
        mColorStride  = stride;
        mColorOffset  = offset;
    }

    
    public void disableColorPointer() {
        mColorEnabled = false;
    }
    
    
    public void enableNormPointer(int coordType, int stride, int offset) {
        mNormEnabled = true;
        mNormType = coordType;
        mNormStride = stride;
        mNormOffset = offset;
    }
    
    
    public void disableNormPointer() {
        mNormEnabled = false;
    }
        
    
    public void enableTexPointer(int coordCount, int coordType, int stride, int offset) {
        mTexEnabled = true;
        mTexSize = coordCount;
        mTexType = coordType;
        mTexStride = stride;
        mTexOffset = offset;
    }
    
    
    public void disableTexPointer() {
        mTexEnabled = false;
    }


    public void enableIndices(int indexType) {
        
        switch(indexType) {
        case GL_UNSIGNED_BYTE:
            mIndexStride = 1;
            break;
            
        case GL_UNSIGNED_SHORT:
            mIndexStride = 2;
            break;
            
        case GL_UNSIGNED_INT:
            mIndexStride = 4;
            break;
            
        default:
            throw new IllegalArgumentException("indexType must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT");
        }
        
        mIndexEnabled = true;
        mIndexType    = indexType;
    }
    
    
    public void disableIndices() {
        mIndexEnabled = false;
    }

    
    
   
    
    public void push(GL gl) {
        if(mVertEnabled) {
            gl.glEnableClientState(GL_VERTEX_ARRAY);
            gl.glVertexPointer(mVertSize, mVertType, mVertStride, mVertOffset);
        }
        
        if(mColorEnabled) {
            gl.glEnableClientState(GL_COLOR_ARRAY);
            gl.glColorPointer(mColorSize, mColorType, mColorStride, mColorOffset);
        }
        
        if(mTexEnabled) {
            gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(mTexSize, mTexType, mTexStride, mTexOffset);
        }
        
        if(mNormEnabled) {
            gl.glEnableClientState(GL_NORMAL_ARRAY);
            gl.glNormalPointer(mNormType, mNormStride, mNormOffset);
        }
    }
                                 
                                 
    public void pop(GL gl) {
        gl.glDisableClientState(GL_COLOR_ARRAY);
        gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL_NORMAL_ARRAY);
    }

    
    public void execute(GL gl) {
        if(!mCommandEnabled)
            return;

        if(mIndexEnabled) {
            gl.glDrawElements(mCommandMode, mCommandCount, mIndexType, mCommandOffset * mIndexStride);
        }else{
            gl.glDrawArrays(mCommandMode, mCommandOffset, mCommandCount);
        }
    }
    
    
    public void execute(GL gl, int mode, int offset, int count) {
        if(mIndexEnabled) {
            gl.glDrawElements(mode, count, mIndexType, offset * mIndexStride);
        }else{
            gl.glDrawArrays(mode, offset, count);
        }
    }
    
}

