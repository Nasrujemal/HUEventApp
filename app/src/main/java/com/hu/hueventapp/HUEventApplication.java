package com.hu.hueventapp;

import android.app.Application;
import android.util.Log;

public class HUEventApplication extends Application {
    private static final String TAG = "HUEventApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            Log.e(TAG, "Uncaught exception", throwable);
            // You could also save the crash log to a file or send it to a server
        });
    }
}