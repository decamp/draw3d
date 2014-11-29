/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.*;

import java.nio.FloatBuffer;
import java.util.*;


/**
 * @author Philip DeCamp
 */
public class Uniforms {

    public static final String PROJ_MAT          = "PROJ_MAT";
    public static final String INV_PROJ_MAT      = "INV_PROJ_MAT";
    public static final String VIEW_MAT          = "VIEW_MAT";
    public static final String INV_VIEW_MAT      = "INV_VIEW_MAT";
    public static final String PROJ_VIEW_MAT     = "PROJ_VIEW_MAT";
    public static final String INV_PROJ_VIEW_MAT = "INV_PROJ_VIEW_MAT";
    public static final String NORM_MAT          = "NORM_MAT";
    public static final String INV_NORM_MAT      = "INV_NORM_MAT";
    public static final String VIEWPORT          = "VIEWPORT";
    public static final String VIEWPORT_MAT      = "VIEWPORT_MAT";
    public static final String INV_VIEWPORT_MAT  = "INV_VIEWPORT_MAT";
    public static final String COLOR_MAT         = "COLOR_MAT";
    public static final String INV_COLOR_MAT     = "INV_COLOR_MAT";
    public static final String TEX_MAT           = "TEX_MAT";
    public static final String INV_TEX_MAT       = "INV_TEX_MAT";
    public static final String LINE_WIDTH        = "LINE_WIDTH";

    public static final String TEX_UNIT0         = "TEX_UNIT0";
    public static final String TEX_UNIT1         = "TEX_UNIT1";
    public static final String TEX_UNIT2         = "TEX_UNIT2";
    public static final String TEX_UNIT3         = "TEX_UNIT3";
    public static final String TEX_UNIT4         = "TEX_UNIT4";
    public static final String TEX_UNIT5         = "TEX_UNIT5";
    public static final String TEX_UNIT6         = "TEX_UNIT6";
    public static final String TEX_UNIT7         = "TEX_UNIT7";


    private static final Map<String, Integer> DEFAULT_BLOCK_BINDINGS = new HashMap<String, Integer>();

    static {
        DEFAULT_BLOCK_BINDINGS.put( "FOG",          5 );
        DEFAULT_BLOCK_BINDINGS.put( "MATERIALS",    6 );
        DEFAULT_BLOCK_BINDINGS.put( "LIGHTS",       7 );
        // Assume that 24 bindLocation locations are available.
    }


    public static DrawTask loaderFor( ProgramResource res ) {
        String name = res.mName.intern();
        if( name == PROJ_MAT ) {
            return new ProjMat( res.mLocation );
        } else if( name == INV_PROJ_MAT ) {
            return new InvProjMat( res.mLocation );
        } else if( name == VIEW_MAT ) {
            return new ViewMat( res.mLocation );
        } else if( name == INV_VIEW_MAT ) {
            return new InvViewMat( res.mLocation );
        } else if( name == PROJ_VIEW_MAT ) {
            return new ProjViewMat( res.mLocation );
        } else if( name == INV_PROJ_VIEW_MAT ) {
            return new InvProjViewMat( res.mLocation );
        } else if( name == NORM_MAT ) {
            return new NormMat( res.mLocation );
        } else if( name == INV_NORM_MAT ) {
            return new InvNormMat( res.mLocation );
        } else if( name == VIEWPORT ) {
            return new Viewport( res.mLocation );
        } else if( name == VIEWPORT_MAT ) {
            return new ViewportMat( res.mLocation );
        } else if( name == INV_VIEWPORT_MAT ) {
            return new InvViewportMat( res.mLocation );
        } else if( name == COLOR_MAT ) {
            return new ColorMat( res.mLocation );
        } else if( name == INV_COLOR_MAT ) {
            return new InvColorMat( res.mLocation );
        } else if( name == TEX_MAT ) {
            return new TexMat( res.mLocation );
        } else if( name == INV_TEX_MAT ) {
            return new InvTexMat( res.mLocation );
        } else if( name == LINE_WIDTH ) {
            return new LineWidth( res.mLocation );
        }

        return null;
    }


    public static void addAvailableLoaders( AutoloadProgram prog ) {
        for( ProgramResource res: prog.uniformsRef() ) {
            DrawTask task = loaderFor( res );
            if( task != null ) {
                prog.addBindTask( task );
            }
        }
    }


    public static int defaultBlockBinding( String name ) {
        Integer n = DEFAULT_BLOCK_BINDINGS.get( name );
        return n == null ? -1 : n;
    }

    /**
     * Sets all sampler uniforms of the form "texUnitXXX" to value "XXX".
     * @param uniforms List of program uniforms for BOUND program.
     */
    public static void setDefaultTexUnits( DrawEnv d, Collection<? extends ProgramResource> uniforms ) {
        for( ProgramResource res: uniforms ) {
            String name = res.mName;
            if( !name.startsWith( "TEX_UNIT" ) ) {
                continue;
            }
            try {
                int n = Integer.parseInt( name.substring( 8 ) );
                d.mGl.glUniform1i( res.mLocation, n );
                d.checkErr();
            } catch( NumberFormatException ignored ) {}
        }
        d.checkErr();
    }


