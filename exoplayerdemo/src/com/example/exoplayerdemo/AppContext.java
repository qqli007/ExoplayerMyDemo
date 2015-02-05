package com.example.exoplayerdemo;

import android.app.Application;
import android.content.Context;

/**
 * Created by lz on 2015/2/5.
 * <p/>
 * DoWhat:
 */

public class AppContext extends Application {

    private static Context appContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }


    public static Context getAppContext() {
        return appContext;
    }

}
