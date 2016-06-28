package com.example.exoplayerdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MyActivity extends Activity {

    public static final int RESULT_CODE_COMPRESS_VIDEO = 1;

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
                String url = Environment.getExternalStorageDirectory() + "/Download/bbb.flv";
                Log.d("0-0", "url=" + url);
                Intent to = new Intent(MyActivity.this, SimplePlayerActivity.class);
                to.setData(Uri.parse(url));
                startActivity(to);

            }
        });

        findViewById(R.id.select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, RESULT_CODE_COMPRESS_VIDEO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            Uri uri = data.getData();

            if (requestCode == RESULT_CODE_COMPRESS_VIDEO) {
                if (uri != null) {

                    String path = uri.getPath();
//                    File file = new File(path);
//                    tempFile = FileUtils.saveTempFile(file.getName(), this, uri);
//                  editText.setText(tempFile.getPath());

                    Log.d("0-0", "onActivityResult path = " + path);
                    Intent to = new Intent(MyActivity.this, SimplePlayerActivity.class);
                    to.setData(Uri.parse(path));
                    startActivity(to);
                }
            }

        }
    }
}