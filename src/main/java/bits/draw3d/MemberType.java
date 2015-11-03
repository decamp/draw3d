/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import static com.jogamp.opengl.GL4.*;

/**
 * @author Philip DeCamp
 */
public enum MemberType {
    FLOAT           ( GL_FLOAT            ,  ComponentType.FLOAT,    1, 1 ),
    FLOAT_VEC2      ( GL_FLOAT_VEC2       ,  ComponentType.FLOAT,    2, 1 ),
    FLOAT_VEC3      ( GL_FLOAT_VEC3       ,  ComponentType.FLOAT,    3, 1 ),
    FLOAT_VEC4      ( GL_FLOAT_VEC4       ,  ComponentType.FLOAT,    4, 1 ),
    FLOAT_MAT2      ( GL_FLOAT_MAT2       ,  ComponentType.FLOAT,    2, 2 ),
    FLOAT_MAT3      ( GL_FLOAT_MAT3       ,  ComponentType.FLOAT,    3, 3 ),
    FLOAT_MAT4      ( GL_FLOAT_MAT4       ,  ComponentType.FLOAT,    4, 4 ),
    FLOAT_MAT2x3    ( GL_FLOAT_MAT2x3     ,  ComponentType.FLOAT,    2, 3 ),
    FLOAT_MAT2x4    ( GL_FLOAT_MAT2x4     ,  ComponentType.FLOAT,    2, 4 ),
    FLOAT_MAT3x2    ( GL_FLOAT_MAT3x2     ,  ComponentType.FLOAT,    3, 2 ),
    FLOAT_MAT3x4    ( GL_FLOAT_MAT3x4     ,  ComponentType.FLOAT,    3, 4 ),
    FLOAT_MAT4x2    ( GL_FLOAT_MAT4x2     ,  ComponentType.FLOAT,    4, 2 ),
    FLOAT_MAT4x3    ( GL_FLOAT_MAT4x3     ,  ComponentType.FLOAT,    4, 3 ),
    INT             ( GL_INT              ,  ComponentType.INT,      1, 1 ),
    INT_VEC2        ( GL_INT_VEC2         ,  ComponentType.INT,      2, 1 ),
    INT_VEC3        ( GL_INT_VEC3         ,  ComponentType.INT,      3, 1 ),
    INT_VEC4        ( GL_INT_VEC4         ,  ComponentType.INT,      4, 1 ),
    UINT            ( GL_UNSIGNED_INT     ,  ComponentType.UINT,     1, 1 ),
    UINT_VEC2       ( GL_UNSIGNED_INT_VEC2,  ComponentType.UINT,     2, 1 ),
    UINT_VEC3       ( GL_UNSIGNED_INT_VEC3,  ComponentType.UINT,     3, 1 ),
    UINT_VEC4       ( GL_UNSIGNED_INT_VEC4,  ComponentType.UINT,     4, 1 ),
    DOUBLE          ( GL_DOUBLE           ,  ComponentType.DOUBLE,   1, 1 ),
    DOUBLE_VEC2     ( GL_DOUBLE_VEC2      ,  ComponentType.DOUBLE,   2, 1 ),
    DOUBLE_VEC3     ( GL_DOUBLE_VEC3      ,  ComponentType.DOUBLE,   3, 1 ),
    DOUBLE_VEC4     ( GL_DOUBLE_VEC4      ,  ComponentType.DOUBLE,   4, 1 ),
    DOUBLE_MAT2     ( GL_DOUBLE_MAT2      ,  ComponentType.DOUBLE,   2, 2 ),
    DOUBLE_MAT3     ( GL_DOUBLE_MAT3      ,  ComponentType.DOUBLE,   3, 3 ),
    DOUBLE_MAT4     ( GL_DOUBLE_MAT4      ,  ComponentType.DOUBLE,   4, 4 ),
    DOUBLE_MAT2x3   ( GL_DOUBLE_MAT2x3    ,  ComponentType.DOUBLE,   2, 3 ),
    DOUBLE_MAT2x4   ( GL_DOUBLE_MAT2x4    ,  ComponentType.DOUBLE,   3, 4 ),
    DOUBLE_MAT3x2   ( GL_DOUBLE_MAT3x2    ,  ComponentType.DOUBLE,   3, 2 ),
    DOUBLE_MAT3x4   ( GL_DOUBLE_MAT3x4    ,  ComponentType.DOUBLE,   3, 4 ),
    DOUBLE_MAT4x2   ( GL_DOUBLE_MAT4x2    ,  ComponentType.DOUBLE,   4, 2 ),
    DOUBLE_MAT4x3   ( GL_DOUBLE_MAT4x3    ,  ComponentType.DOUBLE,   4, 3 );

