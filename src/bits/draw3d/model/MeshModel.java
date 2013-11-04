package bits.draw3d.model;

import java.util.*;


/** 
 * @author Philip DeCamp  
 */
public class MeshModel {

    private List<Group> mGroups;
    
    
    public MeshModel() {
        mGroups = new ArrayList<Group>();
    }

    
    public MeshModel(List<Group> groups) {
        mGroups = (groups == null ? new ArrayList<Group>(0) : groups);
    }

    
    
    /**
     * @return defensive copy of list of groups.
     */
    public List<Group> getGroups() {
        return new ArrayList<Group>(mGroups);
    }
    
    
    /**
     * @return the list of groups, without making a defesive copy 
     */
    public List<Group> getGroupsRef() {
        return mGroups;
    }

    
    /**
     * Sets list of groups, making a defensive copy.
     * @param groups
     */
    public void setGroups(Collection<? extends Group> groups) {
        mGroups = new ArrayList<Group>(mGroups);
    }

    
    /**
     * Sets list of groups by reference, without making defensive copy.
     * @param groups
     */
    public void setGroupRef(List<Group> groups) {
        mGroups = groups;
    }
    


    public List<Triangle> getAllTriangles(List<Triangle> out) {
        if(out == null)
            out = new ArrayList<Triangle>();
        
        for(Group g: mGroups)
            out.addAll(g.getTriangles());
        
        return out;
    }

}
