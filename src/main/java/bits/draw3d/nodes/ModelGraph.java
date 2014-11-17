package bits.draw3d.nodes;

import java.util.*;

import bits.draw3d.actors.Actor;
import bits.draw3d.model.DrawTri;
import bits.draw3d.pick.*;
import bits.draw3d.scene.*;


/**
 * @author decamp
 */
public class ModelGraph implements EmbeddedGraphNode {

    private final ActorNode       mMotionNode;
    private final Object[]        mMaterialNodes;
    private final TrianglesNode[] mTriangleNodes;

    private final SceneGraph mGraph;


    ModelGraph( ActorNode motionNode,
                List<Object> matNodes,
                List<TrianglesNode> triNodes )
    {
        mMotionNode = motionNode;
        mMaterialNodes = matNodes.toArray( new Object[matNodes.size()] );
        mTriangleNodes = triNodes.toArray( new TrianglesNode[triNodes.size()] );
        mGraph = new SceneGraph();

        for( int i = 0; i < mTriangleNodes.length; i++ ) {
            Object node = mTriangleNodes[i];
            mGraph.add( node );

            if( mMaterialNodes[i] != null ) {
                mGraph.connectLast( mMaterialNodes[i], node );
                node = mMaterialNodes[i];
            }

            if( motionNode != null ) {
                mGraph.connectLast( motionNode, node );
                node = motionNode;
            }
        }
    }

    public Actor actor() {
        return mMotionNode == null ? null : mMotionNode.actor();
    }


    public SceneGraph asSceneGraph() {
        return mGraph;
    }

    /**
     * You should not use this method.  It is a hack because I need to get this working
     * right quick.
     */
    @Deprecated public RayPicker newPicker() {
        List<DrawTri> list = new ArrayList<DrawTri>();
        for( TrianglesNode n : mTriangleNodes ) {
            list.addAll( n.trianglesRef() );
        }

        return KdTriangleTree.build( list );
    }


    @Deprecated public Actor getMotionControls() {
        return mMotionNode == null ? null : mMotionNode.actor();
    }


    @Deprecated public TrianglesNode[] getTriangleNodes() {
        return mTriangleNodes.clone();
    }

}