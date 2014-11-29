/*
* Copyright (c) 2014. Massachusetts Institute of Technology
* Released under the BSD 2-Clause License
* http://opensource.org/licenses/BSD-2-Clause
*/

package bits.draw3d.nodes;

import bits.draw3d.*;
import bits.draw3d.lighting.Material;


/**
* @author Philip DeCamp
*/
public class MaterialNode extends DrawUnitAdapter implements DrawNode {

    private UboBlock mFrontBlock;
    private UboBlock mBackBlock;

    private Material mFront;
    private Material mBack;


    public MaterialNode() {}


    public MaterialNode( Material front, Material back ) {
        mFront = front;
        mBack = back;
    }


    @Override
    public void bind( DrawEnv d ) {
        //d.mMaterials.apply( mFront, mBack );
    }

    @Override
    public void unbind( DrawEnv d ) {
        //d.mMaterials.apply( null, null );
    }

    @Override
    public void pushDraw( DrawEnv d ) {
        //d.mMaterials.push();
        bind( d );
    }

    @Override
    public void popDraw( DrawEnv d ) {
        //d.mMaterials.pop();
    }


    private static class MatBlock {



    }


}
