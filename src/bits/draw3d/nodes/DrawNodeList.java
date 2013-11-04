package bits.draw3d.nodes;

import java.util.*;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * Propagates draw action calls to a list of DrawActions.
 * 
 * @author Philip DeCamp
 */
public class DrawNodeList implements DrawNode {


    public static DrawNodeList newDepthFirst( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), true );
    }


    public static DrawNodeList newDepthFirst( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), true );
    }


    public static DrawNodeList newBreadthFirst( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), false );
    }


    public static DrawNodeList newBreadthFirst( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), false );
    }
    


    private final List<DrawNode> mActions;
    private final boolean mDepthFirst;

    
    private DrawNodeList( List<DrawNode> actions, boolean depthFirst ) {
        mActions = actions;
        mDepthFirst = depthFirst;
    }



    public void init( GLAutoDrawable gld ) {
        for( DrawNode a : mActions ) {
            a.init( gld );
        }
    }


    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h ) {
        for( DrawNode a : mActions ) {
            a.reshape( gld, x, y, w, h );
        }
    }


    public void dispose( GLAutoDrawable gld ) {
        for( DrawNode a : mActions ) {
            a.dispose( gld );
        }
    }


    public void pushDraw( GL gl ) {
        if( mDepthFirst ) {
            for( DrawNode a : mActions ) {
                a.pushDraw( gl );
            }
        } else {
            for( DrawNode a : mActions ) {
                a.pushDraw( gl );
                a.popDraw( gl );
            }
        }
    }


    public void popDraw( GL gl ) {
        if( mDepthFirst ) {
            for( int i = mActions.size() - 1; i >= 0; i-- ) {
                mActions.get( i ).popDraw( gl );
            }
        }
    }

    
    

    @Deprecated
    public static DrawNodeList newDepthFirstInstance( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), true );
    }

    @Deprecated
    public static DrawNodeList newDepthFirstInstance( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), true );
    }

    @Deprecated
    public static DrawNodeList newBreadthFirstInstance( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), false );
    }

    @Deprecated
    public static DrawNodeList newBreadthFirstInstance( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), false );
    }
    

    
}
