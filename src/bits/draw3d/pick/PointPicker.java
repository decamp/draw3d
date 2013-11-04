/**
 * MIT Media Lab
 * Cognitive Machines Group
 */

package bits.draw3d.pick;

/** 
 * @author Philip DeCamp  
 */
public interface PointPicker<P> {
    public PointPickResult<P> newPointPickResult();
    public boolean pick(P point, PointPickResult<P> out);
}