    public static void setDefaultBlockBindings( DrawEnv d, int program, Collection<? extends UniformBlock> blocks ) {
        for( UniformBlock ub: blocks ) {
            int binding = defaultBlockBinding( ub.mName );
            if( binding >= 0 ) {
                d.mGl.glUniformBlockBinding( program, ub.mIndex, binding );
            }
        }
        d.checkErr();
    }


    public static final class ProjMat implements DrawTask {
        private final int mLocation;
        public ProjMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            FloatBuffer buf = g.mWorkFloats;
            buf.clear();
            Mat.put( g.mProj.get(), buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvProjMat implements DrawTask {
        private final int mLocation;
        public InvProjMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;

            Mat.invert( g.mProj.get(), mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class ViewMat implements DrawTask {
        private final int mLocation;
        public ViewMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            FloatBuffer buf = g.mWorkFloats;
            buf.clear();
            Mat.put( g.mView.get(), buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvViewMat implements DrawTask {
        private final int mLocation;
        public InvViewMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;

            Mat.invert( g.mView.get(), mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class ProjViewMat implements DrawTask {
        private final int mLocation;
        public ProjViewMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;
            Mat.mult( g.mProj.get(), g.mView.get(), mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvProjViewMat implements DrawTask {
        private final int mLocation;
        public InvProjViewMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;

            Mat.mult( g.mProj.get(), g.mView.get(), mat );
            Mat.invert( mat, mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class NormMat implements DrawTask {
        private final int mLocation;
        public NormMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            // normMat = transpose( inverse( modelView ) )
            Mat4 mat = g.mWorkMat4;
            Mat.invert( g.mView.get(), mat );
            FloatBuffer buf = g.mWorkFloats;
            buf.clear();
            // Only load transpose of top 3x3.
            buf.put( mat.m00 ).put( mat.m01 ).put( mat.m02 );
            buf.put( mat.m10 ).put( mat.m11 ).put( mat.m12 );
            buf.put( mat.m20 ).put( mat.m21 ).put( mat.m22 );
            buf.flip();
            g.mGl.glUniformMatrix3fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvNormMat implements DrawTask {
        private final int mLocation;
        public InvNormMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            Mat.invert( g.mView.get(), mat );
            FloatBuffer buf = g.mWorkFloats;
            buf.clear();
            // Only load transpose of top 3x3.
            buf.put( mat.m00 ).put( mat.m01 ).put( mat.m02 );
            buf.put( mat.m10 ).put( mat.m11 ).put( mat.m12 );
            buf.put( mat.m20 ).put( mat.m21 ).put( mat.m22 );
            buf.flip();
            g.mGl.glUniformMatrix3fv( mLocation, 1, false, buf );
        }
    }


    public static final class Viewport implements DrawTask {
        private final int mLocation;
        public Viewport( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            g.mGl.glUniform4f( mLocation, g.mViewport.mX, g.mViewport.mY, g.mViewport.mW, g.mViewport.mH );
        }
    }


    public static final class ViewportMat implements DrawTask {
        private final int mLocation;
        public ViewportMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;
            Mat.viewport( g.mViewport.mX, g.mViewport.mY, g.mViewport.mW, g.mViewport.mH, mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvViewportMat implements DrawTask {
        private final int mLocation;
        public InvViewportMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;
            Mat.viewport( g.mViewport.mX, g.mViewport.mY, g.mViewport.mW, g.mViewport.mH, mat );
            Mat.invert( mat, mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class ColorMat implements DrawTask {
        private final int mLocation;
        public ColorMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            FloatBuffer buf = g.mWorkFloats;

            buf.clear();
            Mat.put( g.mColorMat.get(), buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvColorMat implements DrawTask {
        private final int mLocation;
        public InvColorMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;

            Mat.invert( g.mColorMat.get(), mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class TexMat implements DrawTask {
        private final int mLocation;
        public TexMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            FloatBuffer buf = g.mWorkFloats;

            buf.clear();
            Mat.put( g.mTexMat.get(), buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class InvTexMat implements DrawTask {
        private final int mLocation;
        public InvTexMat( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            Mat4 mat = g.mWorkMat4;
            FloatBuffer buf = g.mWorkFloats;

            Mat.invert( g.mTexMat.get(), mat );
            buf.clear();
            Mat.put( mat, buf );
            buf.flip();
            g.mGl.glUniformMatrix4fv( mLocation, 1, false, buf );
        }
    }


    public static final class LineWidth implements DrawTask {
        private final int mLocation;
        public LineWidth( int location ) {
            mLocation = location;
        }

        public void run( DrawEnv g ) {
            g.mGl.glUniform1f( mLocation, g.mLineWidth.mValue );
        }
    }

}
