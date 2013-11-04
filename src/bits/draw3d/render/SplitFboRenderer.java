package bits.draw3d.render;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.*;

import bits.draw3d.context.*;
import bits.draw3d.nodes.*;
import bits.draw3d.scene.*;
import bits.math3d.LongRect;

import static javax.media.opengl.GL.*;


/**
 * Performs rendering on a space with multiple SHARING contexts
 * by rendering first into a FBO and then blitting into the
 * other tiles. 
 * 
 * @author decamp
 * @deprecated Of very limited use.
 */
public class SplitFboRenderer implements Renderer {
    
    
    public static SplitFboRenderer newInstance(GLCapabilities caps, RenderSpace space, SceneGraph graph) {
        RenderTile tile     = space.firstTile();
        LongRect bounds     = tile.renderSpaceBounds();
        int blitMask        = GL_COLOR_BUFFER_BIT;
        
        FramebufferNode fbo      = FramebufferNode.newInstance();
        Texture2dNode colorFrame = Texture2dNode.newInstance();
        Texture2dNode depthFrame = Texture2dNode.newInstance();
        depthFrame.param(GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        depthFrame.param(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        depthFrame.format(GL_DEPTH_COMPONENT, GL_DEPTH_COMPONENT, GL_FLOAT);
        
        fbo.attach(GL_COLOR_ATTACHMENT0_EXT, colorFrame);
        fbo.attach(GL_DEPTH_ATTACHMENT_EXT, depthFrame); 
        fbo.size((int)bounds.spanX(), (int)bounds.spanY());

        return new SplitFboRenderer(fbo, colorFrame, space, graph);
    }

    
    
    private final FramebufferNode mFbo;
    private final Texture2dNode mTex;
    
    private final RenderSpace mSpace;
    private final List<GLAutoDrawable> mDrawables;
    private final RenderSpace mOffSpace;
    private final RenderTile mOffTile;
    
    private final DrawNode mDrawHandler;
    private final List<Handler> mHandlers = new ArrayList<Handler>();
    
    
    private SplitFboRenderer( FramebufferNode fbo,
                              Texture2dNode tex,
                              RenderSpace space,
                              SceneGraph graph )
    {
        mFbo = fbo;
        mTex = tex;
    
        mSpace    = space;
        mOffSpace = RenderSpaceBuilder.newUnionSpace(space.firstTile());
        mOffTile  = mOffSpace.firstTile();
        
        GraphPath<Object> path = graph.compilePath();
                
        mDrawHandler = new DrawHandler(RenderUtil.modulePathToNodePath(path, DrawNode.class, mOffTile)); 
        mDrawables   = new ArrayList<GLAutoDrawable>();
                
        for(RenderTile tile: space.tiles()) {
            {
                GLAutoDrawable draw = tile.drawable();
                draw.setAutoSwapBufferMode(false);
                mDrawables.add(draw);
            }
            
            
            if(tile.isFirst()) {
                mHandlers.add(new FullRenderHandler(tile, fbo, tex, mDrawHandler));
            }else{
                mHandlers.add(new BlitRenderHandler(tile, tex));
            }
            
            {
                GraphPath<InstallNode> p;
                p = RenderUtil.modulePathToNodePath(path, InstallNode.class, tile);
                p.retainActionType(GraphActionType.PUSH);
                if(!p.isEmpty()) {
                    mHandlers.add(new InstallHandler(tile, p.toTargetList()));
                }
            }
            
            if(!(tile.drawable() instanceof Component))
                continue;
            
            Component comp = (Component)tile.drawable();
            
            {
                GraphPath<MouseListener> p;
                p = RenderUtil.modulePathToNodePath(path, MouseListener.class, tile);
                p.retainActionType(GraphActionType.PUSH);
                if(!p.isEmpty()) {
                    mHandlers.add(new MouseHandler(comp, p.toTargetList()));
                }
            }
            
            {
                GraphPath<MouseMotionListener> p;
                p = RenderUtil.modulePathToNodePath(path, MouseMotionListener.class, tile);
                p.retainActionType(GraphActionType.PUSH);
                if(!p.isEmpty()) {
                    mHandlers.add(new MouseMotionHandler(comp, p.toTargetList()));
                }
            }
            
            {
                GraphPath<MouseWheelListener> p;
                p = RenderUtil.modulePathToNodePath(path, MouseWheelListener.class, tile);
                p.retainActionType(GraphActionType.PUSH);
                if(!p.isEmpty()) {
                    mHandlers.add(new MouseWheelHandler(comp, p.toTargetList()));
                }
            }
            
            {
                GraphPath<KeyListener> p;
                p = RenderUtil.modulePathToNodePath(path, KeyListener.class, tile);
                p.retainActionType(GraphActionType.PUSH);
                if(!p.isEmpty()) {
                    mHandlers.add(new KeyHandler(comp, p.toTargetList()));
                }
            }
        }
    }
    
    
    
    public void init() {
        for(Handler h: mHandlers) {
            h.init();
        }
        
        for(RenderTile t: mSpace.tiles()) {
            t.installOnscreen();
            t.setVisibleOnscreen(true);
            
            if(t.drawable() instanceof Component) {
                Component c = (Component)t.drawable();
                c.setFocusable(true);
                
                if(t.isFirst()) {
                    c.requestFocus();
                }
            }
        }
    }

    
    public void draw() {
        for(GLAutoDrawable d: mDrawables) {
            d.display();
        }
    }
    
    
    public void finish() {
        for(GLAutoDrawable d: mDrawables) {
            d.swapBuffers();
        }
    }
    
    
    public void dispose() {
        for(Handler h: mHandlers) {
            h.dispose();
        }
        
        for(RenderTile t: mSpace.tiles()) {
            t.dispose();
        }
    }
    
    
    
    private static final class DrawHandler implements DrawNode {
    
        private final GraphPath<DrawNode> mDrawList;
        
        DrawHandler(GraphPath<DrawNode> list) {
            mDrawList = list;
        }
        
        
        public void init(GLAutoDrawable gld) {
            for(GraphStep<DrawNode> s: mDrawList) {
                if(s.type() == GraphActionType.PUSH) {
                    s.target().init(gld);
                }
            }
        }
    
        
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {
            for(GraphStep<DrawNode> s: mDrawList) {
                if(s.type() == GraphActionType.PUSH) {
                    s.target().reshape(gld, x, y, w, h);
                }
            }
        }
        
        
        public void dispose(GLAutoDrawable gld) {
            for(GraphStep<DrawNode> s: mDrawList) {
                if(s.type() == GraphActionType.PUSH) {
                    s.target().dispose(gld);
                }
            }
        }

        
        public void pushDraw(GL gl) {
            for(GraphStep<DrawNode> s: mDrawList) {
                if(s.type() == GraphActionType.PUSH) {
                    DrawNode target = s.target();
                    target.pushDraw(gl);
                }else{
                    s.target().popDraw(gl);
                }
            }
        }

        
        public void popDraw(GL gl) {}
        
    }
    
    
    private static interface Handler {
        public void init();
        public void dispose();
    }
    
    
    private static final class FullRenderHandler implements GLEventListener, Handler {

        private final RenderTile mTile;
        private final FramebufferNode mFbo;
        private final TextureNode mTex;
        private final DrawNode mDrawHandler;
        
        private final int[] mViewport;
        private final DrawNode mViewNode;
        private final DrawNode mAlphaOff   = DrawNodes.newDisableNode(GL_ALPHA_TEST);
        private final DrawNode mStencilOff = DrawNodes.newDisableNode(GL_STENCIL_TEST);
        private final DrawNode mDepthOff   = DrawNodes.newDisableNode(GL_DEPTH_TEST);
        private final DrawNode mBlendOff   = DrawNodes.newDisableNode(GL_BLEND);
        private final DrawNode mCullOff    = DrawNodes.newDisableNode(GL_CULL_FACE);
        
        
        FullRenderHandler(RenderTile tile, FramebufferNode fbo, TextureNode tex, DrawNode draw) {
            mTile = tile;
            mFbo  = fbo;
            mTex  = tex;
            
            mDrawHandler = draw;
            
            LongRect b = tile.renderSpaceBounds();
            mViewport = new int[]{0, 0, (int)b.spanX(), (int)b.spanY()};
            mViewNode = new ViewportNode(mViewport[0], mViewport[1], mViewport[2], mViewport[3]);
        }
        
        
        public void init() {
            mTile.drawable().addGLEventListener(this);
        }
        
        public void dispose() {
            GLContext prev = GLContext.getCurrent();
            GLAutoDrawable gld = mTile.drawable();
            mDrawHandler.dispose(gld);
            if(prev != null) {
                prev.makeCurrent();
            }
            
            mTile.drawable().removeGLEventListener(this);
        }
        
        
        public void init(GLAutoDrawable gld) {
            final GL gl = gld.getGL();
            mDrawHandler.init(gld);
            mDrawHandler.reshape(gld, mViewport[0], mViewport[1], mViewport[2], mViewport[3]);
        }
        
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {}
        
        public void display(GLAutoDrawable gld) {
            final GL gl = gld.getGL();
 
            mFbo.pushDraw(gl);
            mViewNode.pushDraw(gl);
            mDrawHandler.pushDraw(gl);
            mViewNode.popDraw(gl);
            mFbo.popDraw(gl);
            
            gl.glMatrixMode(GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity();
            
            mAlphaOff.pushDraw(gl);  
            mStencilOff.pushDraw(gl);
            mDepthOff.pushDraw(gl);  
            mBlendOff.pushDraw(gl);  
            mCullOff.pushDraw(gl);
            
            LongRect b0 = mTile.renderSpaceBounds();
            LongRect b1 = mTile.tileBounds();
            
            float s0 = (b1.minX() - b0.minX()) / (float)(b0.spanX());
            float s1 = (b1.maxX() - b0.minX()) / (float)(b0.spanX());
            float t0 = (b1.minY() - b0.minY()) / (float)(b0.spanY());
            float t1 = (b1.maxY() - b0.minY()) / (float)(b0.spanY());
            
            mTex.pushDraw(gl);
            
            gl.glColor4f(1,1,1,1);
            gl.glBegin(GL_QUADS);
            gl.glTexCoord2f(s0,t0);
            gl.glVertex2i(-1,-1);
            gl.glTexCoord2f(s1,t0);
            gl.glVertex2i(1,-1);
            gl.glTexCoord2f(s1,t1);
            gl.glVertex2i(1,1);
            gl.glTexCoord2f(s0,t1);
            gl.glVertex2i(-1,1);
            gl.glEnd();
            
            mTex.popDraw(gl);
            
            mCullOff.popDraw(gl);
            mBlendOff.popDraw(gl);
            mDepthOff.popDraw(gl);
            mStencilOff.popDraw(gl);
            mAlphaOff.popDraw(gl);            
        }        
        
        public void displayChanged(GLAutoDrawable gld, boolean modeChanged, boolean displayChanged) {}

    }
    
    
    private static final class BlitRenderHandler implements GLEventListener, Handler {
        
        private final RenderTile mTile;
        private final TextureNode mTex;
        
        
        BlitRenderHandler(RenderTile tile, TextureNode tex) {
            mTile = tile;
            mTex  = tex;
        }
        
        
        public void init() {
            mTile.drawable().addGLEventListener(this);
        }
        
        public void dispose() {
            mTile.drawable().removeGLEventListener(this);
        }
        
        
        public void init(GLAutoDrawable gld) {
            GL gl = gld.getGL();
            gl.glDisable(GL_ALPHA_TEST);
            gl.glDisable(GL_STENCIL_TEST);
            gl.glDisable(GL_DEPTH_TEST);
            gl.glDisable(GL_BLEND);
        }
        
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {}
        
        public void display(GLAutoDrawable gld) {
            GL gl = gld.getGL();
            
            LongRect b0 = mTile.renderSpaceBounds();
            LongRect b1 = mTile.tileBounds();
            
            float s0 = (b1.minX() - b0.minX()) / (float)(b0.spanX());
            float s1 = (b1.maxX() - b0.minX()) / (float)(b0.spanX());
            float t0 = (b1.minY() - b0.minY()) / (float)(b0.spanY());
            float t1 = (b1.maxY() - b0.minY()) / (float)(b0.spanY());
            
            mTex.pushDraw(gl);
            
            gl.glColor4f(1,1,1,1);
            gl.glBegin(GL_QUADS);
            gl.glTexCoord2f(s0,t0);
            gl.glVertex2i(-1,-1);
            gl.glTexCoord2f(s1,t0);
            gl.glVertex2i(1,-1);
            gl.glTexCoord2f(s1,t1);
            gl.glVertex2i(1,1);
            gl.glTexCoord2f(s0,t1);
            gl.glVertex2i(-1,1);
            gl.glEnd();
            
            mTex.popDraw(gl);
        }
        
        public void displayChanged(GLAutoDrawable gld, boolean modeChanged, boolean displayChanged) {}

    }
    
    
    private static final class InstallHandler implements Handler{
        
        private final RenderTile mTile;
        private final List<InstallNode> mInstallList;
        
        InstallHandler(RenderTile tile, List<InstallNode> installList) {
            mTile        = tile;
            mInstallList = installList;
        }
        

        public void init() {
            for(InstallNode m: mInstallList)
                m.install(mTile);
        }

        public void dispose() {
            ListIterator<InstallNode> iter = mInstallList.listIterator(mInstallList.size());
            
            while(iter.hasPrevious()) {
                iter.previous().uninstall(mTile);
            }
        }

    }

    
    private static final class MouseHandler implements MouseListener, Handler {

        private final Component mComponent;
        private final List<MouseListener> mMouseList;
        
        
        MouseHandler(Component comp, List<MouseListener> list) {
            mComponent = comp;
            mMouseList = list;
        }
        
        
        public void init() {
            mComponent.addMouseListener(this);
        }
        
        public void dispose() {
            mComponent.removeMouseListener(this);
        }
        
        public void mousePressed(MouseEvent e) {
            for(MouseListener m: mMouseList) {
                m.mousePressed(e);
                if(e.isConsumed())
                    return;
            }
        }

        public void mouseReleased(MouseEvent e) {
            for(MouseListener m: mMouseList) {
                m.mouseReleased(e);
                if(e.isConsumed())
                    return;
            }
        }
        
        public void mouseClicked(MouseEvent e) {
            for(MouseListener m: mMouseList) {
                m.mouseClicked(e);
                if(e.isConsumed())
                    return;
            }
        }

        public void mouseEntered(MouseEvent e) {
            for(MouseListener m: mMouseList) {
                m.mouseEntered(e);
                if(e.isConsumed())
                    return;
            }
        }

        public void mouseExited(MouseEvent e) {
            for(MouseListener m: mMouseList) {
                m.mouseExited(e);
                if(e.isConsumed())
                    return;
            }
        }
        
    }
    
    
    private static final class MouseMotionHandler implements MouseMotionListener, Handler {
    
        private final Component mComponent;
        private final List<MouseMotionListener> mMouseMotionList;
        
        
        MouseMotionHandler(Component comp, List<MouseMotionListener> list) {
            mComponent = comp;
            mMouseMotionList = list;
        }
        
        
        
        public void init() {
            mComponent.addMouseMotionListener(this);
        }
        
        public void dispose() {
            mComponent.removeMouseMotionListener(this);
        }
        
        public void mouseMoved(MouseEvent e) {
            for(MouseMotionListener m: mMouseMotionList) {
                m.mouseMoved(e);
                if(e.isConsumed())
                    return;
            }
        }
        
        public void mouseDragged(MouseEvent e) {
            for(MouseMotionListener m: mMouseMotionList) {
                m.mouseDragged(e);
                if(e.isConsumed())
                    return;
            }
        }
        
    }


    private static final class MouseWheelHandler implements MouseWheelListener, Handler {

        private final Component mComponent;
        private final List<MouseWheelListener> mMouseWheelList;
        
        
        MouseWheelHandler(Component comp, List<MouseWheelListener> list) {
            mComponent = comp;
            mMouseWheelList = list;
        }
        
        
        
        public void init() {
            mComponent.addMouseWheelListener(this);
        }
        
        public void dispose() {
            mComponent.removeMouseWheelListener(this);
        }
        
        public void mouseWheelMoved(MouseWheelEvent e) {
            for(MouseWheelListener m: mMouseWheelList) {
                m.mouseWheelMoved(e);
                if(e.isConsumed())
                    return;
            }
        }
        
    }
    
    
    private static final class KeyHandler implements KeyListener, Handler {

        private final Component mComponent;
        private final List<KeyListener> mKeyboardList;
        
        
        KeyHandler(Component comp, List<KeyListener> list) {
            mComponent = comp;
            mKeyboardList = list;
        }
        
        
        public void init() {
            mComponent.addKeyListener(this);
        }
        
        public void dispose() {
            mComponent.removeKeyListener(this);
        }
        
        public void keyPressed(KeyEvent e) {
            for(KeyListener k: mKeyboardList) {
                k.keyPressed(e);
                if(e.isConsumed())
                    return;
            }
        }

        public void keyReleased(KeyEvent e) {
            for(KeyListener k: mKeyboardList) {
                k.keyReleased(e);
                if(e.isConsumed())
                    return;
            }
        }

        public void keyTyped(KeyEvent e) {
            for(KeyListener k: mKeyboardList) {
                k.keyTyped(e);
                if(e.isConsumed())
                    return;
            }
        }
        
    }
    
}
