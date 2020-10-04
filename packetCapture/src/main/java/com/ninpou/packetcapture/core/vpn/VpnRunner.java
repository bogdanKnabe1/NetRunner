package com.ninpou.packetcapture.core.vpn;

import com.ninpou.packetcapture.core.forward.TcpProxyServer;
import com.ninpou.packetcapture.core.forward.UdpProxyServer;
import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.util.android.PortHostService;
import com.ninpou.packetcapture.core.util.common.ThreadPool;
import com.ninpou.packetcapture.core.util.net.HttpRequestHeaderParser;
import com.ninpou.packetcapture.core.util.net.Packets;
import com.ninpou.packetcapture.struct.IpHeader;
import com.ninpou.packetcapture.struct.Packet;
import com.ninpou.packetcapture.struct.TcpHeader;
import com.ninpou.packetcapture.struct.UdpHeader;

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

import top.srsea.lever.Lever;
import top.srsea.torque.common.IOUtils;

public class VpnRunner implements Runnable {
    private TcpProxyServer tcpProxyServer;
    private UdpProxyServer udpProxyServer;
    private OutputStream out;
    private InputStream in;
    private ConcurrentLinkedQueue<Packet> udpQueue;

    VpnRunner(FileDescriptor fd) {
        in = new FileInputStream(fd);
        out = new FileOutputStream(fd);
        udpQueue = new ConcurrentLinkedQueue<>();
        try {
            tcpProxyServer = new TcpProxyServer(0);
            tcpProxyServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        udpProxyServer = new UdpProxyServer(udpQueue);
        udpProxyServer.start();
        PortHostService.startParse(Lever.getContext());
    }

    private boolean handlePacket(byte[] data, int len) throws IOException {
        IpHeader ipHeader = new IpHeader(data, 0);
        switch (ipHeader.getProtocol()) {
            case IpHeader.TCP:
                return handleTcpPacket(ipHeader, len);
            case IpHeader.UDP:
                return handleUdpPacket(ipHeader, len);
            default:
                return false;
        }
    }

    private boolean handleTcpPacket(IpHeader ipHeader, int len) throws IOException {
        TcpHeader tcpHeader = new TcpHeader(ipHeader.data, ipHeader.getHeaderLength());
        short srcPort = tcpHeader.getSourcePort();
        if (srcPort == tcpProxyServer.getPort()) {
            NatSession session = NatSessionManager.getSession(tcpHeader.getDestinationPort());
            if (session != null) {
                ipHeader.setSourceIP(ipHeader.getDestinationIP());
                tcpHeader.setSourcePort(session.remotePort);
                ipHeader.setDestinationIP(Packets.ipToInt(VpnServiceProxy.getAddress()));
                Packets.computeTcpChecksum(ipHeader, tcpHeader);
                out.write(ipHeader.data, ipHeader.offset, len);
            }
        } else {
            NatSession session = NatSessionManager.getSession(srcPort);
            if (session == null || session.remoteIP != ipHeader.getDestinationIP()
                    || session.remotePort != tcpHeader.getDestinationPort()) {
                session = NatSessionManager.createSession(srcPort, ipHeader.getDestinationIP(), tcpHeader
                        .getDestinationPort(), NatSession.TCP);
                session.vpnStartTime = VpnServiceProxy.getStartTime();
                ThreadPool.getInstance().execute(new Runnable() {
                    @Override
                    public void run() {
                        PortHostService instance = PortHostService.getInstance();
                        if (instance != null) {
                            instance.refreshSessionInfo();
                        }
                    }
                });
            }
            session.lastRefreshTime = System.currentTimeMillis();
            session.packetSent++;
            int tcpDataSize = ipHeader.getDataLength() - tcpHeader.getHeaderLength();
            if (session.packetSent == 2 && tcpDataSize == 0) {
                return false;
            }

            if (session.bytesSent == 0 && tcpDataSize > 10) {
                int dataOffset = tcpHeader.offset + tcpHeader.getHeaderLength();
                HttpRequestHeaderParser.parseHttpRequestHeader(session, tcpHeader.data, dataOffset,
                        tcpDataSize);
            } else if (session.bytesSent > 0
                    && !session.isHttpsSession
                    && session.isHttp
                    && session.remoteHost == null
                    && session.requestUrl == null) {
                int dataOffset = tcpHeader.offset + tcpHeader.getHeaderLength();
                session.remoteHost = HttpRequestHeaderParser.getRemoteHost(tcpHeader.data, dataOffset,
                        tcpDataSize);
                session.requestUrl = "http://" + session.remoteHost + "/" + session.pathUrl;
            }
            ipHeader.setSourceIP(ipHeader.getDestinationIP());
            ipHeader.setDestinationIP(Packets.ipToInt(VpnServiceProxy.getAddress()));
            tcpHeader.setDestinationPort(tcpProxyServer.getPort());
            Packets.computeTcpChecksum(ipHeader, tcpHeader);
            out.write(ipHeader.data, ipHeader.offset, len);
            session.bytesSent += tcpDataSize;
        }
        VpnEvent.getInstance().notifyReceive();
        return true;
    }

    private boolean handleUdpPacket(IpHeader ipHeader, int len) throws UnknownHostException {
        UdpHeader udpHeader = new UdpHeader(ipHeader.data, ipHeader.getHeaderLength());
        short srcPort = udpHeader.getSourcePort();
        NatSession session = NatSessionManager.getSession(srcPort);
        if (session == null || session.remoteIP != ipHeader.getDestinationIP() || session.remotePort
                != udpHeader.getDestinationPort()) {
            session = NatSessionManager.createSession(srcPort, ipHeader.getDestinationIP(), udpHeader
                    .getDestinationPort(), NatSession.UDP);
            session.vpnStartTime = VpnServiceProxy.getStartTime();
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    if (PortHostService.getInstance() != null) {
                        PortHostService.getInstance().refreshSessionInfo();
                    }
                }
            });
        }
        session.lastRefreshTime = System.currentTimeMillis();
        session.packetSent++;
        byte[] bytes = Arrays.copyOf(ipHeader.data, ipHeader.data.length);
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
                int len = in.read(buf);
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
                    out.write(buffer.array());
                }
            }
        } catch (IOException | InterruptedException ignored) {
        } finally {
            IOUtils.close(in, out);
            tcpProxyServer.stop();
            udpProxyServer.closeAllUdpTunnel();
            NatSessionManager.clearAllSession();
            PortHostService.stopParse(Lever.getContext());
        }
    }
}
