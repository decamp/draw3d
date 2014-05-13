package bits.draw3d.nodes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import bits.draw3d.context.*;
import bits.draw3d.model.*;

import static javax.media.opengl.GL.*;


/**
 * Renders a given model.
 * 
 * @author Philip DeCamp
 */
public class TrianglesNode implements RenderModule {


    public static TrianglesNode create( List<Triangle> trianglesRef, boolean useBuffer ) {
        int texOffset   = 0;
        int normOffset  = 0;
        int colorOffset = 0;

        for( Triangle t : trianglesRef ) {
            if( t.texRef() == null ) {
                texOffset = -1;
            }
            if( t.normalRef() == null ) {
                normOffset = -1;
            }
            if( t.colorRef() == null ) {
                colorOffset = -1;
            }
        }


        int vertBytes = 12;
        if( texOffset >= 0 ) {
            texOffset = vertBytes;
            vertBytes += 8;
        }
        if( normOffset >= 0 ) {
            normOffset = vertBytes;
            vertBytes += 12;
        }
        if( colorOffset >= 0 ) {
            colorOffset = vertBytes;
            vertBytes += 4;
        }

        ByteBuffer buffer = null;
        DrawBufferParams params = null;

        if( useBuffer ) {
            int vertCount = trianglesRef.size() * 3;

            ByteBuffer b = ByteBuffer.allocateDirect( vertBytes * vertCount );
            b.order( ByteOrder.nativeOrder() );

            for( Triangle t : trianglesRef ) {
                double[][] vert = t.vertexRef();
                double[][] tex = t.texRef();
                double[][] col = t.colorRef();
                double[][] norm = t.normalRef();

                for( int i = 0; i < 3; i++ ) {
                    b.putFloat( (float)vert[i][0] );
                    b.putFloat( (float)vert[i][1] );
                    b.putFloat( (float)vert[i][2] );

                    if( texOffset >= 0 ) {
                        b.putFloat( (float)tex[i][0] );
                        b.putFloat( (float)tex[i][1] );
                    }

                    if( normOffset >= 0 ) {
                        b.putFloat( (float)norm[i][0] );
                        b.putFloat( (float)norm[i][1] );
                        b.putFloat( (float)norm[i][2] );
                    }

                    if( colorOffset >= 0 ) {
                        b.put( (byte)(col[i][0] * 255.0 + 0.5) );
                        b.put( (byte)(col[i][1] * 255.0 + 0.5) );
                        b.put( (byte)(col[i][2] * 255.0 + 0.5) );
                        b.put( (byte)(col[i][3] * 255.0 + 0.5) );
                    }
                }
            }

            b.flip();
            buffer = b;

            params = new DrawBufferParams();
            params.verts( 3, GL_FLOAT, vertBytes, 0 );

            if( texOffset >= 0 ) {
                params.texs( 2, GL_FLOAT, vertBytes, texOffset );
            }

            if( normOffset >= 0 ) {
                params.norms( GL_FLOAT, vertBytes, normOffset );
            }

            if( colorOffset >= 0 ) {
                params.colors( 4, GL_UNSIGNED_BYTE, vertBytes, colorOffset );
            }

            params.command( GL_TRIANGLES, 0, vertCount );
        }

        return new TrianglesNode( trianglesRef, buffer, params );
    }


    private final List<Triangle> mTriangles;
    private ByteBuffer mDrawBuffer;
    private DrawBufferParams mDrawParams;
    private TriangleRenderer mRenderer = null;


    public TrianglesNode( List<Triangle> triangleRef,
                          ByteBuffer drawBuffer,
                          DrawBufferParams drawParams )
    {
        mTriangles = triangleRef;
        mDrawBuffer = drawBuffer;
        mDrawParams = drawParams;
    }


    public void enableTextures( boolean enable ) {
        enable &= mDrawParams.mTexSize > 0;
        if( mDrawParams.mTexEnabled == enable ) {
            return;
        }
        mDrawParams.mTexEnabled = enable;
        mRenderer = null;
    }


    public void enableColors( boolean enable ) {
        enable &= mDrawParams.mColorSize > 0;
        if( mDrawParams.mColorEnabled == enable ) {
            return;
        }
        mDrawParams.mColorEnabled = enable;
        mRenderer = null;
    }


    public void enableNormals( boolean enable ) {
        enable &= mDrawParams.mNormStride > 0;
        if( mDrawParams.mNormEnabled == enable ) {
            return;
        }
        mDrawParams.mNormEnabled = enable;
        mRenderer = null;
    }


    public List<Triangle> trianglesRef() {
        return mTriangles;
    }


    @Override
    public Object getNodes( Class<?> nodeClass, RenderTile tile ) {
        if( nodeClass == DrawNode.class ) {
            return new DrawHandler( tile == null || tile.isLast() );
        }

        return null;
    }



