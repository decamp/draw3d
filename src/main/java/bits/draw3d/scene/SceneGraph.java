package bits.draw3d.scene;

import java.util.*;


/**
 * Sorry, no comments yet. SceneGraph is not thread-safe and not really
 * dynamically modifiable, yet.
 * 
 * @author decamp
 */
public class SceneGraph {

    private static final int CONNECT_FIRST = 0;
    private static final int CONNECT_LAST  = 1;
    private static final int CONNECT_ALL   = 2;
    private static final int CONNECT_NONE  = 3;

    private static final int EMBED_GRAPH_ROOTS  = CONNECT_ALL;
    private static final int EMBED_GRAPH_LEAVES = CONNECT_LAST;

    private final Map<Object, NodeInfo> mModuleMap = new LinkedHashMap<Object, NodeInfo>();
    private NodeInfo mPrev = null;


    public SceneGraph() {}


    public SceneGraph( SceneGraph copy ) {
        for( Map.Entry<Object, NodeInfo> e : copy.mModuleMap.entrySet() ) {
            mModuleMap.put( e.getKey(), new NodeInfo( e.getValue() ) );
        }
    }



    public void add( Object node ) {
        mPrev = getNodeInfo( node );
    }


    public void remove( Object node ) {
        NodeInfo info = mModuleMap.remove( node );
        if( info == null ) {
            return;
        }

        if( mPrev == info ) {
            mPrev = null;
        }

        for( GraphEdge e : info.mParentEdges ) {
            NodeInfo parentInfo = mModuleMap.get( e.parent() );

            if( parentInfo != null ) {
                parentInfo.mChildEdges.remove( e );
            }
        }

        for( GraphEdge e : info.mChildEdges ) {
            NodeInfo childInfo = mModuleMap.get( e.child() );

            if( childInfo != null ) {
                childInfo.mParentEdges.remove( e );
            }
        }

    }

    /**
     * Replaces a node with a new object, while retaining all existing
     * connections.
     * 
     * @param oldNode Node to be replaced
     * @param newNode New node.
     * @return true if oldNode is contained in graph and successfully replaced.
     */
    public boolean replace( Object oldNode, Object newNode ) {
        NodeInfo oldInfo = mModuleMap.remove( oldNode );
        if( oldInfo == null ) {
            return false;
        }

        NodeInfo newInfo = getNodeInfo( newNode );
        mPrev = newInfo;

        for( GraphEdge oldEdge : oldInfo.mParentEdges ) {
            NodeInfo parentInfo = mModuleMap.get( oldEdge.parent() );
            if( parentInfo == null ) {
                continue;
            }

            parentInfo.mChildEdges.remove( oldEdge );

            GraphEdge newEdge = new GraphEdge( parentInfo.mNode, newNode, oldEdge.order() );
            parentInfo.mChildEdges.add( newEdge );
            newInfo.mParentEdges.add( newEdge );
        }

        for( GraphEdge oldEdge : oldInfo.mChildEdges ) {
            NodeInfo childInfo = mModuleMap.get( oldEdge.child() );
            if( childInfo == null ) {
                continue;
            }

            childInfo.mParentEdges.remove( oldEdge );

            GraphEdge newEdge = new GraphEdge( newNode, childInfo.mNode, oldEdge.order() );
            childInfo.mParentEdges.add( newEdge );
            newInfo.mChildEdges.add( newEdge );
        }

        return true;
    }


    public void clear() {
        mModuleMap.clear();
    }


    public GraphEdge connect( Object parent, Object child, int order ) {
        NodeInfo parentInfo = getNodeInfo( parent );
        NodeInfo childInfo  = mPrev = getNodeInfo( child );
        GraphEdge e = new GraphEdge( parent, child, order );

        parentInfo.mChildEdges.add( e );
        childInfo.mParentEdges.add( e );

        return e;
    }


