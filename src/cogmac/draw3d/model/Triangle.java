package cogmac.draw3d.model;

/** 
 * @author Philip DeCamp  
 */
public class Triangle implements Cloneable {

    
    private double[][] mVerts;
    private double[][] mNorm;
    private double[][] mTex;
    private double[][] mColor;

    
    public Triangle(double[][] vertices, double[][] norm, double[][] texCoord) {
        mVerts = vertices;
        mNorm = norm;
        mTex = texCoord;
        mColor = null;
    }
    
    public Triangle(double[][] vertices, double[][] norm, double[][] texCoord, double[][] color) {
        mVerts = vertices;
        mNorm = norm;
        mTex = texCoord;
        mColor = color;
    }

    

    public final double[][] vertexRef() {
        return mVerts;
    }
    
    public final double[] vertex(int n) {
        return mVerts[n];
    }
    
    public final double[][] normalRef() {
        return mNorm;
    }
    
    public final double[] normalRef(int n) {
        return mNorm[n];
    }
    
    public final double[][] texRef() {
        return mTex;
    }
    
    public final double[] texRef(int n) {
        return mTex[n];
    }
    
    public final double[][] colorRef() {
        return mColor;
    }
    
    public final double[] colorRef(int n) {
        return mColor[n];
    }
    
    
    public void setNormalRef(double[][] n) {
        mNorm = n;
    }

    public void setTexRef(double[][] t) {
        mTex  = t;
    }
    
    public void setColorRef(double[][] c) {
        mColor = c;
    }

    
        
    public void reverseOrientation() {
        double[] ff;
        
        if(mNorm != null) {
            ff = mNorm[0];
            mNorm[0] = mNorm[1];
            mNorm[1] = ff;
        }
        
        if(mTex != null) {
            ff = mTex[0];
            mTex[0] = mTex[1];
            mTex[1] = ff;
        }
        
        if(mColor != null) {
            ff = mColor[0];
            mColor[0] = mColor[1];
            mColor[1] = ff;
        }
        
        double[] v = mVerts[0];
        mVerts[0] = mVerts[1];
        mVerts[1] = v;
    }

    
    
    public String toString() {
        return String.format("Triangle:\n\t<%f, %f, %f>\n\t<%f, %f, %f>\n\t<%f, %f, %f>", 
                mVerts[0][0], mVerts[0][1], mVerts[0][2],
                mVerts[1][0], mVerts[1][1], mVerts[1][2],
                mVerts[2][0], mVerts[2][1], mVerts[2][2]);
        
    }
    
    public Triangle safeCopy() {
        double[][] v = {mVerts[0].clone(), mVerts[1].clone(), mVerts[2].clone()};
        
        double[][] norm = null;
        double[][] tex = null;
        double[][] color = null;

        if(mNorm != null) {
            norm = new double[][]{{mNorm[0][0], mNorm[0][1], mNorm[0][2]},
                                   {mNorm[1][0], mNorm[1][1], mNorm[1][2]},
                                   {mNorm[2][0], mNorm[2][1], mNorm[2][2]}};
        }
    
        if(mTex != null) {
            tex = new double[][]{{mTex[0][0], mTex[0][1]},
                                 {mTex[1][0], mTex[1][1]},
                                 {mTex[2][0], mTex[2][1]}};
        }
        
        if(mColor != null) {
            color = new double[][]{{mColor[0][0], mColor[0][1], mColor[0][2]},
                                   {mColor[1][0], mColor[1][1], mColor[1][2]},
                                   {mColor[2][0], mColor[2][1], mColor[2][2]}};
        }
        
        return new Triangle(v, norm, tex, color);
    }

}
