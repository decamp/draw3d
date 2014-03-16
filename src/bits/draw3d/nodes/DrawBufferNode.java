package bits.draw3d.nodes;

import java.nio.*;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;


/**
 * @author decamp
 */
public class DrawBufferNode extends DrawNodeAdapter {

    
    public static DrawBufferNode newInstance(DrawBufferParams params) {
        return newInstance(params, null, 0, null, 0);
    }
    
    
    public static DrawBufferNode newInstance( DrawBufferParams params,
                                              ByteBuffer vertBuffer,
                                              int vertUsage )
    {
        return newInstance(params, vertBuffer, vertUsage, null, 0);
    }
    
    
    public static DrawBufferNode newInstance( DrawBufferParams params,
                                              ByteBuffer vertBuffer,
                                              int vertUsage,
                                              ByteBuffer indexBuffer,
                                              int indexUsage )
    {
        List<DrawNode> nodes = new ArrayList<DrawNode>(2);
        
        if(vertBuffer != null) {
            BufferNode vbo = BufferNode.createVertexNode( vertUsage );
            vbo.buffer( vertBuffer );
            nodes.add( vbo );
        }
        
        if(indexBuffer != null) {
            BufferNode ibo = BufferNode.createElementNode( indexUsage );
            ibo.buffer( indexBuffer );
            nodes.add( ibo );
        }
        
        return new DrawBufferNode(nodes.toArray(new DrawNode[nodes.size()]), params);
    }
                                              
    
    
    private DrawNode[] mBuffers;
    private DrawBufferParams mParams = null;
    
    
    private DrawBufferNode( DrawNode[] buffers,
                            DrawBufferParams params ) 
    {
        mBuffers = buffers;
        mParams  = params;
    }
    
    
    
    public DrawBufferParams getParams() {
        return mParams;
    }
    
    
    public void pushDraw(GL gl) {
        for(DrawNode n: mBuffers) {
            n.pushDraw(gl);
        }
        
        mParams.push(gl);
        mParams.execute(gl);
    }
                                 
                                 
    public void popDraw(GL gl) {
        mParams.pop(gl);
        
        for(int i = mBuffers.length - 1; i >= 0; i--) {
            mBuffers[i].popDraw(gl);
        }
    }

    
    public void dispose(GLAutoDrawable gld) {
        for(DrawNode n: mBuffers) {
            n.dispose(gld);
        }
    }
    
}

