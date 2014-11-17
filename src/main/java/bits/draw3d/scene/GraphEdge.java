/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.scene;


public final class GraphEdge implements Comparable<GraphEdge> {
    
    private static long sStaticCount = Long.MIN_VALUE;
    
    private final Object mParent;
    private final Object mChild;
    private final int mOrder;
    private final long mStaticOrder;
    
    GraphEdge(Object parent, Object child, int order) {
        mParent = parent;
        mChild = child;
        mOrder = order;
        
        synchronized(GraphEdge.class) {
            mStaticOrder = sStaticCount++;
        }
    }
    
    
    public Object parent() {
        return mParent;
    }
       
    public Object child() {
        return mChild;
    }
    
    public int order() {
        return mOrder;
    }

    public int compareTo(GraphEdge e) {
        if(e == this)
            return 0;
        
        if(mOrder < e.mOrder)
            return -1;
        
        if(mOrder > e.mOrder)
            return 1;
        
        if(mStaticOrder < e.mStaticOrder)
            return -1;
        
        return 1;
    }

}