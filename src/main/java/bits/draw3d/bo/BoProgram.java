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
