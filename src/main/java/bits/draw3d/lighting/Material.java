/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.lighting;

import bits.math3d.Vec;
import bits.math3d.Vec4;

import com.jogamp.opengl.*;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.*;


/**
 * @author Philip DeCamp  
 */
public class Material {

    private static final Vec4 BLACK = new Vec4( 0, 0, 0, 1 );


    public static Material createDull( float... color ) {
        if( color == null ) {
            return null;
        }
        Vec4 col = new Vec4( color[0], color[1], color[2], ( color.length > 3 ? color[3] : 1f ) );
        return new Material( col, col, BLACK, BLACK, 0f );
    }


    public static Material createEmissive( float... color ) {
        if( color == null ) {
            return null;
        }
        Vec4 col = new Vec4( color[0], color[1], color[2], ( color.length > 3 ? color[3] : 1f ) );
        return new Material( BLACK, BLACK, BLACK, col, 0f );
    }


    //public String mName = "";

    public Vec4   mAmbient;
    public Vec4   mDiffuse;
    public Vec4   mSpecular;
    public Vec4   mEmissive;
    public float  mShininess;

    
    public Material() {
        this( null, null, null, null, 0f );
    }

    
    public Material( Vec4 ambient,
                     Vec4 diffuse,
                     Vec4 specular,
                     Vec4 emissive,
                     float shininess )
    {
        mAmbient   = ambient  == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( ambient );
        mDiffuse   = diffuse  == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( diffuse );
        mSpecular  = specular == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( specular );
        mEmissive  = emissive == null ? new Vec4( 0, 0, 0, 1 ) : new Vec4( emissive );
        mShininess = shininess;
    }
    

    public Material( Material copy ) {
        this( copy.mAmbient,
              copy.mDiffuse,
              copy.mSpecular,
              copy.mEmissive,
              copy.mShininess );
    }


    public float alpha() {
        return mDiffuse.w;
    }


    public void alpha( float alpha ) {
        mDiffuse.w = alpha;
    }


    public void set( Material copy ) {
        Vec.put( copy.mAmbient, mAmbient );
        Vec.put( copy.mAmbient, mAmbient );
        Vec.put( copy.mAmbient, mAmbient );
        Vec.put( copy.mAmbient, mAmbient );
        mShininess = copy.mShininess;
    }


    @Deprecated
    public void read( GL2 gl, int face ) {
        float[] arr = new float[4];
        gl.glGetMaterialfv( face, GL_AMBIENT, arr, 0 );
        Vec.put( arr, mAmbient );
        gl.glGetMaterialfv( face, GL_DIFFUSE, arr, 0 );
        Vec.put( arr, mDiffuse );
        gl.glGetMaterialfv( face, GL_SPECULAR, arr, 0 );
        Vec.put( arr, mSpecular );
        gl.glGetMaterialfv( face, GL_EMISSION, arr, 0 );
        Vec.put( arr, mEmissive );
        gl.glGetMaterialfv( face, GL_SHININESS, arr, 0 );
        mShininess = arr[0];
    }

    @Deprecated
    public void write( GL2 gl, int face ) {
        float[] arr = new float[4];
        Vec.put( mAmbient, arr );
        gl.glMaterialfv( face, GL_AMBIENT, arr, 0 );
        Vec.put ( mDiffuse, arr );
        gl.glMaterialfv( face, GL_DIFFUSE, arr, 0 );
        Vec.put ( mSpecular, arr );
        gl.glMaterialfv( face, GL_SPECULAR, arr, 0 );
        Vec.put ( mEmissive, arr );
        gl.glMaterialfv( face, GL_EMISSION, arr, 0 );
        gl.glMaterialf( face, GL_SHININESS, mShininess );
    }

}
