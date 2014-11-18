/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.scene;

public class GraphStep<T> {
    
    private final GraphActionType mAction;
    private final T mTarget;
    
    
    public GraphStep( GraphActionType action, T target ) {
        mTarget = target;
        mAction = action;
    }

    
    public GraphActionType type() {
        return mAction;
    }


    public T target() {
        return mTarget;
    }
    
}