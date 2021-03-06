package com.example.exoplayerdemo.player;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by lz on 2015/1/29.
 * <p/>
 * DoWhat:
 * <p/>
 * refer to --- https://github.com/sephiroth74/ImageViewZoom
 */

public class VideoZoomTextureView extends VideoTextureView {

    private final static String TAG = "VideoZoomTextureView";

    protected ScaleGestureDetector mScaleDetector;
    protected GestureDetector mGestureDetector;
    protected GestureDetector.OnGestureListener mGestureListener;
    protected ScaleGestureDetector.OnScaleGestureListener mScaleListener;

    private OnImageViewTouchDoubleTapListener mDoubleTapListener;
    private OnImageViewTouchSingleTapListener mSingleTapListener;


    protected boolean mDoubleTapEnabled = true;
    protected boolean mScaleEnabled = true;
    protected boolean mScrollEnabled = true;


    public VideoZoomTextureView(Context context) {
        super(context);
        init();
    }

    public VideoZoomTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoZoomTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mGestureListener = getGestureListener();
        mScaleListener = getScaleListener();

        mScaleDetector = new ScaleGestureDetector(getContext(), mScaleListener);
        mGestureDetector = new GestureDetector(getContext(), mGestureListener, null, true);

    }


    protected GestureDetector.OnGestureListener getGestureListener() {
        return new GestureListener();
    }

    protected ScaleGestureDetector.OnScaleGestureListener getScaleListener() {
        return new ScaleListener();
    }


    public void setDoubleTapListener(OnImageViewTouchDoubleTapListener listener) {
        mDoubleTapListener = listener;
    }

    public void setSingleTapListener(OnImageViewTouchSingleTapListener listener) {
        mSingleTapListener = listener;
    }

    public void setDoubleTapEnabled(boolean value) {
        mDoubleTapEnabled = value;
    }

    public void setScaleEnabled(boolean value) {
        mScaleEnabled = value;
    }

    public void setScrollEnabled(boolean value) {
        mScrollEnabled = value;
    }

    public boolean getDoubleTapEnabled() {
        return mDoubleTapEnabled;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);

        if (!mScaleDetector.isInProgress()) {
            mGestureDetector.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                return onUp(event);
        }
        return true;
    }


    public boolean onSingleTapConfirmed(MotionEvent e) {
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        scrollBy(-distanceX, -distanceY);
        setTransform(mDisplayMatrix);
        invalidate();
        return true;
    }


    public boolean onDown(MotionEvent e) {
        return true;
    }

    public boolean onUp(MotionEvent e) {
        if (getScaleX(mDisplayMatrix) < getMinScale()) {
            zoomTo(getMinScale(), 50);
        }
        return true;
    }

    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }


    @Override
    protected void onZoomAnimationCompleted(float scale) {
        if (scale != getMinScale()) {
            zoomTo(getMinScale(), 0, 0);
            setTransform(mDisplayMatrix);
            invalidate();
        }
    }


    public class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            if (null != mSingleTapListener) {
                mSingleTapListener.onSingleTapConfirmed();
            }

            return VideoZoomTextureView.this.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mDoubleTapEnabled) {
//                float scale = getScale();
//                float targetScale = scale;
//                targetScale = onDoubleTapPost(scale, getMaxScale());
//                targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale()));
//                zoomTo(targetScale, e.getX(), e.getY(), DEFAULT_ANIMATION_DURATION);
//                invalidate();
            }

            if (null != mDoubleTapListener) {
                mDoubleTapListener.onDoubleTap();
            }

            return super.onDoubleTap(e);
        }


        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            if (!mScrollEnabled) return false;
            if (e1 == null || e2 == null) return false;
            if (e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
            if (mScaleDetector.isInProgress()) return false;
            return VideoZoomTextureView.this.onScroll(e1, e2, distanceX, distanceY);
        }


        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return VideoZoomTextureView.this.onSingleTapUp(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return VideoZoomTextureView.this.onDown(e);
        }
    }


    public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        protected boolean mScaled = false;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float span = detector.getCurrentSpan() - detector.getPreviousSpan();
            float targetScale = getScaleX(mDisplayMatrix) * detector.getScaleFactor();

            if (mScaleEnabled) {
                if (mScaled && span != 0) {
                    targetScale = Math.min(getMaxScale(), Math.max(targetScale, getMinScale() - 0.1f));
                    zoomTo(targetScale, detector.getFocusX(), detector.getFocusY());
                    setTransform(mDisplayMatrix);
                    invalidate();
                    return true;
                }

                // This is to prevent a glitch the first time
                // image is scaled.
                if (!mScaled) mScaled = true;
            }
            return true;
        }

    }


    public interface OnImageViewTouchDoubleTapListener {

        void onDoubleTap();
    }

    public interface OnImageViewTouchSingleTapListener {

        void onSingleTapConfirmed();
    }


}
