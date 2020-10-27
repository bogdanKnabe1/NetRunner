package com.ninpou.packetcapture.core.util.net_utils;

import android.content.Context;

import java.lang.ref.WeakReference;

public class UtilityClass {
    private WeakReference<Context> contextWeakReference;

    public static void init(Context appContext) {
        Singleton.INSTANCE.contextWeakReference = new WeakReference<>(appContext);
    }

    public static Context getContext() {
        if (Singleton.INSTANCE.contextWeakReference == null) {
            throw new RuntimeException("Please init lever before use it.");
        }
        return Singleton.INSTANCE.contextWeakReference.get();
    }

    private static class Singleton {
        private static final UtilityClass INSTANCE = new UtilityClass();
    }
}