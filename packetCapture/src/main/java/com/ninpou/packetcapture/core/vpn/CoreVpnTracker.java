package com.ninpou.packetcapture.core.vpn;


import com.ninpou.packetcapture.QbitsApp;
import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.proxy_servers.TcpProxyServer;
import com.ninpou.packetcapture.core.proxy_servers.UdpProxyServer;
import com.ninpou.packetcapture.core.util.common.IOUtils;
import com.ninpou.packetcapture.core.util.common.ThreadPool;
import com.ninpou.packetcapture.core.util.net_utils.HttpRequestHeaderParser;
import com.ninpou.packetcapture.core.util.net_utils.Packets;
import com.ninpou.packetcapture.core.util.process_parse.PortSessionInfoService;
import com.ninpou.packetcapture.tcp_ip_level.IpPacketHeader;
import com.ninpou.packetcapture.tcp_ip_level.Packet;
import com.ninpou.packetcapture.tcp_ip_level.TcpPacketHeader;
import com.ninpou.packetcapture.tcp_ip_level.UdpPacketHeader;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CoreVpnTracker implements Runnable {
    private TcpProxyServer tcpProxyServer;
    private UdpProxyServer udpProxyServer;
    private OutputStream outPutStream;
    private InputStream inPutStream;
    private ConcurrentLinkedQueue<Packet> udpQueue;

    CoreVpnTracker(FileDescriptor fd) {
        inPutStream = new FileInputStream(fd);
        outPutStream = new FileOutputStream(fd);
        udpQueue = new ConcurrentLinkedQueue<>();
        try {
            tcpProxyServer = new TcpProxyServer(0);
            tcpProxyServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        udpProxyServer = new UdpProxyServer(udpQueue);
        udpProxyServer.start();
        PortSessionInfoService.startParse(QbitsApp.getAppContext());
    }

    private boolean handlePacket(byte[] data, int len) throws IOException {
        IpPacketHeader ipPacketHeader = new IpPacketHeader(data, 0);
        switch (ipPacketHeader.getProtocol()) {
            case IpPacketHeader.TCP:
                return disassembleTcpPacket(ipPacketHeader, len);
            case IpPacketHeader.UDP:
                return disassembleUdpPacket(ipPacketHeader, len);
            default:
                return false;
        }
    }

    private boolean disassembleTcpPacket(IpPacketHeader ipPacketHeader, int len) throws IOException {
        TcpPacketHeader tcpPacketHeader = new TcpPacketHeader(ipPacketHeader.data, ipPacketHeader.getHeaderLength());
        short srcPort = tcpPacketHeader.getSourcePort();
        if (srcPort == tcpProxyServer.getPort()) {
            NatSession session = NatSessionManager.getSession(tcpPacketHeader.getDestinationPort());
            if (session != null) {
                ipPacketHeader.setSourceIP(ipPacketHeader.getDestinationIP());
                tcpPacketHeader.setSourcePort(session.remotePort);
                ipPacketHeader.setDestinationIP(Packets.ipToInt(VpnProxyServer.getAddress()));
                Packets.computeTcpChecksum(ipPacketHeader, tcpPacketHeader);
                outPutStream.write(ipPacketHeader.data, ipPacketHeader.offset, len);
            }
        } else {
            NatSession session = NatSessionManager.getSession(srcPort);
            if (session == null || session.remoteIP != ipPacketHeader.getDestinationIP()
                    || session.remotePort != tcpPacketHeader.getDestinationPort()) {
                session = NatSessionManager.createSession(srcPort, ipPacketHeader.getDestinationIP(), tcpPacketHeader
                        .getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = VpnProxyServer.getStartTime();
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        PortSessionInfoService instance = PortSessionInfoService.getInstance();
                        if (instance != null) {
                            instance.refreshSessionInfo();
                        }
                    }
                });
            }
            session.lastRefreshTime = System.currentTimeMillis();
            session.packetSent++;
            int tcpDataSize = ipPacketHeader.getDataLength() - tcpPacketHeader.getHeaderLength();
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false;
            }

            if (session.bytesSent == 0 && tcpDataSize > 10) {
                int dataOffset = tcpPacketHeader.offset + tcpPacketHeader.getHeaderLength();
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpPacketHeader.data, dataOffset,
                        tcpDataSize);
            } else if (session.bytesSent > 0
                    && !session.isHttpsSession
                    && session.isHttp
                    && session.remoteHost == null
                    && session.requestUrl == null) {
                int dataOffset = tcpPacketHeader.offset + tcpPacketHeader.getHeaderLength();
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpPacketHeader.data, dataOffset,
                        tcpDataSize);
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl;
            }
            ipPacketHeader.setSourceIP(ipPacketHeader.getDestinationIP());
            ipPacketHeader.setDestinationIP(Packets.ipToInt(VpnProxyServer.getAddress()));
            tcpPacketHeader.setDestinationPort(tcpProxyServer.getPort());
            Packets.computeTcpChecksum(ipPacketHeader, tcpPacketHeader);
            outPutStream.write(ipPacketHeader.data, ipPacketHeader.offset, len);
            session.bytesSent += tcpDataSize;
        }
        VpnEventHandler.getInstance().notifyReceive();
        return true;
    }

    private boolean disassembleUdpPacket(IpPacketHeader ipPacketHeader, int len) throws UnknownHostException {
        UdpPacketHeader udpPacketHeader = new UdpPacketHeader(ipPacketHeader.data, ipPacketHeader.getHeaderLength());
        short srcPort = udpPacketHeader.getSourcePort();
        NatSession session = NatSessionManager.getSession(srcPort);
        if (session == null || session.remoteIP != ipPacketHeader.getDestinationIP() || session.remotePort
                != udpPacketHeader.getDestinationPort()) {
            session = NatSessionManager.createSession(srcPort, ipPacketHeader.getDestinationIP(), udpPacketHeader
                    .getDestinationPort(), NatSession.UDP);
            session.vpnStartTime = VpnProxyServer.getStartTime();
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (PortSessionInfoService.getInstance() != null) {
                        PortSessionInfoService.getInstance().refreshSessionInfo();
                    }
                }
            });
        }
        session.lastRefreshTime = System.currentTimeMillis();
        session.packetSent++;
        byte[] bytes = Arrays.copyOf(ipPacketHeader.data, ipPacketHeader.data.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, len);
        byteBuffer.limit(len);
        Packet packet = new Packet(byteBuffer);
        udpProxyServer.processUdpPacket(packet, srcPort);
        return false;
    }

    @Override
    public void run() {
        byte[] buf = new byte[VpnServiceImpl.MTU];
        try {
            while (!Thread.interrupted()) {
                int len = inPutStream.read(buf);
                if (len == 0) {
                    Thread.sleep(10);
                    continue;
                } else if (len == -1) {
                    break;
                }
                if (!handlePacket(buf, len)) {
                    Packet packet = udpQueue.poll();
                    if (packet == null) continue;
                    ByteBuffer buffer = packet.backingBuffer;
                    outPutStream.write(buffer.array());
                }
            }
        } catch (IOException | InterruptedException ignored) {
        } finally {
            IOUtils.close(inPutStream, outPutStream);
            tcpProxyServer.stop();
            udpProxyServer.closeAllUdpTunnel();
            NatSessionManager.clearAllSession();
            PortSessionInfoService.stopParse(QbitsApp.getAppContext());
        }
    }
}
