package com.b_knabe.packet_capture.core.util.net_utils.tcp;

import com.b_knabe.packet_capture.Application;
import com.b_knabe.packet_capture.core.util.common.ThreadPool;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;


/**
 * !
 * Low-level java
 * Network Event Handler
 * IMMUTABLE
 */

public class TcpDataSaver {
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    private static final String BASE_DIR = Application.getAppContext().getFilesDir() + "/Netrunner/";
    public static final String DATA_DIR = BASE_DIR + "data/";
    public static final String CONFIG_DIR = BASE_DIR + "config/";
    private int requestNum = 0;
    private int responseNum = 0;
    private String dir;
    private TcpData lastSaveTcpData;
    private File lastSaveFile;

    public TcpDataSaver(String dir) {
        this.dir = dir;
    }

    public void addData(final TcpData tcpData) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                if (lastSaveTcpData == null || (lastSaveTcpData.isRequest ^ tcpData.isRequest)) {
                    newFileAndSaveData(tcpData);
                } else {
                    appendFileData(tcpData);
                }
                lastSaveTcpData = tcpData;
            }
        });
    }

    private void appendFileData(TcpData tcpData) {
        RandomAccessFile randomAccessFile;
        try {
            randomAccessFile = new RandomAccessFile(lastSaveFile.getAbsolutePath(), "rw");
            long length = randomAccessFile.length();
            randomAccessFile.seek(length);
            randomAccessFile.write(tcpData.needParseData, tcpData.offSet, tcpData.playoffSize);
        } catch (Exception ignored) {
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void newFileAndSaveData(TcpData tcpData) {
        int saveNum;
        if (tcpData.isRequest) {
            saveNum = requestNum;
            requestNum++;
        } else {
            saveNum = responseNum;
            responseNum++;
        }
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String childName = (tcpData.isRequest ? REQUEST : RESPONSE) + saveNum;
        lastSaveFile = new File(file, childName);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(lastSaveFile);
            fileOutputStream.write(tcpData.needParseData, tcpData.offSet, tcpData.playoffSize);
            fileOutputStream.flush();
        } catch (Exception ignored) {
        } finally {
            close(fileOutputStream);
        }

    }


    public static class TcpData {
        boolean isRequest;
        byte[] needParseData;
        int offSet;
        int playoffSize;

        private TcpData(Builder builder) {
            isRequest = builder.isRequest;
            needParseData = builder.needParseData;
            offSet = builder.offSet;
            playoffSize = builder.length;
        }


        public static final class Builder {
            private boolean isRequest;
            private byte[] needParseData;
            private int offSet;
            private int length;

            public Builder() {
            }

            public Builder isRequest(boolean val) {
                isRequest = val;
                return this;
            }

            public Builder needParseData(byte[] val) {
                needParseData = val;
                return this;
            }

            public Builder offSet(int val) {
                offSet = val;
                return this;
            }

            public Builder length(int val) {
                length = val;
                return this;
            }

            public TcpData build() {
                return new TcpData(this);
            }
        }
    }
}
