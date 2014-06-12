package bits.draw3d.model.io;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;

import bits.draw3d.model.*;
import bits.math3d.Vectors;


/**
 * @author Philip DeCamp
 */
public class ObjParser {


    public static MeshModel read( URL url ) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );

        List<Group> groups = new ArrayList<Group>();
        List<Triangle> tris = new ArrayList<Triangle>();
        List<double[]> verts = new ArrayList<double[]>();
        List<double[]> norms = new ArrayList<double[]>();
        List<double[]> texes = new ArrayList<double[]>();
        Map<String, ModelMaterial> materialMap = new LinkedHashMap<String, ModelMaterial>();

        String nextGroup = null;
        ModelMaterial nextMat = null;

        for( String k = in.readLine(); k != null; k = in.readLine() ) {
            String[] tok = k.split( "\\s++" );
            if( tok == null || tok.length == 0 || tok[0].startsWith( "#" ) ) {
                continue;
            }

            if( tok[0].equals( "v" ) ) {
                try {
                    double vx = Double.parseDouble( tok[1] );
                    double vy = Double.parseDouble( tok[2] );
                    double vz = Double.parseDouble( tok[3] );
                    verts.add( new double[]{ vx, vy, vz } );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "vt" ) ) {
                try {
                    double vx = Double.parseDouble( tok[1] );
                    double vy = Double.parseDouble( tok[2] );
                    texes.add( new double[]{ vx, vy } );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "vn" ) ) {
                try {
                    double vx = Double.parseDouble( tok[1] );
                    double vy = Double.parseDouble( tok[2] );
                    double vz = Double.parseDouble( tok[3] );
                    norms.add( new double[]{ vx, vy, vz } );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "f" ) ) {
                if( tok.length > 4 ) {
                    throw new IOException( "Cannot handle faces with more than three vertices." );
                }

                try {
                    double[][] v = new double[3][];
                    double[][] n = null;
                    double[][] t = null;
                    String[] subtok = tok[1].split( "/" );

                    if( subtok.length > 2 && subtok[2].length() > 0 ) {
                        n = new double[3][];
                        if( subtok[1].length() > 0 ) {
                            t = new double[3][];
                        }

                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }

                            v[i] = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                            n[i] = norms.get( Integer.parseInt( subtok[2] ) - 1 ).clone();

                            if( t != null ) {
                                t[i] = texes.get( Integer.parseInt( subtok[1] ) - 1 );
                            }
                        }

                    } else if( subtok.length > 1 && subtok[1].length() > 0 ) {
                        t = new double[3][];

                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }

                            v[i] = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                            t[i] = texes.get( Integer.parseInt( subtok[1] ) - 1 ).clone();
                        }

                    } else {
                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }

                            v[i] = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                        }
                    }

                    if( n == null ) {
                        n = new double[3][];
                        n[0] = new double[3];
                        Vectors.cross( v[0], v[1], v[2], n[0] );
                        Vectors.normalize( n[0], 1.0 );
                        n[1] = n[0].clone();
                        n[2] = n[0].clone();
                    }

                    if( t == null ) {
                        t = new double[][]{ { 0, 0 }, { 0, 0 }, { 0, 0 } };
                    }

                    tris.add( new Triangle( v, n, t ) );

                } catch( Exception ex ) {
                    throw new IOException( "Failed to parse face: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "g" ) || tok[0].equals( "usemtl" ) ) {

                // Write out remaining triangles to new group.
                if( tris.size() > 0 ) {
                    if( nextGroup == null ) {
                        nextGroup = String.format( "unnamed_group_%03d", groups.size() );
                    }

                    if( nextMat == null ) {
                        groups.add( new Group( nextGroup, null, null, tris ) );
                    } else {
                        groups.add( new Group( nextGroup, nextMat.mTex, nextMat.mMat, tris ) );
                    }

                    tris = new ArrayList<Triangle>();
                }

                if( tok[0].equals( "g" ) ) {
                    if( tok.length == 1 ) {
                        throw new IOException( "Failed to parse group: " + k );
                    }

                    nextMat = null;
                    nextGroup = tok[1];

                } else {
                    if( tok.length == 1 ) {
                        throw new IOException( "Failed to parse material: " + k );
                    }

                    nextMat = materialMap.get( tok[1] );
                }

            } else if( tok[0].equals( "mtllib" ) ) {
                URL matUrl = getRelativeURL( url, tok[1] );

                try {
                    readMaterials( matUrl, materialMap );
                } catch( IOException ex ) {
                    // TODO: Should be a warning, or something.
                }
            }

        }


        if( tris.size() > 0 ) {
            if( nextGroup == null ) {
                nextGroup = String.format( "unnamed_group_%03d", groups.size() );
            }

            if( nextMat != null ) {
                groups.add( new Group( nextGroup, nextMat.mTex, nextMat.mMat, tris ) );
            } else {
                groups.add( new Group( nextGroup, null, null, tris ) );
            }

            tris = new ArrayList<Triangle>();
        }

        return new MeshModel( groups );
    }


    public static Map<String, ModelMaterial> readMaterials( URL url,
                                                            Map<String, ModelMaterial> out )
                                                            throws IOException
    {
        if( out == null ) {
            out = new LinkedHashMap<String, ModelMaterial>();
        }

        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
        String[] names    = null;
        float[] ambArr    = null;
        float[] difArr    = null;
        float[] specArr   = null;
        float[] emmArr    = null;
        BufferedImage im  = null;
        float alpha       = 1f;
        float shininess   = 0f;
        boolean defMat    = false;
        boolean alloc     = true;

        for( String k = in.readLine(); k != null; k = in.readLine() ) {
            String[] tok = k.split( "\\s++" );
            if( tok == null || tok.length == 0 || tok[0].startsWith( "#" ) ) {
                continue;
            }

            if( alloc ) {
                alloc = false;
                ambArr = new float[]{ 0f, 0f, 0f, 1f };
                difArr = new float[]{ 0f, 0f, 0f, 1f };
                specArr = new float[]{ 0f, 0f, 0f, 1f };
                emmArr = new float[]{ 0f, 0f, 0f, 1f };
                im = null;
                alpha = 1f;
                shininess = 0f;
            }


            if( tok[0].equals( "newmtl" ) ) {
                if( defMat ) {
                    if( alpha != 1f ) {
                        ambArr[3] *= alpha;
                        difArr[3] *= alpha;
                        specArr[3] *= alpha;
                        emmArr[3] *= alpha;
                    }

                    for( String name : names ) {
                        Material mat = new Material( ambArr, difArr, specArr, emmArr, shininess );
                        mat.name( name );
                        out.put( name, new ModelMaterial( im, mat ) );
                    }
                }

                names = new String[tok.length - 1];
                System.arraycopy( tok, 1, names, 0, names.length );
                defMat = true;
                alloc = true;
                continue;
            }

            if( tok[0].equals( "Ka" ) ) {
                ambArr[0] = Float.parseFloat( tok[1] );
                ambArr[1] = Float.parseFloat( tok[2] );
                ambArr[2] = Float.parseFloat( tok[3] );
                ambArr[3] = 1f;
                continue;
            }

            if( tok[0].equals( "Kd" ) ) {
                difArr[0] = Float.parseFloat( tok[1] );
                difArr[1] = Float.parseFloat( tok[2] );
                difArr[2] = Float.parseFloat( tok[3] );
                difArr[3] = 1f;
                continue;
            }


            if( tok[0].equals( "Ks" ) ) {
                specArr[0] = Float.parseFloat( tok[1] );
                specArr[1] = Float.parseFloat( tok[2] );
                specArr[2] = Float.parseFloat( tok[3] );
                specArr[3] = 1f;
                continue;
            }

            if( tok[0].equals( "d" ) || tok[0].equals( "Tr" ) ) {
                alpha = Float.parseFloat( tok[1] );
                continue;
            }

            if( tok[0].equals( "Ns" ) ) {
                shininess = Float.parseFloat( tok[1] );
                continue;
            }

            if( tok[0].equals( "map_Ka" ) ) {
                URL imUrl = getRelativeURL( url, tok[1] );
                im = ImageIO.read( imUrl );
            }
        }

        if( defMat ) {
            if( alpha != 1f ) {
                ambArr[3] *= alpha;
                difArr[3] *= alpha;
                specArr[3] *= alpha;
                emmArr[3] *= alpha;
            }

            for( String name : names ) {
                Material mat = new Material( ambArr, difArr, specArr, emmArr, shininess );
                mat.name( name );
                out.put( name, new ModelMaterial( im, mat ) );
            }
        }

        return out;
    }


    public static URL getMaterialURL( URL url ) {
        String file = url.getFile();
        int idx = file.lastIndexOf( "." );
        if( idx >= 0 ) {
            file = file.substring( 0, idx );
        }

        file = file + ".mtl";
        try {
            return new URL( url.getProtocol(), url.getHost(), url.getPort(), file );
        } catch( MalformedURLException ex ) {
            return null;
        }

    }


    private static URL getRelativeURL( URL url, String path ) throws MalformedURLException {
        if( !path.startsWith( "/" ) ) {
            String basePath = url.getFile();
            int idx = basePath.lastIndexOf( '/' );
            if( idx > 0 ) {
                basePath = basePath.substring( 0, idx + 1 );
            }
            path = basePath + path;
        }

        return new URL( url.getProtocol(),
                        url.getHost(),
                        url.getPort(),
                        path );

    }

}
