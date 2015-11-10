package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.MediaController;

import com.example.exoplayerdemo.player.DemoPlayer;
import com.example.exoplayerdemo.player.ExtractorRendererBuilder;
import com.example.exoplayerdemo.player.VideoSurfaceView;
import com.google.android.exoplayer.*;
import com.google.android.exoplayer.util.Util;

/**
 * Created by lz on 2015/1/23.
 * <p>
 * DoWhat:
 */

public class SimplePlayerActivity extends Activity implements SurfaceHolder.Callback,
        DemoPlayer.Listener {

    private static final String TAG = "SimplePlayerActivity";

    private Uri contentUri;
    private int contentType;

    private MediaController mediaController;
    private FrameLayout videoFrame;
    private VideoSurfaceView surfaceView;

    private DemoPlayer player;
    private boolean playerNeedsPrepare;
    private long playerPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.player_activity_simple);
        initView();

    }

    private void initView() {
        View root = findViewById(R.id.root);
        root.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
                        || keyCode == KeyEvent.KEYCODE_MENU) {
                    return false;
                }
                return mediaController.dispatchKeyEvent(event);
            }
        });

        videoFrame = (FrameLayout) findViewById(R.id.video_frame);
        surfaceView = (VideoSurfaceView) findViewById(R.id.surface_view);

        mediaController = new MediaController(this);
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

    private void initData() {
        Intent intent = getIntent();
        contentUri = intent.getData();
        contentType = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) {
            preparePlayer(true);
        } else {
            player.setBackgrounded(false);
        }
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
            updateButtonVisibilities();
        }
        player.setSurface(surfaceView.getHolder().getSurface());
        player.setPlayWhenReady(playWhenReady);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Release the player
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            playerPosition = player.getCurrentPosition();
            player.release();
            player = null;
        }
    }

    private void updateButtonVisibilities() {

    }

    private void toggleControlsVisibility()  {
        if (mediaController.isShowing()) {
            mediaController.hide();
        } else {
            showControls();
        }
    }

    private void showControls() {
        mediaController.show(0);
    }



    private DemoPlayer.RendererBuilder getRendererBuilder() {
        String userAgent = Util.getUserAgent(this, "ExoPlayerDemo");
        switch (contentType) {
            default:
                return new ExtractorRendererBuilder(this, userAgent, contentUri);
        }
    }

    //----------------DemoPlayer.Listener
    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == ExoPlayer.STATE_ENDED) {
            showControls();
        }
        String text = "playWhenReady=" + playWhenReady + ", playbackState=";
        switch(playbackState) {
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
        updateButtonVisibilities();

    }

    @Override
    public void onError(Exception e) {
        playerNeedsPrepare = true;
        updateButtonVisibilities();
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


}
