package com.b_knabe.packet_capture.core.util.common;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
    /**
     * Try to close all resources
     *  Input and Output utility for files
     * @param resources IO streams, etc...
     */
    public static void close(Closeable... resources) {
        for (Closeable resource : resources) {
            if (resource == null) continue;
            try {
                resource.close();
            } catch (IOException ignored) {
            }
        }
    }
}