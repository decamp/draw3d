package bits.draw3d.nodes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import bits.blob.Blob;
import bits.draw3d.geom.*;
import bits.draw3d.model.io.ModelIO;
import bits.draw3d.shader.ShaderManager;
import bits.draw3d.tex.Material;
import bits.draw3d.tex.Texture2;
import bits.draw3d.util.MatrixIO;
import bits.math3d.Mat4;
import bits.util.Files;


/**
 * @author decamp
 */
@Deprecated public class ModelGraphBuilder {

    private final ShaderManager   mShaderMan;
    private final List<GroupInfo> mGroupList = new ArrayList<GroupInfo>();

    private boolean mEnableMaterials = true;
    private boolean mEnableTex       = false;
    private boolean mEnableNormals   = false;
    private boolean mEnableColor     = false;
    private boolean mEnableMotion    = false;
    private Mat4    mBaseTransform   = null;


    public ModelGraphBuilder( ShaderManager shaderMan ) {
        mShaderMan = shaderMan;
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


    public void addModel( TriModel model ) {
        for( TriGroup g : model.mGroups ) {
            addModelGroup( g );
       }
    }


    public void addModelGroup( TriGroup group ) {
        GroupInfo info  = new GroupInfo();
        info.mTriangles = group.mTris;
        info.mMaterial  = group.mMaterial.mMaterial;
        info.mTexture   = group.mMaterial.mImage;
        mGroupList.add( info );
    }


    public void parseModelBlob( File baseDir, Blob blob ) throws IOException {
        //Find any override materials specified in blob.
        Map<String, ModelMaterial> materialMap = new HashMap<String, ModelMaterial>();
        if( blob.containsKey( "materials" ) ) {
            for( Object obj : blob.keySet( "materials" ) ) {
                String name = obj.toString();
                ModelMaterial mat = ModelIO.readMaterialBlob( name, blob.slice( "materials", obj ) );
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
            TriModel model = ModelIO.read( file );

            //Override materials if necessary.
            for( TriGroup g : model.mGroups ) {
                String n = g.mName;
                ModelMaterial mat = materialMap.get( n );
                if( mat == null ) {
                    mat = materialMap.get( "all" );
                }

                if( mat != null ) {
                    g.mMaterial = mat;
                }
            }
            addModel( model );
        }

        if( blob.containsKey( "transforms" ) ) {
            mBaseTransform = MatrixIO.parseTransformStack( blob.slice( "transforms" ) );
        }
    }


    public void addTriangles( List<DrawTri> triangles, Object materialNode ) {
        GroupInfo info     = new GroupInfo();
        info.mTriangles    = new ArrayList<DrawTri>( triangles );
        info.mMaterialNode = materialNode;
        mGroupList.add( info );
    }

    /**
     * @param transform The base transform to use to transform model before the MovingObject transform.
     */
    public void setBaseTransform( Mat4 transform ) {
        mBaseTransform = transform;
    }


    public ModelGraph build() {
        Map<BufferedImage,Texture2> texMap = new HashMap<BufferedImage,Texture2>();
        Map<Material,MaterialNode> matMap      = new HashMap<Material,MaterialNode>();

        List<Object> matList = new ArrayList<Object>( mGroupList.size() );
        List<TrianglesNode> nodeList = new ArrayList<TrianglesNode>( mGroupList.size() );

        for( GroupInfo info : mGroupList ) {
            nodeList.add( newTrianglesNode( mShaderMan, info.mTriangles ) );
            nodeList.add( newTrianglesNode( mShaderMan, info.mTriangles ) );

            if( !mEnableTex && !mEnableMaterials ) {
                matList.add( null );
                continue;
            }

            if( info.mMaterialNode != null ) {
                matList.add( info.mMaterialNode );
                continue;
            }

            Texture2 texNode = null;
            MaterialNode matNode = null;

            if( mEnableTex && info.mTexture != null ) {
                texNode = texMap.get( info.mTexture );
                if( texNode == null ) {
                    texNode = new Texture2();
                    texNode.buffer( info.mTexture );
                    texMap.put( info.mTexture, texNode );
                }
            }

            if( mEnableMaterials && info.mMaterial != null ) {
                matNode = matMap.get( info.mMaterial );
                if( matNode == null ) {
                    matNode = new MaterialNode( info.mMaterial, info.mMaterial );
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


    private TrianglesNode newTrianglesNode( ShaderManager shaderMan, List<DrawTri> t ) {
        TrianglesNode node = TrianglesNode.create( shaderMan,
                                                   t,
                                                   mEnableTex,
                                                   mEnableNormals,
                                                   mEnableColor );
        return node;
    }


    private static class GroupInfo {
        List<DrawTri>  mTriangles    = null;
        BufferedImage  mTexture      = null;
        Material       mMaterial     = null;
        Object         mMaterialNode = null;
    }

}

