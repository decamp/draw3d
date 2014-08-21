package bits.draw3d.nodes;

import static javax.media.opengl.GL.*;

import javax.media.opengl.GL;


/**
 * @author decamp
 */
public class DrawBufferParams {

    public static DrawBufferParams copy( DrawBufferParams node ) {
        return new DrawBufferParams( node );
    }


    public boolean mVertEnabled = false;
    public int     mVertSize    = 3;
    public int     mVertType    = GL_FLOAT;
    public int     mVertStride  = 0;
    public int     mVertOffset  = 0;

    public boolean mTexEnabled = false;
    public int     mTexSize    = 2;
    public int     mTexType    = GL_FLOAT;
    public int     mTexStride  = 0;
    public int     mTexOffset  = 0;

    public boolean mNormEnabled = false;
    public int     mNormType    = GL_FLOAT;
    public int     mNormStride  = 0;
    public int     mNormOffset  = 0;

    public boolean mColorEnabled = false;
    public int     mColorSize    = 4;
    public int     mColorType    = GL_UNSIGNED_BYTE;
    public int     mColorStride  = 0;
    public int     mColorOffset  = 0;

    public boolean mIndexEnabled = false;
    public int     mIndexType    = GL_UNSIGNED_INT;
    public int     mIndexStride  = 4;

    public boolean mCommandEnabled = false;
    public int     mCommandMode    = GL_TRIANGLES;
    public int     mCommandOffset  = 0;
    public int     mCommandCount   = 0;


    public DrawBufferParams() {}


    public DrawBufferParams( DrawBufferParams copy ) {
        set( copy );
    }



    public void set( DrawBufferParams p ) {
        mVertEnabled    = p.mVertEnabled;
        mVertSize       = p.mVertSize;
        mVertType       = p.mVertType;
        mVertStride     = p.mVertStride;
        mVertOffset     = p.mVertOffset;
        mTexEnabled     = p.mTexEnabled;
        mTexSize        = p.mTexSize;
        mTexStride      = p.mTexStride;
        mTexOffset      = p.mTexOffset;
        mNormEnabled    = p.mNormEnabled;
        mNormType       = p.mNormType;
        mNormStride     = p.mNormStride;
        mNormOffset     = p.mNormOffset;
        mColorEnabled   = p.mColorEnabled;
        mColorSize      = p.mColorSize;
        mColorType      = p.mColorType;
        mColorStride    = p.mColorStride;
        mColorOffset    = p.mColorOffset;
        mIndexEnabled   = p.mIndexEnabled;
        mIndexType      = p.mIndexType;
        mCommandEnabled = p.mCommandEnabled;
        mCommandMode    = p.mCommandMode;
        mCommandOffset  = p.mCommandOffset;
        mCommandCount   = p.mCommandCount;
    }


    public void command( int mode, int offset, int count ) {
        mCommandEnabled = true;
        mCommandMode = mode;
        mCommandOffset = offset;
        mCommandCount = count;
    }


    public void noCommand() {
        mCommandEnabled = false;
    }


    public void verts( int coordCount, int coordType, int stride, int offset ) {
        mVertEnabled = true;
        mVertSize = coordCount;
        mVertType = coordType;
        mVertStride = stride;
        mVertOffset = offset;
    }


    public void noVerts() {
        mVertEnabled = false;
    }


    public void colors( int coordCount, int coordType, int stride, int offset ) {
        mColorEnabled = true;
        mColorSize = coordCount;
        mColorType = coordType;
        mColorStride = stride;
        mColorOffset = offset;
    }


    public void noColors() {
        mColorEnabled = false;
    }


    public void norms( int coordType, int stride, int offset ) {
        mNormEnabled = true;
        mNormType = coordType;
        mNormStride = stride;
        mNormOffset = offset;
    }


    public void noNorms() {
        mNormEnabled = false;
    }


    public void texs( int coordCount, int coordType, int stride, int offset ) {
        mTexEnabled = true;
        mTexSize = coordCount;
        mTexType = coordType;
        mTexStride = stride;
        mTexOffset = offset;
    }


    public void noTexs() {
        mTexEnabled = false;
    }


    public void elements( int indexType ) {
        switch( indexType ) {
        case GL_UNSIGNED_BYTE:
            mIndexStride = 1;
            break;

        case GL_UNSIGNED_SHORT:
            mIndexStride = 2;
            break;

        case GL_UNSIGNED_INT:
            mIndexStride = 4;
            break;

        default:
            throw new IllegalArgumentException(
                    "indexType must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT" );
        }

        mIndexEnabled = true;
        mIndexType = indexType;
    }


