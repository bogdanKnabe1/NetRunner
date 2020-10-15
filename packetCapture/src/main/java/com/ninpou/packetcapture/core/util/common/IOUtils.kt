package com.ninpou.packetcapture.core.util.common

import java.io.Closeable
import java.io.IOException

object IOUtils {
    /**
     * Try to close all resources
     *  Input and Output utility for files
     * @param resources IO streams, etc...
     */
    fun close(vararg resources: Closeable?) {
        for (resource in resources) {
            if (resource == null) continue
            try {
                resource.close()
            } catch (ignored: IOException) {
            }
        }
    }
}