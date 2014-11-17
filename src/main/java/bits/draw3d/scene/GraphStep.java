package bits.draw3d.scene;

public class GraphStep<T> {
    
    private final GraphActionType mAction;
    private final T mTarget;
    
    
    public GraphStep(GraphActionType action, T target) {
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