/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d;


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

        public int arrayStride( MemberType type, int arrayLen ) {
            return type.componentType().bytes() * type.rows() * type.cols() * arrayLen;
        }

        public int matrixStride( MemberType type ) {
            return type.cols() == 1 ? 0 : size( type.componentType(), type.rows() );
        }

    },

    /**
     * The standard implementation independent layout. All items are aligned to a
     * multiple-of-4 of the primitive component. For example, a float, vec2, vec3
     * and vec4 will all consume the same amount of space.
     *
     * <p>Suitable for sharing member blocks. However, this alignment may be
     * inefficient for vertex attributes.
     */
    STD140  {
        public int size( ComponentType type, int count ) {
            return type.bytes() * (align( count, 4 ));
        }

        public int arrayStride( MemberType type, int arrayLen ) {
            if( type.rows() == 1 ) {
                return size( type.componentType(), arrayLen );
            } else {
                return arrayLen * size( type.componentType(), 4 );
            }
        }

        public int matrixStride( MemberType type ) {
            return type.cols() == 1 ? 0 : size( type.componentType(), type.rows() );
        }
    },

    /**
     * The recommended attribute layout for IOS. It's probably good practice
     * to use it on other platforms as well.
     *
     * If you happen to be using 8-byte doubles, this alignment will actually
     * align to 8-bytes, not 4.
     */
    BYTE4   {
        public int size( ComponentType type, int count ) {
            int bytes = type.bytes() * count;
            return align( bytes, Math.max( type.bytes(), 4 ) );
        }

        public int arrayStride( MemberType type, int count ) {
            if( type.rows() == 1 ) {
                return size( type.componentType(), count );
            } else {
                return count * size( type.componentType(), type.rows() * type.cols() );
            }
        }

        public int matrixStride( MemberType type ) {
            return type.cols() == 1 ? 0 : size( type.componentType(), type.rows() );
        }

    };

    /**
     * @return bytes required to hold an array of ComponentType. Similar to {@link #arrayStride} for single components.
     */
    public abstract int size( ComponentType type, int count );

    /**
     * @param type     Type of buffer object member.
     * @param arrayLen Length of array, or 1 if not an array.
     * @return machine units (bytes) between elements of an array of MemberType {@code type}.
     */
    public abstract int arrayStride( MemberType type, int arrayLen );

    /**
     * @return machine units (bytes) between columns in a column-major matrix.
     */
    public abstract int matrixStride( MemberType type );


    public static int align( int size, int alignment ) {
        return size + (alignment - (size % alignment)) % alignment;
    }

}

