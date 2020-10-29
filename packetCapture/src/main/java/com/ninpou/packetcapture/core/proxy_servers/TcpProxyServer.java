package com.ninpou.packetcapture.core.proxy_servers;

import android.util.Log;
import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.tunnels.DefaultTcpTunnel;
import com.ninpou.packetcapture.core.tunnels.KeyHandler;
import com.ninpou.packetcapture.core.tunnels.TunnelFactory;


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

    /**
     * When establishing a new TCP and UDP socket connection, you need to specify the port number for them. In order to avoid this practice of writing dead port numbers
     * Or to find the available port from the local system. Network programmers can use port number 0 as the connection parameter.
     * In this case, the operating system will search for the next available port number from the dynamic port number range.
     * Windows system and other operating systems have some slight differences in handling port number 0.
     * @param port just pass 0
     */
    public TcpProxyServer(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        // Register Channel to Selector, Channel must be non-blocking.
        // So FileChannel does not apply to Selector, because FileChannel cannot be switched to non-blocking mode, more accurately
        // Because FileChannel does not inherit SelectableChannel. Socket channel can be used normally.
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // When the passed port is 0, the system will randomly distribute a usable port
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

    /**
     * Get an InetSocketAddress from the TCP server to the original destination address interface
     * @param localChannel
     * @return
     */
    private InetSocketAddress getDestAddress(SocketChannel localChannel) {
        // Port number occupied by the app used
        short portKey = (short) localChannel.socket().getPort();
        // Get the web session information of the app
        NatSession session = NatSessionManager.getSession(portKey);
        if (session != null) {
            return new InetSocketAddress(localChannel.socket().getInetAddress(), session.remotePort & 0xFFFF);
        }
        return null;
    }

    private void onAccepted(SelectionKey key) {
        DefaultTcpTunnel localTunnel = null;
        try {
            SocketChannel localChannel = serverSocketChannel.accept();
            // Port number occupied by the app used
            localTunnel = TunnelFactory.wrap(localChannel, selector);
            short portKey = (short) localChannel.socket().getPort();
            InetSocketAddress destAddress = getDestAddress(localChannel);
            if (destAddress != null) {
                DefaultTcpTunnel remoteTunnel = TunnelFactory.createTunnelByConfig(destAddress, selector, portKey);
                remoteTunnel.setIsHttpsRequest(localTunnel.isHttpsRequest());
                // associate brother
                remoteTunnel.setBrotherTunnel(localTunnel);
                localTunnel.setBrotherTunnel(remoteTunnel);
                // Start the connection (the local tcp server connects to the target server)
                remoteTunnel.connect(destAddress);
            }
        } catch (Exception ex) {
            if (localTunnel != null) {
                localTunnel.dispose();
            }
        }
    }
}