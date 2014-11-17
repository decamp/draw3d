package bits.draw3d.model;

import bits.draw3d.tex.Material;
import bits.draw3d.tex.Texture;

import java.awt.image.BufferedImage;

public class ModelMaterial {

    public String        mName     = "";
    public BufferedImage mImage    = null;

    public Texture       mTex      = null;
    public Material      mMaterial = null;


    public ModelMaterial() {}


    public ModelMaterial( String name, BufferedImage im, Texture tex, Material matr ) {
        mName     = name;
        mImage    = im;
        mTex      = tex;
        mMaterial = matr;
    }


    public ModelMaterial( ModelMaterial copy ) {
        mName     = copy.mName;
        mImage    = copy.mImage;
        mTex      = copy.mTex;
        mMaterial = copy.mMaterial;
    }



    public int hashCode() {
        return ( mImage    == null ? 0 : mImage.hashCode() ) ^
               ( mTex      == null ? 0 : mTex.hashCode() ) ^
               ( mMaterial == null ? 0 : mMaterial.hashCode() );
    }


    public boolean equals( Object obj ) {
        if( !( obj instanceof ModelMaterial ) ) {
            return false;
        }

        ModelMaterial tm = (ModelMaterial)obj;
        return this == tm ||
               eq( mImage, tm.mImage ) &&
               eq( mTex, tm.mTex ) &&
               eq( mMaterial, tm.mMaterial );
    }


    private static boolean eq( Object a, Object b ) {
        return a == b || a != null && a.equals( b );
    }
    
}
