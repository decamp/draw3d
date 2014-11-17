/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.model;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Philip DeCamp
 */
public class TriModel {

    public String mName = "";
    public List<TriGroup> mGroups;

    public TriModel() {
        mGroups = new ArrayList<TriGroup>();
    }

    public TriModel( String name, List<TriGroup> groupsRef ) {
        mName   = name;
        mGroups = groupsRef == null ? new ArrayList<TriGroup>() : groupsRef;
    }

}
