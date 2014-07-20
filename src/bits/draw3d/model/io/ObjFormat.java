package bits.draw3d.model.io;

import bits.draw3d.model.*;
import bits.draw3d.util.Images;
import bits.util.Files;
import bits.util.OutputFileNamer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;


/**
 * @author Philip DeCamp
 */
public class ObjFormat {

    private static final DecimalFormat DEC_FORMAT = new DecimalFormat( "#" );
    static { DEC_FORMAT.setMaximumFractionDigits( 10 ); }


    public static void write( MeshModel model, File outFile ) throws IOException {
        model = prepModel( model );
        File mtlFile = new OutputFileNamer( outFile.getParentFile(), Files.baseName( outFile ), ".mtl", 1 ).next();
        writeMaterials( model, mtlFile );

        PrintWriter out = new PrintWriter( outFile );
        out.println( "mtllib " + mtlFile.getName() );

        Map<double[],Integer> verts = new HashMap<double[],Integer>();
        Map<double[],Integer> texs  = new HashMap<double[],Integer>();
        Map<double[],Integer> norms = new HashMap<double[],Integer>();

        // Write verts
        for( Group g: model.mGroups ) {
            for( Triangle t: g.mTris ) {
                for( double[] v: t.mVerts ) {
                    if( verts.containsKey( v ) ) {
                        continue;
                    }
                    verts.put( v, verts.size() + 1 );
                    format( "v", v, 4, out );
                }
            }
        }

        // Write tex coords.
        for( Group g: model.mGroups ) {
            for( Triangle t: g.mTris ) {
                if( t.mTexs == null ) {
                    continue;
                }
                for( double[] v: t.mTexs ) {
                    if( texs.containsKey( v ) ) {
                        continue;
                    }
                    texs.put( v, texs.size() + 1 ) ;
                    format( "vt", v, 3, out );
                }
            }
        }

        // Write normals
        for( Group g: model.mGroups ) {
            for( Triangle t: g.mTris ) {
                if( t.mNorms == null ) {
                    continue;
                }
                for( double[] v: t.mNorms ) {
                    if( norms.containsKey( v ) ) {
                        continue;
                    }
                    norms.put( v, norms.size() + 1 );
                    format( "vn", v, 4, out );
                }
            }
        }

        // Write triangles.
        for( Group g: model.mGroups ) {
            out.print( "g " );
            out.println( g.mName );
            out.println( "s off" );
            ModelMaterial modMat = g.createModelMaterial();
            if( modMat != null ) {
                out.println( "usemtl " + modMat.mName );
            }

            for( Triangle t: g.mTris ) {
                out.print( 'f' );

                for( int i = 0; i < 3; i++ ) {
                    out.print( ' ' );
                    out.print( verts.get( t.mVerts[i] ) );

                    if( t.mTexs != null ) {
                        out.print( '/' );
                        out.print( texs.get( t.mTexs[i] ) );
                        if( t.mNorms != null ) {
                            out.print( '/' );
                            out.print( norms.get( t.mNorms[i] ) );
                        }
                    } else if( t.mNorms != null ) {
                        out.print( "//" );
                        out.print( norms.get( t.mNorms[i] ) );
                    }
                }

                out.println();
            }
        }

        out.close();
    }


    private static void writeMaterials( MeshModel model, File outFile ) throws IOException {
        PrintWriter out = new PrintWriter( outFile );
        Set<String > complete = new HashSet<String>();
        Map<BufferedImage,File> texMap = new HashMap<BufferedImage,File>();

        for( Group g: model.mGroups ) {
            ModelMaterial mat = g.createModelMaterial();
            if( mat.mName == null || mat.mName.isEmpty() || !complete.add( mat.mName ) ) {
                continue;
            }

            out.print( "newmtl " );
            out.println( mat.mName );
            float alpha = 1f;

            if( mat.mMat != null ) {
                float[] color = new float[3];
                LinearRGBConverter.srgbToLinear( mat.mMat.ambientRef(), color );
                format( "Ka", color, 3, out );
                LinearRGBConverter.srgbToLinear( mat.mMat.diffuseRef(), color );
                format( "Kd", color, 3, out );
                LinearRGBConverter.srgbToLinear( mat.mMat.specularRef(), color );
                format( "Ks", color, 3, out );

                alpha = mat.mMat.ambientRef()[3];
                if( alpha < 1f ) {
                    format( "d", alpha, out );
                }
            }

            out.println( "illum 2" );

            if( mat.mTex != null ) {
                File texFile = texMap.get( mat.mTex );
                if( texFile == null ) {
                    texFile = new OutputFileNamer( outFile.getParentFile(), g.mName, ".png" ).next();
                    texMap.put( mat.mTex, texFile );
                    BufferedImage flipped = flipImage( mat.mTex );
                    ImageIO.write( flipped, "png", texFile );
                }

                out.println( "map_Ka " + texFile.getName() );
                out.println( "map_Kd " + texFile.getName() );
                out.println( "map_Ks " + texFile.getName() );

                if( alpha < 0f ) {
                    out.println( "map_d " + texFile.getName() );
                }
            }

            out.println();
        }

        out.close();
    }


