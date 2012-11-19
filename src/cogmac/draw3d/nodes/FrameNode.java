//package cogmac.draw3d.nodes;
//
//import javax.media.opengl.*;
//
///**
// * Interface for objects that act as elements of frame buffers.
// * Namely, textures and RenderBufferObjects (RBOs).  
// * <p>
// * FrameNode objects implement the DrawNode interface and may
// * be used as a draw node if desired.  When used as a DrawNode,
// * the primary pushDraw() behavior binds the FrameNode to the 
// * current Framebuffer, and the primary popDraw() behavior
// * reverts the binding to the previous state.
// * 
// * @author decamp
// */
//public interface FrameNode extends DrawNode {
//
//    /**
//     * @return Examples: GL_TEXTURE_2D, GL_TEXTURE_3D, GL_RENDERBUFFER, etc.
//     */
//    public int target();
//    
//    /**
//     * @return the current ID of this object, or 0 if not initialized.
//     */
//    public int id();
//    
//    /**
//     * Internal format value used for glTexImage* commands.  
//     * 
//     * @return Examples: GL_RGBA, GL_DEPTH_COMPONENT16, etc.
//     */
//    public int internalFormat();
//        
//    /**
//     * Sets the size of the FrameNode buffer.  By default,
//     * the size is -1,-1, meaning that the FrameNode does
//     * not attempt to allocate storage for itself.  Changing
//     * this value causes this FRameNode to allocate storage on
//     * the next pushDraw(), bind(), or init() event.
//     *  
//     * @param w  Width of buffer, in pixels.
//     * @param h  Height of buffer, in pixels.
//     */
//    public void size(int w, int h);
//    
//    /**
//     * @return currently defined width of underlying buffer, or -1 if undefined.
//     */
//    public int width();
//    
//    /**
//     * @return currently defined height of underlying buffer, or -1 if undefined. 
//     */
//    public int height();
//    
//    /**
//     * @return true if this FrameNode has a defined size; <tt>width() >= 0 && height() >= 0</tt>.
//     */
//    public boolean hasSize(); 
//    
//    /**
//     * Specifies whether the FrameNode should automatically allocate storage 
//     * to match the size of its context.  If enabled, setSize(w,h) will
//     * be called on each call to reshape(gld, x, y, w, h).
//     * <p>  
//     * Default is <tt>false</tt>.
//     * 
//     * @param resizeOnReshape
//     */
//    public void resizeOnReshape(boolean resizeOnReshape);
//
//    /**
//     * @return true iff resizeOnReshape is enabled.
//     * @see #resizeOnReshape
//     */
//    public boolean resizeOnReshape();
//    
//    /**
//     * (Optional method).  Sets the depth (number of image layers) for this FrameNode.
//     * Only matters for 3D textures.  Calling this method may cause texture to 
//     * reallocate storage.
//     * 
//     * @param layers
//     */
//    public void depth(int depth);
//
//    /**
//     * @return depth of this FrameNode.  Unless TEXTURE_3D, probably 1.
//     */
//    public int depth();
//    
//    /**
//     * @param key  Key of texture param 
//     * @return currently specified value of that texture param, or <tt>null</tt> if not defined.
//     */
//    public Integer param( int key );
//    
//    /**
//     * Sets parameter textures, ala glTexParameter.
//     * 
//     * @param key
//     * @param value
//     */
//    public void param( int key, int value );
//
//    /**
//     * Sets the format of the TextureNode.  Calling this method may cause the
//     * texture to reallocate storage if it has a defined size.  
//     * 
//     * @param intFormat  Same as "internalFormat" param used in glTexImage* commands.
//     * @param format     Same as "format" param used in glTexImage* commands.
//     * @param dataType   Same as "dataType" param used in glTexImage* commands.
//     */
//    public void format( int internalFormat, int format, int dataType );
//    
//    
//    /**
//     * Initializes the FrameNode.  SHOULD be called automatically as 
//     * necessary by <tt>bind()</tt>.  <tt>init()</tt> has the following behavior:<ul>
//     * <li>If this FrameNode has no id, generates an id.</li>
//     * <li>If this FrameNode has a defined size, allocates a buffer using the current format.</li>
//     * </ul>
//     * 
//     * @param gl
//     * @see DrawNode#init(GLAutoDrawable gld)
//     */
//    public void init(GL gl);
//    
//    /**
//     * Binds this FrameNode to it's designated target.
//     * 
//     * @param gl
//     */
//    public void bind(GL gl);
//
//    /**
//     * Binds this FrameNode's designated target to 0.
//     * 
//     * @param gl
//     */
//    public void unbind(GL gl);
//    
//    /**
//     * Disposes this FrameNode's resources.
//     *  
//     * @param gl
//     */
//    public void dispose(GL gl);
//    
//    
//    /**
//     * Equivalent to <tt>init( gld.getGL() )</tt>
//     * 
//     * @param gl 
//     */
//    public void init( GLAutoDrawable gld );
//
//    /**
//     * If autoResizeOnReshape(), produces a call to <tt>size(w, h)</tt>.
//     */
//    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h );
//    
//    /**
//     * Equivalent to <tt>dispose( gld.getGL() )</tt>
//     * 
//     * @param gl
//     */
//    public void dispose( GLAutoDrawable gld );
//    
//    /**
//     * Causes this FrameNode to store the currently bound object 
//     * for its target, then call <tt>bind(gl)</tt>.
//     * 
//     * @param gl
//     */
//    public void pushDraw( GL gl );
//    
//    /**
//     * Causes this FrameNode to bind whatever object was previously
//     * stored in its target during the previous call pushDraw(gl).
//     * NOT equivalent to <tt>unbind(gl)<tt>, which always binds 0.
//     * 
//     * @param gl
//     */
//    public void popDraw( GL gl );
//    
//}
