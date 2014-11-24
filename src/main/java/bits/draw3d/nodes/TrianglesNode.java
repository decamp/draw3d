/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import java.util.List;
import static javax.media.opengl.GL.*;

import bits.draw3d.*;
import bits.draw3d.model.DrawTri;
import bits.draw3d.model.DrawVert;
import bits.draw3d.shaders.BasicShaders;


/**
 * Renders a given model.
 * 
 * @author Philip DeCamp
 */
@Deprecated
public class TrianglesNode implements DrawUnit {

    public static TrianglesNode create( ShaderManager shaderMan,
                                        List<DrawTri> trianglesRef,
                                        boolean color,
                                        boolean norm,
                                        boolean tex )
    {
        BoProgram<DrawVert,DrawTri> program;
        program = BasicShaders.createTriProgram( shaderMan, false, false, tex ? 2 : 0, norm, color );
        int vertNum = trianglesRef.size() * 3;
        DrawGroup<DrawVert,DrawTri> group = DrawGroup.create( program,
                                                              GL_STATIC_DRAW,
                                                              vertNum,
                                                              GL_STATIC_DRAW,
                                                              vertNum );
        for( DrawTri t: trianglesRef ) {
            for( DrawVert v: t.mVerts ) {
                group.vertList().add( v );
            }
            group.elemList().add( t );
        }
        group.drawOnBind( true, GL_TRIANGLES );

        return new TrianglesNode( trianglesRef, group );
    }


    private final List<DrawTri> mTriangles;
    private final DrawGroup<DrawVert,DrawTri> mGroup;


    public TrianglesNode( List<DrawTri> triangleRef, DrawGroup<DrawVert,DrawTri> group ) {
        mTriangles = triangleRef;
        mGroup     = group;
    }



    public List<DrawTri> trianglesRef() {
        return mTriangles;
    }

    @Override
    public void init( DrawEnv d ) {
        mGroup.init( d );
    }

    @Override
    public void dispose( DrawEnv d ) {
        mGroup.dispose( d );
    }

    @Override
    public void bind( DrawEnv d ) {
        mGroup.bind( d );
    }

    @Override
    public void unbind( DrawEnv d ) {
        mGroup.unbind( d );
    }

}
