/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.camera;

import bits.draw3d.actors.Actor;
import bits.draw3d.actors.ActorCoords;
import bits.math3d.*;


public class BasicViewFunc implements ViewFunc {

    private final Mat3   mActorToCameraMat;
    private final Trans3 mWorkTrans = new Trans3();
    private final Mat4   mWork = new Mat4();


    public BasicViewFunc() {
        this( ActorCoords.newActorToViewMat() );
    }


    public BasicViewFunc( Mat3 actorToCameraMatRef ) {
        mActorToCameraMat = actorToCameraMatRef;
    }

    @Override
    public void computeViewMat( Actor camera, Mat4 out ) {
        Mat.transpose( camera.mRot, mWorkTrans.mRot );
        if( mActorToCameraMat != null ) {
            Mat.mult( mActorToCameraMat, mWorkTrans.mRot, mWorkTrans.mRot );
        }

        Mat.put( mWorkTrans.mRot, mWork );
        Vec3 pos = camera.mPos;
        Mat.translate( mWork, -pos.x, -pos.y, -pos.z, out );


        //Trans.invert( camera, mWorkTrans );
        //if( mActorToCameraMat != null ) {
        //    Mat.mult( mActorToCameraMat, mWorkTrans.mRot, mWorkTrans.mRot );
        //}
        //Trans.transToMat( mWorkTrans, out );
    }

}
