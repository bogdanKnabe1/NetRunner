package com.ninpou.packetcapture.core.tunnel;

import com.ninpou.packetcapture.core.nat.NatSessionManager;
import com.ninpou.packetcapture.core.vpn.VpnServiceProxy;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class BaseTcpTunnel implements KeyHandler {

    public static long sessionCount;
    protected InetSocketAddress destAddress;
    ConcurrentLinkedQueue<ByteBuffer> needWriteData = new ConcurrentLinkedQueue<>();
    private short portKey;
    private SocketChannel innerChannel;

    private Selector selector;

    private boolean isHttpsRequest = false;

    private BaseTcpTunnel brotherTunnel;
    private boolean disposed;
    private InetSocketAddress serverEP;

    public BaseTcpTunnel(SocketChannel innerChannel, Selector selector) {
        this.innerChannel = innerChannel;
        this.selector = selector;
        sessionCount++;
    }

    public BaseTcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
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

    public void setBrotherTunnel(BaseTcpTunnel brotherTunnel) {
        this.brotherTunnel = brotherTunnel;
    }


    public void connect(InetSocketAddress destAddress) throws Exception {
        if (VpnServiceProxy.protect(innerChannel.socket())) {
            this.destAddress = destAddress;
            innerChannel.register(selector, SelectionKey.OP_CONNECT, this);
            innerChannel.connect(serverEP);
        } else {
            throw new Exception("VPN protect socket failed.");
        }
    }

    public void onConnectable() {
        try {
            if (innerChannel.finishConnect()) {
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
            ByteBuffer buffer = ByteBuffer.allocate(VpnServiceProxy.getMtu());
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