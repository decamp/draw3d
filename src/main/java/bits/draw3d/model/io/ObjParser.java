/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model.io;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.ImageIO;

import bits.draw3d.model.*;
import bits.draw3d.Material;
import bits.math3d.Vec3;
import bits.math3d.Vec4;


/**
 * @author Philip DeCamp
 */
public class ObjParser {


    public static TriModel read( URL url ) throws IOException {
        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );

        List<TriGroup> groups = new ArrayList<TriGroup>();
        List<DrawTri>  tris   = new ArrayList<DrawTri>();
        List<Vec3>     verts  = new ArrayList<Vec3>();
        List<Vec3>     norms  = new ArrayList<Vec3>();
        List<float[]>  texes  = new ArrayList<float[]>();
        Map<String, DrawMaterial> materialMap = new LinkedHashMap<String, DrawMaterial>();

        String nextGroupName = null;
        DrawMaterial nextMat = null;

        for( String k = in.readLine(); k != null; k = in.readLine() ) {
            String[] tok = k.split( "\\s++" );
            if( tok == null || tok.length == 0 || tok[0].startsWith( "#" ) ) {
                continue;
            }

            if( tok[0].equals( "v" ) ) {
                try {
                    float vx = Float.parseFloat( tok[1] );
                    float vy = Float.parseFloat( tok[2] );
                    float vz = Float.parseFloat( tok[3] );
                    verts.add( new Vec3( vx, vy, vz ) );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "vt" ) ) {
                try {
                    float vx = Float.parseFloat( tok[1] );
                    float vy = Float.parseFloat( tok[2] );
                    texes.add( new float[]{ vx, vy } );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "vn" ) ) {
                try {
                    float vx = Float.parseFloat( tok[1] );
                    float vy = Float.parseFloat( tok[2] );
                    float vz = Float.parseFloat( tok[3] );
                    norms.add( new Vec3( vx, vy, vz ) );
                } catch( Exception ex ) {
                    throw new IOException( "Could not parse vertex: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "f" ) ) {
                if( tok.length > 4 ) {
                    throw new IOException( "Cannot handle faces with more than three vertices." );
                }

                try {
                    DrawTri tri = new DrawTri( new DrawVert(), new DrawVert(), new DrawVert() );
                    String[] subtok = tok[1].split( "/" );

                    if( subtok.length > 2 && subtok[2].length() > 0 ) {
                        boolean hasTex = !subtok[1].isEmpty();

                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }
                            DrawVert v = tri.mVerts[i];
                            v.mPos = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                            v.mNorm = new Vec3( norms.get( Integer.parseInt( subtok[2] ) - 1 ) );
                            if( hasTex ) {
                                v.mTex = texes.get( Integer.parseInt( subtok[1] ) - 1 ).clone();
                            }
                        }
                    } else if( subtok.length > 1 && subtok[1].length() > 0 ) {
                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }
                            DrawVert v = tri.mVerts[i];
                            v.mPos = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                            v.mTex = texes.get( Integer.parseInt( subtok[1] ) - 1 ).clone();
                        }

                    } else {
                        for( int i = 0; i < 3; i++ ) {
                            if( i > 0 ) {
                                subtok = tok[i + 1].split( "/" );
                            }
                            DrawVert v = tri.mVerts[i];
                            v.mPos  = verts.get( Integer.parseInt( subtok[0] ) - 1 );
                        }
                    }

                    tris.add( tri );
                } catch( Exception ex ) {
                    throw new IOException( "Failed to parse face: " + ex.getMessage() + "\n" + k );
                }

            } else if( tok[0].equals( "g" ) || tok[0].equals( "usemtl" ) ) {
                // Write out remaining triangles to new group.
                if( tris.size() > 0 ) {
                    if( nextGroupName == null ) {
                        nextGroupName = String.format( "unnamed_group_%03d", groups.size() );
                    }

                    groups.add( new TriGroup( nextGroupName, nextMat, tris ) );
                    tris = new ArrayList<DrawTri>();
                }

                if( tok[0].equals( "g" ) ) {
                    if( tok.length == 1 ) {
                        throw new IOException( "Failed to parse group: " + k );
                    }
                    nextMat = null;
                    nextGroupName = tok[1];

                } else {
                    if( tok.length == 1 ) {
                        throw new IOException( "Failed to parse material: " + k );
                    }
                    nextMat = materialMap.get( tok[1] );
                }

            } else if( tok[0].equals( "mtllib" ) ) {
                URL matUrl = getRelativeUrl( url, tok[1] );

                try {
                    readMaterials( matUrl, materialMap );
                } catch( IOException ex ) {
                    // TODO: Should be a warning, or something.
                }
            }

        }

        if( tris.size() > 0 ) {
            if( nextGroupName == null ) {
                nextGroupName = String.format( "unnamed_group_%03d", groups.size() );
            }

            groups.add( new TriGroup( nextGroupName, nextMat, tris ) );
        }

        return new TriModel( "", groups );
    }


    public static Map<String, DrawMaterial> readMaterials( URL url,
                                                            Map<String, DrawMaterial> out )
                                                            throws IOException
    {
        if( out == null ) {
            out = new LinkedHashMap<String, DrawMaterial>();
        }

        BufferedReader in = new BufferedReader( new InputStreamReader( url.openStream() ) );
        DrawMaterial material = null;
        String[] names = null;

        for( String k = in.readLine(); k != null; k = in.readLine() ) {
            String[] tok = k.split( "\\s++" );
            if( tok == null || tok.length == 0 || tok[0].startsWith( "#" ) ) {
                continue;
            }

            if( tok[0].equals( "newmtl" ) ) {
                if( material != null ) {
                    for( String name: names ) {
                        DrawMaterial copy = new DrawMaterial( material );
                        copy.mName     = name;
                        copy.mMaterial = new Material( material.mMaterial );
                        out.put( name, material );
                    }
                }

                material = new DrawMaterial();
                names = Arrays.copyOfRange( tok, 1, tok.length );
                material.mName = tok.length > 1 ? tok[1] : "";
                material.mMaterial = new Material();
                for( int i = 1; i < tok.length; i++ ) {
                    String name = tok[i];
                    out.put( name, material );
                }
                continue;
            }

            if( tok[0].equals( "Ka" ) ) {
                Vec4 v = material.mMaterial.mAmbient;
                v.x = Float.parseFloat( tok[1] );
                v.y = Float.parseFloat( tok[2] );
                v.z = Float.parseFloat( tok[3] );
                continue;
            }

            if( tok[0].equals( "Kd" ) ) {
                Vec4 v = material.mMaterial.mDiffuse;
                v.x = Float.parseFloat( tok[1] );
                v.y = Float.parseFloat( tok[2] );
                v.z = Float.parseFloat( tok[3] );
                continue;
            }


            if( tok[0].equals( "Ks" ) ) {
                Vec4 v = material.mMaterial.mSpecular;
                v.x = Float.parseFloat( tok[1] );
                v.y = Float.parseFloat( tok[2] );
                v.z = Float.parseFloat( tok[3] );
                continue;
            }

            if( tok[0].equals( "d" ) || tok[0].equals( "Tr" ) ) {
                material.mMaterial.alpha( Float.parseFloat( tok[1] ) );
                continue;
            }

            if( tok[0].equals( "Ns" ) ) {
                material.mMaterial.mShininess = Float.parseFloat( tok[1] );
                continue;
            }

            if( tok[0].equals( "map_Ka" ) ) {
                URL imUrl = getRelativeUrl( url, tok[1] );
                material.mImage = ImageIO.read( imUrl );
            }
        }

        if( material != null ) {
            for( String name: names ) {
                DrawMaterial copy = new DrawMaterial( material );
                copy.mName     = name;
                copy.mMaterial = new Material( material.mMaterial );
                out.put( name, material );
            }
        }

        return out;
    }


    public static URL getMaterialUrl( URL url ) {
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


    private static URL getRelativeUrl( URL url, String path ) throws MalformedURLException {
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
