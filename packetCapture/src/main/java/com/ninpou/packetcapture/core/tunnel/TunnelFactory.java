package com.ninpou.packetcapture.core.tunnel;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;


public class TunnelFactory {

    public static BaseTcpTunnel wrap(SocketChannel channel, Selector selector) {
        BaseTcpTunnel tunnel = new RawTcpTunnel(channel, selector);
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        if (session != null) {
            tunnel.setIsHttpsRequest(session.isHttpsSession);
        }
        return tunnel;
    }

    public static BaseTcpTunnel createTunnelByConfig(InetSocketAddress destAddress,
                                                     Selector selector,
                                                     short portKey) throws IOException {
        return new RemoteTcpTunnel(destAddress, selector, portKey);
    }
}
