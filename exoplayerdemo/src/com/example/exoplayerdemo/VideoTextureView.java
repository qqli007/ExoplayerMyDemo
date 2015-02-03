package com.example.exoplayerdemo;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * Created by lz on 2015/1/28.
 * <p/>
 * DoWhat:
 */

public class VideoTextureView extends TextureView {


    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;

    protected float[] mMatrixValues = new float[9];
    protected Matrix mDisplayMatrix = new Matrix();
    protected Matrix mBaseMatrix = new Matrix();

    protected float videoAspectRatio;
    protected int videoWidth, videoHeight;
    protected int viewWidth, viewHeight;
    protected float saveScale;
    float mMinScale, mMaxScale;

    int oldMeasuredWidth, oldMeasuredHeight;
    float origWidth, origHeight;

    public VideoTextureView(Context context) {
        super(context);
    }

    public VideoTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    /**
     * Set the aspect ratio that this {@link VideoTextureView} should satisfy.
     *
     * @param widthHeightRatio The width to height ratio.
     */
    public void setVideoWidthHeightRatio(float widthHeightRatio) {
        if (this.videoAspectRatio != widthHeightRatio) {
            this.videoAspectRatio = widthHeightRatio;
            requestLayout();
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d("0-0","----------onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        viewWidth = getMeasuredWidth();
        viewHeight = getMeasuredHeight();
        int width = viewWidth;
        int height = viewHeight;

        //
        // Rescales image on rotation
        //
//        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
//                || viewWidth == 0 || viewHeight == 0)
//            return;
//        oldMeasuredHeight = viewHeight;
//        oldMeasuredWidth = viewWidth;

        if (videoAspectRatio != 0) {

            float viewAspectRatio = (float) width / height;
            float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
            if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                height = (int) (width / videoAspectRatio);
            } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                width = (int) (height * videoAspectRatio);
            }

            //Fit to screen.
            float scaleX = (float) width / (float) viewWidth;
            float scaleY = (float) height / (float) viewHeight;
//            saveScale = 1f;
            mMinScale = scaleX;
            mMaxScale = scaleX * 4;
            mBaseMatrix.setScale(scaleX, scaleY);

            // Center the image
            float redundantYSpace = (float) viewHeight -  height;
            float redundantXSpace = (float) viewWidth -  width;
            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            mBaseMatrix.postTranslate(redundantXSpace, redundantYSpace);

            origWidth = width;
            origHeight = height;

            mDisplayMatrix = mBaseMatrix;
            fixTrans();
            printMatrix(mDisplayMatrix);
            setTransform(mDisplayMatrix);
        }

    }

    /**
     * Scrolls the view by the x and y amount
     *
     * @param x
     * @param y
     */
    public void scrollBy(float x, float y) {
        panBy(x, y);
    }


    protected void panBy(float dx, float dy) {

        float fixTransX = getFixDragTrans(dx, viewWidth, origWidth * saveScale);
        float fixTransY = getFixDragTrans(dy, viewHeight, origHeight * saveScale);
        mDisplayMatrix.postTranslate(fixTransX, fixTransY);
        fixTrans();

    }


    protected void zoomTo(float scale, float centerX, float centerY) {
        float targetScale = scale * getScaleX(mDisplayMatrix);
        if (targetScale > getMaxScale()) scale = getMaxScale();
        if (targetScale < getMinScale()) scale = getMinScale();


        float oldScale = getScaleX(mDisplayMatrix);
        float deltaScale = scale / oldScale;

        if (origWidth * getScaleX(mDisplayMatrix) <= viewWidth || origHeight * getScaleY(mDisplayMatrix) <= viewHeight)
            mDisplayMatrix.postScale(deltaScale, deltaScale, viewWidth / 2, viewHeight / 2);
        else
            mDisplayMatrix.postScale(deltaScale, deltaScale, centerX, centerY);

//        fixTrans();

    }



    protected float getScaleX(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_X);
    }

    protected float getScaleY(Matrix matrix) {
        return getValue(matrix, Matrix.MSCALE_Y);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    public void printMatrix(Matrix matrix) {
        float scalex = getValue(matrix, Matrix.MSCALE_X);
        float scaley = getValue(matrix, Matrix.MSCALE_Y);
        float tx = getValue(matrix, Matrix.MTRANS_X);
        float ty = getValue(matrix, Matrix.MTRANS_Y);
        Log.d("0-0", "matrix: { x: " + tx + ", y: " + ty + ", scalex: " + scalex + ", scaley: " + scaley + " }");
    }




    void fixTrans() {
        mDisplayMatrix.getValues(mMatrixValues);
        float transX = mMatrixValues[Matrix.MTRANS_X];
        float transY = mMatrixValues[Matrix.MTRANS_Y];

        float fixTransX = getFixTrans(transX, viewWidth, origWidth * saveScale);
        float fixTransY = getFixTrans(transY, viewHeight, origHeight * saveScale);

        if (fixTransX != 0 || fixTransY != 0)
            mDisplayMatrix.postTranslate(fixTransX, fixTransY);
    }


    float getFixTrans(float trans, float viewSize, float contentSize) {
        float minTrans, maxTrans;

        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans)
            return -trans + minTrans;
        if (trans > maxTrans)
            return -trans + maxTrans;
        return 0;
    }


    float getFixDragTrans(float delta, float viewSize, float contentSize) {
        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }


    protected float getMaxScale() {
        return mMaxScale;
    }

    protected float getMinScale() {
        return mMinScale;
    }


}
