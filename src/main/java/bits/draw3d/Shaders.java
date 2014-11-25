/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import javax.media.opengl.*;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static javax.media.opengl.GL2ES3.*;


/**
 * @author Philip DeCamp
 */
public class Shaders {


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
            String msg = Shaders.readShaderInfoLog( gl, id );
            msg = "Shader Compilation Failed : " + msg;
            gl.glDeleteShader( id );
            throw new GLException( msg );
        }
        return id;
    }


    public static List<ProgramResource> listAttributes( GL2ES3 gl, int program ) {
        int[] vals = { 0, 0, 0 };
        gl.glGetProgramiv( program, GL_ACTIVE_ATTRIBUTES,           vals, 0 );
        // GL_ACTIVE_ATTRIBUTE_MAX_LENGTH actually represents the max NAME length.
        gl.glGetProgramiv( program, GL_ACTIVE_ATTRIBUTE_MAX_LENGTH, vals, 1 );

        final int num = vals[0];
        byte[] nameBytes = new byte[ vals[1] + 1 ];
        List<ProgramResource> ret = new ArrayList<ProgramResource>( num );

        for( int index = 0; index < num; index++ ) {
            gl.glGetActiveAttrib( program, index, nameBytes.length, vals, 0, vals, 1, vals, 2, nameBytes, 0 );
            String name = "";
            if( vals[0] > 0 ) {
                try {
                    name = new String( nameBytes, 0, vals[0], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }

            ret.add( new ProgramResource( GL4.GL_PROGRAM_INPUT, vals[2], vals[1], index, index, name ) );
        }

        return ret;
    }


    public static List<Uniform> listUniforms( GL2ES3 gl, int program ) {
        final int[] val = { 0 };
        gl.glGetProgramiv( program, GL_ACTIVE_UNIFORMS, val, 0 );
        final int num = val[0];
        int[] inds = new int[ num ];
        for( int i = 0; i < num; i++ ) {
            inds[i] = i;
        }

        List<Uniform> ret = initUniforms( gl, program, inds, num );
        return ret;
    }


    public static List<UniformBlock> listUniformBlocks( GL2ES3 gl, int program, List<Uniform> progUniforms ) {
        int[] val = { 0 };
        gl.glGetProgramiv( program, GL_ACTIVE_UNIFORM_BLOCKS, val, 0 );
        final int numBlocks = val[0];
        gl.glGetProgramiv( program, GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH, val, 0 );
        // GL_ACTIVE_UNIFORM_MAX_NAME_LENGTH may be buggy, so limit size of buffer.
        byte[] nameBytes = new byte[ Math.max( 256, Math.min( 2048, val[0] ) ) ];

        List<UniformBlock> ret = new ArrayList<UniformBlock>( numBlocks );

        for( int index = 0; index < numBlocks; index++ ) {
            gl.glGetActiveUniformBlockiv( program, index, GL_UNIFORM_BLOCK_BINDING, val, 0 );
            final int loc      = val[0];
            gl.glGetActiveUniformBlockiv( program, index, GL_UNIFORM_BLOCK_DATA_SIZE, val, 0 );
            final int dataSize = val[0];
            gl.glGetActiveUniformBlockiv( program, index, GL_UNIFORM_BLOCK_NAME_LENGTH, val, 0 );
            final int nameLen  = val[0];
            gl.glGetActiveUniformBlockiv( program, index, GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS, val, 0 );
            final int childNum = val[0];

            int[] inds = new int[childNum];
            gl.glGetActiveUniformBlockiv( program, index, GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES, inds, 0 );
            List<Uniform> unis = new ArrayList<Uniform>( childNum );
            for( int j = 0; j < childNum; j++ ) {
                unis.add( progUniforms.get( inds[j] ) );
            }

            String name = "";
            if( nameLen > 0 ) {
                try {
                    gl.glGetActiveUniformBlockName( program, index, nameBytes.length, val, 0, nameBytes, 0 );
                    name = new String( nameBytes, 0, val[0], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }

            ret.add( new UniformBlock( index, loc, name, dataSize, unis ) );
        }

        return ret;
    }


    private static List<Uniform> initUniforms( GL2ES3 gl, int prog, int[] inds, int num ) {
        final List<Uniform> ret = new ArrayList<Uniform>( num );

        int[] vals = { 0, 0, 0 };
        gl.glGetProgramiv( prog, GL_ACTIVE_UNIFORM_MAX_LENGTH, vals, 0 );
        final byte[] nameBytes = new byte[ Math.max( 128, Math.min( 2048, vals[0] ) ) ];

        int[] arrayStrides  = new int[ num ];
        int[] matrixStrides = new int[ num ];
        int[] blockIndices  = new int[ num ];
        int[] blockOffsets  = new int[ num ];
        gl.glGetActiveUniformsiv( prog, num, inds, 0, GL_UNIFORM_ARRAY_STRIDE, arrayStrides, 0 );
        gl.glGetActiveUniformsiv( prog, num, inds, 0, GL_UNIFORM_MATRIX_STRIDE, matrixStrides, 0 );
        gl.glGetActiveUniformsiv( prog, num, inds, 0, GL_UNIFORM_BLOCK_INDEX, blockIndices, 0 );
        gl.glGetActiveUniformsiv( prog, num, inds, 0, GL_UNIFORM_OFFSET, blockOffsets, 0 );

        for( int i = 0; i < num; i++ ) {
            gl.glGetActiveUniform( prog, inds[i], nameBytes.length, vals, 0, vals, 1, vals, 2, nameBytes, 0 );
            String name = "";
            if( vals[0] > 0 ) {
                try {
                    name = new String( nameBytes, 0, vals[0], "UTF-8" );
                } catch( UnsupportedEncodingException e ) {
                    throw new RuntimeException( e );
                }
            }

            int loc = gl.glGetUniformLocation( prog, name );
            ret.add( new Uniform( vals[2],
                                  vals[1],
                                  inds[i],
                                  loc,
                                  name,
                                  arrayStrides[i],
                                  matrixStrides[i],
                                  blockIndices[i],
                                  blockOffsets[i] ) );
        }

        return ret;
    }

}
