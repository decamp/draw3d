/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.bo;


import bits.draw3d.shader.Program;


/**
 * @author Philip DeCamp
 */
public class BoProgram<V,E> {
    public Program     mProgram;
    public BoWriter<V> mVertWriter;
    public BoWriter<E> mElemWriter;
}
