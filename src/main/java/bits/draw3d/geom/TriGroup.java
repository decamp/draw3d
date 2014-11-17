package bits.draw3d.geom;

import java.util.*;


/**
 * @author Philip DeCamp
 */
public class TriGroup extends ArrayList<DrawTri> {

    public String        mName;
    public ModelMaterial mMaterial;
    public List<DrawTri> mTris;

    public TriGroup() {
        mTris = new ArrayList<DrawTri>();
    }

    public TriGroup( String name, ModelMaterial material, List<DrawTri> trisRef ) {
        mName     = name;
        mMaterial = material;
        mTris     = trisRef == null ? new ArrayList<DrawTri>() : trisRef;
    }

}
