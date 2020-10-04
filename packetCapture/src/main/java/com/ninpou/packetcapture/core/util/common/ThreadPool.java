package com.ninpou.packetcapture.core.util.common;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
    private final Executor executor;

    private ThreadPool() {
        executor = new ThreadPoolExecutor(1, 4,
                10L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024));
    }

    public static ThreadPool getInstance() {
        return Singleton.instance;
    }

    public void execute(Runnable run) {
        executor.execute(run);
    }

    static class Singleton {
        static ThreadPool instance = new ThreadPool();
    }
}
