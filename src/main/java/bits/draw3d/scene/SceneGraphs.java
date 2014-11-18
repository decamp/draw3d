/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.scene;

import java.util.*;


/**
 * @author decamp
 */
public class SceneGraphs {

    public static <N> GraphPath<N> modulePathToNodePath( GraphPath<?> path, Class<N> actionClass ) {
        GraphPath<N> ret = new GraphPath<N>();
        Map<Object, List<N>> cache = new HashMap<Object, List<N>>();

        for( GraphStep<?> step : path ) {
            final GraphActionType action = step.type();
            final Object target = step.target();

            List<N> nodes = cache.get( target );
            if( nodes == null ) {
                nodes = extractNodes( target, actionClass );
                cache.put( target, nodes );
            }

            if( nodes.isEmpty() ) {
                continue;
            }

            if( action == GraphActionType.PUSH ) {
                for( N node : nodes ) {
                    ret.add( new GraphStep<N>( GraphActionType.PUSH, node ) );
                }
            } else {
                ListIterator<N> iter = nodes.listIterator( nodes.size() );
                while( iter.hasPrevious() ) {
                    N node = iter.previous();
                    ret.add( new GraphStep<N>( GraphActionType.POP, node ) );
                }
            }
        }

        return ret;
    }


    public static String generateDotProgram( GraphPath<?> path, String name ) {
        StringBuilder s = new StringBuilder("digraph ");
        s.append(name);
        s.append(" {\n");
        
        Stack<String> labelStack = new Stack<String>();
        int idx = 1;
        
        for(GraphStep<?> step: path) {
            if(step.type() == GraphActionType.POP) {
                if(!labelStack.isEmpty()) {
                    labelStack.pop();
                }
                
                continue;
            }else if(step.type() != GraphActionType.PUSH) {
                continue;
            }
            
            String label = String.format("n%03d", idx++);
            
            s.append("\t");
            s.append(label);
            s.append(" [label=\"");
            
            Class<?> clazz = step.target() == null ? Void.class : step.target().getClass();
            
            while(clazz.isMemberClass()) {
                if(clazz.getEnclosingClass() == null)
                    break;
                
                clazz = clazz.getEnclosingClass();
            }
            
            s.append(clazz.getSimpleName());
            s.append("\"];\n\t");
            
            if(labelStack.isEmpty()) {
                s.append(label);
            }else{
                s.append(labelStack.peek());
                s.append(" -> ");
                s.append(label);
            }
            
            s.append(";\n");
            labelStack.push(label);
        }
        
        
        s.append("}");
        return s.toString();
        
    }


    @SuppressWarnings("unchecked")
    private static <N> List<N> extractNodes( Object target, Class<N> nodeClass ) {
        if( target == null ) {
            return new ArrayList<N>( 0 );
        }

        if( nodeClass.isAssignableFrom( target.getClass() ) ) {
            ArrayList<N> ret = new ArrayList<N>( 1 );
            ret.add( (N)target );
            return ret;
        }

        if( target instanceof Iterable ) {
            List<N> ret = new ArrayList<N>( 4 );
            for( Object obj : (Iterable<Object>)target ) {
                if( obj != null && nodeClass.isAssignableFrom( obj.getClass() ) ) {
                    ret.add( (N)obj );
                }
            }
            return ret;
        }

        if( target.getClass().isArray() ) {
            if( !Object[].class.isAssignableFrom( target.getClass() ) ) {
                return new ArrayList<N>( 0 );
            }

            Object[] arr = (Object[])target;
            List<N> ret = new ArrayList<N>( arr.length );
            for( Object obj : arr ) {
                if( obj != null && nodeClass.isAssignableFrom( obj.getClass() ) ) {
                    ret.add( (N)obj );
                }
            }

            return ret;
        }

        return new ArrayList<N>( 0 );
    }

}
