/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shader;

import javax.media.opengl.*;
import java.io.UnsupportedEncodingException;

import static javax.media.opengl.GL2ES2.*;


/**
 * @author Philip DeCamp
 */
public class ShaderUtil {


    public static String readShaderInfoLog( GL2ES2 gl, int shader ) {
        int[] len = { 0 };
        gl.glGetShaderiv( shader, GL_INFO_LOG_LENGTH, len, 0 );
        if( len[0] <= 0 ) {
            return "";
        }

        byte[] bytes = new byte[ len[0] ];
        gl.glGetShaderInfoLog( shader, len[0], len, 0, bytes, 0 );
        try {
            return new String( bytes, 0, bytes.length - 1, "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            // No worries here. UTF-8 will be supported.
            throw new RuntimeException( ex );
        }
   }

    /**
     * Convenience method that creates a shader, attaches source, compiles, and checks for errors.
     *
     * @param gl          Current graphics
     * @param shaderType  GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, or GL_GEOMETRY_SHADER
     * @param source      Source GLSL code.
     * @return id of newly created shader.
     * @throws javax.media.opengl.GLException if shader creation fails.
     */
    public static int compile( GL2ES2 gl, int shaderType, String source ) throws GLException {
        int id = gl.glCreateShader( shaderType );
        int[] arr = { source.length() };
        gl.glShaderSource( id, 1, new String[]{ source }, arr, 0 );
        gl.glCompileShader( id );
        gl.glGetShaderiv( id, GL_COMPILE_STATUS, arr, 0 );
        if( arr[0] == 0 ) {
            String msg = ShaderUtil.readShaderInfoLog( gl, id );
            msg = "Shader Compilation Failed : " + msg;
            gl.glDeleteShader( id );
            throw new GLException( msg );
        }
        return id;
    }


    public static ProgramResource[] listAttributes( GL2ES2 gl, int program ) {
        if( gl.getGLProfile().isGL4() ) {
            return listResources( (GL4)gl, program, GL4.GL_PROGRAM_INPUT );
        } else {
            return listAttributes2( gl, program );
        }
    }


    public static ProgramResource[] listUniforms( GL2ES2 gl, int program ) {
        if( gl.getGLProfile().isGL4() ) {
            return listResources( (GL4)gl, program, GL4.GL_UNIFORM );
        } else {
            return listUniforms2( gl, program );
        }
    }


    public static ProgramResource[] listResources( GL4 gl, int prog, int progInterface ) {
        int[] keys = { GL4.GL_NAME_LENGTH, GL4.GL_LOCATION, GL4.GL_TYPE, GL4.GL_ARRAY_SIZE };
        int[] vals = { 0, 0, 0, 0 };
        gl.glGetProgramInterfaceiv( prog, progInterface, GL4.GL_ACTIVE_RESOURCES, vals, 0 );

        final int resourceNum = vals[0];
        ProgramResource[] ret = new ProgramResource[ resourceNum ];
        byte[] nameBytes = new byte[128];

        for( int index = 0; index < resourceNum; index++ ) {
            gl.glGetProgramResourceiv( prog, progInterface, index, 4, keys, 0, 4, null, 0, vals, 0 );
            String name = "";

            // Check if length of name is greater than 0.
            if( vals[0] > 0 ) {
                if( nameBytes.length < vals[0] + 1 ) {
                    nameBytes = new byte[ vals[0] + 1 ];
                }
                gl.glGetProgramResourceName( prog, progInterface, index, nameBytes.length, vals, 0, nameBytes, 0 );

                try {
                    name = new String( nameBytes, 0, vals[0] - 1, "UTF-8" );
                } catch( UnsupportedEncodingException ex ) {
                    throw new RuntimeException( ex );
                }
            }
            ret[index] = new ProgramResource( progInterface, index, vals[1], name, vals[2], vals[3] );
        }

        return ret;
    }




    private static ProgramResource[] listAttributes2( GL2ES2 gl, int prog ) {
        int[] vals = { 0, 0, 0 };
        gl.glGetProgramiv( prog, GL_ACTIVE_ATTRIBUTES, vals, 0 );
        gl.glGetProgramiv( prog, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, vals, 1 );

        final int num = vals[0];
        byte[] nameBytes = new byte[ vals[1] + 1 ];
        ProgramResource[] ret = new ProgramResource[num];

        for( int index = 0; index < num; index++ ) {
            gl.glGetActiveAttrib( prog, index, nameBytes.length, vals, 0, vals, 1, vals, 2, nameBytes, 0 );
            String name = "";
            if( vals[0] > 0 ) {
                try {
                    name = new String( nameBytes, 0, vals[0], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }

            ret[index] = new ProgramResource( GL4.GL_PROGRAM_INPUT, index, index, name, vals[2], vals[1] );
        }

        return ret;
    }


    private static ProgramResource[] listUniforms2( GL2ES2 gl, int prog ) {
        int[] vals = { 0, 0, 0 };
        gl.glGetProgramiv( prog, GL_ACTIVE_UNIFORMS, vals, 0 );
        gl.glGetProgramiv( prog, GL_ACTIVE_UNIFORM_MAX_LENGTH, vals, 1 );
        final int num = vals[0];
        // GL_ACTIVE_UNIFORM_MAX_LENGTH may be buggy, so limit size of buffer.
        byte[] nameBytes = new byte[ Math.max( 256, Math.min( 2048, vals[1] ) ) ];
        ProgramResource[] ret = new ProgramResource[num];

        for( int index = 0; index < num; index++ ) {
            gl.glGetActiveUniform( prog, index, nameBytes.length, vals, 0, vals, 1, vals, 2, nameBytes, 0 );
            String name = "";
            if( vals[0] > 0 ) {
                try {
                    name = new String( nameBytes, 0, vals[0], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }
            int loc = gl.glGetUniformLocation( prog, name );
            ret[index] = new ProgramResource( GL4.GL_UNIFORM, index, loc, name, vals[2], vals[1] );
        }

        return ret;
    }

}