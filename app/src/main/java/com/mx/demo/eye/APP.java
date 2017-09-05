package com.mx.demo.eye;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

/**
 * Created by mx on 5/17.
 */

class APP extends Application {
    private static final boolean DEVELOP = false;
    private static Context mContext;

    public static Context getApplication() {
        return mContext;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                //log
            }
        });
        if (DEVELOP) {
            StrictMode.enableDefaults();
        }
    }

}
