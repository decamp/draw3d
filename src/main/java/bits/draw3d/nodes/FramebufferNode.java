package bits.draw3d.nodes;

import bits.draw3d.*;
import bits.draw3d.tex.Texture;

import java.util.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL3.*;

/**
 * @author decamp
 */
public final class FramebufferNode implements DrawNode, ReshapeListener {
    
    
    private final int[] mId = { 0 };

    private int mWidth  = -1;
    private int mHeight = -1;
    private boolean mResizeOnReshape = false;
    
    private Attachment[] mAtts      = {};
    private final int[] mDrawRevert = { 0 };
    
    private boolean mNeedsInit  = true;
    
    
    public FramebufferNode() {}
        
    
    public void attach( int attachment, Texture node ) {
        Attachment a = new Attachment( attachment, node );
        Attachment[] atts = new Attachment[mAtts.length + 1];
        System.arraycopy( mAtts, 0, atts, 0, mAtts.length );
        atts[atts.length-1] = a;
        mAtts      = atts;
        mNeedsInit = true;
    }
    
    public int attachmentCount() {
        return mAtts.length;
    }
    
    public Texture texture( int index ) {
        return mAtts[index].mTex;
    }
    
    public Texture textureFor( int attachment ) {
        for( Attachment a: mAtts ) {
            if( a.mAttachment == attachment ) {
                return a.mTex;
            }
        }
        
        return null;
    }

    
    public int target() {
        return GL_DRAW_FRAMEBUFFER;
    }
    
    public int id() {
        return mId[0];
    }
    
    public void size( int w, int h ) {
        if( w < 0 || h < 0 ) {
            w = -1;
            h = -1;
        }
        
        if( w == mWidth && h == mHeight ) { 
            return;
        }
        
        mWidth     = w;
        mHeight    = h;
        mNeedsInit = true;
    }
    
    public int width() {
        return mWidth;
    }
    
    public int height() {
        return mHeight;
    }
    
    public boolean hasSize() {
        return mWidth >= 0 && mHeight >= 0;
    }
    
    public void resizeOnReshape( boolean enable ) {
        mResizeOnReshape = enable;
    }
    
    public boolean resizeOnReshape() {
        return mResizeOnReshape;
    }
    

    public void bindDraw( DrawEnv d ) {
        doBind( d, GL_DRAW_FRAMEBUFFER );
        int status = d.mGl.glCheckFramebufferStatus( GL_DRAW_FRAMEBUFFER );
        if( status != GL_FRAMEBUFFER_COMPLETE ) {
            throw new IllegalStateException( "Failed to complete framebuffer before attachment: " + status );
        }
    }
    
    public void bindRead( DrawEnv d ) {
        doBind( d, GL_READ_FRAMEBUFFER );
    }
    
