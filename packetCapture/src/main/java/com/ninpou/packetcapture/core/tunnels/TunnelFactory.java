package com.ninpou.packetcapture.core.tunnels;

import com.ninpou.packetcapture.core.nat.NatSession;
import com.ninpou.packetcapture.core.nat.NatSessionManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class TunnelFactory {
    /**
     * Generally used to create TcpTunnel from the APP used by the user to the local TCP server
     * @param channel
     * @param selector
     * @return
     */
    public static DefaultTcpTunnel wrap(SocketChannel channel, Selector selector) {
        // Since the source port number of the original message (ie: the port occupied by the app used) is not modified before sending to the tcp server, so here
        // Get the port number used by this app through channel.socket().getPort(), and then get the session information
        DefaultTcpTunnel tunnel = new RawJavaTcpTunnel(channel, selector);
        NatSession session = NatSessionManager.getSession((short) channel.socket().getPort());
        if (session != null) {
            tunnel.setIsHttpsRequest(session.isHttpsSession);
        }
        return tunnel;
    }

    /**
     * TcpTunnel is generally used to connect the tcp server to the target server
     * @param destAddress
     * @param selector
     * @param portKey
     * @return
     * @throws IOException
     */
    public static DefaultTcpTunnel createTunnelByConfig(InetSocketAddress destAddress,
                                                        Selector selector,
                                                        short portKey) throws IOException {
        return new RemoteJavaTcpTunnel(destAddress, selector, portKey);
    }
}