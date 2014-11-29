/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.draw3d.*;
import java.util.*;

/**
 * Propagates draw action calls to a list of DrawActions.
 * 
 * @author Philip DeCamp
 */
public class DrawNodeList implements DrawNode {


    public static DrawNodeList createDepthFirst( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), true );
    }


    public static DrawNodeList createDepthFirst( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), true );
    }


    public static DrawNodeList createBreadthFirst( Collection<DrawNode> actions ) {
        return new DrawNodeList( new ArrayList<DrawNode>( actions ), false );
    }


    public static DrawNodeList createBreadthFirst( DrawNode... actions ) {
        return new DrawNodeList( Arrays.asList( actions ), false );
    }


    private final List<DrawNode> mActions;
    private final boolean mDepthFirst;


    private DrawNodeList( List<DrawNode> actions, boolean depthFirst ) {
        mActions = actions;
        mDepthFirst = depthFirst;
    }


    public boolean isDepthFirst() {
        return mDepthFirst;
    }


    public boolean isBreadthFirst() {
        return !mDepthFirst;
    }


    public void add( DrawNode node ) {
        mActions.add( node );
    }


    public void remove( DrawNode node ) {
        mActions.remove( node );
    }


    public void init( DrawEnv d ) {
        for( DrawNode a : mActions ) {
            a.init( d );
        }
    }


    public void reshape( DrawEnv d ) {
        for( DrawNode a : mActions ) {
            if( a instanceof ReshapeListener ) {
                ((ReshapeListener)a).reshape( d );
            }
        }
    }


    public void dispose( DrawEnv d ) {
        for( DrawNode a : mActions ) {
            a.dispose( d );
        }
    }


    public void pushDraw( DrawEnv d ) {
        if( mDepthFirst ) {
            for( DrawNode a : mActions ) {
                a.pushDraw( d );
            }
        } else {
            for( DrawNode a : mActions ) {
                a.pushDraw( d );
                a.popDraw( d );
            }
        }
    }


    public void popDraw( DrawEnv d ) {
        if( mDepthFirst ) {
            for( int i = mActions.size() - 1; i >= 0; i-- ) {
                mActions.get( i ).popDraw( d );
            }
        }
    }

}