    public void unbindDraw( DrawEnv d ) {
        d.mGl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, 0 );
    }
    
    public void unbindRead( DrawEnv d ) {
        d.mGl.glBindFramebuffer( GL_READ_FRAMEBUFFER, 0 );
    }

    public void dispose( DrawEnv d ) {
        if( mId[0] != 0 ) {
            d.mGl.glDeleteFramebuffers( 1, mId, 0 );
            mId[0] = 0;
        }
        
        for( Attachment a: mAtts ) {
            a.mTex.dispose( d );
        }
        
        mNeedsInit = true;
        mAtts = new Attachment[0];
    }
    
    public void pushDraw( DrawEnv d ) {
        if( mNeedsInit ) {
            doInit( d, GL_DRAW_FRAMEBUFFER );
        }
        
        pushDrawBuffer( d, mId[0] );
        int status = d.mGl.glCheckFramebufferStatus( GL_DRAW_FRAMEBUFFER );
        if( status != GL_FRAMEBUFFER_COMPLETE ) {
            throw new IllegalStateException( "Failed to complete framebuffer before attachment." );
        }
    }
    
    public void popDraw( DrawEnv d ) {
        popDrawBuffer( d );
    }
    
    public void pushRead( DrawEnv d ) {
        if( mNeedsInit ) {
            doInit( d, GL_READ_FRAMEBUFFER );
        }
        pushReadBuffer( d, mId[0] );
    }
    
    public void popRead( DrawEnv d ) {
        popReadBuffer( d );
    }


    /**
     * TODO: The revert on this is all wrong.
     */
    public void init( DrawEnv d ) {
        BindingStack stack = currentStack();
        d.mGl.glGetIntegerv( GL_DRAW_FRAMEBUFFER_BINDING, mDrawRevert, 0 );
        doInit( d, GL_DRAW_FRAMEBUFFER );
        d.mGl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, mDrawRevert[0] );
    }
    
    public void reshape( DrawEnv d ) {
        if( mResizeOnReshape ) {
            size( d.mViewport.mW, d.mViewport.mH );
        }
    }


    private void doInit( DrawEnv d, int target ) {
        if( !mNeedsInit ) {
            return;
        }
        mNeedsInit = false;
        
        if( mId[0] == 0 ) {
            d.mGl.glGenFramebuffers( 1, mId, 0 );
            if( mId[0] == 0 ) {
                throw new RuntimeException("Failed to allocate FrameBufferObject.");
            }
        }
        
        boolean initSize = mWidth >= 0 && mHeight >= 0;
        for( Attachment a: mAtts ) {
            if( initSize ) {
                a.mTex.size( mWidth, mHeight );
            }
            a.mTex.init( d );
        }
        d.mGl.glBindFramebuffer( target, mId[0] );
        
        for( Attachment a: mAtts ) {
            switch( a.mTex.target() ) {
            case GL_RENDERBUFFER:
                d.mGl.glFramebufferRenderbuffer( target,
                                                 a.mAttachment,
                                                 GL_RENDERBUFFER,
                                                 a.mTex.id() );
                break;
            default:
                d.mGl.glFramebufferTexture(  target, a.mAttachment, a.mTex.id(), 0 );
                break;
            }
        }
    }
    
    private void doBind( DrawEnv d, int target ) {
        if( mNeedsInit ) {
            doInit( d, target );
        }
        d.mGl.glBindFramebuffer( target, mId[0] );
    }
    
        
    private static class Attachment {

        final int     mAttachment;
        final Texture mTex;

        Attachment( int attachment, Texture tex ) {
            mAttachment = attachment;
            mTex = tex;
        }

    }



    //Ugh.  There's a bug with glGetIntegerv( GL_FRAMEBUFFER_BINDING ), which makes this
    //hack necessary.
    // TODO: Test if this hackery is still necessary.
    private static final Map<GLContext, BindingStack> mStacks = new WeakHashMap<GLContext, BindingStack>();

    private static final class BindingStack {
        final Stack<Integer> mDrawStack = new Stack<Integer>();
        final Stack<Integer> mReadStack = new Stack<Integer>();

        int mDrawId = 0;
        int mReadId = 0;
    }

    private static synchronized BindingStack currentStack() {
        GLContext context = GLContext.getCurrent();
        BindingStack stack = mStacks.get( context );
        if( stack == null ) {
            stack = new BindingStack();
            mStacks.put( context, stack );
        }
        return stack;
    }

    private static synchronized void pushDrawBuffer( DrawEnv d, int id ) {
        BindingStack stack = currentStack();
        stack.mDrawStack.push( stack.mDrawId );
        stack.mDrawId = id;
        d.mGl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, id );
    }

    private static synchronized void popDrawBuffer( DrawEnv d ) {
        BindingStack stack = currentStack();
        if( stack.mDrawStack.isEmpty() ) {
            //Just in case.
            stack.mDrawId = 0;
        } else {
            stack.mDrawId = stack.mDrawStack.pop();
        }
        d.mGl.glBindFramebuffer( GL_DRAW_FRAMEBUFFER, stack.mDrawId );
    }

    private static synchronized void pushReadBuffer( DrawEnv d, int id ) {
        BindingStack stack = currentStack();
        stack.mReadStack.push( stack.mReadId );
        stack.mReadId = id;
        d.mGl.glBindFramebuffer( GL_READ_FRAMEBUFFER, id );
    }

    private static synchronized void popReadBuffer( DrawEnv d ) {
        BindingStack stack = currentStack();
        if( stack.mReadStack.isEmpty() ) {
            //Just in case
            stack.mReadId = 0;
        } else {
            stack.mReadId = stack.mReadStack.pop();
        }
        d.mGl.glBindFramebuffer( GL_READ_FRAMEBUFFER, stack.mReadId );
    }


    public static FramebufferNode newInstance() {
        return new FramebufferNode();
    }


}
