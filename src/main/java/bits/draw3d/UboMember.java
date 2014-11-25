/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import bits.math3d.*;


/**
 * Provides access to one member of a block of UBO data.
 *
 * <p>After altering data (calling any of the {@code apply} commands),
 * you will need to rebind the parent block.
 *
 * @author Philip DeCamp
 */
public interface UboMember {
    public Uniform target();
    public UboBlock block();

    public int   getInt();
    public int getComponentInt( int elem, int row, int col );
    public float getFloat();
    public float getComponentFloat( int elem, int row, int col );
    public void  get( Vec2 vec );
    public void  get( Vec3 vec );
    public void  get( Mat3 mat );
    public void  get( Mat4 mat );
    public void  get( int[] out, int off, int len );
    public void  get( float[] out, int off, int len );
    public void  get( Vec2[] out, int off, int len );
    public void  get( Vec3[] out, int off, int len );
    public void  get( Mat3[] out, int off, int len );
    public void  get( Mat4[] out, int off, int len );


    public void  set( int val );
    public void setComponent( int elem, int row, int col, int val );
    public void  set( float val );
    public void setComponent( int elem, int row, int col, float val );
    public void  set( Vec2 vec );
    public void  set( Vec3 vec );
    public void  set( Mat3 mat );
    public void  set( Mat4 mat );
    public void  set( int[] vals, int off, int len );
    public void  set( float[] vals, int off, int len );
    public void  set( Vec2[] vals, int off, int len );
    public void  set( Vec3[] vals, int off, int len );
    public void  set( Mat3[] vals, int off, int len );
    public void  set( Mat4[] vals, int off, int len );
}