    public GraphEdge connectFirst( Object parent, Object child ) {
        NodeInfo parentInfo = getNodeInfo( parent );
        NodeInfo childInfo  = mPrev = getNodeInfo( child );

        GraphEdge edge;

        if( parentInfo.mChildEdges.isEmpty() ) {
            edge = new GraphEdge( parent, child, 0 );
        } else {
            GraphEdge first = parentInfo.mChildEdges.first();
            if( first.order() < Integer.MIN_VALUE + 10 ) {
                edge = new GraphEdge( parent, child, Integer.MIN_VALUE );
            } else {
                edge = new GraphEdge( parent, child, first.order() - 10 );
            }
        }

        parentInfo.mChildEdges.add( edge );
        childInfo.mParentEdges.add( edge );

        return edge;
    }

    /**
     * Like {@code #connectFirst(Object,Object)}, but implicitly uses the last node added as the parent.

     * @param node Node to add.
     * @return Edge connecting last added-node with {@code node}.
     */
    public GraphEdge connectFirst( Object node ) {
        if( mPrev == null ) {
            add( node );
            return null;
        }

        if( mPrev.mNode == node ) {
            return null;
        }

        return connectFirst( mPrev.mNode, node );
    }


    public GraphEdge connectLast( Object parent, Object child ) {
        NodeInfo parentInfo = getNodeInfo( parent );
        NodeInfo childInfo  = mPrev = getNodeInfo( child );

        GraphEdge edge;

        if( parentInfo.mChildEdges.isEmpty() ) {
            edge = new GraphEdge( parent, child, 0 );
        } else {
            GraphEdge last = parentInfo.mChildEdges.last();

            if( last.order() > Integer.MAX_VALUE - 10 ) {
                edge = new GraphEdge( parent, child, Integer.MAX_VALUE );
            } else {
                edge = new GraphEdge( parent, child, last.order() + 10 );
            }
        }

        parentInfo.mChildEdges.add( edge );
        childInfo.mParentEdges.add( edge );

        return edge;

    }

    /**
     * Like {@code #connectLast(Object,Object)}, but implicitly uses the last node added as the parent.

     * @param node Node to add.
     * @return Edge connecting last added-node with {@code node}.
     */
    public GraphEdge connectLast( Object node ) {
        if( mPrev == null ) {
            add( node );
            return null;
        }

        if( mPrev.mNode == node ) {
            return null;
        }

        return connectLast( mPrev.mNode, node );
    }


    public void disconnect( GraphEdge edge ) {
        NodeInfo parentInfo = mModuleMap.get( edge.parent() );
        NodeInfo childInfo = mModuleMap.get( edge.child() );

        if( parentInfo == null || childInfo == null ) {
            return;
        }

        parentInfo.mChildEdges.remove( edge );
        childInfo.mParentEdges.remove( edge );
    }


    public void disconnect( Object parent, Object child ) {
        NodeInfo parentInfo = mModuleMap.get( parent );
        if( parentInfo == null ) {
            return;
        }

        NodeInfo childInfo = mModuleMap.get( child );
        if( childInfo == null ) {
            return;
        }

        for( Iterator<GraphEdge> iter = parentInfo.mChildEdges.iterator(); iter.hasNext(); ) {
            GraphEdge e = iter.next();
            if( e.child().equals( child ) ) {
                iter.remove();
                childInfo.mParentEdges.remove( e );
                return;
            }
        }
    }


    public void disconnectAll( Object parent, Object child ) {
        NodeInfo parentInfo = mModuleMap.get( parent );
        if( parentInfo == null ) {
            return;
        }

        NodeInfo childInfo = mModuleMap.get( child );
        if( childInfo == null ) {
            return;
        }

        for( Iterator<GraphEdge> iter = parentInfo.mChildEdges.iterator(); iter.hasNext(); ) {
            GraphEdge e = iter.next();

            if( e.child().equals( child ) ) {
                iter.remove();
                childInfo.mParentEdges.remove( e );
            }
        }
    }


    public List<GraphEdge> listEdges() {
        List<GraphEdge> ret = new ArrayList<GraphEdge>();

        for( NodeInfo n : mModuleMap.values() ) {
            ret.addAll( n.mChildEdges );
        }

        return ret;
    }


    public List<Object> listNodes() {
        return new ArrayList<Object>( mModuleMap.keySet() );
    }


    public GraphPath<Object> compilePath() {
        GraphPath<NodeInfo> path = flatten().compileAllNodePaths();
        GraphPath<Object> ret = new GraphPath<Object>();

        for( GraphStep<NodeInfo> s : path ) {
            ret.add( new GraphStep<Object>( s.type(), s.target().mNode ) );
        }

        return ret;
    }

