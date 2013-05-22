package cogmac.draw3d.nodes;

import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class AlphaTestNode extends DrawNodeAdapter {
    
    public boolean mEnable = false;
    public int mFunc       = GL_ALWAYS;
    public float mRef      = 0f;

    private boolean mRevertEnable = false;
    private final int[] mRevertFunc = { GL_ALWAYS };
    private final float[] mRevertRef = { 0f };
    
    
    public AlphaTestNode() {
        this(false, GL_ALWAYS, 0f );
    }
    
    public AlphaTestNode( boolean enable ) {
        this( enable, GL_ALWAYS, 0f );
    }
    
    public AlphaTestNode( boolean enable, int func, float ref ) {
        mEnable = enable;
        mFunc   = func;
        mRef    = ref;
    }
    
    
    public boolean enable() {
        return mEnable;
    }
    
    public AlphaTestNode enable( boolean enable ) {
        mEnable = enable;
        return this;
    }
    
    public int func() {
        return mFunc;
    }
    
    public AlphaTestNode func( int func ) {
        mFunc = func;
        return this;
    }
    
    public float ref() {
        return mRef;
    }
    
    public AlphaTestNode ref( float ref ) {
        mRef = ref;
        return this;
    }
    
    
    
    @Override
    public void pushDraw( GL gl ) {
        mRevertEnable = gl.glIsEnabled( GL_ALPHA_TEST );
        gl.glGetIntegerv( GL_ALPHA_TEST_FUNC, mRevertFunc, 0 );
        gl.glGetFloatv( GL_ALPHA_TEST_REF, mRevertRef, 0 );
        
        if( mEnable ) {
            gl.glEnable( GL_ALPHA_TEST );
        } else {
            gl.glDisable( GL_ALPHA_TEST );
        }
        
        gl.glAlphaFunc( mFunc, mRef );
    }
    
    @Override
    public void popDraw( GL gl ) {
        if( mRevertEnable ) {
            gl.glEnable( GL_ALPHA_TEST );
        } else {
            gl.glDisable( GL_ALPHA_TEST );
        }
        gl.glAlphaFunc( mRevertFunc[0], mRevertRef[0] );
    }
    
}
