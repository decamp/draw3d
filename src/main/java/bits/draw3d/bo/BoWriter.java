package bits.draw3d.bo;

import java.nio.ByteBuffer;


/**
 * Buffer Object Serializer. Places Java objects into ByteBuffers for rendering.
 *
 * @author Philip DeCamp
 */
public interface BoWriter<T> {
    /**
     * @return class of item type supported by this writer.
     */
    Class<T> itemClass();

    /**
     * Indicates type of BufferObject this BoWriter is for.
     * @return GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER, GL_UNIFORM_BUFFER, or 0 if other.
     */
    int boType();

    /**
     * Indicates the size of each element to be written, including padding.
     *
     * @return For GL_ARRAY_BUFFER type, the number of bytes per vertex. <br>
     *         For GL_ELEMENT_ARARY_BUFFER type, the number of bytes per element. <br>
     *         For GL_UNIFORM_BUFFER, may be -1 to indicate element size is not constant. <br>
     */
    int bytesPerElem();

    /**
     * The number of elements needed in the Buffer Object to hold each item.
     * Not all items will require the same number of bytes, in which case this method must
     * return -1.
     *
     * @return number of bytes per item, or -1 if not constant.
     */
    int elemsPerItem();

    /**
     * Number of bytes needed for a specific item.
     * If {@code #elemsPerItem()} returns a value other than -1, this method MUST return
     * the same value, regardles of arguments.
     *
     * @param item Item to be serialized into a Bo.
     * @return Number of elements required to store {@code item}
     */
    int elemNum( T item );

    /**
     * Valid for GL_ARRAY_BUFFER BoWriters only.
     *
     * @param out VAO object to receive attributes.
     */
    void attributes( Vao out );

    /**
     * MUST be called before {@link #write}. {@code markAdd} is called before serialization both
     * to compute buffer layout and to prepare and index the item.
     *
     * @param item   Item that will be serialized.
     * @param pos    The element position in the BO into which the object will be inserted.
     *               Multiply by {@code #bytesPerElem()} for byte poistion.
     * @return       Number of elements to be added, or -1 if item may not be added. This value MUST
     *               be equal to or less than what is returned by {@code elemNum( item )}.
     */
    int markAdd( T item, int pos );

    /**
     * @param item item to serializer
     * @param bo   Buffer to receive vertices.
     */
    void write( T item, ByteBuffer bo );

    /**
     * Must be called when item no longer being used.
     *
     * @param item  Item to be removed from BOs.
     * @return      Number of elements removed, or -1 if item may not be removed.
     *
     */
    int markRemove( T item );
}
