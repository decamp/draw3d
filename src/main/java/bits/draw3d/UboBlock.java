/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

/**
 * Provides access to a block of UBO data.
 *
 * <p>If you need to refresh buffer data, just bind it again.
 *
 * @author Philip DeCamp
 */
public interface UboBlock {
    public UniformBlock target();

    public int  bindLocation();
    public void bindLocation( int loc );

    public int memberNum();
    public UboMember member( int idx );
    public UboMember member( String name );

    public void bind( DrawEnv d );
    public void bind( DrawEnv d, int location );

    public void unbind( DrawEnv d );
    public void unbind( DrawEnv d, int location );

}
