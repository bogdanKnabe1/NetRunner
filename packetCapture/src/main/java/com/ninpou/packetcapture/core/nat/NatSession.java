package com.ninpou.packetcapture.core.nat;


import com.ninpou.packetcapture.core.util.net_utils.Packets;
import com.ninpou.packetcapture.core.util.process_parse.ApplicationInfo;

import java.io.Serializable;
import java.util.Locale;

public class NatSession implements Serializable {
    public static final String TCP = "TCP";
    public static final String UDP = "UDP";
    public String type;
    public String ipAndPort;
    public int remoteIP;
    public short remotePort;
    public String remoteHost;
    public short localPort;
    public int bytesSent;
    public int packetSent;
    public long receiveByteNum;
    public long receivePacketNum;
    public long lastRefreshTime;
    public boolean isHttpsSession;
    public String requestUrl;
    public String pathUrl;
    public String method;
    public ApplicationInfo applicationInfo;
    public long connectionStartTime = System.currentTimeMillis();
    public long vpnStartTime;
    public boolean isHttp;

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s/%s:%d packet: %d",
                remoteHost, Packets.ipToString(remoteIP),
                remotePort & 0xFFFF, packetSent);
    }

    public String getUniqueName() {
        String uinID = ipAndPort + connectionStartTime;
        return String.valueOf(uinID.hashCode());
    }

    public void refreshIpAndPort() {
        int remoteIPStr1 = (remoteIP & 0XFF000000) >> 24 & 0XFF;
        int remoteIPStr2 = (remoteIP & 0X00FF0000) >> 16;
        int remoteIPStr3 = (remoteIP & 0X0000FF00) >> 8;
        int remoteIPStr4 = remoteIP & 0X000000FF;
        String remoteIPStr = "" + remoteIPStr1 + ":" + remoteIPStr2 + ":" + remoteIPStr3 + ":" + remoteIPStr4;
        ipAndPort = type + ":" + remoteIPStr + ":" + remotePort + " " + ((int) localPort & 0XFFFF);
    }

    public String getType() {
        return type;
    }

    public String getIpAndPort() {
        return ipAndPort;
    }

    public int getRemoteIP() {
        return remoteIP;
    }

    public short getRemotePort() {
        return remotePort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public short getLocalPort() {
        return localPort;
    }

    public int getBytesSent() {
        return bytesSent;
    }

    public int getPacketSent() {
        return packetSent;
    }

    public long getReceiveByteNum() {
        return receiveByteNum;
    }

    public long getReceivePacketNum() {
        return receivePacketNum;
    }

    public long getRefreshTime() {
        return lastRefreshTime;
    }

    public boolean isHttpsSession() {
        return isHttpsSession;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public String getPathUrl() {
        return pathUrl;
    }

    public String getMethod() {
        return method;
    }

    public ApplicationInfo getApplicationInfo() {
        return applicationInfo;
    }

    public long getConnectionStartTime() {
        return connectionStartTime;
    }

    public long getVpnStartTime() {
        return vpnStartTime;
    }
}
