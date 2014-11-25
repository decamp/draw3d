/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;

import java.util.*;


/**
 * @author Philip DeCamp
 */
public class AutoloadProgram extends Program {

    private boolean mCreateUniformLoadersOnInit = true;
    private boolean mConfigBlockBindingsOnInit  = true;

    protected List<ProgramResource> mAttribs;
    protected List<Uniform>         mUniforms;
    protected List<UniformBlock>    mBlocks;


    private List<DrawTask> mOnBind = null;


    public AutoloadProgram() {}


    public boolean createUniformLoadersOnInit() {
        return mCreateUniformLoadersOnInit;
    }

    /**
     * @param enable If true, this program will automatically configure itself during initialization
     *               to automatically load common uniforms.
     * @see UniformLoaders#addAvailableLoaders
     */
    public void createUniformLoadersOnInit( boolean enable ) {
        mCreateUniformLoadersOnInit = enable;
    }


    public boolean configBlockBindingsOnInit() {
        return mConfigBlockBindingsOnInit;
    }

    /**
     * @param enable If true, this program will automatically configure itself during initialization
     *               to automatically load common uniforms.
     * @see UniformLoaders#addAvailableLoaders
     */
    public void configBlockBindingsOnInit( boolean enable ) {
        mConfigBlockBindingsOnInit = enable;
    }



    /**
     * Not available until initialized.
     */
    public List<ProgramResource> attribsRef() {
        return mAttribs;
    }

    /**
     * Not available until initialized.
     */
    public List<Uniform> uniformsRef() {
        return mUniforms;
    }

    /**
     * Not available until initialized.
     */
    public List<UniformBlock> uniformBlocksRef() {
        return mBlocks;
    }


    public void addBindTask( DrawTask task ) {
        if( mOnBind == null ) {
            mOnBind = new ArrayList<DrawTask>( 6 );
        }
        mOnBind.add( task );
    }


    @Override
    public void init( DrawEnv d ) {
        super.init( d );
        mAttribs = Shaders.listAttributes( d.mGl, mId );
        mUniforms = Shaders.listUniforms( d.mGl, mId );
        mBlocks = Shaders.listUniformBlocks( d.mGl, mId, mUniforms );

        // Remove uniforms in blocks.
        Iterator<Uniform> iter = mUniforms.iterator();
        while( iter.hasNext() ) {
            if( iter.next().mBlockIndex >= 0 ) {
                iter.remove();
            }
        }

        if( mCreateUniformLoadersOnInit ) {
            UniformLoaders.addAvailableLoaders( this );
            bind( d );
        }

        UniformLoaders.setDefaultBlockBindings( d, mId, mBlocks );
        UniformLoaders.setDefaultTexUnits( d, mUniforms );
        unbind( d );
        d.checkErr();
    }

    @Override
    public void bind( DrawEnv d ) {
        super.bind( d );
        List<DrawTask> list = mOnBind;
        if( list == null ) {
            return;
        }
        final int len = list.size();
        for( int i = 0; i < len; i++ ) {
            list.get( i ).run( d );
        }
    }


    public void unbind( DrawEnv d ) {
        d.mGl.glUseProgram( 0 );
    }

}
