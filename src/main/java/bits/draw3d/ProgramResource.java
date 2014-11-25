/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

/**
 * @author Philip DeCamp
 */
public class ProgramResource {

    /**
     * The program interface, eg GL_UNIFORM, GL_PROGRAM_INPUT, GL_PROGRAM_OUTPUT, etc.
     */
    public final int mInterface;

    /**
     * Type of element, eg GL_FLOAT, GL_FLOAT_VEC3, GL_FLOAT_MAT4x4, GL_SAMPLER_2D, etc.
     */
    public final int mMemberType;

    /**
     * Length of array, or 1 if not an array.
     */
    public final int mArrayLength;

    /**
     * Index of resource.
     *
     * <p>For uniforms, index is is used for queries, while location is used for writes.
     * For attributes, index and location should be the same.
     */
    public final int mIndex;

    /**
     * Location of resource.
     *
     * <p>For attributes, index and location should be the same.
     * <p>For uniforms, index is is used for reading and location is used for writing.
     * <p>For member blocks, this is the bindLocation location, if defined by the shader.
     */
    public final int mLocation;

    /**
     * Name of resource.
     */
    public final String mName;


    public ProgramResource( int inter,
                            int memberType,
                            int arrayLen,
                            int index,
                            int location,
                            String optName )
    {
        mInterface = inter;
        mMemberType = memberType;
        mArrayLength = arrayLen;
        mIndex = index;
        mLocation = location;
        mName = optName != null ? optName : "";
    }


    public ProgramResource( ProgramResource copy ) {
        mInterface   = copy.mInterface;
        mMemberType  = copy.mMemberType;
        mArrayLength = copy.mArrayLength;
        mIndex       = copy.mIndex;
        mLocation    = copy.mLocation;
        mName        = copy.mName;
    }

}
