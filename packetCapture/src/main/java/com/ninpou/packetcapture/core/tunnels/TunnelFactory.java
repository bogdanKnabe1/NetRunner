package com.ninpou.packetcapture.core.tunnels;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {

    public static DefaultTcpTunnel wrap(SocketChannel channel, Selector selector) {
        DefaultTcpTunnel tunnel = new RawJavaTcpTunnel(channel, selector);
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        if (session != null) {
            tunnel.setIsHttpsRequest(session.isHttpsSession);
        }
        return tunnel;
    }

    public static DefaultTcpTunnel createTunnelByConfig(InetSocketAddress destAddress,
                                                        Selector selector,
                                                        short portKey) throws IOException {
        return new RemoteJavaTcpTunnel(destAddress, selector, portKey);
    }
}