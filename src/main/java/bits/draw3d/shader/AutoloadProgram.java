/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.shader;

import bits.draw3d.*;

import java.util.*;


/**
 * @author Philip DeCamp
 */
public class AutoloadProgram extends Program {

    private boolean mCreateUniformLoadersOnInit = true;

    protected final Map<String, ProgramResource> mAttribs  = new HashMap<String, ProgramResource>();
    protected final Map<String, ProgramResource> mUniforms = new HashMap<String, ProgramResource>();
    private List<DrawTask> mOnBind = null;


    public AutoloadProgram() {}



    public boolean createUniformLoadersOnInit() {
        return mCreateUniformLoadersOnInit;
    }

    /**
     * @param enable If true, this program will automatically configure itself during initialization
     *               to automatically load common uniforms.
     * @see bits.draw3d.shader.UniformLoaders#addAvailableLoaders
     */
    public void createUniformLoadersOnInit( boolean enable ) {
        mCreateUniformLoadersOnInit = enable;
    }

    /**
     * Not available until initialized.
     */
    public ProgramResource attrib( String name ) {
        return mAttribs.get( name );
    }

    /**
     * Not available until initialized.
     */
    public Map<String,ProgramResource> attribs() {
        return mAttribs;
    }

    /**
     * Not available until initialized.
     */
    public ProgramResource uniform( String name ) {
        return mUniforms.get( name );
    }

    /**
     * Not available until initialized.
     */
    public Map<String,ProgramResource> uniforms() {
        return mUniforms;
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
        for( ProgramResource p: ShaderUtil.listAttributes( d.mGl, mId ) ) {
            mAttribs.put( p.mName, p );
        }
        for( ProgramResource p: ShaderUtil.listUniforms( d.mGl, mId ) ) {
            mUniforms.put( p.mName, p );
        }

        if( mCreateUniformLoadersOnInit ) {
            UniformLoaders.addAvailableLoaders( this );
            bind( d );
        }

        UniformLoaders.setDefaultTexUnits( d, mUniforms.values() );
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