    @SuppressWarnings( "unchecked" )
    public <T> GraphPath<T> compilePath( Class<T> nodeClass ) {
        GraphPath<NodeInfo> path = flatten().compileAllNodePaths();
        GraphPath<T> ret = new GraphPath<T>();

        for( GraphStep<NodeInfo> s : path ) {
            NodeInfo info = s.target();
            if( nodeClass.isInstance( info.mNode ) ) {
                ret.add( new GraphStep<T>( s.type(), (T)info.mNode ) );
            }
        }

        return ret;
    }



    private SceneGraph flatten() {
        Stack<SceneGraph> history = new Stack<SceneGraph>();
        return flatten( history );
    }


    private SceneGraph flatten( Stack<SceneGraph> history ) {
        if( history.contains( this ) ) {
            throw new IllegalStateException( "Recursively embedded SceneGraph." );
        }

        history.push( this );

        Map<Object, List<Object>> rootMap = new HashMap<Object, List<Object>>();
        Map<Object, List<Object>> leafMap = new HashMap<Object, List<Object>>();
        SceneGraph ret = new SceneGraph();

        // Find all head/tail nodes for embedded graphs.
        for( NodeInfo nodeInfo : mModuleMap.values() ) {
            Object node = nodeInfo.mNode;
            SceneGraph subgraph = null;

            if( node instanceof EmbeddedGraphNode ) {
                subgraph = ((EmbeddedGraphNode)node).asSceneGraph();
                if( subgraph == null ) {
                    continue;
                }

            } else if( node instanceof SceneGraph ) {
                subgraph = (SceneGraph)node;

            } else {
                // No need to flatten simple nodes.
                ret.add( node );
                continue;
            }

            // Lists for holding all roots and leafs of node bundle.
            List<Object> rootList = new ArrayList<Object>();
            List<Object> leafList = new ArrayList<Object>();
            
            subgraph = subgraph.flatten( history );
            rootList.addAll( subgraph.findRoots() );
            leafList.addAll( subgraph.findLeaves() );

            // Embed subgraph.
            for( NodeInfo subinfo : subgraph.mModuleMap.values() ) {
                ret.add( subinfo.mNode );

                for( GraphEdge edge : subinfo.mChildEdges ) {
                    ret.connect( edge.parent(), edge.child(), edge.order() );
                }
            }

            rootMap.put( node, rootList );
            leafMap.put( node, leafList );
        }


        // Add in all edges.
        for( NodeInfo mod : mModuleMap.values() ) {
            for( GraphEdge edge : mod.mChildEdges ) {

                List<Object> parentList = null;
                List<Object> childList = null;
                List<Object> allList = leafMap.get( edge.parent() );

                // Check graph behavior. This may change in the future, or be a
                // variable.
                if( allList == null ) {
                    parentList = Arrays.asList( edge.parent() );

                } else {

                    switch( EMBED_GRAPH_LEAVES ) {
                    case CONNECT_FIRST:
                    {
                        parentList = new ArrayList<Object>( 1 );

                        if( !allList.isEmpty() ) {
                            parentList.add( allList.get( 0 ) );
                        }

                        break;
                    }

                    case CONNECT_LAST:
                    {
                        parentList = new ArrayList<Object>( 1 );

                        if( !allList.isEmpty() ) {
                            parentList.add( allList.get( allList.size() - 1 ) );
                        }

                        break;
                    }

                    case CONNECT_ALL:
                        parentList = allList;
                        break;

                    case CONNECT_NONE:
                    default:
                        parentList = Collections.emptyList();
                        break;
                    }
                }


                allList = rootMap.get( edge.child() );

                if( allList == null ) {
                    childList = Arrays.asList( edge.child() );

                } else {
                    switch( EMBED_GRAPH_ROOTS ) {
                    case CONNECT_FIRST:
                    {
                        childList = new ArrayList<Object>( 1 );

                        if( !allList.isEmpty() ) {
                            childList.add( allList.get( 0 ) );
                        }

                        break;
                    }

                    case CONNECT_LAST:
                    {
                        childList = new ArrayList<Object>( 1 );

                        if( !allList.isEmpty() ) {
                            childList.add( allList.get( allList.size() - 1 ) );
                        }

                        break;
                    }

                    case CONNECT_ALL:
                        childList = allList;
                        break;


                    case CONNECT_NONE:
                    default:
                        childList = Collections.emptyList();
                        break;
                    }
                }

                // Connect selected parent leaves to child roots.
                for( Object parentNode : parentList ) {
                    for( Object childNode : childList ) {
                        ret.connect( parentNode, childNode, edge.order() );
                    }
                }
            }
        }

        history.pop();
        return ret;
    }


