//package cogmac.draw3d.model;
//
///** 
// * @author Philip DeCamp  
// */
//public class BasicMaterial implements Material {
//
//    private final float[] mAmbient;
//    private final float[] mDiffuse;
//    private final float[] mSpecular;
//    private final float[] mEmissive;
//    private float mAlpha;
//    private float mShininess;
//    
//    private String mName;
//    
//    
//    public BasicMaterial( String name, 
//                          float[] ambient4x1,
//                          float[] diffuse4x1,
//                          float[] specular4x1,
//                          float[] emissive4x1,
//                          float alpha, 
//                          float shininess)
//    {
//        mName = name;
//        mAmbient = copyColor(ambient4x1);
//        mDiffuse = copyColor(diffuse4x1);
//        mSpecular = copyColor(specular4x1);
//        mEmissive = copyColor(emissive4x1);
//        mAlpha = alpha;
//        mShininess = shininess;
//    }
//    
//    
//    public String getName() {
//        return mName;
//    }
//    
//    
//    public void setName(String name) {
//        mName = name;
//    }
//
//    
//    public float[] getAmbientRef() {
//        return mAmbient;
//    }
//    
//    
//    public float[] getDiffuseRef() {
//        return mDiffuse;
//    }
//    
//    
//    public float[] getSpecularRef() {
//        return mSpecular;
//    }
//    
//    
//    public float[] getEmissiveRef() {
//        return mEmissive;
//    }
//    
//    
//    public float getShininess() {
//        return mShininess;
//    }
//    
//    
//    public float getTransparency() {
//        return mAlpha;
//    }
//    
//
//    public String toString() {
//        return String.format("Material [name: %s, path: %s, alpha: %f, shiny: %f]", 
//                            mName, null, mAlpha, mShininess);
//    }
//
//    
//
//    private static float[] copyColor(float[] c) {
//        if(c == null)
//            return null;
//        
//        if(c.length < 4)
//            throw new IllegalArgumentException("Color with length < 4");
//        
//        return new float[]{c[0], c[1], c[2], c[3]};
//    }
//
//}