    private TriangleRenderer getRenderer() {
        TriangleRenderer rend = mRenderer;
        if( rend != null ) {
            return rend;
        }

        if( mDrawParams.mTexEnabled ) {
            if( mDrawParams.mColorEnabled ) {
                if( mDrawParams.mNormEnabled ) {
                    rend = new OnOnOnRenderer();
                } else {
                    rend = new OnOnOffRenderer();
                }
            } else {
                if( mDrawParams.mNormEnabled ) {
                    rend = new OnOffOnRenderer();
                } else {
                    rend = new OnOffOffRenderer();
                }
            }
        } else {
            if( mDrawParams.mColorEnabled ) {
                if( mDrawParams.mNormEnabled ) {
                    rend = new OffOnOnRenderer();
                } else {
                    rend = new OffOnOffRenderer();
                }
            } else {
                if( mDrawParams.mNormEnabled ) {
                    rend = new OffOffOnRenderer();
                } else {
                    rend = new OffOffOffRenderer();
                }
            }
        }

        mRenderer = rend;
        return rend;
    }



    private class DrawHandler extends DrawNodeAdapter {

        private final boolean mIsLast;
        private final BufferNode mBufferNode;

        DrawHandler( boolean isLast ) {
            mIsLast = isLast;
            if( mDrawBuffer != null ) {
                mBufferNode = BufferNode.createVertexNode( GL_STATIC_DRAW );
                mBufferNode.buffer( mDrawBuffer );
            } else {
                mBufferNode = null;
            }
        }


        @Override
        public void init( GLAutoDrawable gld ) {
            if( mIsLast ) {
                mDrawBuffer = null;
            }
        }


        @Override
        public void pushDraw( GL gl ) {
            if( mBufferNode != null ) {
                mBufferNode.pushDraw( gl );
                mDrawParams.push( gl );
                mDrawParams.execute( gl );
                mDrawParams.pop( gl );
                mBufferNode.popDraw( gl );
            } else {
                render( gl );
            }
        }


        private void render( GL gl ) {
            TriangleRenderer rend = getRenderer();

            gl.glBegin( GL_TRIANGLES );
            rend.render( gl, mTriangles );
            gl.glEnd();
        }

    }



    private static interface TriangleRenderer {
        public void render( GL gl, List<Triangle> tris );
    }


    private static final class OnOnOnRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {
            for( Triangle t : tris ) {
                double[][] tex = t.texRef();

                for( int i = 0; i < 3; i++ ) {
                    gl.glTexCoord2d( tex[i][0], tex[i][1] );
                    gl.glColor3dv( t.colorRef( i ), 0 );
                    gl.glNormal3dv( t.normalRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    private static final class OnOnOffRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {

            for( Triangle t : tris ) {
                double[][] tex = t.texRef();

                for( int i = 0; i < 3; i++ ) {
                    gl.glTexCoord2d( tex[i][0], tex[i][1] );
                    gl.glColor3dv( t.colorRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }

        }
    }


    private static final class OnOffOnRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {

            for( Triangle t : tris ) {
                double[][] tex = t.texRef();

                for( int i = 0; i < 3; i++ ) {
                    gl.glTexCoord2d( tex[i][0], tex[i][1] );
                    gl.glNormal3dv( t.normalRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }

        }
    }


    private static final class OnOffOffRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {

            for( Triangle t : tris ) {
                double[][] tex = t.texRef();

                for( int i = 0; i < 3; i++ ) {
                    gl.glTexCoord2d( tex[i][0], tex[i][1] );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    private static final class OffOnOnRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {
            for( Triangle t : tris ) {
                for( int i = 0; i < 3; i++ ) {
                    gl.glColor3dv( t.colorRef( i ), 0 );
                    gl.glNormal3dv( t.normalRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    private static final class OffOnOffRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {
            for( Triangle t : tris ) {
                for( int i = 0; i < 3; i++ ) {
                    gl.glColor3dv( t.colorRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    private static final class OffOffOnRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {
            for( Triangle t : tris ) {
                for( int i = 0; i < 3; i++ ) {
                    gl.glNormal3dv( t.normalRef( i ), 0 );
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    private static final class OffOffOffRenderer implements TriangleRenderer {
        @Override
        public void render( GL gl, List<Triangle> tris ) {
            for( Triangle t : tris ) {
                for( int i = 0; i < 3; i++ ) {
                    gl.glVertex3dv( t.vertex( i ), 0 );
                }
            }
        }
    }


    @Deprecated public static TrianglesNode newInstance( List<Triangle> trianglesRef, boolean useBuffer ) {
        return create( trianglesRef, useBuffer );
    }


    @Deprecated public void setBindTextureCoords( boolean enable ) {
        enableTextures( enable );
    }


    @Deprecated public void setBindColors( boolean enable ) {
        enableColors( enable );
    }


    @Deprecated public void setBindNormals( boolean enable ) {
        enableNormals( enable );
    }

}
