/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.bo;


import java.util.*;
import java.nio.ByteBuffer;
import static javax.media.opengl.GL3.*;

import bits.draw3d.*;
import bits.draw3d.util.*;
import bits.collect.PublicList;


/**
 * Buffer Object List. Adding an object to a BoList will
 * result in that object being serialized and added to
 * a set of OpenGL Buffer Objects (VBO/IBO).
 *
 * <p>While BoList manages the item and buffer lists,
 * a BoSerializer is needed to perform actual serialization
 * of the objects.
 *
 * @author Philip DeCamp
 */
public class BoList<T> extends DrawUnitAdapter implements Collection<T> {


    public static <T> BoList<T> create( BoWriter<T> writer, int usage ) {
        return new BoList<T>( writer.itemClass(), writer, usage, 10 );
    }


    public static <T> BoList<T> create( BoWriter<T> writer, int usage, int initialCapacity ) {
        return new BoList<T>( writer.itemClass(), writer, usage, initialCapacity );
    }


    public static <T> BoList<T> create( Class<T> clazz, BoWriter<? super T> writer, int usage, int initialCapacity ) {
        return new BoList<T>( clazz, writer, usage, initialCapacity );
    }



    private final PublicList<T>       mList;
    private final BoWriter<? super T> mWriter;
    private final Bo                  mBo;

    // If mAutoWrite, objects are serialized every push.
    private final boolean mAutoWrite;

    private long mModCount       = 0;
    private long mBufferModCount = -1;

    private boolean mPushed = false;

    int mElemNum = 0;


    protected BoList( Class<T> clazz, BoWriter<? super T> writer, int usage, int initCapacity ) {
        mList      = PublicList.create( clazz, initCapacity );
        mWriter    = writer;
        mAutoWrite = usage == GL_STREAM_DRAW;
        mBo        = new Bo( writer.boType(), usage );
    }



    public PublicList<T> items() {
        return mList;
    }


    public BoWriter<? super T> writer() {
        return mWriter;
    }


    public Bo bo() {
        return mBo;
    }


    public int elemNum() {
        return mElemNum;
    }


    public void markModified() {
        mModCount++;
    }


    public void sort( Comparator<? super T> comp ) {
        TimSort.defaultInstance().sort( mList.mArr, 0, mList.mSize, comp );
    }


    public boolean needsUpdate() {
        return mAutoWrite || mBufferModCount != mModCount;
    }

    @Override
    public boolean add( T item ) {
        int num = mWriter.markAdd( item, mElemNum );
        if( num < 0 ) {
            return false;
        }
        mElemNum += num;
        mList.add( item );
        mModCount++;
        return true;
    }

    @Override
    public boolean addAll( Collection<? extends T> coll ) {
        boolean ret = false;
        for( T item : coll ) {
            ret |= add( item );
        }
        return ret;
    }

    @Override
    public void clear() {
        mModCount++;
        mElemNum = 0;
        for( T item: mList ) {
            mWriter.markRemove( item );
        }
        mList.clear();
    }

    @Override
    public boolean contains( Object item ) {
        return mList.contains( item );
    }

    @Override
    public boolean containsAll( Collection<?> items ) {
        for( Object item: items ) {
            if( !contains( item ) ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return mList.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return new Iter();
    }

    @Override
    @SuppressWarnings( { "unchecked", "SuspiciousMethodCalls" } )
    public boolean remove( Object item ) {
        int idx = mList.indexOf( item );
        return idx >= 0 && remove( idx ) != null;
    }


    public T remove( int idx ) {
        T ret = mList.mArr[idx];
        mList.removeFast( idx );
        mModCount++;
        int elems = mWriter.markRemove( ret );
        if( elems >= 0 ) {
            mElemNum -= elems;
        }
        return ret;
    }

    @Override
    public boolean removeAll( Collection<?> objects ) {
        boolean ret = false;
        for( Object obj: objects ) {
            ret |= remove( obj );
        }
        return ret;
    }

    @Override
    public boolean retainAll( Collection<?> items ) {
        boolean ret = false;

        for( int i = mList.mSize - 1; i >= 0; i-- ) {
            if( !items.contains( mList.mArr[i] ) ) {
                remove( i );
                ret = true;
            }
        }

        return ret;
    }

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public T[] toArray() {
        return mList.toArray();
    }

    @SuppressWarnings( "SuspiciousToArrayCall" )
    @Override
    public <S> S[] toArray( S[] arr ) {
        return mList.toArray( arr );
    }


    @Override
    public void init( DrawEnv d ) {
        bind( d );
        unbind( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        mBo.dispose( d );
        clear();
    }

    @Override
    public void bind( DrawEnv d ) {
        if( mAutoWrite || mBufferModCount != mModCount ) {
            write( d );
        } else {
            mBo.bind( d );
        }
    }

    @Override
    public void unbind( DrawEnv d ) {
        mBo.unbind( d );
    }



    private void write( DrawEnv d ) {
        mBufferModCount = mModCount;
        int bytes = mWriter.bytesPerElem() * mElemNum;
        Bo bo = mBo;
        if( bytes > bo.capacity() ) {
            bo.alloc( bytes * 11 / 10 );
        }
        bo.bind( d );
        ByteBuffer bb = bo.map( d, GL_WRITE_ONLY );

        int num = mList.mSize;
        T[] arr = mList.mArr;
        for( int i = 0; i < num; i++ ) {
            mWriter.write( arr[i], bb );
        }

        bo.unmap( d );
        d.checkErr();
    }


    private final class Iter implements Iterator<T> {
        private int mOffset = 0;
        private int mPrev = -1;

        Iter() {}


        public boolean hasNext() {
            return mOffset < mList.mSize;
        }


        public T next() {
            if( mOffset >= mList.mSize ) {
                throw new NoSuchElementException();
            }
            mPrev = mOffset++;
            return mList.mArr[mPrev];
        }


        public void remove() {
            if( mPrev < 0 ) {
                throw new IllegalStateException( "No call to mNext() or present()." );
            }
            BoList.this.remove( mPrev );
            if( mPrev < mOffset ) {
                mOffset--;
            }
            mPrev = -1;
        }
    }

}
