/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.nodes;

import bits.draw3d.DrawEnv;
import bits.draw3d.DrawNodeAdapter;
import bits.draw3d.actors.Actor;
import bits.math3d.Mat4;


/**
 * @author decamp
 */
public class ActorNode extends DrawNodeAdapter {

    private final Actor mSpatial;
    private final Mat4  mBaseTransform;
    private final Mat4 mWork = new Mat4();


    public ActorNode( Actor spatial, Mat4 optBaseTransform ) {
        mSpatial = spatial != null ? spatial : new Actor();
        mBaseTransform = optBaseTransform != null ? new Mat4( optBaseTransform ) : null;
    }


    public Actor actor() {
        return mSpatial;
    }


    @Override
    public void pushDraw( DrawEnv d ) {
        d.mView.push();

        //Set modelview to actor object.
        mSpatial.computeTransform( mWork );
        d.mView.mult( mWork );

        if( mBaseTransform != null ) {
            d.mView.mult( mBaseTransform );
        }
    }

    @Override
    public void popDraw( DrawEnv d ) {
        d.mView.pop();
    }

}
