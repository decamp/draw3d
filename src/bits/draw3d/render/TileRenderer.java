package bits.draw3d.render;

import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;

import bits.draw3d.context.RenderTile;
import bits.draw3d.nodes.*;
import bits.draw3d.scene.*;



/**
 * Not thread-safe.
 * 
 * @author decamp
 */
@SuppressWarnings( "deprecation" )
public class TileRenderer implements Renderer {

    
    public static TileRenderer newInstance(RenderTile tile, SceneGraph graph) {
        TileRenderer ret = new TileRenderer(tile, graph);
        return ret;
    }
    
    
    
    private final RenderTile mTile;
    private final GLAutoDrawable mDrawable;
    private final Component mComponent;
    
    private final GraphPath<DrawNode> mDrawList;
    private final List<InstallNode> mInstallList;
    private final List<MouseListener> mMouseList;
    private final List<MouseMotionListener> mMouseMotionList;
    private final List<MouseWheelListener> mMouseWheelList;
    private final List<KeyListener> mKeyboardList;
    
    private final GlHandler mGlHandler;
    private final InstallNode mInstallHandler;
    private final DrawNode mDrawHandler;
    private final MouseListener mMouseHandler;
    private final MouseMotionListener mMouseMotionHandler;
    private final MouseWheelListener mMouseWheelHandler;
    private final KeyListener mKeyHandler;
    
    
    TileRenderer(RenderTile tile, SceneGraph graph) {
        mTile = tile;
        mDrawable = mTile.drawable();
        mDrawable.setAutoSwapBufferMode(false);
        
        GraphPath<Object> path = graph.compilePath();
                
        if(mTile.drawable() instanceof Component) {
            mComponent = (Component)mTile.drawable();
        }else{
            mComponent = null;
        }

        mGlHandler = new GlHandler();
        
        {
            GraphPath<InstallNode> p = RenderUtil.modulePathToNodePath(path, InstallNode.class, mTile);
            p.retainActionType(GraphActionType.PUSH);
            mInstallList = p.toTargetList();
            mInstallHandler = new InstallHandler();
        }
        
        mDrawList = RenderUtil.modulePathToNodePath(path, DrawNode.class, mTile);
        mDrawHandler = new DrawHandler();
        
        
        if(mComponent == null) {
            mMouseList = null;
            mMouseHandler = new MouseAdapter() {};
        }else{
            GraphPath<MouseListener> p = RenderUtil.modulePathToNodePath(path, MouseListener.class, mTile);
            p.retainActionType(GraphActionType.PUSH);
            mMouseList = p.toTargetList();
            mMouseHandler = new MouseHandler();
        }
         
        if(mComponent == null) {
            mMouseMotionList = null;
            mMouseMotionHandler = new MouseMotionAdapter() {};
        }else{
            GraphPath<MouseMotionListener> p = RenderUtil.modulePathToNodePath(path, MouseMotionListener.class, mTile);
            p.retainActionType(GraphActionType.PUSH);
            mMouseMotionList = p.toTargetList();
            mMouseMotionHandler = new MouseMotionHandler();
        }
        
        if(mComponent == null) {
            mMouseWheelList = null;
            mMouseWheelHandler = new MouseAdapter() {};
        }else{
            GraphPath<MouseWheelListener> p = RenderUtil.modulePathToNodePath(path, MouseWheelListener.class, mTile);
            mMouseWheelList = p.toTargetList();
            mMouseWheelHandler = new MouseWheelHandler();
        }
        
        if(mComponent == null) {
            mKeyboardList = null;
            mKeyHandler = new KeyAdapter() {};
        }else{
            GraphPath<KeyListener> p = RenderUtil.modulePathToNodePath(path, KeyListener.class, mTile);
            p.retainActionType(GraphActionType.PUSH);
            mKeyboardList = p.toTargetList();
            mKeyHandler = new KeyHandler();
        }
        
    }
    
    
    
    public RenderTile tile() {
        return mTile;
    }
    
    
    public void init() {
        mInstallHandler.install(mTile);
        
        if(mComponent != null) {
            mComponent.addMouseListener(mMouseHandler);
            mComponent.addMouseMotionListener(mMouseMotionHandler);
            mComponent.addMouseWheelListener(mMouseWheelHandler);
            mComponent.addKeyListener(mKeyHandler);
        }
        
        mTile.drawable().addGLEventListener(mGlHandler);
        
        if(!mTile.isOffscreen()) {
            mTile.installOnscreen();
            mTile.setVisibleOnscreen(true);
            
            if(mTile.drawable() instanceof Component) {
                Component c = (Component)mTile.drawable();
                c.setFocusable(true);
                if(mTile.isFirst()) {
                    c.requestFocus();
                }
            }
        }
    }
    
    
    public void draw() {
        mDrawable.display();
    }
    
    
    public void finish() {
        mDrawable.swapBuffers();
    }
    
    
    public void dispose() {
        if(mComponent != null) {
            mComponent.removeMouseListener(mMouseHandler);
            mComponent.removeMouseMotionListener(mMouseMotionHandler);
            mComponent.removeKeyListener(mKeyHandler);
        }
        
        GLContext prev = GLContext.getCurrent();
        mDrawable.getContext().makeCurrent();
        mDrawHandler.dispose(mDrawable);
        if(prev != null) {
            prev.makeCurrent();
        }
        
        mInstallHandler.uninstall(mTile);
        mTile.dispose();
    }
    
    
    
    private final class InstallHandler implements InstallNode {

        public void install(RenderTile tile) {
            for(InstallNode m: mInstallList)
                m.install(tile);
        }

        public void uninstall(RenderTile tile) {
            ListIterator<InstallNode> iter = mInstallList.listIterator(mInstallList.size());
            
            while(iter.hasPrevious()) {
                iter.previous().uninstall(tile);
            }
        }

    }
    
    
    private final class DrawHandler implements DrawNode {
        
        
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

    
    private final class MouseHandler implements MouseListener {

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
    

    private final class MouseMotionHandler implements MouseMotionListener {
        
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


    private final class MouseWheelHandler implements MouseWheelListener {

        public void mouseWheelMoved(MouseWheelEvent e) {
            for(MouseWheelListener m: mMouseWheelList) {
                m.mouseWheelMoved(e);
                if(e.isConsumed())
                    return;
            }
        }
        
    }
    
    
    private final class KeyHandler implements KeyListener {

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
    

    private final class GlHandler implements GLEventListener {

        
        public void init(GLAutoDrawable gld) {
            final GL gl = gld.getGL();
            mDrawHandler.init(gld);
            gl.glFlush();
        }
        
        
        public void reshape(GLAutoDrawable gld, int x, int y, int w, int h) {
            final GL gl = gld.getGL();
            mDrawHandler.reshape(gld, x, y, w, h);
            gl.glFlush();
        }

        
        public void display(GLAutoDrawable gld) {
            final GL gl = gld.getGL();
            mDrawHandler.pushDraw(gl);
            gl.glFlush();
        }
        
        
        public void displayChanged(GLAutoDrawable gld, boolean modeChanged, boolean displayChanged) {}

    }

}
