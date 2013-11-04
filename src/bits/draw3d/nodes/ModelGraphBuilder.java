package bits.draw3d.nodes;

import java.io.File;
import java.io.IOException;
import java.util.*;

import bits.blob.Blob;
import bits.draw3d.model.*;
import bits.draw3d.util.MatrixIO;


/**
 * @author decamp
 */
public class ModelGraphBuilder {
    
    private final List<GroupInfo> mGroupList = new ArrayList<GroupInfo>();
    
    private boolean mEnableMaterials = true;
    private Boolean mEnableTex       = null;
    private Boolean mEnableNormals   = null;
    private Boolean mEnableColor     = false;
    private boolean mEnableMotion    = false;
    private double[] mBaseTransform  = null;
    
    
    public ModelGraphBuilder() {}
    
    
    
    public void parseModelBlob( File baseDir, Blob blob ) throws IOException {
        
        //Find any override materials specified in blob.
        Map<String,Material> materialMap = new HashMap<String,Material>();
        
        if( blob.containsKey( "materials" ) ) {
            for( Object obj: blob.keySet( "materials" ) ) {
                String name  = obj.toString();
                Material mat = ModelIO.readMaterialBlob( name, blob.slice("materials", obj) );
                materialMap.put( name, mat );
            }
        }        
        
        //Read in each model.
        int size = blob.size( "files" );
        
        for(int i = 0; i < size; i++) {
            String path = blob.getString("files", i);
            if(path == null)
                throw new IOException("Non-string path in Blob");
            
            File file = resolvePath(baseDir, path);
            MeshModel model = ModelIO.read(file);

            //Override materials if necessary.
            for(Group g: model.getGroupsRef()) {
                String n = g.getName();
                
                Material mat = materialMap.get(n);
                if(mat == null)
                    mat = materialMap.get("all");
                
                if(mat != null) {
                    g.setMaterial(mat);
                }
            }

            addModel(model);
        }
        
        if(blob.containsKey("transforms")) {
            mBaseTransform = MatrixIO.parseTransformStack(blob.slice("transforms"));
        }
    }
    
    
    public void addModel(MeshModel model) {
        for( Group g: model.getGroupsRef() ) {
            GroupInfo info  = new GroupInfo();
            info.mTriangles = g.getTriangles();
            info.mMaterial  = g.getMaterial();

            mGroupList.add(info);
        }
    }

    
    public void addTriangles(List<Triangle> triangles, Object materialNode) {
        GroupInfo info = new GroupInfo();
        info.mTriangles = new ArrayList<Triangle>(triangles);
        info.mMaterialNode = materialNode;
        mGroupList.add(info);
    }
    
    
    public void enableMaterials(boolean enable) {
        mEnableMaterials = enable;
    }
    
    
    public void enableTextureCoords(boolean enable) {
        mEnableTex = enable;
    }
    
    
    public void enableNormals(boolean enable) {
        mEnableNormals = enable;
    }
    
    
    public void enableColor(boolean enable) {
        mEnableColor = enable;
    }

    
    public void enableMotionControls(boolean enable) {
        mEnableMotion = enable;
    }

    /**
     * Sets the base transform, which is used to transform the model before
     * the MovingObject transforms.
     * 
     * @param transform
     */
    public void setBaseTransform(double[] transform) {
        mBaseTransform = transform;
    }
    
    
    
    public ModelGraph build() {
        List<TrianglesNode> nodeList = new ArrayList<TrianglesNode>( mGroupList.size() );
        List<Object> matList         = new ArrayList<Object>( mGroupList.size() );

        for( GroupInfo info : mGroupList ) {
            nodeList.add( newTrianglesNode( info.mTriangles ) );

            if( mEnableMaterials ) {
                if( info.mMaterialNode != null ) {
                    matList.add( info.mMaterialNode );
                } else if( info.mMaterial != null ) {
                    matList.add( DrawNodes.newMaterialNode( info.mTexture, info.mMaterial ) );
                } else {
                    matList.add( null );
                }
            } else {
                matList.add( null );
            }
        }

        ActorNode motionNode = null;
        if( mEnableMotion )
            motionNode = ActorNode.newInstance( null, mBaseTransform );

        return new ModelGraph( motionNode, matList, nodeList );
    }

    

    private TrianglesNode newTrianglesNode(List<Triangle> t) {
        TrianglesNode node = TrianglesNode.newInstance(t, true);
        
        if(mEnableTex != null)
            node.setBindTextureCoords(mEnableTex);
        
        if(mEnableNormals != null)
            node.setBindNormals(mEnableNormals);
    
        if(mEnableColor != null)
            node.setBindColors(mEnableColor);
        
        return node;
    }

    
    private static class GroupInfo {
        List<Triangle> mTriangles = null;
        TextureNode mTexture      = null;
        Material mMaterial        = null;
        Object mMaterialNode      = null;
    }
    

    /**
     * Determines if a path is relative or absolute and combines it
     * with a base directory as necessary.
     * 
     * @param base
     * @param path
     * @return
     */
    private static File resolvePath(File base, String path) {
        if(path == null)
            return null;
        
        if(base == null || path.startsWith(File. separator))
            return new File(path);
        
        return new File(base, path);
    }
}
