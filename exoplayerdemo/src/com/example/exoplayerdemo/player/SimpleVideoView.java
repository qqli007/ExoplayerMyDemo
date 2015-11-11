package com.example.exoplayerdemo.player;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.exoplayerdemo.FullScreenActivity;
import com.example.exoplayerdemo.R;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.util.Util;

/**
 * Created by lz on 15/11/10.
 */

public class SimpleVideoView extends FrameLayout implements SurfaceHolder.Callback,
        DemoPlayer.Listener, NativeMediaController.MediaControllerGenerator,
        NativeMediaController.MediaControllerOtherOperation {

    private static final String TAG = "SimpleVideoView";

    private Context context;
    private NativeMediaController mediaController;
    private DemoPlayer player;
    private VideoSurfaceView surfaceView;

    private int contentType = 0;
    private Uri contentUri;
    private boolean playerNeedsPrepare;
    private long playerPosition;
    private int viewMode;

    public SimpleVideoView(Context context) {
        super(context);
        this.context = context;
        initVideoView();
    }

    public SimpleVideoView(Context context, int viewMode) {
        super(context);
        this.context = context;
        this.viewMode = viewMode;
        initVideoView();
    }

    public SimpleVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initVideoView();
    }

    public SimpleVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initVideoView();
    }

    private void initVideoView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.simple_video_view_layout, this);
        FrameLayout videoFrame = (FrameLayout) root.findViewById(R.id.video_frame);
        surfaceView = (VideoSurfaceView) root.findViewById(R.id.surface_view);
        mediaController = new NativeMediaController(context);
        mediaController.setUIGenerator(this);
        mediaController.setControllerOtherOperation(this);
        mediaController.setAnchorView(videoFrame);
        surfaceView.getHolder().addCallback(this);

        videoFrame.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    toggleControlsVisibility();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    view.performClick();
                }
                return true;
            }
        });

    }

    private void preparePlayer(boolean playWhenReady) {
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.seekTo(playerPosition);
            playerNeedsPrepare = true;
            mediaController.setMediaPlayer(player.getPlayerControl());
            mediaController.setEnabled(true);
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }

    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(context, "ExoPlayerDemo");
        switch (contentType) {
            default:
                return new ExtractorRendererBuilder(context, userAgent, contentUri);
        }
    }


    public void start() {
        Log.d("0-0", TAG + " start() player=" + player);
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
    }

    public void pause() {
        Log.d("0-0", TAG + " pause() player=" + player);
        if (player != null) {
            player.getPlayerControl().pause();
        }
    }


    public void release() {
        Log.d("0-0", TAG + " release() player=" + player);
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    private void showControls() {
        mediaController.show(0);
    }

    private void toggleControlsVisibility() {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    //----------------NativeMediaController.MediaControllerOtherOperation
    @Override
    public void doFullscreen() {
        if (viewMode == NativeMediaController.VIEW_MODE_LANDSCAPE) {
            ((Activity) context).finish();
        } else {
            playerPosition = player.getCurrentPosition();
            FullScreenActivity.intentTo(context, contentUri, playerPosition);
        }
    }

    @Override
    public boolean isFullscreen() {
        if (viewMode == NativeMediaController.VIEW_MODE_LANDSCAPE) {
            return true;
        } else {
            return false;
        }
    }

    //----------------NativeMediaController.MediaControllerGenerator
    @Override
    public BaseMediaControllerHolder generateMediaController() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_native_media_controler_custom, null);

        BaseMediaControllerHolder holder = new BaseMediaControllerHolder();
        holder.parentLayout = view;
        holder.pauseButton = (ImageButton) view.findViewById(R.id.footer_start);
        holder.currentTimeView = (TextView) view.findViewById(R.id.footer_currenttime);
        holder.totalTimeView = (TextView) view.findViewById(R.id.footer_totaltime);
        holder.seekbar = (SeekBar) view.findViewById(R.id.footer_seekbar);
        holder.fullScreenButton = (ImageButton) view.findViewById(R.id.footer_fullscreen);
        holder.headerView = view.findViewById(R.id.video_controller_view_header);
        if (viewMode == NativeMediaController.VIEW_MODE_LANDSCAPE) {
            holder.headerView.setVisibility(VISIBLE);
        } else {
            holder.headerView.setVisibility(GONE);
        }
        holder.headerBack = (ImageButton) view.findViewById(R.id.header_back);
        holder.headerTitle = (TextView) view.findViewById(R.id.header_title);

        holder.pauseResId = R.drawable.selector_video_btn_pause;
        holder.startResId = R.drawable.selector_video_btn_start;
        holder.fullscreenResId = R.drawable.selector_video_btn_fullscreen;
        holder.unfullscreenResId = R.drawable.selector_video_btn_unfullscreen;

        return holder;
    }

    //----------------DemoPlayer.Listener
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                text += "buffering";
                break;
            case ExoPlayer.STATE_ENDED:
                text += "ended";
                break;
            case ExoPlayer.STATE_IDLE:
                text += "idle";
                break;
            case ExoPlayer.STATE_PREPARING:
                text += "preparing";
                break;
            case ExoPlayer.STATE_READY:
                text += "ready";
                break;
            default:
                text += "unknown";
                break;
        }

        Log.d("0-0", "onStateChanged " + text);

    }

    @Override
    public void onError(Exception e) {
        playerNeedsPrepare = true;
        showControls();
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
        surfaceView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthHeightRatio) / height);
    }

    //----------------SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.d("0-0", "----------surfaceCreated");
        if (player != null) {
            player.setSurface(surfaceHolder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d("0-0", "----------surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.d("0-0", "----------surfaceDestroyed");
        if (player != null) {
            player.blockingClearSurface();
        }
    }

    public void setContentUri(Uri contentUri) {
        this.contentUri = contentUri;
    }

    public void setPlayerPosition(long playerPosition) {
        this.playerPosition = playerPosition;
    }
}
