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
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/005567bf061b2b4bf3ad89a77aba06a9cdb367b6");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/06ed123436d134c77ef11f815149c801597a221c");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/000f8d76c6d87bb61e61b145fa79b931bded20fb");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/140f36e320a2f889b6be4bd314b7006498d1f6e1");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/b4d6906c1fd0975a852e07337f4a29c963e42245");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/52803b6d72c954a876bf847d103b0f1ee85c39b2");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/b4665822042b19fbd86754fc17f5c2229533b7b6");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/8006669cc35bc1f64c032ac11efb50836c3eab3e");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/174b9413dc7a22c0ef8b45db05ff983d26b1267d");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/496708301f5681a88223b36ff1a983d42ea103db");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/a356f0ac6631307caa1bae8f7c706b01d815a731");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/273622fe8f7b21af72bb2bc643872c724b84e751");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/3190afb3f54d45be37d28acaf7428a4c6a70f7ff");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/cf4b845bb05efa7ca0cb7d19dcaf9da21c81a720");
        data.add("http://7xl9af.com1.z0.glb.clouddn.com/4f4ed89be6d65825443e7c2c45eddbca67aba294");

        Random random = new Random();
        return data.get(random.nextInt(data.size()));
    }


}
