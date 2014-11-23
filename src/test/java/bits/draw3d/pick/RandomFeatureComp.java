/*
 * Copyright (c) 2014. Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause
 */

package bits.draw3d.pick;


import bits.math3d.VecView;


/**
 * @author decamp
 */
public class RandomFeatureComp implements VecView<RandomFeature> {

    private final int mDim;


    public RandomFeatureComp( int dim ) {
        mDim = dim;
    }

    public int dimCount( RandomFeature f ) {
        return f.vectorRef().length;
    }

    public double value( RandomFeature f, int dim ) {
        return f.vectorRef()[dim];
    }

    @Override
    public int dim() {
        return mDim;
    }

    @Override
    public double get( RandomFeature item, int n ) {
        return item.mVector[n];
    }

    @Override
    public void set( RandomFeature item, int n, double v ) {
        item.mVector[n] = (float)v;
    }

    @Override
    public double x( RandomFeature item ) {
        return item.mVector[0];
    }

    @Override
    public void x( RandomFeature item, double x ) {
        item.mVector[0] = (float)x;
    }

    @Override
    public double y( RandomFeature item ) {
        return item.mVector[1];
    }

    @Override
    public void y( RandomFeature item, double y ) {
        item.mVector[1] = (float)y;
    }

    @Override
    public double z( RandomFeature item ) {
        return item.mVector[2];
    }

    @Override
    public void z( RandomFeature item, double z ) {
        item.mVector[2] = (float)z;
    }

    @Override
    public double w( RandomFeature item ) {
        return item.mVector[3];
    }

    @Override
    public void w( RandomFeature item, double w ) {
        item.mVector[3] = (float)w;
    }
}
