package com.ninpou.qbits;

import android.app.Application;

import top.srsea.lever.Lever;

public class QbitsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Lever.init(getApplicationContext());
    }
}
