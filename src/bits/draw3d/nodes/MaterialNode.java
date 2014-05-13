package bits.draw3d.nodes;

import javax.media.opengl.GL;

import bits.draw3d.model.Material;

import static javax.media.opengl.GL.*;



/**
 * @author decamp
 */
public class MaterialNode extends DrawNodeAdapter {


    private final Material mFront;
    private final Material mBack;


    public MaterialNode( Material mat ) {
        mFront = mat;
        mBack  = mat;
    }


    public MaterialNode( Material front, Material back ) {
        mFront = front;
        mBack  = back;
    }
    

    @Override
    public void pushDraw( GL gl ) {
        if( mFront == mBack ) {
            if( mFront != null ) {
                mFront.write( gl, GL_FRONT_AND_BACK );
            }
        } else {
            if( mFront != null ) {
                mFront.write( gl, GL_FRONT );
            }
            
            if( mBack != null ) {
                mBack.write( gl, GL_BACK );
            }
        }
    }

    @Override
    public void popDraw( GL gl ) {}


    @Deprecated public static MaterialNode newInstance( Material mat ) {
        return new MaterialNode( mat, mat );
    }


    @Deprecated public static MaterialNode newInstance( Material front, Material back ) {
        return new MaterialNode( front, back );
    }

}
