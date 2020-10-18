package com.ninpou.packetcapture.core.util.common;

import java.io.IOException;
import java.util.*;

public class Shells {
    public static String getDns() {
        Scanner scanner = null;
        try {
            Process process = Runtime.getRuntime().exec("getprop net.dns1");
            scanner = new Scanner(process.getInputStream());
            return scanner.nextLine();
        } catch (IOException e) {
            return null;
        } finally {
            IOUtils.close(scanner);
        }
    }
}
