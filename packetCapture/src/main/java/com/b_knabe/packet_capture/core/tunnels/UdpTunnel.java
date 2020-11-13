package com.b_knabe.packet_capture.core.tunnels;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.b_knabe.packet_capture.core.proxy_servers.UdpProxyServer;
import com.b_knabe.packet_capture.core.nat.NatSession;
import com.b_knabe.packet_capture.core.nat.NatSessionManager;
import com.b_knabe.packet_capture.core.util.common.ACache;
import com.b_knabe.packet_capture.core.util.common.IOUtils;
import com.b_knabe.packet_capture.core.util.common.ThreadPool;
import com.b_knabe.packet_capture.core.util.common.TimeFormatter;
import com.b_knabe.packet_capture.core.util.net_utils.tcp.TcpDataSaver;
import com.b_knabe.packet_capture.core.util.process_parse.PortSessionInfoService;
import com.b_knabe.packet_capture.core.vpn.VpnProxyServer;
import com.b_knabe.packet_capture.tcp_ip_level.Packet;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpTunnel implements KeyHandler {
    private static final String TAG = "UdpTunnel";
    private static final int HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE;
    private final Selector selector;
    private final UdpProxyServer udpProxyServer;
    private final Queue<Packet> outputQueue;
    private final ConcurrentLinkedQueue<Packet> toNetWorkPackets = new ConcurrentLinkedQueue<>();
    private final NatSession session;
    private final Handler handler;
    private String ipAndPort;
    private TcpDataSaver tcpDataSaver;
    private Packet referencePacket;
    private SelectionKey selectionKey;
    private DatagramChannel channel;
    private Short portKey;

    public UdpTunnel(Selector selector, UdpProxyServer udpProxyServer, Packet packet, Queue<Packet> outputQueue, short portKey) {
        this.selector = selector;
        this.udpProxyServer = udpProxyServer;
        this.referencePacket = packet;
        ipAndPort = packet.getIpAndPort();
        this.outputQueue = outputQueue;
        this.portKey = portKey;
        session = NatSessionManager.getSession(portKey);
        handler = new Handler(Looper.getMainLooper());
        String dir = new StringBuilder()
                .append(TcpDataSaver.DATA_DIR)
                .append(TimeFormatter.formatToYYMMDDHHMMSS(session.vpnStartTime))
                .append("/")
                .append(session.getUniqueName())
                .toString();
        tcpDataSaver = new TcpDataSaver(dir);
    }


    private void processKey(SelectionKey key) {
        if (key.isWritable()) {
            processSend();
        } else if (key.isReadable()) {
            processReceived();
        }
        updateInterests();
    }

    private void processReceived() {
        ByteBuffer receiveBuffer = ByteBuffer.allocate(VpnProxyServer.getMtu());
        // Leave space for the header
        receiveBuffer.position(HEADER_SIZE);
        int readBytes;
        try {
            readBytes = channel.read(receiveBuffer);
        } catch (Exception e) {
            udpProxyServer.closeUdpTunnel(this);
            return;
        }
        if (readBytes == -1) {
            udpProxyServer.closeUdpTunnel(this);
        } else if (readBytes == 0) {
            Log.d(TAG, "read no data :" + ipAndPort);
        } else {
            Log.d(TAG, "read readBytes:" + readBytes + " ipAndPort:" + ipAndPort);
            Packet newPacket = referencePacket.duplicated();
            newPacket.updateUDPBuffer(receiveBuffer, readBytes);
            receiveBuffer.position(HEADER_SIZE + readBytes);
            outputQueue.offer(newPacket);
            session.receivePacketNum++;
            session.receiveByteNum += readBytes;
            session.lastRefreshTime = System.currentTimeMillis();
            if (tcpDataSaver != null) {
                saveData(receiveBuffer.array(), readBytes, false);
            }
        }
    }

    private void saveData(byte[] array, int saveSize, boolean isRequest) {
        TcpDataSaver.TcpData saveTcpData = new TcpDataSaver.TcpData
                .Builder()
                .offSet(HEADER_SIZE)
                .length(saveSize)
                .needParseData(array)
                .isRequest(isRequest)
                .build();
        tcpDataSaver.addData(saveTcpData);
    }

    private void processSend() {
        Log.d(TAG, "processWriteUDPData " + ipAndPort);
        Packet toNetWorkPacket = getToNetWorkPackets();
        if (toNetWorkPacket == null) {
            Log.d(TAG, "write data  no packet ");
            return;
        }
        try {
            ByteBuffer payloadBuffer = toNetWorkPacket.backingBuffer;
            session.packetSent++;
            int sendSize = payloadBuffer.limit() - payloadBuffer.position();
            session.bytesSent += sendSize;
            if (tcpDataSaver != null) {
                saveData(payloadBuffer.array(), sendSize, true);
            }
            session.lastRefreshTime = System.currentTimeMillis();
            while (payloadBuffer.hasRemaining()) {
                channel.write(payloadBuffer);
            }
        } catch (IOException e) {
            Log.w(TAG, "Network write error: " + ipAndPort, e);
            udpProxyServer.closeUdpTunnel(this);
        }
    }

    public void initConnection() {
        Log.d(TAG, "init  ipAndPort:" + ipAndPort);
        InetAddress destinationAddress = referencePacket.ip4Header.destinationAddress;
        int destinationPort = referencePacket.udpHeader.destinationPort;
        try {
            channel = DatagramChannel.open();
            VpnProxyServer.protect(channel.socket());
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress(destinationAddress, destinationPort));
            selector.wakeup();
            selectionKey = channel.register(selector,
                    SelectionKey.OP_READ, this);
        } catch (IOException e) {
            IOUtils.close(channel);
            return;
        }
        referencePacket.swapSourceAndDestination();
        addToNetWorkPacket(referencePacket);
    }

    public void processPacket(Packet packet) {
        addToNetWorkPacket(packet);
        updateInterests();
    }

    public void close() {
        try {
            if (selectionKey != null) {
                selectionKey.cancel();
            }
            if (channel != null) {
                channel.close();
            }
            if (session.applicationInfo == null && PortSessionInfoService.getInstance() != null) {
                PortSessionInfoService.getInstance().refreshSessionInfo();
            }
            // Need to delay one second before saving and wait until the app information is completely refreshed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ThreadPool.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (session.receiveByteNum == 0 && session.bytesSent == 0) {
                                return;
                            }
                            String configFileDir = TcpDataSaver.CONFIG_DIR
                                    + TimeFormatter.formatToYYMMDDHHMMSS(session.vpnStartTime);
                            File parentFile = new File(configFileDir);
                            if (!parentFile.exists()) {
                                parentFile.mkdirs();
                            }
                            //Said it has been saved
                            File file = new File(parentFile, session.getUniqueName());
                            if (file.exists()) {
                                return;
                            }
                            ACache configACache = ACache.get(parentFile);
                            configACache.put(session.getUniqueName(), session);
                        }
                    });
                }
            }, 1000);
        } catch (Exception e) {
            Log.w(TAG, "error to close UDP channel IpAndPort" + ipAndPort + ",error is " + e.getMessage());
        }

    }


    public Packet getToNetWorkPackets() {
        return toNetWorkPackets.poll();
    }

    private void addToNetWorkPacket(Packet packet) {
        toNetWorkPackets.offer(packet);
        updateInterests();
    }

    DatagramChannel getChannel() {
        return channel;
    }

    private void updateInterests() {
        int ops;
        if (toNetWorkPackets.isEmpty()) {
            ops = SelectionKey.OP_READ;
        } else {
            ops = SelectionKey.OP_WRITE | SelectionKey.OP_READ;
        }
        selector.wakeup();
        selectionKey.interestOps(ops);
    }

    public Packet getReferencePacket() {
        return referencePacket;
    }

    @Override
    public void onKeyReady(SelectionKey key) {
        processKey(key);
    }

    public Short getPortKey() {
        return portKey;
    }
}

