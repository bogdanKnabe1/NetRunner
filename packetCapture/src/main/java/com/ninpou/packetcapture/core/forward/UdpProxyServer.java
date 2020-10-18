package com.ninpou.packetcapture.core.forward;

import com.ninpou.packetcapture.core.tunnel.KeyHandler;
import com.ninpou.packetcapture.core.tunnel.UdpTunnel;
import com.ninpou.packetcapture.core.util.common.LruCache;
import com.ninpou.packetcapture.struct.Packet;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UdpProxyServer {
    private static final int MAX_UDP_CACHE_SIZE = 50;
    private final LruCache<Short, UdpTunnel> udpTunnels = new LruCache<>(MAX_UDP_CACHE_SIZE,
            new LruCache.CleanupCallback<UdpTunnel>() {
                @Override
                public void cleanUp(UdpTunnel udpTunnel) {
                    udpTunnel.close();
                }
            });
    private ConcurrentLinkedQueue<Packet> outputQueue;
    private Selector selector;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    int select = selector.select();
                    if (select == 0) {
                        Thread.sleep(5);
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isValid()) {
                            try {
                                Object attachment = key.attachment();
                                if (attachment instanceof KeyHandler) {
                                    ((KeyHandler) attachment).onKeyReady(key);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                        keyIterator.remove();
                    }
                }
            } catch (Exception ignored) {
            }
            stop();
        }
    };


    public UdpProxyServer(ConcurrentLinkedQueue<Packet> outputQueue) {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.outputQueue = outputQueue;
    }

    public void start() {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void processUdpPacket(Packet packet, short portKey) {
        UdpTunnel udpTunnel = getUdpTunnel(portKey);
        if (udpTunnel == null) {
            udpTunnel = new UdpTunnel(selector, this, packet, outputQueue, portKey);
            putUdpTunnel(portKey, udpTunnel);
            udpTunnel.initConnection();
        } else {
            udpTunnel.processPacket(packet);
        }
    }


    public void closeAllUdpTunnel() {
        synchronized (udpTunnels) {
            Iterator<Map.Entry<Short, UdpTunnel>> it = udpTunnels.entrySet().iterator();
            while (it.hasNext()) {
                it.next().getValue().close();
                it.remove();
            }
        }
    }

    public void closeUdpTunnel(UdpTunnel udpTunnel) {
        synchronized (udpTunnels) {
            udpTunnel.close();
            udpTunnels.remove(udpTunnel.getPortKey());
        }
    }

    private UdpTunnel getUdpTunnel(short portKey) {
        synchronized (udpTunnels) {
            return udpTunnels.get(portKey);
        }
    }

    private void putUdpTunnel(short ipAndPort, UdpTunnel udpTunnel) {
        synchronized (udpTunnels) {
            udpTunnels.put(ipAndPort, udpTunnel);
        }
    }

    private void stop() {
        try {
            selector.close();
            selector = null;
        } catch (Exception ignored) {
        }
    }
}