package cogmac.draw3d.nodes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import static javax.media.opengl.GL.*;

import cogmac.draw3d.context.*;
import cogmac.draw3d.model.*;



/** 
 * Renders a given model.
 * 
 * @author Philip DeCamp  
 */
public class TrianglesNode implements RenderModule {


    public static TrianglesNode newInstance(List<Triangle> trianglesRef, boolean useBuffer) {
    
        int vertOffset   =  0;
        int texOffset    =  0;
        int normOffset   =  0;
        int colorOffset  =  0;
        
        for(Triangle t: trianglesRef) {
            if(t.texRef() == null)
                texOffset = -1;
            
            if(t.normalRef() == null)
                normOffset = -1;
            
            if(t.colorRef() == null)
                colorOffset = -1;
        }
        
        
        int vertBytes = 12;
        
        if(texOffset >= 0) {
            texOffset = vertBytes;
            vertBytes += 8;
        }
        
        if(normOffset >= 0) {
            normOffset = vertBytes;
            vertBytes += 12;
        }
        
        if(colorOffset >= 0) {
            colorOffset = vertBytes;
            vertBytes += 4;
        }
        
        
        ByteBuffer buffer       = null;
        DrawBufferParams params = null;
        
        
        if(useBuffer) {
            int vertCount = trianglesRef.size() * 3;
            
            ByteBuffer b = ByteBuffer.allocateDirect(vertBytes * vertCount);
            b.order(ByteOrder.nativeOrder());
            
            for(Triangle t: trianglesRef) {
                double[][] vert = t.vertexRef();
                double[][] tex  = t.texRef();
                double[][] col  = t.colorRef();
                double[][] norm = t.normalRef();
                
                for(int i = 0; i < 3; i++) {
                    b.putFloat((float)vert[i][0]);
                    b.putFloat((float)vert[i][1]);
                    b.putFloat((float)vert[i][2]);
                    
                    if(texOffset >= 0) {
                        b.putFloat((float)tex[i][0]);
                        b.putFloat((float)tex[i][1]);
                    }
                    
                    if(normOffset >= 0) {
                        b.putFloat((float)norm[i][0]);
                        b.putFloat((float)norm[i][1]);
                        b.putFloat((float)norm[i][2]);
                    }
                    
                    if(colorOffset >= 0) {
                        b.put((byte)(col[i][0] * 255.0 + 0.5));
                        b.put((byte)(col[i][1] * 255.0 + 0.5));
                        b.put((byte)(col[i][2] * 255.0 + 0.5));
                        b.put((byte)(col[i][3] * 255.0 + 0.5));
                    }
                }
            }
            
            b.flip();
            buffer = b;
            
            params = DrawBufferParams.newInstance();
            params.enableVertexPointer(3, GL_FLOAT, vertBytes, vertOffset);
            
            if(texOffset >= 0)
                params.enableTexPointer(2, GL_FLOAT, vertBytes, texOffset);
                        
            if(normOffset >= 0)
                params.enableNormPointer(GL_FLOAT, vertBytes, normOffset);

            if(colorOffset >= 0)
                params.enableColorPointer(4, GL_UNSIGNED_BYTE, vertBytes, colorOffset);
            
            params.enableCommand(GL_TRIANGLES, 0, vertCount);
        }
        
        return new TrianglesNode( trianglesRef,
                                  vertBytes,
                                  vertOffset, 
                                  texOffset, 
                                  normOffset, 
                                  colorOffset, 
                                  buffer, 
                                  params );
    }
    
    
    private final List<Triangle> mTriangles;

    private final int mVertBytes;
    private final int mVertOffset;
    private final int mTexOffset;
    private final int mNormOffset;
    private final int mColorOffset;
    
    private ByteBuffer mDrawBuffer;
    private DrawBufferParams mDrawParams;
    private TriangleRenderer mRenderer = null;
    
    private boolean mTextureEnabled;
    private boolean mColorEnabled;
    private boolean mNormalEnabled;
    
    
    public TrianglesNode( List<Triangle> triangleRef,
                          int vertBytes,
                          int vertOffset,
                          int texOffset,
                          int normOffset,
                          int colorOffset,
                          ByteBuffer drawBuffer,
                          DrawBufferParams drawParams )
    {
        mTriangles   = triangleRef;
        mVertBytes   = vertBytes;
        mVertOffset  = vertOffset;
        mTexOffset   = texOffset;
        mNormOffset  = normOffset;
        mColorOffset = colorOffset;
        mDrawBuffer  = drawBuffer;
        mDrawParams  = drawParams;
        
        guessParameters();
    }
    
    
    
    
    public void guessParameters() {
        mTextureEnabled = mTexOffset >= 0;
        mColorEnabled   = mColorOffset >= 0 && mTexOffset < 0;
        mNormalEnabled  = mNormOffset >= 0;
        mRenderer = null;
    }
    
    
    public void setBindTextureCoords(boolean enable) {
        if(mTexOffset < 0 || mTextureEnabled == enable)
            return;
        
        mTextureEnabled = enable;
        mRenderer = null;
        
        if(mDrawParams != null) {
            if(enable) {
                mDrawParams.enableTexPointer(2, GL_FLOAT, mVertBytes, mTexOffset);
            }else{
                mDrawParams.disableTexPointer();
            }
        }
    }
    
    
    public void setBindColors(boolean enable) {
        if(mColorOffset < 0 || mColorEnabled == enable)
            return;
        
        mColorEnabled = enable;
        mRenderer = null;
        
        if(mDrawParams != null) {
            if(enable) {
                mDrawParams.enableColorPointer(4, GL_UNSIGNED_BYTE, mVertBytes, mColorOffset);
            }else{
                mDrawParams.disableColorPointer();
            }
        }
    }
    
    
    public void setBindNormals(boolean enable) {
        if(mNormOffset < 0 || mNormalEnabled == enable)
            return;
        
        mNormalEnabled = enable;
        mRenderer = null;
        
        if(mDrawParams != null) {
            if(enable) {
                mDrawParams.enableNormPointer(GL_FLOAT, mVertBytes, mNormOffset);
            }else{
                mDrawParams.disableNormPointer();
            }
        }
    }
    
    
    public List<Triangle> trianglesRef() {
        return mTriangles;
    }
    
    
    
