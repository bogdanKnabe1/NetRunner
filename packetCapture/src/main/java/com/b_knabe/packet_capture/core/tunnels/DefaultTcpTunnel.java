package com.b_knabe.packet_capture.core.tunnels;

import com.b_knabe.packet_capture.core.nat.NatSessionManager;
import com.b_knabe.packet_capture.core.vpn.VpnProxyServer;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class DefaultTcpTunnel implements KeyHandler {

    public static long sessionCount;
    protected InetSocketAddress destAddress;
    // The data sent by the user is transformed into a channel sent to the tcp server
    ConcurrentLinkedQueue<ByteBuffer> needWriteData = new ConcurrentLinkedQueue<>();
    private short portKey;
    private SocketChannel innerChannel;
    /**
     * Send data buffer
     */
    private Selector selector;

    private boolean isHttpsRequest = false;
    // Two Tunnels are responsible for communication with the external network, one is responsible for the communication between Apps and TCP proxy server, and the other is responsible for TCP proxy server
    // The communication with the external network server, the data exchange between Apps and the external network server is carried out by these two tunnels; these two tunnels are brothers to each other and must
    // assign mBrotherTunnel to the other party
    private DefaultTcpTunnel brotherTunnel;
    private boolean disposed;
    // Used to save the connection information from the tcp server to the target server
    private InetSocketAddress serverEP;
    // The port of the target server
    public DefaultTcpTunnel(SocketChannel innerChannel, Selector selector) {
        this.innerChannel = innerChannel;
        this.selector = selector;
        sessionCount++;
    }

    public DefaultTcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        SocketChannel innerChannel = SocketChannel.open();
        innerChannel.configureBlocking(false);
        this.innerChannel = innerChannel;
        this.selector = selector;
        this.serverEP = serverAddress;
        this.portKey = portKey;
        sessionCount++;
    }

    @Override
    public void onKeyReady(SelectionKey key) {
        if (key.isReadable()) {
            onReadable(key);
        } else if (key.isWritable()) {
            onWritable(key);
        } else if (key.isConnectable()) {
            onConnectable();
        }
    }

    protected abstract void onConnected() throws Exception;

    protected abstract boolean isTunnelEstablished();

    protected abstract void beforeSend(ByteBuffer buffer) throws Exception;

    protected abstract void afterReceived(ByteBuffer buffer) throws Exception;

    protected abstract void onDispose();

    public void setBrotherTunnel(DefaultTcpTunnel brotherTunnel) {
        this.brotherTunnel = brotherTunnel;
    }

    /**
     * Method calling sequence:
     * connect() -> onKeyReady() -> onConnectable() -> onConnected()[Subclass implementation] -> onTunnelEstablished
     * beginReceived() -> onReadable() -> afterReceived()[Subclass implementation]
     */

        /**
         * Used to connect the local tcp server to the target server, there is no need to go vpn here, because the data sent by the user has been modified
         * @param destAddress
         * @throws Exception
         */
    public void connect(InetSocketAddress destAddress) throws Exception {
        //Protect the socket from VPN
        if (VpnProxyServer.protect(innerChannel.socket())) {
            this.destAddress = destAddress;
            // Register the connection event, SelectionKey will bind the link from the local tcp server to the target server,
            // but not the link from the user app to the local tcp server
            innerChannel.register(selector, SelectionKey.OP_CONNECT, this);
            innerChannel.connect(serverEP);
        } else {
            throw new Exception("VPN protect socket failed.");
        }
    }

    public void onConnectable() {
        // Check whether the SocketChannel that is connecting to the socket has been connected
        // is true if, and only if, this channel's socket is now connected
        try {
            if (innerChannel.finishConnect()) {
                // Notify the subclass that TCP is connected, the subclass can implement handshake etc. according to the protocol
                onConnected();
            } else {
                dispose();
            }
        } catch (Exception e) {
            dispose();
        }
    }

    protected void beginReceived() throws Exception {
        if (innerChannel.isBlocking()) {
            innerChannel.configureBlocking(false);
        }
        selector.wakeup();
        innerChannel.register(selector, SelectionKey.OP_READ, this);
    }

    public void onReadable(SelectionKey key) {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(VpnProxyServer.getMtu());
            buffer.clear();
            int bytesRead = innerChannel.read(buffer);
            if (bytesRead > 0) {
                buffer.flip();
                afterReceived(buffer);
                sendToBrother(key, buffer);
            } else if (bytesRead < 0) {
                dispose();
            }
        } catch (Exception ex) {
            dispose();
        }
    }


    protected void sendToBrother(SelectionKey key, ByteBuffer buffer) throws Exception {
        if (isTunnelEstablished() && buffer.hasRemaining()) {
            brotherTunnel.getWriteDataFromBrother(buffer);

        }
    }

    private void getWriteDataFromBrother(ByteBuffer buffer) {
        if (buffer.hasRemaining() && needWriteData.size() == 0) {
            int writeSize;
            try {
                writeSize = write(buffer);
            } catch (Exception e) {
                writeSize = 0;
                e.printStackTrace();
            }
            if (writeSize > 0) {
                return;
            }
        }
        needWriteData.offer(buffer);
        try {
            selector.wakeup();
            innerChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, this);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    protected int write(ByteBuffer buffer) throws Exception {
        int byteSendSum = 0;
        beforeSend(buffer);
        while (buffer.hasRemaining()) {
            int byteSent = innerChannel.write(buffer);
            byteSendSum += byteSent;
            if (byteSent == 0) {
                break;
            }
        }
        return byteSendSum;
    }


    public void onWritable(SelectionKey key) {
        try {
            //Before sending, let the subclass handle it, such as encryption
            ByteBuffer mSendRemainBuffer = needWriteData.poll();
            if (mSendRemainBuffer == null) {
                return;
            }

            write(mSendRemainBuffer);
            if (needWriteData.size() == 0) {
                try {
                    selector.wakeup();
                    innerChannel.register(selector, SelectionKey.OP_READ, this);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception ex) {
            dispose();
        }
    }

    protected void onTunnelEstablished() throws Exception {
        this.beginReceived();
        brotherTunnel.beginReceived();
    }

    public void dispose() {
        disposeInternal(true);
    }

    void disposeInternal(boolean disposeBrother) {
        if (!disposed) {
            try {
                innerChannel.close();
            } catch (Exception ignored) {
            }
            if (brotherTunnel != null && disposeBrother) {
                brotherTunnel.disposeInternal(false);
            }

            innerChannel = null;
            selector = null;
            brotherTunnel = null;
            disposed = true;
            --sessionCount;
            onDispose();
            NatSessionManager.removeSession(portKey);
        }
    }

    public void setIsHttpsRequest(boolean isHttpsRequest) {
        this.isHttpsRequest = isHttpsRequest;
    }

    public boolean isHttpsRequest() {
        return isHttpsRequest;
    }
}