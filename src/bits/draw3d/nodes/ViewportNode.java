package bits.draw3d.nodes;

import javax.media.opengl.*;

import static javax.media.opengl.GL.*;


/**
 * @author decamp
 */
public class ViewportNode extends DrawNodeAdapter {

    private final int[] mViewport;
    private final int[] mRevert = { 0, 0, 0, 0 };

    private boolean mResizeOnReshape = false;


    public ViewportNode() {
        mViewport = new int[]{ 0, 0, 0, 0 };
    }

    public ViewportNode( int x, int y, int w, int h ) {
        mViewport = new int[]{ x, y, w, h };
    }


    public int[] viewportRef() {
        return mViewport;
    }

    public ViewportNode viewport( int x, int y, int w, int h ) {
        mViewport[0] = x;
        mViewport[1] = y;
        mViewport[2] = w;
        mViewport[3] = h;
        return this;
    }

    public boolean resizeOnReshape() {
        return mResizeOnReshape;
    }

    public ViewportNode resizeOnReshape( boolean enable ) {
        mResizeOnReshape = enable;
        return this;
    }


    @Override
    public void pushDraw( GL gl ) {
        gl.glGetIntegerv( GL_VIEWPORT, mRevert, 0 );
        gl.glViewport( mViewport[0], mViewport[1], mViewport[2], mViewport[3] );
    }

    @Override
    public void popDraw( GL gl ) {
        gl.glViewport( mRevert[0], mRevert[1], mRevert[2], mRevert[3] );
    }

    @Override
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        if( mResizeOnReshape ) {
            mViewport[0] = x;
            mViewport[1] = y;
            mViewport[2] = w;
            mViewport[3] = h;
        }
    }

}