    public Object getNodes(Class<?> nodeClass, RenderTile tile) {
        if(nodeClass == DrawNode.class)
            return new DrawHandler( tile == null || tile.isLast() );
        
        return null;
    }
    
    
    
    private TriangleRenderer getRenderer() {
        TriangleRenderer rend = mRenderer;
        if(rend != null)
            return rend;
        
        if(mTextureEnabled) {
            if(mColorEnabled) {
                if(mNormalEnabled) {
                    rend = new OnOnOnRenderer();
                }else{
                    rend = new OnOnOffRenderer();
                }
            }else{
                if(mNormalEnabled) {
                    rend = new OnOffOnRenderer();
                }else{
                    rend = new OnOffOffRenderer();
                }
            }
        }else{
            if(mColorEnabled) {
                if(mNormalEnabled) {
                    rend = new OffOnOnRenderer();
                }else{
                    rend = new OffOnOffRenderer();
                }
            }else{
                if(mNormalEnabled) {
                    rend = new OffOffOnRenderer();
                }else{
                    rend = new OffOffOffRenderer();
                }
            }
        }
        
        mRenderer = rend;
        return rend;
    }
    

    
    private class DrawHandler extends DrawNodeAdapter {
        
        private final boolean mIsLast;
        private final BufferNode mBufferNode;
        
        DrawHandler(boolean isLast) {
            mIsLast = isLast;
            
            if(mDrawBuffer != null) {
                mBufferNode = BufferNode.newVertexInstance(mDrawBuffer, GL_STATIC_DRAW);
            }else{
                mBufferNode = null;
            }
        }
        
        
        public void init(GLAutoDrawable gld) {
            if(mIsLast) {
                mDrawBuffer = null;
            }
        }

        
        @Override
        public void pushDraw(GL gl) {
            if(mBufferNode != null) {
                mBufferNode.pushDraw(gl);
                mDrawParams.push(gl);
                mDrawParams.execute(gl);
                mDrawParams.pop(gl);
                mBufferNode.popDraw(gl);
            }else{
                render(gl);
            }
        }
        
        
        private void render(GL gl) {
            TriangleRenderer rend = getRenderer();
            
            gl.glBegin(GL_TRIANGLES);
            rend.render(gl, mTriangles);
            gl.glEnd();
        }
        
    }
    
    
    
    
    private static interface TriangleRenderer {
        public void render(GL gl, List<Triangle> tris);
    }
    
    
    private static final class OnOnOnRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            for(Triangle t: tris) {
                double[][] tex = t.texRef();
                
                for(int i = 0; i < 3; i++) {
                    gl.glTexCoord2d(tex[i][0], tex[i][1]);
                    gl.glColor3dv(t.colorRef(i), 0);
                    gl.glNormal3dv(t.normalRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }
    
    
    private static final class OnOnOffRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {

            for(Triangle t: tris) {
                double[][] tex = t.texRef();
                
                for(int i = 0; i < 3; i++) {
                    gl.glTexCoord2d(tex[i][0], tex[i][1]);
                    gl.glColor3dv(t.colorRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
            
        }
    }

    
    private static final class OnOffOnRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            
            for(Triangle t: tris) {
                double[][] tex = t.texRef();
                
                for(int i = 0; i < 3; i++) {
                    gl.glTexCoord2d(tex[i][0], tex[i][1]);
                    gl.glNormal3dv(t.normalRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
            
        }
    }
    
    
    private static final class OnOffOffRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            
            for(Triangle t: tris) {
                double[][] tex = t.texRef();
                
                for(int i = 0; i < 3; i++) {
                    gl.glTexCoord2d(tex[i][0], tex[i][1]);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }
    
    
    private static final class OffOnOnRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            for(Triangle t: tris) {
                for(int i = 0; i < 3; i++) {
                    gl.glColor3dv(t.colorRef(i), 0);
                    gl.glNormal3dv(t.normalRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }
    
    
    private static final class OffOnOffRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            for(Triangle t: tris) {
                for(int i = 0; i < 3; i++) {
                    gl.glColor3dv(t.colorRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }
    
    
    private static final class OffOffOnRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            for(Triangle t: tris) {
                for(int i = 0; i < 3; i++) {
                    gl.glNormal3dv(t.normalRef(i), 0);
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }

    
    private static final class OffOffOffRenderer implements TriangleRenderer {
        public void render(GL gl, List<Triangle> tris) {
            for(Triangle t: tris) {
                for(int i = 0; i < 3; i++) {
                    gl.glVertex3dv(t.vertex(i), 0);                
                }
            }
        }
    }
        
}
