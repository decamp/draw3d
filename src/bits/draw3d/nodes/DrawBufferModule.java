package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import bits.draw3d.context.RenderTile;
import bits.draw3d.model.*;


/**
 * @author Philip DeCamp
 */
public class DrawBufferModule implements RenderModule {


    public static DrawBufferModule newInstance( DrawBufferParams params,
                                                ByteBuffer vertBuffer,
                                                int vertUsage,
                                                ByteBuffer indexBuffer,
                                                int indexUsage )
    {
        return new DrawBufferModule( params, vertBuffer, vertUsage, indexBuffer, indexUsage );
    }


    private final DrawBufferParams mParams;
    private       ByteBuffer       mVertBuffer;
    private       int              mVertUsage;
    private       ByteBuffer       mIndexBuffer;
    private       int              mIndexUsage;
    private boolean mCleared = false;


    private DrawBufferModule( DrawBufferParams params,
                              ByteBuffer vertBuffer,
                              int vertUsage,
                              ByteBuffer indBuffer,
                              int indUsage )
    {
        mParams = params;
        mVertBuffer = vertBuffer == null ? null : vertBuffer.duplicate();
        mVertUsage = vertUsage;
        mIndexBuffer = indBuffer == null ? null : indBuffer.duplicate();
        mIndexUsage = indUsage;
    }


    public Object getNodes( Class<?> nodeClass, RenderTile tile ) {
        if( nodeClass != DrawNode.class || mCleared ) {
            return null;
        }

        return new DrawBufferNode( mParams, mVertBuffer, mVertUsage, mIndexBuffer, mIndexUsage );
    }


    /**
     * SHOULD ONLY BE CALLED AFTER MODULE HAS BEEN ACTIVATED
     * FOR ALL DESIRED CONTEXTS.  After the data is clear, it
     * is no longer available to be loaded into a context.
     */
    public void clearData() {
        mCleared = true;
        mVertBuffer = null;
        mIndexBuffer = null;
    }


    private static byte colorToByte( double c ) {
        int n = (int)(c * 255.0 + 0.5);
        return (byte)(n & 0xFF);
    }


    /**
     * @deprecated Use TrianglesNode.
     */
    @Deprecated public static DrawBufferModule fromTriangles( Collection<Triangle> tris ) {
        boolean tex = false;
        boolean norm = false;
        boolean color = false;

        Iterator<Triangle> iter = tris.iterator();
        if( iter.hasNext() ) {
            Triangle t = iter.next();
            tex = t.texRef() != null;
            norm = t.normalRef() != null;
            color = t.colorRef() != null;
        }

        return fromTriangles( tris, tex, norm, color );
    }


    /**
     * @deprecated Use TrianglesNode.
     */
    @Deprecated public static DrawBufferModule fromTriangles( Collection<Triangle> tris,
                                                              boolean tex,
                                                              boolean norm,
                                                              boolean color )
    {
        int count = tris.size();
        int vertSize = 12 + (tex ? 8 : 0) + (norm ? 12 : 0) + (color ? 4 : 0);
        int offset = 0;


        DrawBufferParams params = DrawBufferParams.newInstance();
        params.enableVertexPointer( 3, GL_FLOAT, vertSize, offset );
        offset += 12;

        if( tex ) {
            params.enableTexPointer( 2, GL_FLOAT, vertSize, offset );
            offset += 8;
        }

        if( norm ) {
            params.enableNormPointer( GL_FLOAT, vertSize, offset );
            offset += 12;
        }

        if( color ) {
            params.enableColorPointer( 4, GL_UNSIGNED_BYTE, vertSize, offset );
            offset += 4;
        }

        params.enableCommand( GL_TRIANGLES, 0, count * 3 );


        ByteBuffer buf = ByteBuffer.allocateDirect( count * vertSize );
        buf.order( ByteOrder.nativeOrder() );

        for( Triangle t : tris ) {
            for( int i = 0; i < 3; i++ ) {
                double[] v = t.vertex( i );
                buf.putFloat( (float)v[0] );
                buf.putFloat( (float)v[1] );
                buf.putFloat( (float)v[2] );

                if( tex ) {
                    v = t.texRef( i );

                    if( v != null ) {
                        buf.putFloat( (float)v[0] );
                        buf.putFloat( (float)v[1] );
                    } else {
                        buf.putFloat( 0f );
                        buf.putFloat( 0f );
                    }
                }

                if( norm ) {
                    v = t.normalRef( i );

                    if( v != null ) {
                        buf.putFloat( (float)v[0] );
                        buf.putFloat( (float)v[1] );
                        buf.putFloat( (float)v[2] );
                    } else {
                        buf.putFloat( 0f );
                        buf.putFloat( 0f );
                        buf.putFloat( 0f );
                    }
                }

                if( color ) {
                    v = t.colorRef( i );

                    if( v != null ) {
                        buf.put( colorToByte( v[0] ) );
                        buf.put( colorToByte( v[1] ) );
                        buf.put( colorToByte( v[2] ) );
                        buf.put( colorToByte( v[3] ) );
                    } else {
                        buf.put( (byte)0 );
                        buf.put( (byte)0 );
                        buf.put( (byte)0 );
                        buf.put( (byte)255 );
                    }
                }
            }
        }

        buf.flip();
        return newInstance( params, buf, GL_STATIC_DRAW, null, 0 );
    }


}
