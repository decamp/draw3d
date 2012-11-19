package cogmac.draw3d.nodes;

import static javax.media.opengl.GL.*;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import cogmac.draw3d.context.RenderTile;

/**
 * @author decamp
 */
public class BufferNode extends DrawNodeAdapter {
    
    
    public static BufferNode newVertexInstance() {
        return newVertexInstance(null, GL_STATIC_DRAW);
    }
    
    
    public static BufferNode newVertexInstance(ByteBuffer buffer, int usage) {
        return new BufferNode(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, buffer, 0, usage);
    }
    
    
    public static BufferNode newVertexInstance(int size, int usage) {
        return new BufferNode(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, null, size, usage);
        
    }
    
    
    public static BufferNode newElementInstance() {
        return newElementInstance(null, GL_STATIC_DRAW);
    }
    
    
    public static BufferNode newElementInstance(ByteBuffer buffer, int usage) {
        return new BufferNode(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING, buffer, 0, usage);
    }
    
    
    public static BufferNode newElementInstance(int size, int usage) {
        return new BufferNode(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING, null, size, usage);
        
    }
    
    
    public static RenderModule newVertexModule() {
        return newModule(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, null, -1, GL_STATIC_DRAW);
    }
    
    
    public static RenderModule newVertexModule(ByteBuffer buffer, int usage) {
        return newModule(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, buffer, 0, usage);
    }
    
    
    public static RenderModule newVertexModule(int size, int usage) {
        return newModule(GL_ARRAY_BUFFER, GL_ARRAY_BUFFER_BINDING, null, size, usage);
    }
    
    
    public static RenderModule newElementModule() {
        return newModule(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING, null, -1, GL_STATIC_DRAW);
    }
    
    
    public static RenderModule newElementModule(ByteBuffer buffer, int usage) {
        return newModule(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING, buffer, 0, usage);
    }
    
    
    public static RenderModule newElementModule(int size, int usage) {
        return newModule(GL_ELEMENT_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER_BINDING, null, size, usage);
    }
    
    
    private static RenderModule newModule(final int type, final int getType, ByteBuffer buffer, final int size, final int usage) {
        final ByteBuffer data = buffer == null ? null : buffer.duplicate();
        
        NodeFactory<DrawNode> factory = new NodeFactory<DrawNode>() {
            public DrawNode newInstance(RenderTile tile) {
                return new BufferNode(type, getType, data, size, usage);
            }
        };
        
        RenderModuleBuilder b = new RenderModuleBuilder();
        b.addFactory(DrawNode.class, factory, false);
        return b.build();
    }
    
    
    
    
    
    private final int mType;
    private final int mGetType;
    
    private final int[] mRevert = {0};
    private final int[] mId     = {0};
    
    private final int mUsage;
    
    private ByteBuffer mCopyBytes;
    private int mAllocBytes;
    
    
    
    private BufferNode(int type, int getType, ByteBuffer data, int size, int usage) {
        mType       = type;
        mGetType    = getType;
        mCopyBytes  = data == null ? null : data.duplicate();
        mAllocBytes = size;
        mUsage      = usage;
    }

    
    
    public void pushDraw(GL gl) {
        gl.glGetIntegerv(mGetType, mRevert, 0);
        
        if(mId[0] != 0) {
            gl.glBindBuffer(mType, mId[0]);
            return;
        }
        
        gl.glGenBuffers(1, mId, 0);
        gl.glBindBuffer(mType, mId[0]);
        
        if(mCopyBytes != null) {
            gl.glBufferData(mType, mCopyBytes.remaining(), mCopyBytes, mUsage);
        }else if(mAllocBytes >= 0) {
            gl.glBufferData(mType, mAllocBytes, null, mUsage);
        }
        
        mCopyBytes  = null;
        mAllocBytes = -1;
    }


    public void popDraw(GL gl) {
        gl.glBindBuffer(mType, mRevert[0]);
    }

    
    @Override
    public void dispose(GLAutoDrawable gld) {
        if(mId[0] != 0) {
            gld.getGL().glDeleteBuffers(1, mId, 0);
            mId[0] = 0;
        }
        
        mCopyBytes = null;
    }

    
    public ByteBuffer map(GL gl, int access) {
        return gl.glMapBuffer(mType, access);
    }
    
    
    public boolean unmap(GL gl) {
        return gl.glUnmapBuffer(mType);
    }
    
}
