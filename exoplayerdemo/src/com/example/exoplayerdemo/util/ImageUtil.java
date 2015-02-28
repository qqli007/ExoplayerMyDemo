package com.example.exoplayerdemo.util;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.widget.Toast;
import com.example.exoplayerdemo.AppContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lz on 2015/2/5.
 * <p/>
 * DoWhat:
 */

public class ImageUtil {

    public static final int SNAPSHOT_SUCCESS = 1;
    public static final int SNAPSHOT_FAILED = -1;

    public static void doSnapshot (Bitmap bitmap) {
        Date now = new Date();
        SimpleDateFormat dateFormater = new SimpleDateFormat(
                "yyyy-MM-dd");
        String date = dateFormater.format(now);
        SimpleDateFormat timeFormater = new SimpleDateFormat("HH_mm_ss");
        String time = timeFormater.format(now);
        String filePath = Environment.getExternalStorageDirectory()
                .toString()
                + "/Download/"
                + "/" + date + "/" + time + ".jpg";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        int result = saveBitmapToSDcard(baos.toByteArray(), new File(filePath));
        if (result == SNAPSHOT_SUCCESS) {
            ImageUtil.mediaScan("file://" + filePath);
        }
    }


    private static int saveBitmapToSDcard(byte[] data, File savePath) {
        String sdcardState = Environment.getExternalStorageState();
        if (sdcardState.equals(Environment.MEDIA_MOUNTED)
                && !sdcardState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            // SD card is mounted, and is writable.
            try {
                File parent = savePath.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(savePath);
                if (data.length > 0) {
                    StatFs fs = new StatFs(Environment
                            .getExternalStorageDirectory().toString());
                    // return -2, if don't have enough space
                    long availableSpace = (long) fs.getAvailableBlocks()
                            * fs.getBlockSize();
                    if (data.length > availableSpace) {
                        fos.close();
                        if (savePath.exists()) {
                            savePath.delete();
                        }
                        return SNAPSHOT_FAILED;
                    }
                    fos.write(data);
                    fos.flush();
                    fos.close();
                } else {
                    fos.close();
                    if (savePath.exists()) {
                        savePath.delete();
                    }
                    return SNAPSHOT_FAILED;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (savePath.exists()) {
                    savePath.delete();
                }
                return SNAPSHOT_FAILED;
            }
        } else {
            return SNAPSHOT_FAILED;
        }
        return 1;
    }


    public static void mediaScan(String path)
    {
        try
        {

            Uri uri = Uri.parse(path);
            AppContext.getAppContext().sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            Toast.makeText(AppContext.getAppContext(), "File saved:" + path, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}
