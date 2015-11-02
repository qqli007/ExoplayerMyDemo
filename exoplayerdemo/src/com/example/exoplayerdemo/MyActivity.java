package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MyActivity extends Activity {

    private TextView go;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
    }

    private void initView() {
        go = (TextView) findViewById(R.id.go);
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String url = Environment.getExternalStorageDirectory() + "/video.mp4";
                String url = getRandomUrl();

                Intent to = new Intent(MyActivity.this, SimplePlayerActivity.class);
                to.setData(Uri.parse(url));
                startActivity(to);

            }
        });
    }

    private String getRandomUrl() {
        List<String> data = new ArrayList<>();
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/011549bf539a672c2f245abe38d62a90d56c5f44");
//        data.add("http://7xl9af.com1.z0.glb.clouddn.com/005567bf061b2b4bf3ad89a77aba06a9cdb367b6");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/06ed123436d134c77ef11f815149c801597a221c");
//        data.add("http://7xl9af.com1.z0.glb.clouddn.com/000f8d76c6d87bb61e61b145fa79b931bded20fb");

        Random random = new Random();
        return data.get(random.nextInt(data.size()));
    }


}
