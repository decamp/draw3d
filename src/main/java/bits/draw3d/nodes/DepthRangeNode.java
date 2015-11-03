/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawUnitAdapter;

import com.jogamp.opengl.GL;


/**
 * Configures the depth range as stored in the z-buffer. Normally, after
 * performing the <i>projection transform</i> and mapping all geometry to
 * normalized device coordinates, OpenGL performs <i>depth mapping</i> to map
 * the z-value of each fragment from (-1,1) to the range (0,1) before performing
 * the depth test. This class alters the depth range to simulate a clipping
 * plane separate from the near plane, without affecting the z-order of the
 * geometry. For this class to work, the projection matrix MUST be modified
 * accordingly. When this action is pushed, the nearplane of the projection
 * transform should be apply to clipDepth. Rendering will then operate AS IF the
 * near-plane of the projection is at nearDepth, but fragments will be clipped
 * at clipDepth.
 * <p>
 * What we want to do is render some geometry with a cross-section effect, where
 * the cross-section is achieved by clipping fragements based on depth, and then
 * we want to render additional geometry into the same space without the
 * clipping. The problem here is that the only way to perform depth-based
 * clipping is to use the near-plane of the projection transform , which means
 * that the clipping plane MUST be mapped by the projection transform onto the
 * near-plane which MUST have depth = -1. Therefore, the only way to render
 * additional geometry in front of the clipping plane is to temporarily alter
 * the depth buffer range, such that the nearplane gets mapped to something
 * greater than 0 in the depth buffer. The math here is kind a pain in the ass,
 * as seen in the constructor, but it essentially computes the correct z-buffer
 * depth that correspond with an arbitrary clipping plane, so that the altered
 * projection transform will be compatible with existing geometry.
 * 
 * @author decamp
 */
public class DepthRangeNode extends DrawUnitAdapter {

    private final double mClipZ;


    public DepthRangeNode( double nearDepth, double farDepth, double clipDepth ) {
        double cn = -(farDepth + nearDepth) / (farDepth - nearDepth);
        double cs = -(farDepth + clipDepth) / (farDepth - clipDepth);
        double dn = -2.0 * farDepth * nearDepth / (farDepth - nearDepth);
        double ds = -2.0 * farDepth * clipDepth / (farDepth - clipDepth);
        double t  = -(cn - dn * cs / ds);
        mClipZ = t;
    }

    @Override
    public void bind( DrawEnv env ) {
        env.mGl.glDepthRange( mClipZ, 1.0 );
    }

    @Override
    public void unbind( DrawEnv env ) {
        env.mGl.glDepthRange( 0.0, 1.0 );
    }

}