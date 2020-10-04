package com.ninpou.packetcapture.core.vpn;

import java.lang.ref.WeakReference;
import java.net.DatagramSocket;
import java.net.Socket;

public class VpnServiceProxy {
    private static WeakReference<VpnServiceImpl> vpnService = null;

    public static void setVpnService(VpnServiceImpl vpnService) {
        VpnServiceProxy.vpnService = new WeakReference<>(vpnService);
    }

    public static boolean protect(Socket socket) {
        if (vpnService == null || vpnService.get() == null) return false;
        return vpnService.get().protect(socket);
    }

    public static boolean protect(DatagramSocket socket) {
        if (vpnService == null || vpnService.get() == null) return false;
        return vpnService.get().protect(socket);
    }

    public static int getMtu() {
        return VpnServiceImpl.MTU;
    }

    public static String getAddress() {
        return VpnServiceImpl.ADDRESS;
    }

    public static long getStartTime() {
        if (vpnService == null || vpnService.get() == null) return -1;
        return vpnService.get().getStartTime();
    }
}
