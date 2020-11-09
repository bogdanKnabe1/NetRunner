package com.b_knabe.packet_capture.core.util.process_parse;

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
public class NetWorkFileManager {
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

    public static NetWorkFileManager getInstance() {
        return Singleton.instance;
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

    public void execute(String[] cmmand, String directory, int type) throws IOException {
        NetworkInfo networkInfo = null;
        String sTmp = null;

        ProcessBuilder builder = new ProcessBuilder(cmmand);

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
            networkInfo = parseDataNew(sTmp);
            if (networkInfo != null) {
                networkInfo.setType(type);
                saveToMap(networkInfo);
            }
        }
    }

    /**
     * Convert a string to int data, and return the default value iDefault when the incoming string str is null. One of the functions in this class is:
     * Convert the port number in string format obtained from the ip address to int type
     *
     * @paramStr The string type of the port number, generally four digits in hexadecimal xxxx.
     * @paramIHex str is in decimal
     * @paramIDefault The default return value if str is null
     * @return
     */
    private int strToInt(String value, int iHex, int iDefault) {
        int iValue = iDefault;
        if (value == null) {
            return iValue;
        }

        try {
            iValue = Integer.parseInt(value, iHex);
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
     * This is an example of this app accessing github in the tcp6 file, where 10126 is the verified uid of this app.
     * Each piece of information read is (in quotation marks):
     * "3: 0000000000000000FFFF00000F03000A:C204 0000000000000000FFFF000075D06071:0050 08 00000000:00000001 00:00000000 00000000 10126 0 38012 1 00000000 20 4 26 10 -1"
     * 0 1 2 3 4 5 6 7 8
     * Note: Since the string will be intercepted with spaces, the starting position of "3:" is 1.
     * Because some items have more than one space, we cannot simply use "" to distinguish them. Here we use regular expressions:
     * \s+ separated. You can separate multiple or one space.
     * The third item is the remote IP address and port item, and the eighth item is UID
     *
     */
    private NetworkInfo parseDataNew(String sData) {
        String sSplitItem[] = sData.split("\\s+");
        String sTmp = null;
        if (sSplitItem.length < 9) {
            return null;
        }

        NetworkInfo networkInfo = new NetworkInfo();
        // Get local ip and port number
        sTmp = sSplitItem[DATA_LOCAL];
        String sSourceItem[] = sTmp.split(":");
        if (sSourceItem.length < 2) {
            return null;
        }
        networkInfo.setSourPort(strToInt(sSourceItem[1], 16, 0));
        // 取得远程 ip 和 端口号

        sTmp = sSplitItem[DATA_REMOTE];
        String sDesItem[] = sTmp.split(":");
        if (sDesItem.length < 2) {
            return null;
        }
        networkInfo.setPort(strToInt(sDesItem[1], 16, 0));
        /**
         * The last 8 bits of the ip address are intercepted to adapt to IPv6. It should be that this address is very long in IPv6,
         * See the comment of {@link #parseDataNew(String)} for details
         */
        sTmp = sDesItem[0];
        // Convert the ip address to long and save
        int len = sTmp.length();
        if (len < 8) {
            return null;
        }

        sTmp = sTmp.substring(len - 8);
        networkInfo.setIp(strToLong(sTmp, 16, 0));

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
        networkInfo.setAddress(sTmp);

        if (sTmp.equals("0.0.0.0")) {
            return null;
        }

        sTmp = sSplitItem[DATA_UID];
        networkInfo.setUid(strToInt(sTmp, 10, 0));

        return networkInfo;
    }

    private void saveToMap(NetworkInfo networkInfo) {
        if (networkInfo == null) {
            return;
        }
        processHost.put(networkInfo.getSourPort(), networkInfo.getUid());
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
     * When the Android system has a network request, the configuration file will be changed. If it detects that the last modification time of the configuration file has changed, it will refresh
     * Information in the corresponding file. Because the initial value of {@linkplain #lastTime} is 0, all configuration files will be traversed the first time it is called
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
     * @paramSourcePort
     * @return
     */
    public Integer getUid(int port) {
        return processHost.get(port);
    }

    static class Singleton {
        static NetWorkFileManager instance = new NetWorkFileManager();
    }
}
