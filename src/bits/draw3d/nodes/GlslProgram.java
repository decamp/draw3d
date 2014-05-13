package bits.draw3d.nodes;

import java.io.*;
import java.util.*;
import javax.media.opengl.*;


/**
 * @author decamp
 */
public class GlslProgram extends DrawNodeAdapter {


    private List<Shader> mShaders   = new ArrayList<Shader>();
    private int          mProgramId = 0;
    private int[]        mShaderIds = null;


    public GlslProgram() {}


    public synchronized void addShader( ShaderType type, File code ) throws IOException {
        addShader( type, fileToString( code ) );
    }


    public synchronized void addShader( ShaderType type, String code ) throws IOException {
        if( mShaders == null ) {
            throw new IllegalStateException( "Program already initialized." );
        }

        Shader shader = new Shader( type, code );
        mShaders.add( shader );
    }


    @Override
    public void pushDraw( GL gl ) {
        gl.glUseProgram( mProgramId );
    }


    @Override
    public void popDraw( GL gl ) {
        gl.glUseProgram( 0 );
    }


    @Override
    public synchronized void dispose( GLAutoDrawable gld ) {
        GL gl = gld.getGL();

        for( int i = 0; i < mShaderIds.length; i++ ) {
            if( mShaderIds[i] != 0 ) {
                gl.glDeleteShader( mShaderIds[i] );
                mShaderIds[i] = 0;
            }
        }

        if( mProgramId != 0 ) {
            gl.glDeleteProgram( mProgramId );
            mProgramId = 0;
        }
    }


    public int programId() {
        return mProgramId;
    }


    public void compile( GL gl ) {
        List<Shader> shaders;
        int err;

        synchronized( this ) {
            shaders = mShaders;
            mShaders = null;
        }

        if( shaders.isEmpty() ) {
            return;
        }

        int[] ids = new int[shaders.size()];

        for( int i = 0; i < ids.length; i++ ) {
            Shader s = shaders.get( i );
            ids[i] = gl.glCreateShader( s.mType.glslCode() );

            if( ids[i] == 0 ) {
                throw new RuntimeException( "Failed to generate shader" );
            }

            gl.glShaderSource( ids[i], 1, new String[]{ s.mCode }, null );
            gl.glCompileShader( ids[i] );

            err = gl.glGetError();
            if( err != 0 ) {
                throw new UnsatisfiedLinkError( "Failed to compile GLSL Shader: " + err );
            }
        }

        mShaderIds = ids;
        mProgramId = gl.glCreateProgram();

        if( mProgramId == 0 ) {
            throw new RuntimeException( "Failed to create GLSL program." );
        }

        for( int i = 0; i < ids.length; i++ ) {
            gl.glAttachShader( mProgramId, ids[i] );
            err = gl.glGetError();
            if( err != 0 ) {
                throw new UnsatisfiedLinkError( "Failed to attach GLSL shader: " + err );
            }
        }
    }


    public void link( GL gl ) {
        gl.glLinkProgram( mProgramId );
        int err = gl.glGetError();

        if( err != 0 ) {
            throw new UnsatisfiedLinkError( "Failed to link GLSL program: " + err );
        }

    }


    private static String fileToString( File file ) throws IOException {
        StringBuilder s = new StringBuilder();

        BufferedReader in = new BufferedReader( new FileReader( file ) );
        for( String k = in.readLine(); k != null; k = in.readLine() ) {
            s.append( k ).append( '\n' );
        }
        in.close();

        return s.toString();
    }


    private static class Shader {

        final ShaderType mType;
        final String     mCode;

        Shader( ShaderType type, String code ) {
            mType = type;
            mCode = code;
        }

    }


    @Deprecated
    public static GlslProgram newInstance() {
        return new GlslProgram();
    }


}
