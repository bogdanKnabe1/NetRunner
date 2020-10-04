package com.ninpou.packetcapture.core.forward;

import android.util.Log;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.tunnel.BaseTcpTunnel;
import com.ninpou.packetcapture.core.tunnel.KeyHandler;
import com.ninpou.packetcapture.core.tunnel.TunnelFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


public class TcpProxyServer {
    private static final String TAG = "TcpProxyServer";
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private Thread serverThread;
    private boolean stopped;
    private short port;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    int select = selector.select();
                    if (select == 0) {
                        Thread.sleep(5);
                        continue;
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    if (selectionKeys == null) {
                        continue;
                    }
                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        if (key.isValid()) {
                            try {
                                if (key.isAcceptable()) {
                                    Log.d(TAG, "isAcceptable");
                                    onAccepted(key);
                                } else {
                                    Object attachment = key.attachment();
                                    if (attachment instanceof KeyHandler) {
                                        ((KeyHandler) attachment).onKeyReady(key);
                                    }
                                }

                            } catch (Exception ignored) {
                            }
                        }
                        keyIterator.remove();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                stop();
            }
        }
    };
    public TcpProxyServer(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.port = (short) serverSocketChannel.socket().getLocalPort();
    }

    public boolean isStopped() {
        return stopped;
    }

    public short getPort() {
        return port;
    }

    public void start() {
        serverThread = new Thread(runnable);
        serverThread.start();
    }

    public void stop() {
        stopped = true;
        if (selector != null) {
            try {
                selector.close();
                selector = null;
            } catch (Exception ignored) {
            }
        }
        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
                serverSocketChannel = null;
            } catch (Exception ignored) {
            }
        }
    }

    private InetSocketAddress getDestAddress(SocketChannel localChannel) {
        short portKey = (short) localChannel.socket().getPort();
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            return new InetSocketAddress(localChannel.socket().getInetAddress(), session.remotePort & 0xFFFF);
        }
        return null;
    }

    private void onAccepted(SelectionKey key) {
        BaseTcpTunnel localTunnel = null;
        try {
            SocketChannel localChannel = serverSocketChannel.accept();
            localTunnel = TunnelFactory.wrap(localChannel, selector);
            short portKey = (short) localChannel.socket().getPort();
            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                BaseTcpTunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, selector, portKey);
                remoteTunnel.setIsHttpsRequest(localTunnel.isHttpsRequest());
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                remoteTunnel.connect(destAddress);
            }
        } catch (Exception ex) {
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }
}
