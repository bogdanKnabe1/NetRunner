package com.ninpou.packetcapture.core.util.net_utils;

import com.ninpou.packetcapture.tcp_ip_level.IpPacketHeader;
import com.ninpou.packetcapture.tcp_ip_level.TcpPacketHeader;
import com.ninpou.packetcapture.tcp_ip_level.UdpPacketHeader;

/**!
 * Low-level java
 * Network Event Handler
 * IMMUTABLE
 * */

public class Packets {
    public static int readInt(byte[] data, int offset) {
        return ((data[offset] & 0xFF) << 24)
                | ((data[offset + 1] & 0xFF) << 16)
                | ((data[offset + 2] & 0xFF) << 8)
                | (data[offset + 3] & 0xFF);
    }

    public static short readShort(byte[] data, int offset) {
        int ren = ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF);
        return (short) ren;
    }

    public static void writeInt(byte[] data, int offset, int value) {
        data[offset] = (byte) (value >> 24);
        data[offset + 1] = (byte) (value >> 16);
        data[offset + 2] = (byte) (value >> 8);
        data[offset + 3] = (byte) value;
    }

    public static void writeShort(byte[] data, int offset, short value) {
        data[offset] = (byte) (value >> 8);
        data[offset + 1] = (byte) (value);
    }

    public static String ipToString(int value) {
        return String.format("%s.%s.%s.%s", (value >> 24) & 0x00FF,
                (value >> 16) & 0x00FF, (value >> 8) & 0x00FF, (value & 0x00FF));
    }

    public static int ipToInt(String value) {
        String[] arrayStrings = value.split("\\.");
        return (Integer.parseInt(arrayStrings[0]) << 24)
                | (Integer.parseInt(arrayStrings[1]) << 16)
                | (Integer.parseInt(arrayStrings[2]) << 8)
                | (Integer.parseInt(arrayStrings[3]));
    }

    public static long getSum(byte[] buf, int offset, int len) {
        long sum = 0;
        while (len > 1) {
            sum += readShort(buf, offset) & 0xFFFF;
            offset += 2;
            len -= 2;
        }
        if (len > 0) {
            sum += (buf[offset] & 0xFF) << 8;
        }
        return sum;
    }

    public static short checksum(long sum, byte[] buf, int offset, int len) {
        sum += getSum(buf, offset, len);
        while ((sum >> 16) > 0) {
            sum = (sum & 0xFFFF) + (sum >> 16);
        }
        return (short) ~sum;
    }

    public static boolean computeIpChecksum(IpPacketHeader ipPacketHeader) {
        short oldCrc = ipPacketHeader.getCrc();
        ipPacketHeader.setCrc((short) 0);
        short newCrc = checksum(0, ipPacketHeader.data, ipPacketHeader.offset, ipPacketHeader.getHeaderLength());
        ipPacketHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }

    public static boolean computeTcpChecksum(IpPacketHeader ipPacketHeader, TcpPacketHeader tcpPacketHeader) {
        computeIpChecksum(ipPacketHeader);
        int ipData_len = ipPacketHeader.getDataLength();
        if (ipData_len < 0) {
            return false;
        }
        long sum = getSum(ipPacketHeader.data, ipPacketHeader.offset + IpPacketHeader.offset_src_ip, 8);
        sum += ipPacketHeader.getProtocol() & 0xFF;
        sum += ipData_len;

        short oldCrc = tcpPacketHeader.getCrc();
        tcpPacketHeader.setCrc((short) 0);
        short newCrc = checksum(sum, tcpPacketHeader.data, tcpPacketHeader.offset, ipData_len);
        tcpPacketHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }

    public static boolean computeUdpChecksum(IpPacketHeader ipPacketHeader, UdpPacketHeader udpPacketHeader) {
        computeIpChecksum(ipPacketHeader);
        int ipData_len = ipPacketHeader.getDataLength();
        if (ipData_len < 0) {
            return false;
        }
        long sum = getSum(ipPacketHeader.data, ipPacketHeader.offset + IpPacketHeader.offset_src_ip, 8);
        sum += ipPacketHeader.getProtocol() & 0xFF;
        sum += ipData_len;
        short oldCrc = udpPacketHeader.getCrc();
        udpPacketHeader.setCrc((short) 0);
        short newCrc = checksum(sum, udpPacketHeader.data, udpPacketHeader.offset, ipData_len);
        udpPacketHeader.setCrc(newCrc);
        return oldCrc == newCrc;
    }
}
