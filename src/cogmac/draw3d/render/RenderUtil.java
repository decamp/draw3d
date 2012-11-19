package cogmac.draw3d.render;

import static javax.media.opengl.GL.*;

import java.util.*;

import javax.media.opengl.GLCapabilities;

import cogmac.draw3d.context.*;
import cogmac.draw3d.nodes.*;
import cogmac.draw3d.scene.*;


/**
 * @author decamp
 */
public class RenderUtil {
    
    
    public static <N> GraphPath<N> modulePathToNodePath(GraphPath<?> path, Class<N> actionClass, RenderTile tile) {
        GraphPath<N> ret = new GraphPath<N>();
        Map<Object,List<N>> cache = new HashMap<Object,List<N>>();
        
        for(GraphStep<?> step: path) {
            final GraphActionType action = step.type();
            final Object target          = step.target();
            
            List<N> nodes = cache.get(target);
            
            if(nodes == null) {
                nodes = extractNodes(target, actionClass, tile);
                cache.put(target, nodes);
            }
            
            if(nodes.isEmpty())
                continue;
            
            if(action == GraphActionType.PUSH) {
                for(N node: nodes) {
                    ret.add(new GraphStep<N>(GraphActionType.PUSH, node));
                }
            }else{
                ListIterator<N> iter = nodes.listIterator(nodes.size());
                
                while(iter.hasPrevious()) {
                    N node = iter.previous();
                    ret.add(new GraphStep<N>(GraphActionType.POP, node));
                }
            }
        }
        
        return ret;
    }

    
    public static FramebufferNode framebufferForCaps(GLCapabilities caps) {
        FramebufferNode fbo           = FramebufferNode.newInstance();
        Texture2dNode colorFrame      = Texture2dNode.newInstance();
        RenderbufferNode depthFrame   = null;
        RenderbufferNode stencilFrame = null;
        
        colorFrame.format( GL_RGBA, GL_RGBA, GL_UNSIGNED_BYTE );
        
        if(caps != null) {
            switch(caps.getDepthBits()) {
            case 0:
                break;
            case 16:
                //depthFrame = RenderbufferNode.newInstance(GL_DEPTH_COMPONENT16);
                //break;
            case 24:
                //depthFrame = RenderbufferNode.newInstance(GL_DEPTH_COMPONENT24);
                //break;
            case 32:
                //depthFrame = RenderbufferNode.newInstance(GL_DEPTH_COMPONENT32);
                //break;
            default:
                depthFrame = RenderbufferNode.newInstance();
                depthFrame.format( GL_DEPTH_COMPONENT, 0, 0 );
                break;
            }
        }
        
        if(caps != null) {
            switch(caps.getStencilBits()) {
            case 0:
                break;
            case 1:
                //stencilFrame = RenderbufferNode.newInstance(GL_STENCIL_INDEX1_EXT);
                //break;
            case 4:
                //stencilFrame = RenderbufferNode.newInstance(GL_STENCIL_INDEX4_EXT);
                //break;
            case 8:
                //stencilFrame = RenderbufferNode.newInstance(GL_STENCIL_INDEX8_EXT);
                //break;
            case 16:
                //stencilFrame = RenderbufferNode.newInstance(GL_STENCIL_INDEX16_EXT);
                //break;
            default:
                stencilFrame = RenderbufferNode.newInstance();
                stencilFrame.format( GL_STENCIL_INDEX, 0, 0 );
                break;
            }
        }
        
        fbo.attach( GL_COLOR_ATTACHMENT0_EXT, colorFrame );
        
        if(depthFrame != null) {
            fbo.attach(GL_DEPTH_ATTACHMENT_EXT, depthFrame);
        }
        
        if(stencilFrame != null) {
            fbo.attach(GL_STENCIL_ATTACHMENT_EXT, stencilFrame);
        }
        
        return fbo;
    }
    
    
    public static int bufferBits(GLCapabilities caps) {
        int ret = 0;
        
        if(caps == null)
            return 0;
        
        if(caps.getAccumAlphaBits() > 0) 
            ret |= GL_ACCUM_BUFFER_BIT;
        
        if( caps.getAlphaBits() > 0 || 
            caps.getRedBits() > 0 || 
            caps.getGreenBits() > 0 || 
            caps.getBlueBits() > 0) 
        {
            ret |= GL_COLOR_BUFFER_BIT;
        }
        
        if(caps.getDepthBits() > 0) 
            ret |= GL_DEPTH_BUFFER_BIT;
        
        if(caps.getStencilBits() > 0)
            ret |= GL_STENCIL_BUFFER_BIT;
        
        return ret;
    }
    
    
    
    @SuppressWarnings("unchecked")
    private static <N> List<N> extractNodes(Object target, Class<N> nodeClass, RenderTile tile) {
        while(target instanceof RenderModule) {
            target = ((RenderModule)target).getNodes(nodeClass, tile);
        }
        
        
        if(target == null) {
            return new ArrayList<N>(0);
        }
        
        
        if(nodeClass.isAssignableFrom(target.getClass())) {
            ArrayList<N> ret = new ArrayList<N>(1);
            ret.add((N)target);
            return ret;
        }
        
        
        if(target instanceof Iterable) {
            List<N> ret = new ArrayList<N>(4);
            
            for(Object obj: (Iterable<Object>)target) {
                if(obj != null && nodeClass.isAssignableFrom(obj.getClass())) {
                    ret.add((N)obj);
                }
            }
            
            return ret;
        }
        
        
        if(target.getClass().isArray()) {
            if(!Object[].class.isAssignableFrom(target.getClass()))
                return new ArrayList<N>(0);
            
            Object[] arr = (Object[])target;
            List<N> ret = new ArrayList<N>(arr.length);
            
            for(Object obj: arr) {
                if(obj != null && nodeClass.isAssignableFrom(obj.getClass())) {
                    ret.add((N)obj);
                }
            }
            
            return ret;
        }
        
        return new ArrayList<N>(0);
    }
    
}
