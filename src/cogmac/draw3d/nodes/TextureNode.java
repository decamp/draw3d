package cogmac.draw3d.nodes;

import javax.media.opengl.*;

/**
 * Interface for objects that act as textures, FBOs, or RBOs.
 * <p>
 * The TextureNode interface provides calls for using the
 * object directly as a texture, FBO, or RBO. Additionally,
 * it extends DrawNode and may be used directly within a
 * scene graph if desired. When used as a DrawNode, the
 * primary pushDraw() behavior binds the TextureNode to the 
 * current Framebuffer, and the primary popDraw() behavior
 * reverts the binding to the previous state.
 * 
 * @author decamp
 */
public interface TextureNode extends DrawNode {

    /**
     * @return Examples: GL_TEXTURE_2D, GL_TEXTURE_3D, GL_RENDERBUFFER, etc.
     */
    public int target();
    
    /**
     * @return the current ID of this object, or 0 if not initialized.
     */
    public int id();
    
    /**
     * Internal format value used for glTexImage* commands.  
     * 
     * @return Examples: GL_RGBA, GL_DEPTH_COMPONENT16, etc.
     */
    public int internalFormat();
    
    /**
     * @return format of samples in main memory.
     */
    public int format();
    
    /**
     * @return datatype of components in main memory.
     */
    public int dataType();
    
    /**
     * Sets the size of the TextureNode buffer.  By default,
     * the size is -1,-1, meaning that the TextureNode does
     * not attempt to allocate storage for itself.  Changing
     * this value causes this TextureNode to allocate storage on
     * the next pushDraw(), bind(), or init() event.
     *  
     * @param w  Width of buffer, in pixels.
     * @param h  Height of buffer, in pixels.
     */
    public void size( int w, int h );
    
    /**
     * @return currently defined width of underlying buffer, or -1 if undefined.
     */
    public int width();
    
    /**
     * @return currently defined height of underlying buffer, or -1 if undefined. 
     */
    public int height();
    
    /**
     * @return true if this TextureNode has a defined size; <tt>width() >= 0 && height() >= 0</tt>.
     */
    public boolean hasSize(); 
    
    /**
     * Specifies whether the TextureNode should automatically allocate storage 
     * to match the size of its context.  If enabled, setSize(w,h) will
     * be called on each call to reshape(gld, x, y, w, h).
     * <p>  
     * Default is <tt>false</tt>.
     * 
     * @param resizeOnReshape
     */
    public void resizeOnReshape( boolean resizeOnReshape );

    /**
     * @return true iff resizeOnReshape is enabled.
     * @see #resizeOnReshape
     */
    public boolean resizeOnReshape();
    
    /**
     * (Optional method).  Sets the depth (number of image layers) for this TextureNode.
     * Only matters for 3D textures.  Calling this method may cause texture to 
     * reallocate storage.
     * 
     * @param layers
     */
    public void depth( int depth );

    /**
     * @return depth of this TextureNode.  Unless TEXTURE_3D, probably 1.
     */
    public int depth();
    
    /**
     * @param key  Key of texture param 
     * @return currently specified value of that texture param, or <tt>null</tt> if not defined.
     */
    public Integer param( int key );
    
    /**
     * Sets parameter textures, ala glTexParameter.
     * 
     * @param key
     * @param value
     */
    public void param( int key, int value );

    /**
     * Sets the format of the TextureNode.  Calling this method may cause the
     * texture to reallocate storage if it has a defined size.  
     * 
     * @param intFormat  Same as "internalFormat" param used in glTexImage* commands.
     * @param format     Same as "format" param used in glTexImage* commands.
     * @param dataType   Same as "dataType" param used in glTexImage* commands.
     */
    public void format( int internalFormat, int format, int dataType );
    
    
    /**
     * Initializes the TextureNode.  SHOULD be called automatically as 
     * necessary by <tt>bind()</tt>.  <tt>init()</tt> has the following behavior:<ul>
     * <li>If this TextureNode has no id, generates an id.</li>
     * <li>If this TextureNode has a defined size, allocates a buffer using the current format.</li>
     * </ul>
     * 
     * @param gl
     * @see DrawNode#init(GLAutoDrawable gld)
     */
    public void init( GL gl );
    
    /**
     * Binds this TextureNode to it's designated target.
     * 
     * @param gl
     */
    public void bind( GL gl );

    /**
     * Binds this TextureNode's designated target to 0.
     * 
     * @param gl
     */
    public void unbind( GL gl );
    
    /**
     * Disposes this TextureNode's resources.
     *  
     * @param gl
     */
    public void dispose( GL gl );
    
    
    /**
     * Equivalent to <tt>init( gld.getGL() )</tt>
     * 
     * @param gl 
     */
    public void init( GLAutoDrawable gld );

    /**
     * If autoResizeOnReshape(), produces a call to <tt>size(w, h)</tt>.
     */
    public void reshape( GLAutoDrawable gld, int x, int y, int w, int h );
    
    /**
     * Equivalent to <tt>dispose( gld.getGL() )</tt>
     * 
     * @param gl
     */
    public void dispose( GLAutoDrawable gld );
    
    /**
     * Causes this TextureNode to store the currently bound object 
     * for its target, then call <tt>bind(gl)</tt>.
     * 
     * @param gl
     */
    public void pushDraw( GL gl );
    
    /**
     * Causes this TextureNode to bind whatever object was previously
     * stored in its target during the previous call pushDraw(gl).
     * NOT equivalent to <tt>unbind(gl)<tt>, which always binds 0.
     * 
     * @param gl
     */
    public void popDraw( GL gl );
    
}