    private NodeInfo getNodeInfo( Object node ) {
        if( node == null ) {
            throw new IllegalArgumentException( "Cannot place null objects in scene graph." );
        }

        NodeInfo info = mModuleMap.get( node );
        if( info == null ) {
            info = new NodeInfo( node );
            mModuleMap.put( node, info );
        }

        return info;
    }


    private List<NodeInfo> findRoots( Set<NodeInfo> modules ) {
        List<NodeInfo> ret = new ArrayList<NodeInfo>();

        for( NodeInfo n : modules ) {
            if( n.mParentEdges.isEmpty() ) {
                ret.add( n );
            }
        }

        return ret;
    }

    
    private List<Object> findRoots() {
        List<Object> ret = new ArrayList<Object>();

        for( Map.Entry<Object, NodeInfo> e : mModuleMap.entrySet() ) {
            if( e.getValue().mParentEdges.isEmpty() ) {
                ret.add( e.getKey() );
            }
        }

        return ret;
    }


    private List<Object> findLeaves() {
        List<Object> ret = new ArrayList<Object>();

        for( Map.Entry<Object, NodeInfo> e : mModuleMap.entrySet() ) {
            if( e.getValue().mChildEdges.isEmpty() ) {
                ret.add( e.getKey() );
            }
        }

        return ret;
    }


    private GraphPath<NodeInfo> compileAllNodePaths() {
        final Set<NodeInfo> nodes = new HashSet<NodeInfo>( mModuleMap.values() );
        final List<NodeInfo> roots = findRoots( nodes );
        final GraphPath<NodeInfo> ret = new GraphPath<NodeInfo>();

        for( NodeInfo root : roots ) {
            compileNodeTreePath( root, ret );
        }

        return ret;
    }


    private void compileNodeTreePath( NodeInfo root, GraphPath<NodeInfo> out ) {

        // Stack of pushed nodes.
        Stack<NodeInfo> nodeStack = new Stack<NodeInfo>();

        // Stack of iterators of node children.
        Stack<Iterator<GraphEdge>> iterStack = new Stack<Iterator<GraphEdge>>();

        // Get root node.
        NodeInfo node = root;
        Iterator<GraphEdge> iter = node.mChildEdges.iterator();

        nodeStack.push( node );
        iterStack.push( iter );

        out.add( new GraphStep<NodeInfo>( GraphActionType.PUSH, node ) );

        while( !nodeStack.isEmpty() ) {
            node = nodeStack.pop();
            iter = iterStack.pop();

            if( iter.hasNext() ) {
                nodeStack.push( node );
                iterStack.push( iter );

                node = mModuleMap.get( iter.next().child() );
                iter = node.mChildEdges.iterator();

                if( nodeStack.contains( node ) ) {
                    throw new IllegalStateException( "Graph circularity." );
                }

                nodeStack.push( node );
                iterStack.push( iter );

                out.add( new GraphStep<NodeInfo>( GraphActionType.PUSH, node ) );

            } else {
                out.add( new GraphStep<NodeInfo>( GraphActionType.POP, node ) );

            }
        }
    }


    private static final class NodeInfo {

        final Object mNode;
        final SortedSet<GraphEdge> mParentEdges = new TreeSet<GraphEdge>();
        final SortedSet<GraphEdge> mChildEdges = new TreeSet<GraphEdge>();


        NodeInfo( Object node ) {
            mNode = node;
        }

        NodeInfo( NodeInfo copy ) {
            mNode = copy.mNode;
            mParentEdges.addAll( copy.mParentEdges );
            mChildEdges.addAll( copy.mChildEdges );
        }

    }

}
