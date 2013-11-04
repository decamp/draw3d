package bits.draw3d.nodes;

import java.util.Arrays;

import javax.media.opengl.GL;

import bits.draw3d.context.RenderTile;
import bits.draw3d.model.*;
import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public final class DrawNodes {
    

    public static DrawNode newEnableNode( final int glConstant ) {
        return new DrawNodeAdapter() {
            boolean mRevert = false;

            @Override
            public void pushDraw( GL gl ) {
                mRevert = gl.glIsEnabled( glConstant );
                gl.glEnable( glConstant );
            }

            @Override
            public void popDraw( GL gl ) {
                if( mRevert ) {
                    gl.glEnable( glConstant );
                } else {
                    gl.glDisable( glConstant );
                }
            }

        };
    }


    public static DrawNode newDisableNode( final int glConstant ) {
        return new DrawNodeAdapter() {
            boolean mRevert = false;

            public void pushDraw( GL gl ) {
                mRevert = gl.glIsEnabled( glConstant );
                gl.glDisable( glConstant );
            }

            public void popDraw( GL gl ) {
                if( mRevert ) {
                    gl.glEnable( glConstant );
                } else {
                    gl.glDisable( glConstant );
                }
            }
        };

    }


    public static DrawNode newDepthMaskNode( final boolean enable ) {
        return new DrawNodeAdapter() {
            byte[] mPrev = { 0 };

            public void pushDraw( GL gl ) {
                gl.glGetBooleanv( GL_DEPTH_WRITEMASK, mPrev, 0 );
                gl.glDepthMask( enable );
            }

            public void popDraw( GL gl ) {
                gl.glDepthMask( mPrev[0] != 0 );
            }

        };
    }


    public static DrawNode newColorMaskNode( final boolean red,
            final boolean green,
            final boolean blue,
            final boolean alpha )
    {
        return new DrawNodeAdapter() {
            private final byte[] mRevert = { 0, 0, 0, 0 };

            @Override
            public void pushDraw( GL gl ) {
                gl.glGetBooleanv( GL_COLOR_WRITEMASK, mRevert, 0 );
                gl.glColorMask( red, green, blue, alpha );
            }

            @Override
            public void popDraw( GL gl ) {
                gl.glColorMask( mRevert[0] != 0,
                        mRevert[1] != 0,
                        mRevert[2] != 0,
                        mRevert[3] != 0 );
            }
        };
    }


    public static DrawNode newPointSizeNode( final float size ) {
        return new DrawNodeAdapter() {
            float[] mRevert = { 1f };

            public void pushDraw( GL gl ) {
                gl.glGetFloatv( GL_POINT_SIZE, mRevert, 0 );
                gl.glPointSize( size );
            }

            public void popDraw( GL gl ) {
                gl.glPointSize( mRevert[0] );
            }
        };
    }


    public static DrawNode newLineWidthNode( final float width ) {
        return new DrawNodeAdapter() {
            float[] mRevert = { 1f };

            public void pushDraw( GL gl ) {
                gl.glGetFloatv( GL_LINE_WIDTH, mRevert, 0 );
                gl.glPointSize( width );
            }

            public void popDraw( GL gl ) {
                gl.glPointSize( mRevert[0] );
            }
        };
    }

    
    public static DrawNode newLoadIdentityNode() {
        return LOAD_IDENTITY;
    }
    
    
    public static DrawNode newLoadMatrixNode( final int matrix, final double[] matRef ) {
        return new DrawNodeAdapter() {
            @Override
            public void pushDraw( GL gl ) {
                gl.glMatrixMode( matrix );
                gl.glPushMatrix();
                gl.glLoadMatrixd( matRef, 0 );
            }
            
            @Override
            public void popDraw( GL gl ) {
                gl.glMatrixMode( matrix );
                gl.glPopMatrix();
            }
        };
    }
    
    

    public static Object newMaterialNode( TextureNode tex, Material mat ) {
        if( tex == null ) {
            if( mat == null ) {
                return null;
            }
            return MaterialNode.newInstance( mat );

        } else if( mat == null ) {
            return tex;
        }

        return Arrays.<Object>asList( tex, MaterialNode.newInstance( mat ) );
    }



    public static <N> RenderModule factoryToModule( final Class<N> nodeClass, final NodeFactory<N> factory ) {
        return new RenderModule() {
            public Object getNodes( Class<?> clazz, RenderTile tile ) {
                if( clazz != nodeClass )
                    return null;

                return factory.newInstance( tile );
            }
        };
    }



    private DrawNodes() {}
    
    
    
    private static DrawNode LOAD_IDENTITY = new DrawNodeAdapter() {

        @Override
        public void pushDraw( GL gl ) {
            gl.glMatrixMode( GL_PROJECTION );
            gl.glPushMatrix();
            gl.glLoadIdentity();
            gl.glMatrixMode( GL_MODELVIEW );
            gl.glPushMatrix();
            gl.glLoadIdentity();
        }
        
        @Override
        public void popDraw( GL gl ) {
            gl.glMatrixMode( GL_PROJECTION );
            gl.glPopMatrix();
            gl.glMatrixMode( GL_MODELVIEW );
            gl.glPopMatrix();
        }
        
    };

    
    
    
    

    /**
     * @deprecated Use CullFaceNode class instance.
     */
    public static DrawNode newCullFaceNode( final boolean enable, final int face ) {
        return new DrawNodeAdapter() {
            private boolean     mRevertEnable = false;
            private final int[] mRevertFace   = { GL_BACK };

            public void pushDraw( GL gl ) {
                mRevertEnable = gl.glIsEnabled( GL_CULL_FACE );
                gl.glGetIntegerv( GL_CULL_FACE_MODE, mRevertFace, 0 );

                if( enable ) {
                    gl.glEnable( GL_CULL_FACE );
                } else {
                    gl.glDisable( GL_CULL_FACE );
                }

                gl.glCullFace( face );
            }


            public void popDraw( GL gl ) {
                if( mRevertEnable ) {
                    gl.glEnable( GL_CULL_FACE );
                } else {
                    gl.glDisable( GL_CULL_FACE );
                }

                gl.glCullFace( mRevertFace[0] );
            }
        };
    }

    /**
     * @deprecated use PolyOffsetNode class instead.
     */
    public static DrawNode newPolygonOffsetNode( final float factor, final float units ) {
        return new DrawNodeAdapter() {
            private final float[] mRevert = { 0, 0 };

            public void pushDraw( GL gl ) {
                gl.glGetFloatv( GL_POLYGON_OFFSET_FACTOR, mRevert, 0 );
                gl.glGetFloatv( GL_POLYGON_OFFSET_UNITS, mRevert, 1 );
                gl.glPolygonOffset( factor, units );
            }

            public void popDraw( GL gl ) {
                gl.glPolygonOffset( mRevert[0], mRevert[1] );
            }
        };
    }

    
}
