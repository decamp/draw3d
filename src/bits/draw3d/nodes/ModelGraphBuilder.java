package bits.draw3d.nodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import static javax.media.opengl.GL.*;

import bits.blob.Blob;
import bits.draw3d.model.*;
import bits.draw3d.util.MatrixIO;
import bits.util.Files;


/**
 * @author decamp
 */
public class ModelGraphBuilder {

    private final List<GroupInfo> mGroupList = new ArrayList<GroupInfo>();

    private boolean  mEnableMaterials = true;
    private Boolean  mEnableTex       = null;
    private Boolean  mEnableNormals   = null;
    private Boolean  mEnableColor     = false;
    private boolean  mEnableMotion    = false;
    private double[] mBaseTransform   = null;


    public ModelGraphBuilder() {}


    public void parseModelBlob( File baseDir, Blob blob ) throws IOException {
        //Find any override materials specified in blob.
        Map<String, Material> materialMap = new HashMap<String, Material>();

        if( blob.containsKey( "materials" ) ) {
            for( Object obj : blob.keySet( "materials" ) ) {
                String name = obj.toString();
                Material mat = ModelIO.readMaterialBlob( name, blob.slice( "materials", obj ) );
                materialMap.put( name, mat );
            }
        }

        //Read in each model.
        int size = blob.size( "files" );

        for( int i = 0; i < size; i++ ) {
            String path = blob.getString( "files", i );
            if( path == null ) {
                throw new IOException( "Non-string path in Blob" );
            }

            File file = Files.resolve( baseDir, path );
            MeshModel model = ModelIO.read( file );

            //Override materials if necessary.
            for( Group g : model.getGroupsRef() ) {
                String n = g.getName();
                Material mat = materialMap.get( n );
                if( mat == null ) {
                    mat = materialMap.get( "all" );
                }

                if( mat != null ) {
                    g.setMaterial( mat );
                }
            }

            addModel( model );
        }

        if( blob.containsKey( "transforms" ) ) {
            mBaseTransform = MatrixIO.parseTransformStack( blob.slice( "transforms" ) );
        }
    }


    public void addModel( MeshModel model ) {
        for( Group g : model.getGroupsRef() ) {
            addModelGroup( g );
       }
    }


    public void addModelGroup( Group group ) {
        GroupInfo info  = new GroupInfo();
        info.mTriangles = group.getTriangles();
        info.mMaterial  = group.getMaterial();
        info.mTexture   = group.getTexture();
        mGroupList.add( info );
    }


    public void addTriangles( List<Triangle> triangles, Object materialNode ) {
        GroupInfo info     = new GroupInfo();
        info.mTriangles    = new ArrayList<Triangle>( triangles );
        info.mMaterialNode = materialNode;
        mGroupList.add( info );
    }


    public void enableMaterials( boolean enable ) {
        mEnableMaterials = enable;
    }


    public void enableTextureCoords( boolean enable ) {
        mEnableTex = enable;
    }


    public void enableNormals( boolean enable ) {
        mEnableNormals = enable;
    }


    public void enableColor( boolean enable ) {
        mEnableColor = enable;
    }


    public void enableMotionControls( boolean enable ) {
        mEnableMotion = enable;
    }

    /**
     * @param transform The base transform to use to transform model before the MovingObject transform.
     */
    public void setBaseTransform( double[] transform ) {
        mBaseTransform = transform;
    }


    public ModelGraph build() {
        Map<BufferedImage,Texture2Node> texMap = new HashMap<BufferedImage,Texture2Node>();
        Map<Material,MaterialNode> matMap      = new HashMap<Material,MaterialNode>();

        List<Object> matList = new ArrayList<Object>( mGroupList.size() );
        List<TrianglesNode> nodeList = new ArrayList<TrianglesNode>( mGroupList.size() );

        for( GroupInfo info : mGroupList ) {
            nodeList.add( newTrianglesNode( info.mTriangles ) );

            if( !mEnableTex && !mEnableMaterials ) {
                matList.add( null );
                continue;
            }

            if( info.mMaterialNode != null ) {
                matList.add( info.mMaterialNode );
                continue;
            }

            Texture2Node texNode = null;
            MaterialNode matNode = null;

            if( mEnableTex && info.mTexture != null ) {
                texNode = texMap.get( info.mTexture );
                if( texNode == null ) {
                    texNode = new Texture2Node();
                    texNode.buffer( info.mTexture );
                    texMap.put( info.mTexture, texNode );
                }
            }

            if( mEnableMaterials && info.mMaterial != null ) {
                matNode = matMap.get( info.mMaterial );
                if( matNode == null ) {
                    matNode = new MaterialNode( info.mMaterial );
                    matMap.put( info.mMaterial, matNode );
                }
            }

            if( texNode == null ) {
                matList.add( matNode );
            } else if( matNode == null ) {
                matList.add( texNode );
            } else {
                matList.add( Arrays.asList( texNode, matNode ) );
            }
        }

        ActorNode motionNode = null;
        if( mEnableMotion ) {
            motionNode = new ActorNode( null, mBaseTransform );
        }

        return new ModelGraph( motionNode, matList, nodeList );
    }


    private TrianglesNode newTrianglesNode( List<Triangle> t ) {
        TrianglesNode node = TrianglesNode.create( t, true );

        if( mEnableTex != null ) {
            node.enableTextures( mEnableTex );
        }

        if( mEnableNormals != null ) {
            node.enableNormals( mEnableNormals );
        }

        if( mEnableColor != null ) {
            node.enableColors( mEnableColor );
        }

        return node;
    }


    private static class GroupInfo {
        List<Triangle> mTriangles    = null;
        BufferedImage  mTexture      = null;
        Material       mMaterial     = null;
        Object         mMaterialNode = null;
    }

}

