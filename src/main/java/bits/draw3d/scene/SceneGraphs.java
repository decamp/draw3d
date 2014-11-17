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

    public static String generateDotProgram(GraphPath<?> path, String name) {
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
    
}