    private static MeshModel prepModel( MeshModel model ) {
        MeshModel ret      = new MeshModel();
        Renamer groupNamer = new Renamer();
        Renamer matNamer   = new Renamer();

        Map<ModelMaterial,ModelMaterial> matMap = new HashMap<ModelMaterial,ModelMaterial>();

        for( Group g: model.mGroups ) {
            String groupName = groupNamer.rename( g.mName );
            ModelMaterial m = g.createModelMaterial();
            if( m == null ) {
                continue;
            }

            ModelMaterial prev = matMap.put( m, m );
            if( prev != null ) {
                matMap.put( prev, prev );
                m = prev;

            } else {
                prev = m;
                m = new ModelMaterial( prev.mTex, prev.mMat );
                if( !prev.mName.isEmpty() ) {
                    m.mName = matNamer.rename( prev.mName );
                } else {
                    m.mName = matNamer.rename( "mtl_" + groupName );
                }
            }

            Group newGroup = new MatGroup( groupName, m, g.mTris );
            ret.mGroups.add( newGroup );
        }

        return ret;
    }


    private static void format( String type, double[] v, int max, PrintWriter out ) {
        if( v == null ) {
            return;
        }
        out.print( type );
        max = Math.min( max, v.length );

        for( int i = 0; i < max; i++ ) {
            out.print( ' ' );
            out.print( DEC_FORMAT.format( v[i] ) );
        }
        out.println();
    }


    private static void format( String type, float[] v, int max, PrintWriter out ) {
        if( v == null ) {
            return;
        }
        out.print( type );
        max = Math.min( max, v.length );
        for( int i = 0; i < max; i++ ) {
            out.print( ' ' );
            out.print( DEC_FORMAT.format( v[i] ) );
        }
        out.println();
    }


    private static void format( String type, float v, PrintWriter out ) {
        out.print( type );
        out.print( ' ' );
        out.println( DEC_FORMAT.format( v ) );
    }


    private static void format( double v, PrintWriter out ) {
        out.print( DEC_FORMAT.format( v ) );
    }


    private static void format( float v, PrintWriter out ) {
        out.print( DEC_FORMAT.format( v ) );
    }


    private static BufferedImage flipImage( BufferedImage im ) {
        final int w = im.getWidth();
        final int h = im.getHeight();
        final int[] row = new int[w];
        BufferedImage ret = new BufferedImage( w, h, im.getType() );

        for( int y = 0; y < h; y++ ) {
            im.getRGB( 0, y, w, 1, row, 0, w );
            ret.setRGB( 0, h - y - 1, w, 1, row, 0, w );
        }

        return ret;
    }


    private static class MatGroup extends Group {

        ModelMaterial mMat;

        public MatGroup( String name, ModelMaterial mat, List<Triangle> tris ) {
            super( name, mat.mTex, mat.mMat, tris );
            mMat = mat;
        }

        @Override
        public ModelMaterial createModelMaterial() {
            return mMat;
        }

    }


    private static class Renamer extends HashSet<String> {

        public String rename( String name ) {
            int suffix = 0;
            String ret = name == null ? "" : name;

            while( ret.isEmpty() || !add( ret ) ) {
                suffix++;
                if( name == null || name.isEmpty() ) {
                    ret = Integer.toString( suffix );
                } else {
                    ret = name + "_" + suffix;
                }
            }

            return ret;
        }

    }

}
