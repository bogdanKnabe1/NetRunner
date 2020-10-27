package com.ninpou.packetcapture;

import android.app.Application;
import android.content.Context;

public class Qbits extends Application {

    private static Qbits mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }
}
