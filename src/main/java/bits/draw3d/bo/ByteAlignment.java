package bits.draw3d.bo;

import bits.draw3d.ComponentType;


/**
 * Ways of packing components into byte buffers. Different methods are used for efficiency.
 *
 *
 * @author Philip DeCamp
 */
public enum ByteAlignment {

    /**
     * Does not provide any padding.
     */
    NONE {
        public int size( ComponentType type, int count ) {
            return count * type.bytes();
        }
    },

    /**
     * The standard implementation independent layout. All items are aligned to a
     * multiple-of-4 of the primitive component. For example, a float, vec2, vec3
     * and vec4 will all consume the same amount of space.
     *
     * <p>Suitable for sharing uniform blocks. However, this alignment may be
     * inefficient for vertex attributes.
     */
    STD140
            {
                public int size( ComponentType type, int count ) {
                    return type.bytes() * (align( count, 4 ));
                }
            },

    /**
     * The recommended attribute layout for IOS. It's probably good practice
     * to use it on other platforms as well.
     *
     * If you happen to be using 8-byte doubles, this alignment will actually
     * align to 8-bytes, not 4.
     */
    BYTE4
            {
                public int size( ComponentType type, int count ) {
                    int bytes = type.bytes() * count;
                    return align( bytes, Math.max( type.bytes(), 4 ) );
                }
            };


    public abstract int size( ComponentType type, int count );


    public static int align( int size, int alignment ) {
        return size + (alignment - (size % alignment)) % alignment;
    }

}
