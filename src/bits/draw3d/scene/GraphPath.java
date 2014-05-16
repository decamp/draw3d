package bits.draw3d.scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * @author decamp
 */
public class GraphPath<T> extends ArrayList<GraphStep<T>> {


    public GraphPath() {}


    public GraphPath( Collection<? extends GraphStep<T>> collection ) {
        super( collection );
    }


    public void retainActionType( GraphActionType actionType ) {
        Iterator<GraphStep<T>> iter = iterator();
        while( iter.hasNext() ) {
            if( iter.next().type() != actionType ) {
                iter.remove();
            }
        }
    }


    public List<T> toTargetList() {
        List<T> ret = new ArrayList<T>( size() );
        for( GraphStep<T> s : this ) {
            ret.add( s.target() );
        }
        return ret;
    }


}
