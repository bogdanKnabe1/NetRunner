package com.ninpou.packetcapture.core.util.net;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

/**!
 * Low-level java
 * Network Event Handler
 * IMMUTABLE
 * */

public class TcpDataLoader {
    private static final String TAG = "TcpDataLoader";
    private static final int HEADER_LIMIT = 256 * 1024;
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String GZIP = "gzip";
    private static final String IMAGE = "image";
    private static final String URLENCODED = "urlencoded";

    public static TcpData loadSaveFile(File childFile) {
        try {
            String encodingType = null;
            String contentType = null;
            long headerLimit = HEADER_LIMIT;
            String name = childFile.getName();
            TcpData tcpData = new TcpData();
            if (name.contains(TcpDataSaver.REQUEST)) {
                tcpData.isRequest = true;
            }
            Source fileSource = Okio.source(childFile);
            BufferedSource buffer = Okio.buffer(fileSource);
            String line = buffer.readUtf8LineStrict(headerLimit);
            StringBuilder headBuilder = new StringBuilder();
            while (line != null && line.length() > 0) {
                headerLimit = HEADER_LIMIT - line.length();
                String[] split = line.split(":");
                if (CONTENT_ENCODING.equalsIgnoreCase(split[0])) {
                    encodingType = split[1];
                }
                if (CONTENT_TYPE.equalsIgnoreCase(split[0])) {
                    contentType = split[1];
                }
                headBuilder.append(line).append("\n");
                line = buffer.readUtf8LineStrict(headerLimit);
            }
            tcpData.headStr = headBuilder.toString();

            if (encodingType != null) {
                String s = encodingType.toLowerCase();
                if (s.equals(GZIP)) {
                    tcpData.bodyStr = getGzipStr(buffer);
                    return tcpData;
                }
            }
            if (contentType != null) {
                if (contentType.toLowerCase().contains(IMAGE)) {
                    byte[] bytes = buffer.readByteArray();
                    try {
                        tcpData.bodyImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    } catch (Exception e) {
                        Log.d(TAG, "error parse map");
                    }
                    if (tcpData.bodyImage == null) {
                        tcpData.bodyStr = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
                    }

                    return tcpData;
                } else if (contentType.toLowerCase().contains(URLENCODED)) {
                    String readUtf8 = buffer.readUtf8();
                    tcpData.bodyStr = URLDecoder.decode(readUtf8);
                    return tcpData;
                }
            }
            tcpData.bodyStr = buffer.readUtf8();
            Log.d(TAG, "bodyStr is " + tcpData.bodyStr);
            return tcpData;
        } catch (Exception e) {
            Log.d(TAG, "loadSaveFile " + e.getMessage());
            return getRawDataFromFile(childFile);
        }
    }

    private static TcpData getRawDataFromFile(File childFile) {
        Source fileSource = null;
        TcpData tcpData = new TcpData();
        try {
            String name = childFile.getName();
            tcpData.isRequest = name.contains(TcpDataSaver.REQUEST);
            fileSource = Okio.source(childFile);
            BufferedSource buffer = Okio.buffer(fileSource);
            tcpData.headStr = buffer.readUtf8();
            Log.d(TAG, tcpData.headStr);
            return tcpData;
        } catch (Exception e) {
            Log.d(TAG, "failed to getRawDataFromFile" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String getGzipStr(BufferedSource buffer) {
        GzipSource gzipSource = new GzipSource(buffer);
        BufferedSource gzipBuffer = Okio.buffer(gzipSource);
        try {
            byte[] bytes = gzipBuffer.readByteArray();
            String s = new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
            Log.d(TAG, "s is" + s);
            return s;
        } catch (IOException e) {
            Log.d(TAG, "failed to getGzipStr");
        }
        return null;
    }

    public static class TcpData {
        boolean isRequest;
        String headStr;
        String bodyStr;
        Bitmap bodyImage;

        public boolean isRequest() {
            return isRequest;
        }

        public String getHeadStr() {
            return headStr;
        }

        public String getBodyStr() {
            return bodyStr;
        }

        public Bitmap getBodyImage() {
            return bodyImage;
        }

        public boolean isBodyNull() {
            return TextUtils.isEmpty(bodyStr) && bodyImage == null;
        }
    }
}
