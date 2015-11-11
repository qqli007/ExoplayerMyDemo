package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.exoplayerdemo.player.NativeMediaController;
import com.example.exoplayerdemo.player.SimpleVideoView;

/**
 * Created by lz on 15/11/11.
 */
public class FullScreenActivity extends Activity {

    private Uri contentUri;
    private SimpleVideoView videoView;
    private long playerPosition;

    public static void intentTo(Context context, Uri uri, long position) {
        Intent intent = new Intent(context, FullScreenActivity.class);
        intent.setData(uri);
        intent.putExtra("p1", position);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.player_activity_fullscreen);

        initView();

    }

    private void initData(){
        Intent intent = getIntent();
        contentUri = intent.getData();
        playerPosition = intent.getLongExtra("p1", 0);
    }

    private void initView(){
        FrameLayout root = (FrameLayout) findViewById(R.id.root_fullscreen);
        videoView = new SimpleVideoView(this, NativeMediaController.VIEW_MODE_LANDSCAPE);
        videoView.setContentUri(contentUri);
        videoView.setPlayerPosition(playerPosition);
        root.addView(videoView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.start();
    }



    @Override
    protected void onPause() {
        super.onPause();
        videoView.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.release();
    }

}
