package com.example.exoplayerdemo.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by lz on 15/11/10.
 */

public class VideoSurfaceView extends SurfaceView {

    private static final float MAX_ASPECT_RATIO_DEFORMATION_PERCENT = 0.01f;
    private float videoAspectRatio;

    public VideoSurfaceView(Context context) {
        super(context);
    }

    public VideoSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setVideoWidthHeightRatio(float widthHeightRatio) {
        if(videoAspectRatio != widthHeightRatio) {
            videoAspectRatio = widthHeightRatio;
            requestLayout();
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        if(this.videoAspectRatio != 0) {
            float viewAspectRatio = (float) width / height;
            float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
            if (aspectDeformation > MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                height = (int) (width / videoAspectRatio);
            } else if (aspectDeformation < -MAX_ASPECT_RATIO_DEFORMATION_PERCENT) {
                width = (int) (height * videoAspectRatio);
            }
        }

        setMeasuredDimension(width, height);
    }

}
