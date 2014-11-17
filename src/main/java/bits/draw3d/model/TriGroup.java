/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model;

import java.util.*;


/**
 * @author Philip DeCamp
 */
public class TriGroup extends ArrayList<DrawTri> {

    public String        mName;
    public DrawMaterial  mMaterial;
    public List<DrawTri> mTris;

    public TriGroup() {
        mTris = new ArrayList<DrawTri>();
    }

    public TriGroup( String name, DrawMaterial material, List<DrawTri> trisRef ) {
        mName = name;
        mMaterial = material;
        mTris = trisRef == null ? new ArrayList<DrawTri>() : trisRef;
    }

}
