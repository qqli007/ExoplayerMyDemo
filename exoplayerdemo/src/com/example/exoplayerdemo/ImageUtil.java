package com.example.exoplayerdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
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

    public static void doSnapshotSynch (VideoTextureView textureView) {
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
        Bitmap bitmap = textureView.getBitmap(textureView.videoWidth, textureView.videoHeight);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100,baos);
        saveBitmapToSDcard(baos.toByteArray(), new File(filePath));
        ImageUtil.mediaScan("file://" + filePath);
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
                        return -1;
                    }
                    fos.write(data);
                    fos.flush();
                    fos.close();
                } else {
                    fos.close();
                    if (savePath.exists()) {
                        savePath.delete();
                    }
                    return -1;
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (savePath.exists()) {
                    savePath.delete();
                }
                return -1;
            }
        } else {
            return -1;
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



}