    public void noElements() {
        mIndexEnabled = false;
    }


    public void push( GL gl ) {
        if( mVertEnabled ) {
            gl.glEnableClientState( GL_VERTEX_ARRAY );
            gl.glVertexPointer( mVertSize, mVertType, mVertStride, mVertOffset );
        }

        if( mColorEnabled ) {
            gl.glEnableClientState( GL_COLOR_ARRAY );
            gl.glColorPointer( mColorSize, mColorType, mColorStride, mColorOffset );
        }

        if( mTexEnabled ) {
            gl.glEnableClientState( GL_TEXTURE_COORD_ARRAY );
            gl.glTexCoordPointer( mTexSize, mTexType, mTexStride, mTexOffset );
        }

        if( mNormEnabled ) {
            gl.glEnableClientState( GL_NORMAL_ARRAY );
            gl.glNormalPointer( mNormType, mNormStride, mNormOffset );
        }
    }


    public void pop( GL gl ) {
        gl.glDisableClientState( GL_COLOR_ARRAY );
        gl.glDisableClientState( GL_TEXTURE_COORD_ARRAY );
        gl.glDisableClientState( GL_NORMAL_ARRAY );
    }


    public void execute( GL gl ) {
        if( !mCommandEnabled ) {
            return;
        }

        if( mIndexEnabled ) {
            gl.glDrawElements( mCommandMode, mCommandCount, mIndexType, mCommandOffset * mIndexStride );
        } else {
            gl.glDrawArrays( mCommandMode, mCommandOffset, mCommandCount );
        }
    }


    public void execute( GL gl, int mode, int offset, int count ) {
        if( mIndexEnabled ) {
            gl.glDrawElements( mode, count, mIndexType, offset * mIndexStride );
        } else {
            gl.glDrawArrays( mode, offset, count );
        }
    }


    @Deprecated public void enableCommand( int mode, int offset, int count ) {
        mCommandEnabled = true;
        mCommandMode = mode;
        mCommandOffset = offset;
        mCommandCount = count;
    }


    @Deprecated public void disableCommand() {
        mCommandEnabled = false;
    }


    @Deprecated public void enableVertexPointer( int coordCount, int coordType, int stride, int offset ) {
        mVertEnabled = true;
        mVertSize = coordCount;
        mVertType = coordType;
        mVertStride = stride;
        mVertOffset = offset;
    }


    @Deprecated public void disableVertexPointer() {
        mVertEnabled = false;
    }


    @Deprecated public void enableColorPointer( int coordCount, int coordType, int stride, int offset ) {
        mColorEnabled = true;
        mColorSize = coordCount;
        mColorType = coordType;
        mColorStride = stride;
        mColorOffset = offset;
    }


    @Deprecated public void disableColorPointer() {
        mColorEnabled = false;
    }


    @Deprecated public void enableNormPointer( int coordType, int stride, int offset ) {
        mNormEnabled = true;
        mNormType = coordType;
        mNormStride = stride;
        mNormOffset = offset;
    }


    @Deprecated public void disableNormPointer() {
        mNormEnabled = false;
    }


    @Deprecated public void enableTexPointer( int coordCount, int coordType, int stride, int offset ) {
        mTexEnabled = true;
        mTexSize = coordCount;
        mTexType = coordType;
        mTexStride = stride;
        mTexOffset = offset;
    }


    @Deprecated public void disableTexPointer() {
        mTexEnabled = false;
    }


    @Deprecated public void enableIndices( int indexType ) {

        switch( indexType ) {
        case GL_UNSIGNED_BYTE:
            mIndexStride = 1;
            break;

        case GL_UNSIGNED_SHORT:
            mIndexStride = 2;
            break;

        case GL_UNSIGNED_INT:
            mIndexStride = 4;
            break;

        default:
            throw new IllegalArgumentException(
                    "indexType must be GL_UNSIGNED_BYTE, GL_UNSIGNED_SHORT, or GL_UNSIGNED_INT" );
        }

        mIndexEnabled = true;
        mIndexType = indexType;
    }


    @Deprecated public void disableIndices() {
        mIndexEnabled = false;
    }


    @Deprecated public static DrawBufferParams newInstance() {
        return new DrawBufferParams();
    }

}

