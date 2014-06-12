package bits.draw3d.model;

import bits.math3d.Vectors;


/**
 * @author Philip DeCamp
 */
public class Triangle implements Cloneable {

    public static Triangle fromVerts( double[] v0, double[] v1, double[] v2, boolean computeNorms ) {
        Triangle ret = new Triangle( new double[][]{ v0, v1, v2 }, null, null, null );
        if( computeNorms ) {
            ret.mNorms = ret.createNormVecs();
        }
        return ret;
    }


    public static Triangle fromVerts( double[] v0, double[] v1, double[] v2,
                                      double[] tex0, double[] tex1, double[] tex2,
                                      boolean computeNorms )
    {
        Triangle ret = new Triangle( new double[][]{ v0, v1, v2 },
                                     null,
                                     new double[][]{ tex0, tex1, tex2 },
                                     null );
        if( computeNorms ) {
            ret.mNorms = ret.createNormVecs();
        }
        return ret;
    }


    public double[][] mVerts;
    public double[][] mNorms;
    public double[][] mTexs;
    public double[][] mColors;


    public Triangle() {}


    public Triangle( double[][] vertices, double[][] norm, double[][] texCoord ) {
        mVerts = vertices;
        mNorms = norm;
        mTexs = texCoord;
        mColors = null;
    }


    public Triangle( double[][] vertices, double[][] norm, double[][] texCoord, double[][] color ) {
        mVerts = vertices;
        mNorms = norm;
        mTexs = texCoord;
        mColors = color;
    }


    public void center( double[] out3 ) {
        out3[0] = (mVerts[0][0] + mVerts[1][0] + mVerts[2][0]) / 3.0;
        out3[1] = (mVerts[0][1] + mVerts[1][1] + mVerts[2][1]) / 3.0;
        out3[2] = (mVerts[0][2] + mVerts[1][2] + mVerts[2][2]) / 3.0;
    }


    public void reverseOrientation() {
        double[] ff;

        if( mNorms != null ) {
            ff = mNorms[0];
            mNorms[0] = mNorms[1];
            mNorms[1] = ff;
        }

        if( mTexs != null ) {
            ff = mTexs[0];
            mTexs[0] = mTexs[1];
            mTexs[1] = ff;
        }

        if( mColors != null ) {
            ff = mColors[0];
            mColors[0] = mColors[1];
            mColors[1] = ff;
        }

        double[] v = mVerts[0];
        mVerts[0] = mVerts[1];
        mVerts[1] = v;
    }


    public double[][] createNormVecs() {
        double[][] ret = new double[3][3];
        Vectors.cross( mVerts[0], mVerts[1], mVerts[2], ret[0] );
        Vectors.normalize( ret[0], 1.0 );
        ret[2][0] = ret[1][0] = ret[0][0];
        ret[2][1] = ret[1][1] = ret[0][1];
        ret[2][2] = ret[1][2] = ret[0][2];
        return ret;
    }




    @Override
    public String toString() {
        return String.format( "Triangle:\n\t<%f, %f, %f>\n\t<%f, %f, %f>\n\t<%f, %f, %f>",
                              mVerts[0][0], mVerts[0][1], mVerts[0][2],
                              mVerts[1][0], mVerts[1][1], mVerts[1][2],
                              mVerts[2][0], mVerts[2][1], mVerts[2][2] );

    }


    public Triangle safeCopy() {
        double[][] v = { mVerts[0].clone(), mVerts[1].clone(), mVerts[2].clone() };

        double[][] norm = null;
        double[][] tex = null;
        double[][] color = null;

        if( mNorms != null ) {
            norm = new double[][]{ { mNorms[0][0], mNorms[0][1], mNorms[0][2] },
                                   { mNorms[1][0], mNorms[1][1], mNorms[1][2] },
                                   { mNorms[2][0], mNorms[2][1], mNorms[2][2] } };
        }

        if( mTexs != null ) {
            tex = new double[][]{ { mTexs[0][0], mTexs[0][1] },
                                  { mTexs[1][0], mTexs[1][1] },
                                  { mTexs[2][0], mTexs[2][1] } };
        }

        if( mColors != null ) {
            color = new double[][]{ { mColors[0][0], mColors[0][1], mColors[0][2] },
                                    { mColors[1][0], mColors[1][1], mColors[1][2] },
                                    { mColors[2][0], mColors[2][1], mColors[2][2] } };
        }

        return new Triangle( v, norm, tex, color );
    }




    @Deprecated public final double[][] vertexRef() {
        return mVerts;
    }


    @Deprecated public final double[] vertex( int n ) {
        return mVerts[n];
    }


    @Deprecated public final double[][] normalRef() {
        return mNorms;
    }


    @Deprecated public final double[] normalRef( int n ) {
        return mNorms[n];
    }


    @Deprecated public final double[][] texRef() {
        return mTexs;
    }


    @Deprecated public final double[] texRef( int n ) {
        return mTexs[n];
    }


    @Deprecated public final double[][] colorRef() {
        return mColors;
    }


    @Deprecated public final double[] colorRef( int n ) {
        return mColors[n];
    }


    @Deprecated public void setNormalRef( double[][] n ) {
        mNorms = n;
    }


    @Deprecated public void setTexRef( double[][] t ) {
        mTexs = t;
    }


    @Deprecated public void setColorRef( double[][] c ) {
        mColors = c;
    }

}
