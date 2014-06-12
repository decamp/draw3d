package bits.draw3d.model;

import java.awt.image.BufferedImage;
import java.util.*;


/**
 * @author Philip DeCamp
 */
public class Group {

    public String        mName     = "";
    public BufferedImage mTex      = null;
    public Material      mMaterial = null;
    public List<Triangle> mTris;


    public Group() {
        mName = "";
        mMaterial = null;
        mTris = new ArrayList<Triangle>();
    }


    public Group( String name, BufferedImage tex, Material mat, List<Triangle> listRef ) {
        mName = (name == null ? "" : name);
        mTex = tex;
        mMaterial = mat;
        mTris = (listRef == null ? new ArrayList<Triangle>() : listRef);
    }


    public void collectVerts( Collection<double[]> out ) {
        for( Triangle t: mTris ) {
            out.add( t.mVerts[0] );
            out.add( t.mVerts[0] );
            out.add( t.mVerts[0] );
        }
    }


    public ModelMaterial createModelMaterial() {
        return new ModelMaterial( mTex, mMaterial );
    }



    @Deprecated public String getName() {
        return mName;
    }


    @Deprecated public void setName( String name ) {
        mName = name;
    }


    @Deprecated public Material getMaterial() {
        return mMaterial;
    }


    @Deprecated public BufferedImage getTexture() {
        return mTex;
    }


    @Deprecated public void setTexture( BufferedImage tex ) {
        mTex = tex;
    }


    @Deprecated public void setMaterial( Material mat ) {
        mMaterial = mat;
    }

    /**
     * Returns list of triangles, making a defensive copy.
     * 
     * @return triangles
     */
    @Deprecated public List<Triangle> getTriangles() {
        return new ArrayList<Triangle>( mTris );
    }

    /**
     * Gets list of triangles, without making a defensive copy.
     * 
     * @return triangles
     */
    @Deprecated public List<Triangle> getTrianglesRef() {
        return mTris;
    }

    /**
     * Sets list of triangles, making a defensive copy.
     */
    @Deprecated public void setTriangles( List<Triangle> tris ) {
        if( tris == null )
            throw new NullPointerException();

        mTris = new ArrayList<Triangle>( tris );
    }

    /**
     * Sets list of triangles without making a defensive copy.
     */
    @Deprecated public void setTrianglesRef( List<Triangle> tris ) {
        mTris = ( tris == null ? new ArrayList<Triangle>() : tris );
    }

}
