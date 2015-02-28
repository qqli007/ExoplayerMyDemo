package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.ImageView;
import com.example.exoplayerdemo.player.DefaultRendererBuilder;
import com.example.exoplayerdemo.player.DemoPlayer;
import com.example.exoplayerdemo.player.VideoTextureView;
import com.example.exoplayerdemo.player.DemoPlayer.RendererBuilder;
import com.example.exoplayerdemo.util.ImageUtil;
import com.google.android.exoplayer.*;

/**
 * Created by lz on 2015/1/23.
 * <p/>
 * DoWhat:
 */

public class SimplePlayerActivity extends Activity implements
        TextureView.SurfaceTextureListener,
        DemoPlayer.Listener,
        DemoPlayer.TextListener {

    private static final String TAG = "SimplePlayerActivity";

    private Uri contentUri;
    private int contentType;

    private DemoPlayer player;

    private VideoTextureView textureView;
    private ImageView show_img_view;

    private boolean playerNeedsPrepare;

    private boolean snapEnabled = false;
    private boolean isShowImage = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.player_activity_simple);
        initView();

    }

    private void initView() {

        textureView = (VideoTextureView) findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(this);
        show_img_view = (ImageView) findViewById(R.id.show_img_view);

    }

    private void initData() {
        Intent intent = getIntent();
        contentUri = intent.getData();
        contentType = 0;
    }

    private void setActivityBgColor() {
        TypedValue a = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.windowBackground, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            // windowBackground is a color,do nothing
            int color = a.data;
        } else {
            // windowBackground is not a color, probably a drawable
            Drawable d = getResources().getDrawable(a.resourceId);
            d.setAlpha(255);
            show_img_view.setBackground(d);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.player_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_snapshot:
                snapEnabled = true;
                return true;
            case R.id.menu_showimg:
                if (isShowImage) {
                    isShowImage = false;
                    item.setTitle(R.string.menu_close_img_view);
                    show_img_view.setVisibility(View.VISIBLE);
                } else {
                    isShowImage = true;
                    item.setTitle(R.string.menu_show_img_view);
                    show_img_view.setVisibility(View.GONE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        preparePlayer();
    }

    private void preparePlayer() {
        Log.d("0-0","----------preparePlayer");
        if (player == null) {
            player = new DemoPlayer(getRendererBuilder());
            player.addListener(this);
            player.setTextListener(this);
            playerNeedsPrepare = true;
        }
        if (playerNeedsPrepare) {
            player.prepare();
            playerNeedsPrepare = false;
        }
        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        if (surfaceTexture != null && player.getSurface() == null) {
            player.setSurface(new Surface(surfaceTexture));
        }
        player.setPlayWhenReady(true);
    }


    private void releasePlayer() {
        Log.d("0-0","----------releasePlayer");
        if (player != null) {
            player.release();
            player = null;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }


    private RendererBuilder getRendererBuilder() {
        switch (contentType) {
            default:
                return new DefaultRendererBuilder(this, contentUri, null);
        }
    }

    @Override
    public void onStateChanged(boolean playWhenReady, int playbackState) {
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
        Log.d("0-0", "----------onStateChanged     " + text);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
        playerNeedsPrepare = true;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, float pixelWidthAspectRatio) {
        textureView.setVideoWidthHeightRatio(
                height == 0 ? 1 : (width * pixelWidthAspectRatio) / height, width, height);
    }

    private void doSnapshot(Bitmap bitmap) {
        Log.d("0-0", "----------doSnapshot");
        if (bitmap == null) {
            return;
        }
        ImageUtil.doSnapshot(bitmap);

    }


    //------------------SurfaceTextureListener
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("0-0", "----------onSurfaceTextureAvailable");
        if (player != null && player.getSurface() == null) {
            player.setSurface(new Surface(surfaceTexture));
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.d("0-0", "----------onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.d("0-0", "----------onSurfaceTextureDestroyed");
        if (player != null) {
            player.blockingClearSurface();
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        if (snapEnabled) {
            snapEnabled = false;
            Bitmap bitmap = textureView.getBitmap(textureView.videoWidth, textureView.videoHeight);
            doSnapshot(bitmap);
            if (View.VISIBLE == show_img_view.getVisibility()) {
                setActivityBgColor();
                show_img_view.setImageBitmap(bitmap);
            }
        }
    }


    @Override
    public void onText(String text) {

    }




}
