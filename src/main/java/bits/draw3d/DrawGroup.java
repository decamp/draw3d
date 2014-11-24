/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawUnit;

import static javax.media.opengl.GL2ES3.*;

/**
 * @author Philip DeCamp
 */
public class DrawGroup<V, E> implements DrawUnit {



    public static <V,E> DrawGroup<V,E> create( BoProgram<V,E> program,
                                               int vertUsage,
                                               int vertItemCap,
                                               int elemUsage,
                                               int elemItemCap )
    {
        return new DrawGroup<V,E>( program, vertUsage, vertItemCap, elemUsage, elemItemCap );
    }


    private final BoProgram<V, E> mProgram;
    private final Vao             mVao;
    private final BoList<V>       mVertList;
    private final BoList<E>       mItemList;

    private boolean mInitialized  = false;
    private boolean mDrawOnBind   = false;
    private int     mDrawMode     = -1;


    public DrawGroup( BoProgram<V, E> program,
                      int vertUsage,
                      int vertItemCap,
                      int elemUsage,
                      int elemItemCap )
    {
        mProgram  = program;
        mVertList = BoList.create( program.mVertWriter, vertUsage, vertItemCap );
        if( program.mElemWriter != null ) {
            mItemList = BoList.create( program.mElemWriter, elemUsage, elemItemCap );
        } else {
            mItemList = null;
        }
        mVao = new Vao();
        program.mVertWriter.attributes( mVao );
    }


    public BoList<V> vertList() {
        return mVertList;
    }


    public BoList<E> elemList() {
        return mItemList;
    }


    public void drawOnBind( boolean enable, int mode ) {
        mDrawOnBind = enable;
        mDrawMode   = mode;
    }


    public void init( DrawEnv d ) {
        if( mInitialized ) {
            return;
        }
        mInitialized = true;
        mProgram.mProgram.init( d );
        mVao.init( d );
        mVertList.bind( d );
        if( mItemList != null ) {
            mItemList.bind( d );
        }
    }


    public void dispose( DrawEnv d ) {
        if( !mInitialized ) {
            return;
        }
        mProgram.mProgram.dispose( d );
        mVertList.dispose( d );
        if( mItemList != null ) {
            mItemList.dispose( d );
        }
    }


    public void bind( DrawEnv d ) {
        mVao.bind( d );
        if( mVertList.needsUpdate() ) {
            mVertList.bind( d );
        }
        if( mItemList != null && mItemList.needsUpdate() ) {
            mItemList.bind( d );
        }
        if( mDrawOnBind ) {
            drawAll( d, mDrawMode );
        }
    }


    public void unbind( DrawEnv d ) {
        mVao.unbind( d );
    }


    public void drawAll( DrawEnv d, int mode ) {
        if( mItemList == null ) {
            int len = mVertList.elemNum();
            if( len > 0 ) {
                d.mGl.glDrawArrays( mode, 0, len );
            }
        } else {
            int len = mItemList.elemNum();
            if( len > 0 ) {
                int type;
                switch( mProgram.mElemWriter.bytesPerElem() ) {
                case 1:
                    type = GL_UNSIGNED_BYTE;
                    break;
                case 2:
                    type = GL_UNSIGNED_SHORT;
                    break;
                case 4:
                    type = GL_UNSIGNED_INT;
                    break;
                default:
                    throw new IllegalStateException( "Invalid element size: " + mProgram.mElemWriter.bytesPerElem() );
                }

                d.mGl.glDrawElements( mode, len, type, 0 );
            }
        }
    }

}
