package com.ninpou.packetcapture.core.util.common

import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class ThreadPool private constructor() {
    private val executor: Executor
    fun execute(run: Runnable?) {
        executor.execute(run)
    }

    internal object Singleton {
        var instance = ThreadPool()
    }

    companion object {
        @JvmStatic
        val instance: ThreadPool
            get() = Singleton.instance
    }

    init {
        executor = ThreadPoolExecutor(1, 4,
                10L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(1024))
    }
}