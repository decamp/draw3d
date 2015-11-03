/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.util.ArrayList;
import java.util.List;

import static com.jogamp.opengl.GL4.*;

/**
 * @author Philip DeCamp
 */
public class UniformBlock extends ProgramResource {

    /**
     * Size of member block in bytes, with padding.
     */
    public final int mDataSize;

    /**
     * List of uniforms belonging to this UniformBlock.
     */
    public final List<Uniform> mUniforms;


    public UniformBlock( int index,
                         int bindingLoc,
                         String optName,
                         int dataSize,
                         List<Uniform> optUniforms )
    {
        super( GL_UNIFORM_BLOCK, -1, 1, index, bindingLoc, optName );
        mDataSize = dataSize;
        mUniforms = optUniforms != null ? optUniforms : new ArrayList<Uniform>();
    }

}
