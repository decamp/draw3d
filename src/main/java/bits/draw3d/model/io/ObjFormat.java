package bits.draw3d.model.io;

import bits.draw3d.model.*;
import bits.math3d.Vec3;
import bits.math3d.Vec4;
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


    public static void write( TriModel model, File outFile ) throws IOException {
        model = prepModel( model );
        File mtlFile = new OutputFileNamer( outFile.getParentFile(), Files.baseName( outFile ), ".mtl", 1 ).next();
        writeMaterials( model, mtlFile );

        PrintWriter out = new PrintWriter( outFile );
        out.println( "mtllib " + mtlFile.getName() );

        Map<DrawVert,Integer> verts = new HashMap<DrawVert,Integer>();
        Map<float[],Integer>  texs  = new HashMap<float[],Integer>();
        Map<Vec3,Integer>     norms = new HashMap<Vec3,Integer>();

        // Write verts
        for( TriGroup g: model.mGroups ) {
            for( DrawTri t: g.mTris ) {
                for( DrawVert v: t.mVerts ) {
                    if( verts.containsKey( v ) ) {
                        continue;
                    }
                    verts.put( v, verts.size() + 1 );
                    format3( "v", v.mPos, out );
                }
            }
        }

        // Write tex coords.
        for( TriGroup g: model.mGroups ) {
            for( DrawTri t: g.mTris ) {
                for( DrawVert v: t.mVerts ) {
                    if( v.mTex == null || texs.containsKey( v.mTex ) ) {
                        continue;
                    }
                    texs.put( v.mTex, texs.size() + 1 ) ;
                    format( "vt", v.mTex, 3, out );
                }
            }
        }

        // Write normals
        for( TriGroup g: model.mGroups ) {
            for( DrawTri t: g.mTris ) {
                for( DrawVert v: t.mVerts ) {
                    if( v.mNorm == null || norms.containsKey( v.mNorm ) ) {
                        continue;
                    }
                    norms.put( v.mNorm, norms.size() + 1 );
                    format3( "vn", v.mNorm, out );
                }
            }
        }

        // Write triangles.
        for( TriGroup g: model.mGroups ) {
            out.print( "g " );
            out.println( g.mName );
            out.println( "s off" );
            ModelMaterial modMat = g.mMaterial;
            if( modMat != null ) {
                out.println( "usemtl " + modMat.mName );
            }

            for( DrawTri t: g.mTris ) {
                out.print( 'f' );

                for( int i = 0; i < 3; i++ ) {
                    out.print( ' ' );
                    DrawVert v = t.mVerts[i];
                    out.print( verts.get( v ) );
                    if( v.mTex != null ) {
                        out.print( '/' );
                        out.print( texs.get( v.mTex ) );
                        if( v.mNorm != null ) {
                            out.print( '/' );
                            out.print( norms.get( v.mNorm ) );
                        }
                    } else if( v.mNorm != null ) {
                        out.print( "//" );
                        out.print( norms.get( v.mNorm ) );
                    }
                }
                out.println();
            }
        }

        out.close();
    }


    private static void writeMaterials( TriModel model, File outFile ) throws IOException {
        PrintWriter out = new PrintWriter( outFile );
        Set<String > complete = new HashSet<String>();
        Map<BufferedImage,File> texMap = new HashMap<BufferedImage,File>();

        for( TriGroup g: model.mGroups ) {
            ModelMaterial mat = g.mMaterial;
            if( mat.mName == null || mat.mName.isEmpty() || !complete.add( mat.mName ) ) {
                continue;
            }

            out.print( "newmtl " );
            out.println( mat.mName );
            float alpha = 1f;

            if( mat.mMaterial != null ) {
                Vec4 color = new Vec4();
                LinearRGBConverter.srgbToLinear( mat.mMaterial.mAmbient, color );
                format3( "Ka", color, out );
                LinearRGBConverter.srgbToLinear( mat.mMaterial.mDiffuse, color );
                format3( "Kd", color, out );
                LinearRGBConverter.srgbToLinear( mat.mMaterial.mSpecular, color );
                format3( "Ks", color, out );

                alpha = mat.mMaterial.mDiffuse.w;
                if( alpha < 1f ) {
                    format( "d", alpha, out );
                }
            }

            out.println( "illum 2" );

            if( mat.mTex != null ) {
                File texFile = texMap.get( mat.mImage );
                if( texFile == null ) {
                    texFile = new OutputFileNamer( outFile.getParentFile(), g.mName, ".png" ).next();
                    texMap.put( mat.mImage, texFile );
                    BufferedImage flipped = flipImage( mat.mImage );
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


    private static TriModel prepModel( TriModel model ) {
        TriModel ret       = new TriModel();
        Renamer groupNamer = new Renamer();
        Renamer matNamer   = new Renamer();

        Map<ModelMaterial,ModelMaterial> matMap = new HashMap<ModelMaterial,ModelMaterial>();

        for( TriGroup g: model.mGroups ) {
            String groupName = groupNamer.rename( g.mName );
            ModelMaterial m = g.mMaterial;
            if( m == null ) {
                continue;
            }

            ModelMaterial prev = matMap.put( m, m );
            if( prev != null ) {
                matMap.put( prev, prev );
                m = prev;

            } else {
                prev = m;
                m = new ModelMaterial( prev );
                if( !prev.mName.isEmpty() ) {
                    m.mName = matNamer.rename( prev.mName );
                } else {
                    m.mName = matNamer.rename( "mtl_" + groupName );
                }
            }

            TriGroup newGroup = new TriGroup( groupName, m, g.mTris );
            ret.mGroups.add( newGroup );
        }

        return ret;
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


    private static void format3( String type, Vec3 v, PrintWriter out ) {
        if( v == null ) {
            return;
        }
        out.print( type );
        out.print( ' ' );
        format( v.x, out );
        out.print( ' ' );
        format( v.y, out );
        out.print( ' ' );
        format( v.z, out );
        out.println();
    }


    private static void format( String type, float v, PrintWriter out ) {
        out.print( type );
        out.print( ' ' );
        out.println( DEC_FORMAT.format( v ) );
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


    private static class Renamer {

        private HashSet<String> mSet = new HashSet<String>();

        public String rename( String name ) {
            int suffix = 0;
            String ret = name == null ? "" : name;
            while( ret.isEmpty() || !mSet.add( ret ) ) {
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
