package com.ninpou.packetcapture.core.util.processparse;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**!
 * Low-level java
 * Network Event Handler
 * IMMUTABLE
 * */

public class NetFileManager {

    private static final String TAG = "NetFileManager";
    /**
     * The minimum number of copies of each information in the system configuration file that can be divided
     */
    public final static int DATA_MIN_LENGTH = 9;
    /**
     * The ip address and port number extracted from each piece of information in the system configuration file are separated by ":",
     * so the length after verification should be 2
     */
    public final static int DATA_IP_ADDRESS_PARTS = 2;
    /**
     * The minimum length of ip address in hexadecimal, 8 is IPv4
     */
    public final static int IP_ADDRESS_MIN_LENGTH = 8;
    public final static String SYSTEM_DEFAULT_IP_ADDRESS = "0.0.0.0";
    public final static int TYPE_TCP = 0;
    public final static int TYPE_TCP6 = 1;
    public final static int TYPE_UDP = 2;
    public final static int TYPE_UDP6 = 3;
    public final static int TYPE_RAW = 4;
    public final static int TYPE_RAW6 = 5;
    public final static int TYPE_MAX = 6;

    private final static int DATA_LOCAL = 2;
    private final static int DATA_REMOTE = 3;
    private final static int DATA_UID = 8;
    /**
     * key: the port number used by the app process (sourcePort) value: UID
     */
    private Map<Integer, Integer> processHost = new ConcurrentHashMap<>();
    private File[] file;
    private long[] lastTime;
    private StringBuilder sbBuilder = new StringBuilder();

    static class InnerClass {
        static NetFileManager instance = new NetFileManager();
    }

    public static NetFileManager getInstance() {
        InnerClass.instance.init();
        return InnerClass.instance;
    }

    public void init() {
        final String PATH_TCP = "/proc/net/tcp";
        final String PATH_TCP6 = "/proc/net/tcp6";
        final String PATH_UDP = "/proc/net/udp";
        final String PATH_UDP6 = "/proc/net/udp6";
        final String PATH_RAW = "/proc/net/raw";
        final String PATH_RAW6 = "/proc/net/raw6";

        file = new File[TYPE_MAX];
        file[0] = new File(PATH_TCP);
        file[1] = new File(PATH_TCP6);
        file[2] = new File(PATH_UDP);
        file[3] = new File(PATH_UDP6);
        file[4] = new File(PATH_RAW);
        file[5] = new File(PATH_RAW6);

        lastTime = new long[TYPE_MAX];
        // Initialize each value in lastTime
        Arrays.fill(lastTime, 0);
    }

    public void execute(String[] command, String directory, int type) throws IOException {
        NetInfo netInfo = null;
        String sTmp = null;

        ProcessBuilder builder = new ProcessBuilder(command);

        if (directory != null) {
            builder.directory(new File(directory));
        }
        builder.redirectErrorStream(true);
        Process process = builder.start();
        InputStream is = process.getInputStream();

        Scanner s = new Scanner(is);
        s.useDelimiter("\n");
        while (s.hasNextLine()) {
            sTmp = s.nextLine();
            netInfo = parseDataNew(sTmp);
            if (netInfo != null) {
                netInfo.setType(type);
                saveToMap(netInfo);
            }
        }
    }

