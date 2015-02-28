package com.example.exoplayerdemo.easing;

/**
 * Created by lz on 2015/2/28.
 * <p/>
 * DoWhat:
 *
 *
 */

public class Linear implements Easing {

    public double easeNone(double time, double start, double end, double duration) {
        return end * time / duration + start;
    }

    @Override
    public double easeOut(double time, double start, double end, double duration) {
        return end * time / duration + start;
    }

    @Override
    public double easeIn(double time, double start, double end, double duration) {
        return end * time / duration + start;
    }

    @Override
    public double easeInOut(double time, double start, double end, double duration) {
        return end * time / duration + start;
    }

}