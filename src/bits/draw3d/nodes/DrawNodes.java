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
    

    public static DrawNode createEnable( final int glConstant ) {
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


    public static DrawNode createDisable( final int glConstant ) {
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


    public static DrawNode createDepthMask( final boolean enable ) {
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


    public static DrawNode createColorMask( final boolean red,
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


    public static DrawNode createPointSize( final float size ) {
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


    public static DrawNode createLineWidth( final float width ) {
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

    
    public static DrawNode createLoadIdentity() {
        return LOAD_IDENTITY;
    }
    
    
    public static DrawNode createLoadMatrix( final int matrix, final double[] matRef ) {
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
    

    public static Object createMaterial( TextureNode optTex, Material optMat ) {
        if( optTex == null ) {
            if( optMat == null ) {
                return null;
            }
            return new MaterialNode( optMat );

        } else if( optMat == null ) {
            return optTex;
        }

        return Arrays.<Object>asList( optTex, new MaterialNode( optMat ) );
    }


    public static <N> RenderModule factoryToModule( final Class<N> nodeClass, final NodeFactory<N> factory ) {
        return new RenderModule() {
            public Object getNodes( Class<?> clazz, RenderTile tile ) {
                if( clazz != nodeClass ) {
                    return null;
                }
                return factory.create( tile );
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
    @Deprecated public static DrawNode newCullFaceNode( final boolean enable, final int face ) {
        return new CullFaceNode( enable, face );
    }

    /**
     * @deprecated use OffsetNode class instead.
     */
    @Deprecated public static DrawNode newPolygonOffsetNode( final float factor, final float units ) {
        return OffsetNode.createFillOffset( true, factor, units );
   }



    public static DrawNode newEnableNode( int glConstant ) {
        return createEnable( glConstant );
    }


    public static DrawNode newDisableNode( int glConstant ) {
        return createDisable( glConstant );
    }


    public static DrawNode newDepthMaskNode( boolean enable ) {
        return createDepthMask( enable );
    }


    public static DrawNode newColorMaskNode( boolean red,
                                             boolean green,
                                             boolean blue,
                                             final boolean alpha )
    {
        return createColorMask( red, green, blue, alpha );
    }


    public static DrawNode newPointSizeNode( float size ) {
        return createPointSize( size );
    }


    public static DrawNode newLineWidthNode( float width ) {
        return createLineWidth( width );
    }


    public static DrawNode newLoadIdentityNode() {
        return LOAD_IDENTITY;
    }


    public static DrawNode newLoadMatrixNode( int matrix, double[] matRef ) {
        return createLoadMatrix( matrix, matRef );
    }


    public static Object newMaterialNode( TextureNode tex, Material mat ) {
        return createMaterial( tex, mat );
    }

}