    /**
     * Convert a string to int data, and return the default value iDefault when the incoming string str is null. One of the functions in this class is:
     * Convert the port number in string format obtained from the ip address to int type
     *
     * @param str The string type of the port number, generally four digits in hexadecimal xxxx.
     * @param iHex str is in decimal
     * @param iDefault The default return value if str is null
     * @return
     */
    private int strToInt(String str, int iHex, int iDefault) {
        int iValue = iDefault;
        if (str == null) {
            return iValue;
        }
        try {
            iValue = Integer.parseInt(str, iHex);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return iValue;
    }

    private long strToLong(String value, int iHex, int iDefault) {
        long iValue = iDefault;
        if (value == null) {
            return iValue;
        }
        try {
            iValue = Long.parseLong(value, iHex);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return iValue;
    }

    /**
     * This is an example of this app accessing github in the tcp6 file, where 10126 is the uid of this app has been verified.
     * sl local_address remote_address st tx_queue rx_queue tr tm->when retrnsmt uid timeout inode
     * Each piece of information read is (in quotation marks):
     * "3: 0000000000000000FFFF00000F03000A:C204 0000000000000000FFFF000075D06071:0050 08 00000000:00000001 00:00000000 00000000 10126 0 38012 1 00000000 20 4 26 10 -1"
     * 0 1 2 3 4 5 6 7 8
     * Note: Since the string will be intercepted with spaces, the starting position of "3:" is 1.
     * Because some items have more than one space, we cannot simply use "" to distinguish them.
     * Here we use regular expressions: \s+ separated. You can separate multiple or one space.
     * The third item is the remote IP address and port item, and the eighth item is UID
     *
     * @param sData
     * @return
     */
    private NetInfo parseDataNew(@NonNull String sData) {
        String[] sSplitItem = sData.split("\\s+");
        if (sSplitItem.length < DATA_MIN_LENGTH) {
            return null;
        }
        String sTmp = null;
        NetInfo netInfo = new NetInfo();
        // Get local ip and port number
        sTmp = sSplitItem[DATA_LOCAL];
        String[] sSourceItem = sTmp.split(":");
        if (sSourceItem.length < DATA_IP_ADDRESS_PARTS) {
            return null;
        }
        netInfo.setSourPort(strToInt(sSourceItem[1], 16, 0));
        // Get remote ip and port number
        sTmp = sSplitItem[DATA_REMOTE];
        String[] sDesItem = sTmp.split(":");
        if (sDesItem.length < DATA_IP_ADDRESS_PARTS) {
            return null;
        }
        netInfo.setPort(strToInt(sDesItem[1], 16, 0));
        sTmp = sDesItem[0];
        int len = sTmp.length();
        if (len < IP_ADDRESS_MIN_LENGTH) {
            return null;
        }
        /**
         * The last 8 bits of the ip address are intercepted to adapt to IPv6. It should be that this address is very long in IPv6,
         * See the comment of {@link #parseDataNew(String)} for details
         */
        sTmp = sTmp.substring(len - 8);
        // Convert ip address to long and save
        netInfo.setIp(strToLong(sTmp, 16, 0));
        sbBuilder.setLength(0);
        // Get an ip address like 192.168.1.1
        sbBuilder.append(strToInt(sTmp.substring(6, 8), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(4, 6), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(2, 4), 16, 0))
                .append(".")
                .append(strToInt(sTmp.substring(0, 2), 16, 0));

        sTmp = sbBuilder.toString();
        netInfo.setAddress(sTmp);

        if (SYSTEM_DEFAULT_IP_ADDRESS.equals(sTmp)) {
            return null;
        }
        sTmp = sSplitItem[DATA_UID];
        netInfo.setUid(strToInt(sTmp, 10, 0));
        return netInfo;
    }

    private void saveToMap(NetInfo netInfo) {
        if (netInfo == null) {
            return;
        }
        Log.d(TAG, "saveToMap  port " + netInfo.getSourPort() + " uid " + netInfo.getUid());
        processHost.put(netInfo.getSourPort(), netInfo.getUid());
    }

    /**
     * Refresh the correspondence between app network request port and uid according to protocol type type
     *
     * @param type
     */
    public void read(int type) {
        try {
            switch (type) {
                case TYPE_TCP:
                    String[] ARGS = {"cat", "/proc/net/tcp"};
                    execute(ARGS, "/", TYPE_TCP);
                    break;
                case TYPE_TCP6:
                    String[] ARGS1 = {"cat", "/proc/net/tcp6"};
                    execute(ARGS1, "/", TYPE_TCP6);
                    break;
                case TYPE_UDP:
                    String[] ARGS2 = {"cat", "/proc/net/udp"};
                    execute(ARGS2, "/", TYPE_UDP);
                    break;
                case TYPE_UDP6:
                    String[] ARGS3 = {"cat", "/proc/net/udp6"};
                    execute(ARGS3, "/", TYPE_UDP6);
                    break;
                case TYPE_RAW:
                    String[] ARGS4 = {"cat", "/proc/net/raw"};
                    execute(ARGS4, "/", TYPE_UDP);
                    break;
                case TYPE_RAW6:
                    String[] ARGS5 = {"cat", "/proc/net/raw6"};
                    execute(ARGS5, "/", TYPE_UDP6);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the Android system has a network request, the configuration file will be changed.
     * If it detects that the last modification time of the configuration file has changed, it will refresh
     * Information in the corresponding file. Because the initial value of {@linkplain #lastTime}
     * is 0, all configuration files will be traversed the first time you call
     */
    public void refresh() {
        for (int i = 0; i < TYPE_MAX; i++) {
            long iTime = file[i].lastModified();
            if (iTime != lastTime[i]) {
                read(i);
                lastTime[i] = iTime;
            }
        }
    }

    /**
     * Return the uid of the process according to sourcePort (the port used by the process to access the network)
     *
     * @param sourcePort
     * @return
     */
    public Integer getUid(int sourcePort) {
        return processHost.get(sourcePort);
    }
}
