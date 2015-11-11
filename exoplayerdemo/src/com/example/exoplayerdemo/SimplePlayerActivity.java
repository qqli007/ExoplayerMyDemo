package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.example.exoplayerdemo.player.NativeMediaController;
import com.example.exoplayerdemo.player.SimpleVideoView;

/**
 * Created by lz on 2015/1/23.
 * <p>
 * DoWhat:
 */

public class SimplePlayerActivity extends Activity {

    private static final String TAG = "SimplePlayerActivity";

    private Uri contentUri;
    private SimpleVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();

        setContentView(R.layout.player_activity_simple);

        initView();

    }

    private void initView() {
        FrameLayout root = (FrameLayout) findViewById(R.id.root);
//        root.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE
//                        || keyCode == KeyEvent.KEYCODE_MENU) {
//                    return false;
//                }
//                return mediaController.dispatchKeyEvent(event);
//            }
//        });
        videoView = new SimpleVideoView(this, NativeMediaController.VIEW_MODE_PORTRAIT);
        videoView.setContentUri(contentUri);
        root.addView(videoView);

    }

    private void initData() {
        Intent intent = getIntent();
        contentUri = intent.getData();
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
