package com.b_knabe.packet_capture;

import android.content.Context;

public class Application extends android.app.Application {

    private static Application mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static Context getAppContext() {
        return mInstance.getApplicationContext();
    }
}