    private final int mCode;
    private final ComponentType mComp;
    private final int mRows;
    private final int mCols;


    MemberType( int code, ComponentType comp, int rows, int cols ) {
        mCode = code;
        mComp = comp;
        mRows = rows;
        mCols = cols;
    }


    public int id() {
        return mCode;
    }

    public ComponentType componentType() {
        return mComp;
    }

    public int rows() {
        return mRows;
    }

    public int cols() {
        return mCols;
    }

    public int bytes() {
        return mComp.bytes() * mRows * mCols;
    }


    public static MemberType fromGl( int code ) {
        switch( code ) {
        case GL_FLOAT            : return FLOAT         ;
        case GL_FLOAT_VEC2       : return FLOAT_VEC2    ;
        case GL_FLOAT_VEC3       : return FLOAT_VEC3    ;
        case GL_FLOAT_VEC4       : return FLOAT_VEC4    ;
        case GL_FLOAT_MAT2       : return FLOAT_MAT2    ;
        case GL_FLOAT_MAT3       : return FLOAT_MAT3    ;
        case GL_FLOAT_MAT4       : return FLOAT_MAT4    ;
        case GL_FLOAT_MAT2x3     : return FLOAT_MAT2x3  ;
        case GL_FLOAT_MAT2x4     : return FLOAT_MAT2x4  ;
        case GL_FLOAT_MAT3x2     : return FLOAT_MAT3x2  ;
        case GL_FLOAT_MAT3x4     : return FLOAT_MAT3x4  ;
        case GL_FLOAT_MAT4x2     : return FLOAT_MAT4x2  ;
        case GL_FLOAT_MAT4x3     : return FLOAT_MAT4x3  ;
        case GL_INT              : return INT           ;
        case GL_INT_VEC2         : return INT_VEC2      ;
        case GL_INT_VEC3         : return INT_VEC3      ;
        case GL_INT_VEC4         : return INT_VEC4      ;
        case GL_UNSIGNED_INT     : return UINT          ;
        case GL_UNSIGNED_INT_VEC2: return UINT_VEC2     ;
        case GL_UNSIGNED_INT_VEC3: return UINT_VEC3     ;
        case GL_UNSIGNED_INT_VEC4: return UINT_VEC4     ;
        case GL_DOUBLE           : return DOUBLE        ;
        case GL_DOUBLE_VEC2      : return DOUBLE_VEC2   ;
        case GL_DOUBLE_VEC3      : return DOUBLE_VEC3   ;
        case GL_DOUBLE_VEC4      : return DOUBLE_VEC4   ;
        case GL_DOUBLE_MAT2      : return DOUBLE_MAT2   ;
        case GL_DOUBLE_MAT3      : return DOUBLE_MAT3   ;
        case GL_DOUBLE_MAT4      : return DOUBLE_MAT4   ;
        case GL_DOUBLE_MAT2x3    : return DOUBLE_MAT2x3 ;
        case GL_DOUBLE_MAT2x4    : return DOUBLE_MAT2x4 ;
        case GL_DOUBLE_MAT3x2    : return DOUBLE_MAT3x2 ;
        case GL_DOUBLE_MAT3x4    : return DOUBLE_MAT3x4 ;
        case GL_DOUBLE_MAT4x2    : return DOUBLE_MAT4x2 ;
        case GL_DOUBLE_MAT4x3    : return DOUBLE_MAT4x3 ;
        default                  : return null          ;
        }
    }

}
