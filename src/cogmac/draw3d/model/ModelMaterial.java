package cogmac.draw3d.model;

import java.awt.image.BufferedImage;

public class ModelMaterial {
    
    public BufferedImage mTex;
    public Material mMat;
    
    
    public ModelMaterial() {}
    
    public ModelMaterial( BufferedImage tex, Material mat ) {
        mTex = tex;
        mMat = mat;
    }
    
    
    public int hashCode() {
        return ( mTex == null ? 0 : mTex.hashCode() ) ^
               ( mMat == null ? 0 : mMat.hashCode() );
    }

    public boolean equals( Object obj ) {
        if( !( obj instanceof ModelMaterial ) ) {
            return false;
        }
        
        ModelMaterial tm = (ModelMaterial)obj;
        
        return ( mTex == tm.mTex || mTex != null && mTex.equals( tm.mTex ) ) &&
               ( mMat == tm.mMat || mMat != null && mMat.equals( tm.mMat ) );
    }
    
}
