/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import static javax.media.opengl.GL4.*;

/**
 * Information about the OpenGL data component types.
 *
 * @author Philip DeCamp
 */
public enum ComponentType {
    BYTE        ( GL_BYTE           , 1 ),
    UBYTE       ( GL_UNSIGNED_BYTE  , 1 ),
    SHORT       ( GL_SHORT          , 2 ),
    USHORT      ( GL_UNSIGNED_SHORT , 2 ),
    INT         ( GL_INT            , 4 ),
    UINT        ( GL_UNSIGNED_INT   , 4 ),
    FIXED       ( GL_FIXED          , 4 ),
    HALF_FLOAT  ( GL_HALF_FLOAT     , 2 ),
    FLOAT       ( GL_FLOAT          , 4 ),
    DOUBLE      ( GL_DOUBLE         , 8 );

    private final int mCode;
    private final int mBytes;

    ComponentType( int code, int bytes ) {
        mCode  = code;
        mBytes = bytes;
    }


    /**
     * @return The OpenGL enum value for this data component type.
     */
    public int id() {
        return mCode;
    }


    public int bytes() {
        return mBytes;
    }


    public static ComponentType fromGl( int code ) {
        switch( code ) {
        case GL_BYTE           : return BYTE       ;
        case GL_UNSIGNED_BYTE  : return UBYTE      ;
        case GL_SHORT          : return SHORT      ;
        case GL_UNSIGNED_SHORT : return USHORT     ;
        case GL_INT            : return INT        ;
        case GL_UNSIGNED_INT   : return UINT       ;
        case GL_FIXED          : return FIXED      ;
        case GL_HALF_FLOAT     : return HALF_FLOAT ;
        case GL_FLOAT          : return FLOAT      ;
        case GL_DOUBLE         : return DOUBLE     ;
        default                : return null       ;
        }
    }

}
