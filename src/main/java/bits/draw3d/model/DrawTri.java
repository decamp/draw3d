package bits.draw3d.model;


/**
 * @author Philip DeCamp
 */
public class DrawTri {

    public final DrawVert[] mVerts = new DrawVert[3];

    /**
     * Position in IBO.
     */
    public int mIboPos = -1;


    public DrawTri() {}

    public DrawTri( DrawVert a, DrawVert b, DrawVert c ) {
        mVerts[0] = a;
        mVerts[1] = b;
        mVerts[2] = c;
    }

    @Override
    public String toString() {
        return String.format( "DrawTri< %s, %s, %s >", mVerts[0], mVerts[1], mVerts[2] );
    }

}
