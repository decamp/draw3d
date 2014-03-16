package bits.draw3d.nodes;

import java.util.*;

import javax.media.opengl.*;
import static javax.media.opengl.GL.*;

/**
 * @author decamp
 */
public final class FramebufferNode implements DrawNode {
    
    
    public static FramebufferNode newInstance() {
        return new FramebufferNode();
    }
    
    
    private final int[] mId = { 0 };
    
    private int mWidth  = -1;
    private int mHeight = -1;
    private boolean mResizeOnReshape = false;
    
    private Attachment[] mAtts      = {};
    private final int[] mDrawRevert = { 0 };
    
    private boolean mNeedsInit  = true;
    
    
    private FramebufferNode() {}
        
    
    public void attach( int attachment, TextureNode node ) {
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
    
    public TextureNode texture( int index ) {
        return mAtts[index].mNode;
    }
    
    public TextureNode textureFor( int attachment ) {
        for( Attachment a: mAtts ) {
            if( a.mAttachment == attachment ) {
                return a.mNode;
            }
        }
        
        return null;
    }
    
    
    
    public int target() {
        return GL_DRAW_FRAMEBUFFER_EXT;
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
    
    
    
    public void bindDraw( GL gl ) {
        doBind( gl, GL_DRAW_FRAMEBUFFER_EXT );
        int status = gl.glCheckFramebufferStatusEXT( GL_DRAW_FRAMEBUFFER_EXT );
        if( status != GL_FRAMEBUFFER_COMPLETE_EXT ) {
            throw new IllegalStateException( "Failed to complete framebuffer before attachment." );
        }
    }
    
    public void bindRead( GL gl ) {
        doBind( gl, GL_READ_FRAMEBUFFER_EXT );
    }
    
    public void unbindDraw( GL gl ) {
        gl.glBindFramebufferEXT( GL_DRAW_FRAMEBUFFER_EXT, 0 );
    }
    
    public void unbindRead( GL gl ) {
        gl.glBindFramebufferEXT( GL_READ_FRAMEBUFFER_EXT, 0 );
    }

    public void dispose( GL gl ) {
        if( mId[0] != 0 ) {
            gl.glDeleteFramebuffersEXT( 1, mId, 0 );
            mId[0] = 0;
        }
        
        for( Attachment a: mAtts ) {
            a.mNode.dispose( gl );
        }
        
        mNeedsInit = true;
        mAtts = new Attachment[0];
    }
    
    public void pushDraw( GL gl ) {
        if( mNeedsInit ) {
            doInit( gl, GL_DRAW_FRAMEBUFFER_EXT );
        }
        
        pushDrawBuffer( gl, mId[0] );
        int status = gl.glCheckFramebufferStatusEXT( GL_DRAW_FRAMEBUFFER_EXT );
        if( status != GL_FRAMEBUFFER_COMPLETE_EXT ) {
            throw new IllegalStateException( "Failed to complete framebuffer before attachment." );
        }
    }
    
    public void popDraw( GL gl ) {
        popDrawBuffer( gl );
    }
    
    public void pushRead( GL gl ) {
        if( mNeedsInit ) {
            doInit( gl, GL_READ_FRAMEBUFFER_EXT );
        }
        pushReadBuffer( gl, mId[0] );
    }
    
    public void popRead( GL gl ) {
        popReadBuffer( gl );
    }
    
    
    /**
     * TODO: The revert on this is all wrong.
     */
    public void init( GLAutoDrawable gld ) {
        GL gl = gld.getGL();
        gl.glGetIntegerv( GL_DRAW_FRAMEBUFFER_BINDING_EXT, mDrawRevert, 0 );
        doInit( gl, GL_DRAW_FRAMEBUFFER_EXT );
        gl.glBindFramebufferEXT( GL_DRAW_FRAMEBUFFER_EXT, mDrawRevert[0] );
    }
    
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        if( mResizeOnReshape ) {
            size( w, h );
        }
    }

    public void dispose( GLAutoDrawable gld ) {
        dispose( gld.getGL() );
    }
    
    
    private void doInit( GL gl, int target ) {
        if( !mNeedsInit ) {
            return;
        }
        mNeedsInit = false;
        
        if( mId[0] == 0 ) {
            gl.glGenFramebuffersEXT( 1, mId, 0 );
            if( mId[0] == 0 ) {
                throw new RuntimeException("Failed to allocate FrameBufferObject.");
            }
        }
        
        boolean initSize = mWidth >= 0 && mHeight >= 0;
        for( Attachment a: mAtts ) {
            if( initSize ) {
                a.mNode.size( mWidth, mHeight );
            }
            a.mNode.init( gl );
        }
        gl.glBindFramebufferEXT( target, mId[0] );
        
        for( Attachment a: mAtts ) {
            switch( a.mNode.target() ) {
            case GL_RENDERBUFFER_EXT:
                gl.glFramebufferRenderbufferEXT( target,
                                                 a.mAttachment,
                                                 GL_RENDERBUFFER_EXT,
                                                 a.mNode.id() );
                break;
                
            case GL_TEXTURE_1D:
                gl.glFramebufferTexture1DEXT( target,
                                              a.mAttachment,
                                              GL_TEXTURE_1D,
                                              a.mNode.id(),
                                              0 );
                break;
                
            case GL_TEXTURE_2D:
                gl.glFramebufferTexture2DEXT( target,
                                              a.mAttachment,
                                              GL_TEXTURE_2D,
                                              a.mNode.id(),
                                              0 );
                break;
                
            case GL_TEXTURE_3D:
                gl.glFramebufferTexture3DEXT( target,
                                              a.mAttachment,
                                              GL_TEXTURE_3D,
                                              a.mNode.id(),
                                              0,
                                              0 );
                break;
            }
        }
    }
    
    private void doBind( GL gl, int target ) {
        if( mNeedsInit ) {
            doInit( gl, target );
        }
        
        gl.glBindFramebufferEXT( target, mId[0] );
    }
    
        
    private static class Attachment {
        
        final int mAttachment;
        final TextureNode mNode;
        
        Attachment( int attachment, TextureNode node ) {
            mAttachment = attachment;
            mNode       = node;
        }
        
    }
    
    

    //Ugh.  There's a bug with glGetIntegerv(GL_FRAMEBUFFER_BINDING_EXT), which makes this
    //hack necessary.
    
    private static final Map<GLContext,BindingStack> mStacks = new WeakHashMap<GLContext,BindingStack>();
    
    private static final class BindingStack {
        final Stack<Integer> mDrawStack = new Stack<Integer>();
        final Stack<Integer> mReadStack = new Stack<Integer>();
        
        int mDrawId = 0;
        int mReadId = 0;
    }
    
    private static synchronized BindingStack currentStack() {
        GLContext context  = GLContext.getCurrent();
        BindingStack stack = mStacks.get( context );
        if( stack == null ) {
            stack = new BindingStack();
            mStacks.put( context, stack );
        }
        
        return stack;
    }
    
    private static synchronized void pushDrawBuffer( GL gl, int id ) {
        BindingStack stack = currentStack();
        stack.mDrawStack.push( stack.mDrawId );
        stack.mDrawId = id;
        gl.glBindFramebufferEXT( GL_DRAW_FRAMEBUFFER_EXT, id );
    }
    
    private static synchronized void popDrawBuffer( GL gl ) {
        BindingStack stack = currentStack();
        if( stack.mDrawStack.isEmpty() ) {
            //Just in case.
            stack.mDrawId = 0;
        } else {
            stack.mDrawId = stack.mDrawStack.pop();
        }
        
        gl.glBindFramebufferEXT( GL_DRAW_FRAMEBUFFER_EXT, stack.mDrawId );
    }
    
    private static synchronized void pushReadBuffer( GL gl, int id ) {
        BindingStack stack = currentStack();
        stack.mReadStack.push( stack.mReadId );
        stack.mReadId = id;
        gl.glBindFramebufferEXT( GL_READ_FRAMEBUFFER_EXT, id );
    }
    
    private static synchronized void popReadBuffer( GL gl ) {
        BindingStack stack = currentStack();
        if( stack.mReadStack.isEmpty() ) {
            //Just in case
            stack.mReadId = 0;
        } else {
            stack.mReadId = stack.mReadStack.pop();
        }
        
        gl.glBindFramebufferEXT( GL_READ_FRAMEBUFFER_EXT, stack.mReadId );
    }
    
}
