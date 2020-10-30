package com.ninpou.packetcapture.core.tunnels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RawJavaTcpTunnel extends DefaultTcpTunnel {

    public RawJavaTcpTunnel(SocketChannel innerChannel, Selector selector) {
        super(innerChannel, selector);
    }

    public RawJavaTcpTunnel(InetSocketAddress serverAddress, Selector selector, short portKey) throws IOException {
        super(serverAddress, selector, portKey);
    }

    @Override
    protected void onConnected() throws Exception {
        onTunnelEstablished();
    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
    }

    @Override
    protected void onDispose() {
    }
}