package bits.draw3d.geom;

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
