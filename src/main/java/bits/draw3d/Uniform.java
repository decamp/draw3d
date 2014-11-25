/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import static javax.media.opengl.GL4.*;

/**
 * @author Philip DeCamp
 */
public class Uniform extends ProgramResource {

    /**
     * Byte stride between elements of array, or -1 if not an array.
     */
    public final int mArrayStride;

    /**
     * Byte stride between columns of matrix, or -1 if not a matrix.
     */
    public final int mMatrixStride;

    /**
     * Index of containing block, or -1 if member is not
     * in a named member block.
     */
    public final int mBlockIndex;

    /**
     * Byte offset of member within a named member  block,
     * or -1 if member is not in a named block.
     */
    public final int mBlockOffset;


    public Uniform( int memberType,
                    int arrayLen,
                    int index,
                    int location,
                    String name,
                    int arrayStride,
                    int matrixStride,
                    int blockIndex,
                    int blockOffset )
    {
        super( GL_UNIFORM, memberType, arrayLen, index, location, name );
        mArrayStride  = arrayStride;
        mMatrixStride = matrixStride;
        mBlockIndex   = blockIndex;
        mBlockOffset  = blockOffset;
    }

}
