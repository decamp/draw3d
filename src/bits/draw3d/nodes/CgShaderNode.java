package bits.draw3d.nodes;

import java.io.File;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import bits.draw3d.nodes.DrawNodeAdapter;

import com.sun.opengl.cg.*;


/**
 * @author decamp
 */
public class CgShaderNode extends DrawNodeAdapter {


    public static final int PROFILE_VERTEX = CgGL.CG_PROFILE_ARBVP1;
    public static final int PROFILE_FRAGMENT = CgGL.CG_PROFILE_ARBFP1;

    public static final Object STATE_CLASS_VERTEX_SHADER = new Object();
    public static final Object STATE_CLASS_FRAGMENT_SHADER = new Object();

    private static CGcontext sContext = null;


    public static synchronized CGcontext getContext() {
        if( sContext == null ) {
            sContext = CgGL.cgCreateContext();
            CgGL.cgGLSetOptimalOptions( PROFILE_VERTEX );
            CgGL.cgGLSetOptimalOptions( PROFILE_FRAGMENT );
        }

        return sContext;
    }


    public static CGprogram loadProgramFromFile( int profile, File file ) {
        CGcontext context = getContext();
        return CgGL.cgCreateProgramFromFile( context, CgGL.CG_SOURCE, file.getPath(), profile, null, null );
    }


    public static CGprogram loadProgramFromString( int profile, String string ) {
        CGcontext context = getContext();
        return CgGL.cgCreateProgram( context, CgGL.CG_SOURCE, string, profile, null, null );
    }



    private final int mProfile;
    private final CGprogram mProgram;

    private boolean mLoaded = false;


    public CgShaderNode( int profile, CGprogram program ) {
        mProfile = profile;
        mProgram = program;
    }



    @Override
    public void dispose( GLAutoDrawable gld ) {
        mLoaded = false;
        // TODO: LOW: Add implementation when jogl CG bindings catch up.
    }


    @Override
    public void pushDraw( GL gl ) {
        if( !mLoaded ) {
            load();
        }

        CgGL.cgGLEnableProfile( mProfile );
        CgGL.cgGLBindProgram( mProgram );
    }


    @Override
    public void popDraw( GL gl ) {
        CgGL.cgGLDisableProfile( mProfile );
    }



    public CGprogram getProgram() {
        return mProgram;
    }


    public CGparameter getNamedParameter( String name ) {
        return CgGL.cgGetNamedParameter( mProgram, name );
    }



    private void load() {
        CgGL.cgGLLoadProgram( mProgram );
        mLoaded = true;
    }


    @Deprecated public static CgShaderNode newInstance( int profile, CGprogram program ) {
        return new CgShaderNode( profile, program );
    }

}
