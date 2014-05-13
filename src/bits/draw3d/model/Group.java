package bits.draw3d.model;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;


/**
 * @author Philip DeCamp
 */
public class Group {

    private String mName = "";
    private BufferedImage mTex = null;
    private Material mMaterial = null;
    private List<Triangle> mTriangles;


    public Group() {
        mName = "";
        mMaterial = null;
        mTriangles = new ArrayList<Triangle>();
    }


    public Group( String name, BufferedImage tex, Material mat, List<Triangle> listRef ) {
        mName = (name == null ? "" : name);
        mTex = tex;
        mMaterial = mat;
        mTriangles = (listRef == null ? new ArrayList<Triangle>() : listRef);
    }



    public String getName() {
        return mName;
    }


    public void setName( String name ) {
        mName = name;
    }


    public Material getMaterial() {
        return mMaterial;
    }


    public BufferedImage getTexture() {
        return mTex;
    }


    public void setTexture( BufferedImage tex ) {
        mTex = tex;
    }


    public void setMaterial( Material mat ) {
        mMaterial = mat;
    }

    /**
     * Returns list of triangles, making a defensive copy.
     * 
     * @return triangles
     */
    public List<Triangle> getTriangles() {
        return new ArrayList<Triangle>( mTriangles );
    }

    /**
     * Gets list of triangles, without making a defensive copy.
     * 
     * @return triangles
     */
    public List<Triangle> getTrianglesRef() {
        return mTriangles;
    }

    /**
     * Sets list of triangles, making a defensive copy.
     */
    public void setTriangles( List<Triangle> tris ) {
        if( tris == null )
            throw new NullPointerException();

        mTriangles = new ArrayList<Triangle>( tris );
    }

    /**
     * Sets list of triangles without making a defensive copy.
     */
    public void setTrianglesRef( List<Triangle> tris ) {
        mTriangles = ( tris == null ? new ArrayList<Triangle>() : tris );
    }


    public ModelMaterial createModelMaterial() {
        return new ModelMaterial( mTex, mMaterial );
    }

}
