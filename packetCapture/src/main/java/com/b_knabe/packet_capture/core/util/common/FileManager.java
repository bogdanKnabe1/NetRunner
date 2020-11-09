package com.b_knabe.packet_capture.core.util.common;

import java.io.File;

public class FileManager {
    private static void deleteFile(File file, boolean reserveSelf) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File child : files) {
                delete(child);
            }
        }
        if (!reserveSelf) file.delete();
    }

    public static void delete(File file) {
        deleteFile(file, false);
    }

    public static void deleteUnder(File file) {
        deleteFile(file, true);
    }
}